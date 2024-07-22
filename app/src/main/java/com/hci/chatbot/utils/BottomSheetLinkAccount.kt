package com.hci.chatbot.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.app.ActivityCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.hci.chatbot.network.DiseaseListItemData
import com.hci.chatbot.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.hci.chatbot.MainActivity
import com.hci.chatbot.network.AccountLinkResponse
import com.hci.chatbot.network.AccountLoginResponse
import com.hci.chatbot.network.NetworkManager
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.Permission


class BottomSheetLinkAccount : BottomSheetDialogFragment() {

    private lateinit var viewOfLayout: View
    private lateinit var kakaoBtn: Button
    private lateinit var googleBtn: SignInButton

    private var mAuth: FirebaseAuth? = null

    //android.os.TransactionTooLargeException 발생 시 CredentialManager가 작동하지 않아 구버전을 혼합하여 사용
    //계정이 많은 경우 발생으로 추정 중
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    //신버전 먼저 시도 후 안되면 구버전으로 실행시킬 용도
    private var oldGoogleLoginjob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewOfLayout = inflater.inflate(R.layout.link_account_layout, container, false)

        mAuth = FirebaseAuth.getInstance()

        googleBtn = viewOfLayout.findViewById<SignInButton>(R.id.google_sign_btn)
        googleBtn.setOnClickListener {
            blockDismiss(true)

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_sdk_id))
                .requestEmail()
                .build()

            mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

            signIn() //CredentialManager 사용 로그인
        }

        kakaoBtn = viewOfLayout.findViewById<Button>(R.id.kakao_sign_btn)
        kakaoBtn.setOnClickListener {
            blockDismiss(true)
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    blockDismiss(false)
                    Log.e("Kakao Login", "카카오계정으로 로그인 실패", error)
                    Snackbar.make(viewOfLayout.findViewById(R.id.rootContainer), "로그인에 실패하였습니다.", Snackbar.LENGTH_LONG).show()
                } else if (token != null) {
                    //아래처럼 조작이 가능해진다는 문제점을 해결
                    //mAuth!!.createUserWithEmailAndPassword("fake@fake.com", "fakepassword")

                    kakaoInfoSave()
                    linkAccount("kakao", token.accessToken)
                }
            }

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireContext())) {
                UserApiClient.instance.loginWithKakaoTalk(requireContext()) { token, error ->
                    if (error != null) {
                        Log.e("Kakao Login", "카카오톡으로 로그인 실패", error)

                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            blockDismiss(false)
                            Snackbar.make(viewOfLayout.findViewById(R.id.rootContainer), "로그인을 취소하였습니다.", Snackbar.LENGTH_LONG).show()
                            return@loginWithKakaoTalk
                        }

                        UserApiClient.instance.loginWithKakaoAccount(requireContext(), callback = callback)
                    } else if (token != null) {
                        kakaoInfoSave()
                        linkAccount("kakao", token.accessToken)
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(requireContext(), callback = callback)
            }
        }

        return viewOfLayout
    }

    fun blockDismiss(block: Boolean) {
        when(block) {
            true -> {
                this.isCancelable = true
                kakaoBtn.isClickable = false
                googleBtn.isClickable = false

            }
            false -> {
                this.isCancelable = false
                kakaoBtn.isClickable = true
                googleBtn.isClickable = true
            }
        }
    }

    @Deprecated("구버전 API 사용 중, 이후 CredentialManager 관련 문제 해결 시 삭제")
    fun initGoogle(){
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent: Intent? = result.data
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(intent)
                try {
                    // 구글 로그인은 성공, Firebase에 로그인
                    val account: GoogleSignInAccount = task.getResult(ApiException::class.java)

                    val profileManager = SharedPreferenceManager(requireContext())
                    profileManager.saveProfileInfo(
                        account.photoUrl.toString(),
                        account.displayName?: "기본 닉네임",
                        account.email?: "이메일 알 수 없음"
                    )

                    linkAccount("google", account.idToken!!)
                } catch (e: ApiException) {
                    // 구글 로그인 실패
                    Log.e("Google Login Error", e.toString())
                    Snackbar.make(viewOfLayout.findViewById(R.id.rootContainer), "로그인에 실패하였습니다.", Snackbar.LENGTH_LONG).show()
                    blockDismiss(false)
                }
            }
        }
    }

    private fun signIn() {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.google_sdk_id))
            //.setAutoSelectEnabled(true) //자동로그인
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(requireContext())

        oldGoogleLoginjob = lifecycleScope.launch {
            delay(3000)
            //3초를 기다려보고 신버전 로그인 창이 뜨지 않으면 구버전 로그인 진행
            val signInIntent = mGoogleSignInClient!!.signInIntent
            activityResultLauncher!!.launch(signInIntent)
        }

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireContext(),
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.e("Google Credential Error", e.toString())
                if(e is NoCredentialException) {
                    Snackbar.make(viewOfLayout.findViewById(R.id.rootContainer), "오류가 발생하여 다른 방법으로 재시도합니다.", Snackbar.LENGTH_LONG).show()
                    return@launch
                }
                oldGoogleLoginjob?.cancel()
                Snackbar.make(viewOfLayout.findViewById(R.id.rootContainer), "로그인에 실패했습니다.", Snackbar.LENGTH_LONG).show()
                blockDismiss(false)
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            //TODO 7월 9일 구글 로그인 인증 방식 다양화 필요
            is PasswordCredential -> {
                val username = credential.id
                val password = credential.password

                Log.e("Unsupported Credential", buildString {
                    append(username)
                    append(" : ")
                    append(password)
                })
                blockDismiss(false)
            }
            // GoogleIdToken credential
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                        val profileManager = SharedPreferenceManager(requireContext())
                        profileManager.saveProfileInfo(
                            googleIdTokenCredential.profilePictureUri.toString(),
                            googleIdTokenCredential.displayName?: "기본 닉네임",
                            googleIdTokenCredential.id
                        )

                        linkAccount("google", googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        blockDismiss(false)
                        Snackbar.make(viewOfLayout.findViewById(R.id.rootContainer), "로그인에 실패하였습니다.", Snackbar.LENGTH_LONG).show()
                        Log.e("Google Login Error", "Received an invalid google id token response", e)
                    }
                } else {
                    blockDismiss(false)
                    Snackbar.make(viewOfLayout.findViewById(R.id.rootContainer), "로그인에 실패하였습니다.", Snackbar.LENGTH_LONG).show()
                    Log.e("Google Login Error", "Unexpected type of credential")
                }
            }
            else -> {
                blockDismiss(false)
                Snackbar.make(viewOfLayout.findViewById(R.id.rootContainer), "로그인에 실패하였습니다.", Snackbar.LENGTH_LONG).show()
                Log.e("Google Login Error", "Unexpected type of credential")
            }
        }
    }

    private fun kakaoInfoSave() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("Kakao Error", "사용자 정보 요청 실패", error)
            }
            else if (user != null) {
                val profileManager = SharedPreferenceManager(requireContext())
                profileManager.saveProfileInfo(
                    user.kakaoAccount?.profile?.thumbnailImageUrl!!, //필수 동의임
                    user.kakaoAccount?.profile?.nickname!!, //필수 동의임
                    user.kakaoAccount?.email!! //필수 동의임
                )
            }
        }
    }

    private fun linkAccount(platform: String, token: String) {
        val ssaid = requireActivity().getSSAID()
        if(ssaid == null) {
            Snackbar.make(viewOfLayout.findViewById(R.id.rootContainer), "로그인에 충분한 정보가 없는 기기입니다.", Snackbar.LENGTH_LONG).show()
            blockDismiss(false)
            return
        }

        NetworkManager.apiService.linkAccount(platform, token, ssaid).enqueue(object :
            Callback<AccountLinkResponse> {
            override fun onResponse(call: Call<AccountLinkResponse>, response: Response<AccountLinkResponse>) {
                if(!response.isSuccessful) {
                    Snackbar.make(viewOfLayout.findViewById(R.id.rootContainer), "연동에 실패했습니다.", Snackbar.LENGTH_SHORT).show();
                    blockDismiss(false)
                    return
                }


                val result = response.body()!!.result

                when(result) {
                    "success" -> {
                        Toast.makeText(requireContext(), "연동이 완료되어 초기 화면으로 넘어갑니다.", Toast.LENGTH_LONG).show()
                        mAuth!!.signOut()
                        requireActivity().finish()
                    }
                    "fail" -> {
                        Toast.makeText(requireContext(), response.body()!!.msg, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<AccountLinkResponse>, err: Throwable) {
                Log.e("Network Error", "Authentication Network Failed. $err")
                Snackbar.make(viewOfLayout.findViewById(R.id.rootContainer), "Authentication Network Failed.", Snackbar.LENGTH_SHORT).show();
                blockDismiss(false)
            }
        })
    }
}
