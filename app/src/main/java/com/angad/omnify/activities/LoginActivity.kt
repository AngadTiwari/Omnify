package com.angad.omnify.activities

import android.animation.AnimatorInflater
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialog
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.angad.omnify.R
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity(), View.OnClickListener, FacebookCallback<LoginResult> {

    private val tag: String? = LoginActivity::class.java.simpleName
    private var mAuth: FirebaseAuth? = null
    private var mCallbackManager: CallbackManager? = null

    private val RC_SIGN_IN = 100
    private val RESOLVE_HINT = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance() // Initialize Firebase Auth
        mCallbackManager = CallbackManager.Factory.create() // Initialize Facebook Login button
        btn_fb_login.setReadPermissions("email", "public_profile")

        styleGoogleSignIn()
        animateLoginContainer()
        attachListeners()
    }

    /**
     * attach google + facebook + phone verification button click listener
     */
    private fun attachListeners() {
        btn_google_signin.setOnClickListener(this)
        btn_fb_login.registerCallback(mCallbackManager, this)
        btn_phone_login.setOnClickListener(this)
    }

    /**
     * in animation for login container
     */
    private fun animateLoginContainer() {
        val animator1 = AnimatorInflater.loadAnimator(this, R.animator.anim_login_in)
        animator1.setTarget(layout_login)
        animator1.start()
    }

    /**
     * style google sigin in button
     */
    private fun styleGoogleSignIn() {
        btn_google_signin.setSize(SignInButton.SIZE_WIDE)
    }

    /**
     * dialog to prompt user to give his/her phone number
     */
    private fun askForPhoneNo() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(R.string.login_phoneauth_title)
        dialogBuilder.setMessage(R.string.login_phoneauth_ms)
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_PHONE
        input.hint = "+918989979511"
        input.id = R.id.edit_phone
        dialogBuilder.setView(input)
        dialogBuilder.setPositiveButton("Send Code", {dialogInterface, i -> sendCodeClickHandler((dialogInterface as AppCompatDialog).findViewById<EditText>(R.id.edit_phone)?.text.toString()) })
        dialogBuilder.setNegativeButton("Cancel", { dialog, whichButton -> progress_bar.visibility = View.GONE})
        dialogBuilder.setCancelable(false)
        dialogBuilder.create().show()
    }

    override fun onClick(view: View?) {
        progress_bar.visibility = View.VISIBLE
        when(view?.id) {
            R.id.btn_google_signin -> signIn()
            R.id.btn_phone_login -> askForPhoneNo()
        }
    }

    /**
     * facebook login success callback
     */
    override fun onSuccess(loginResult: LoginResult?) {
        Log.d(tag, "facebook:onSuccess:$loginResult")
        progress_bar.visibility = View.GONE
        handleFacebookAccessToken(loginResult?.accessToken)
    }

    /**
     * facebook login cancel callback
     */
    override fun onCancel() {
        Log.d(tag, "facebook:onCancel")
        progress_bar.visibility = View.GONE
    }

    /**
     * facebook login error callback
     */
    override fun onError(error: FacebookException?) {
        progress_bar.visibility = View.GONE
        Log.d(tag, "facebook:onError", error)
    }

    /**
     * onactivity result to handle google + facebook + phone verification result handling
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java) // Google Sign In was successful, authenticate with Firebase
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w(tag, "Google sign in failed", e) // Google Sign In failed, update UI appropriately
            }

        } else if( requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                val credential: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
                Log.d(tag, "phonenumber: $credential") // credential.getId(); <-- E.164 format phone number on 10.2.+ devices
            }
        } else {
            mCallbackManager?.onActivityResult(requestCode, resultCode, data) // Pass the activity result back to the Facebook SDK
        }
    }

    /**
     * Check if user is signed in (non-null) and navigate to screen accordingly.
     */
    public override fun onStart() {
        super.onStart()
        val currentUser = mAuth?.currentUser
        Log.d(tag, "already login via: ${currentUser?.displayName}")
        //startActivity(Intent(this@LoginActivity, ActiclesListActivity::class.java))
    }

    /**
     * send the code to user's phone number
     */
    private fun sendCodeClickHandler(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,       // Phone number to verify
            60,                // Timeout duration
            TimeUnit.SECONDS,  // Unit of timeout
            this@LoginActivity,// Activity (for callback binding)
            object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(tag, "onVerificationCompleted:$credential")
                    signInWithPhoneAuthCredential(credential)
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    Log.w(tag, "onVerificationFailed", e)
                    if (e is FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                    } else if (e is FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                    }
                }
                override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
                    Log.d(tag, "onCodeSent:" + verificationId!!)
                }
            }
        )
    }

    /**
     * process phone verification credential & redirect to next screen
     */
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = task.result?.user
                        Log.d(tag, "phone signInWithCredential:success: ${user?.displayName}")
                        progress_bar.visibility = View.GONE
                        startActivity(Intent(this@LoginActivity, ArticlesListActivity::class.java))
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(tag, "signInWithCredential:failure", task.exception)
                        progress_bar.visibility = View.GONE
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                        }
                    }
                }
    }

    /**
     * processing facebook access token & redirect to next screen
     */
    private fun handleFacebookAccessToken(token: AccessToken?) {
        Log.d(tag, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token?.token!!)
        mAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's
                    val user = mAuth?.currentUser
                    Log.d(tag, "fb signInWithCredential:success: ${user?.displayName}")
                    progress_bar.visibility = View.GONE
                    startActivity(Intent(this@LoginActivity, ArticlesListActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(tag, "fb signInWithCredential:failure", task.exception)
                    progress_bar.visibility = View.GONE
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * google sigin button click
     */
    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    /**
     * processing google sigin token or account detail & redirect to next screen
     */
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        Log.d(tag, "firebaseAuthWithGoogle:" + acct?.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = mAuth?.currentUser
                    Log.d(tag, "signInWithCredential:success: ${user?.displayName}")
                    progress_bar.visibility = View.GONE
                    startActivity(Intent(this@LoginActivity, ArticlesListActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    progress_bar.visibility = View.GONE
                    Log.w(tag, "signInWithCredential:failure", task.exception)
                }
            }
    }
}
