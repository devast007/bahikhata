package com.datamangement.devast007.bahikhata.ui

//import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.support.v7.app.AppCompatActivity
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.SignInProfile
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_google_signin.*
import java.util.*


class GoogleSigninActivity : AppCompatActivity(), View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private val TAG = "GoogleSigninActivity"
    private val REQUEST_CODE_SIGN_IN = 1234
    private val TYPE_NOT_SIGNED = 0;
    private val TYPE_SIGNED_DATA_FETCHING = 1;
    private val TYPE_SIGNED = 2;
    private var mAuth: FirebaseAuth? = null
    var mSignInProfile: SignInProfile? = null
    private var mCurrentType: Int = TYPE_NOT_SIGNED

    private var mGoogleApiClient: GoogleApiClient? = null

    private var mContext: GoogleSigninActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_signin)
        mContext = this
        toolbar.setTitle(R.string.app_name)
        setSupportActionBar(toolbar)
        btn_sign_in.setOnClickListener(this)

        tv_admin_all_projects.setOnClickListener(this)
        tv_admin_all_users.setOnClickListener(this)
        tv_admin_all_transactions.setOnClickListener(this)
        tv_admin_all_accounts.setOnClickListener(this)

        tv_users_normal.setOnClickListener(this)
        tv_users_supervisor.setOnClickListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        mAuth = FirebaseAuth.getInstance()
        mCurrentUserInfo = mAuth!!.currentUser
        if (mCurrentUserInfo != null) {
            mCurrentType = TYPE_SIGNED_DATA_FETCHING
        }
        var mode = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(mode.build())
    }

    override fun onResume() {
        super.onResume()
        // Check if user is signed in (non-null) and update UI accordingly.
        when (mCurrentType) {
            TYPE_NOT_SIGNED -> {
                et_company_id.setText(LedgerSharePrefManger(mContext).getCompanyName())
                updateUI(TYPE_NOT_SIGNED)
            }
            TYPE_SIGNED_DATA_FETCHING -> {
                updateUI(TYPE_SIGNED_DATA_FETCHING)
                mHandler.sendEmptyMessage(MSG_USER_PROFILE)
            }
            TYPE_SIGNED -> {
                updateUI(TYPE_SIGNED)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_signout_button_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item!!.itemId == R.id.action_signout) {
            signOut()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        val i = view!!.id
        when (i) {
            R.id.btn_sign_in -> signIn()
            R.id.tv_admin_all_projects -> launchProjectView()
            R.id.tv_admin_all_users -> launchUserView()
            R.id.tv_admin_all_transactions -> launchTransactionView(LedgerDefine.DESIGNATION_ADMIN)
            R.id.tv_admin_all_accounts -> launchAccountView()

            R.id.tv_users_supervisor -> launchTransactionView(LedgerDefine.DESIGNATION_SUPERVISOR)
            R.id.tv_users_normal -> launchTransactionView(LedgerDefine.DESIGNATION_NORMAL)

        }
    }

    private fun launchAccountView() {
        val intent = Intent(mContext, BankAccountViewActivity::class.java)
        startActivity(intent)
    }

    private fun launchProjectView() {
        val intent = Intent(mContext, ProjectsViewActivity::class.java)
        startActivity(intent)
    }

    private fun launchUserView() {
        val intent = Intent(mContext, UsersViewActivity::class.java)
        startActivity(intent)
    }

    private fun launchTransactionView(type: Long) {
        val intent = Intent(mContext, TransactionViewActivity::class.java)
        if (type == LedgerDefine.DESIGNATION_ADMIN) {
            intent.putExtra(LedgerDefine.TRANSACTION_VIEW_TYPE, LedgerDefine.TRANSACTION_VIEW_TYPE_ALL)
            // skip everything
        } else if (type == LedgerDefine.DESIGNATION_SUPERVISOR) {
            intent.putExtra(LedgerDefine.TRANSACTION_VIEW_TYPE, LedgerDefine.TRANSACTION_VIEW_TYPE_USER)
            intent.putExtra(LedgerDefine.ID, mSignInProfile!!.supervisorID)
            intent.putExtra(LedgerDefine.DESIGNATION, LedgerDefine.DESIGNATION_SUPERVISOR)
        } else if (type == LedgerDefine.DESIGNATION_NORMAL) {
            intent.putExtra(LedgerDefine.TRANSACTION_VIEW_TYPE, LedgerDefine.TRANSACTION_VIEW_TYPE_USER)
            intent.putExtra(LedgerDefine.ID, mSignInProfile!!.normalId)
            intent.putExtra(LedgerDefine.DESIGNATION, LedgerDefine.DESIGNATION_NORMAL)
        }
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent();
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // successful -> authenticate with Firebase
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                // failed -> update UI
                updateUI(TYPE_NOT_SIGNED)
                Toast.makeText(
                    applicationContext, "SignIn: failed!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private var mCurrentUserInfo: FirebaseUser? = null

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.e(TAG, "firebaseAuthWithGoogle():" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.e(TAG, "signInWithCredential: Success!")
                    mCurrentUserInfo = mAuth!!.currentUser
                    updateUI(TYPE_SIGNED_DATA_FETCHING)
                    mHandler.sendEmptyMessage(MSG_USER_PROFILE)
                } else {
                    // Sign in fails
                    Log.w(TAG, "signInWithCredential: Failed!", task.exception)
                    Toast.makeText(
                        applicationContext, "Authentication failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(TYPE_NOT_SIGNED)
                }
            }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.e(TAG, "onConnectionFailed():" + connectionResult);
        Toast.makeText(applicationContext, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private fun signIn() {
        var companyID = et_company_id.text.toString()
        if (TextUtils.isEmpty(companyID)) return
        LedgerSharePrefManger(this!!.mContext!!).setCompanyName(companyID)
        val intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(intent, REQUEST_CODE_SIGN_IN)
    }

    private fun signOut() {
        // sign out Firebase
        mAuth!!.signOut()

        // sign out Google
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback { updateUI(TYPE_NOT_SIGNED) }
    }


    /*this method will update for 3 case
        *  1. signin
        *  2. fetching the data from server
        *  3. after fetching update the data.
        * */
    private fun updateUI(type: Int) {
        Log.d(TAG, "type =  " + type)
        mCurrentType = type
        when (type) {
            TYPE_NOT_SIGNED -> {
                layout_01_sign_in.visibility = View.VISIBLE
                layout_02_loading.visibility = View.GONE
                layout_03_admin.visibility = View.GONE
                layout_03_normal.visibility = View.GONE
            }
            TYPE_SIGNED_DATA_FETCHING
            -> {
                layout_01_sign_in.visibility = View.GONE
                layout_02_loading.visibility = View.VISIBLE
                layout_03_admin.visibility = View.GONE
                layout_03_normal.visibility = View.GONE
            }
            TYPE_SIGNED
            -> {
                if (mSignInProfile == null) {
                    Toast.makeText(mContext, R.string.error_02, Toast.LENGTH_LONG)
                    signOut()
                    return
                }
                layout_01_sign_in.visibility = View.GONE
                layout_02_loading.visibility = View.GONE
                if (mSignInProfile!!.isAdmin) {
                    layout_03_admin.visibility = View.VISIBLE
                    layout_03_normal.visibility = View.GONE
                    tv_user_name.setText(R.string.admin)
                } else {
                    layout_03_admin.visibility = View.GONE
                    layout_03_normal.visibility = View.VISIBLE
                    Log.d(TAG, "mSignInProfile!!.isSupervisor =  " + mSignInProfile!!.isSupervisor)
                    if (mSignInProfile!!.isSupervisor) {
                        Log.d(TAG, "inside mSignInProfile!!.supervisorID " + mSignInProfile!!.supervisorID)
                        layout_supervisor_account.visibility = View.VISIBLE
                        tv_users_supervisor.text =
                                " Owner: " + mSignInProfile!!.supervisorID
                        tv_users_supervisor_amount.text =
                                LedgerUtils.getRupeesFormatted(mSignInProfile!!.supervisorAmount)


                        var adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_list_item_1,
                            mSignInProfile!!.accesibleProjects
                        );
                        listview.adapter = adapter
                    } else {
                        layout_supervisor_account.visibility = View.GONE
                    }
                    Log.d(TAG, "mSignInProfile!!.isSupervisor =  " + mSignInProfile!!.isNormal)
                    if (mSignInProfile!!.isNormal) {
                        tv_users_normal.text =
                                " Normal: " + mSignInProfile!!.normalId
                        tv_users_normal_amount.text = LedgerUtils.getRupeesFormatted(mSignInProfile!!.normalAmount)
                    }
                    tv_user_name.text = mSignInProfile!!.name
                }

            }
        }
    }

    fun getUnderLineText(str: String): SpannableString {
        val content = SpannableString(str)
        content.setSpan(UnderlineSpan(), 0, str.length, 0)
        content.setSpan(ForegroundColorSpan(Color.BLUE), 0, str.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return content
    }

    private val MSG_USER_PROFILE = 1;


    val mHandler: Handler = Handler() {
        when (it.what) {
            MSG_USER_PROFILE -> getProfileInfo()
            else -> {
                return@Handler false
            }
        }
    }


    private fun getProfileInfo(): Boolean {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        Log.d(TAG, "companyID => " + companyID + " , mCurrentUserInfo!!.email = " + mCurrentUserInfo!!.email)
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/users")
            .whereEqualTo("email", mCurrentUserInfo!!.email)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    mSignInProfile = null
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        Log.d(TAG, " document.get(\"name\")+ => " + document.get("name"))
                        setProfileInfo(document);
                    }
                    LedgerUtils.signInProfile = mSignInProfile
                    updateUI(TYPE_SIGNED)
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG)
                }
            })
        return true;
    }

    private fun setProfileInfo(document: QueryDocumentSnapshot?) {
        if (document != null) {
            if (mSignInProfile == null) {
                mSignInProfile = SignInProfile()
            }

            var userID = document.get(LedgerDefine.USER_ID)
            var amount = document.get(LedgerDefine.AMOUNT)
            var designation = document.get(LedgerDefine.DESIGNATION)
            Log.d(TAG, " designation = " + designation)
            if (designation != null) {
                designation = designation as Long
                if (designation == LedgerDefine.DESIGNATION_SUPERVISOR) {
                    Log.d(TAG, " designation DESIGNATION_SUPERVISOR userID = " + userID)
                    mSignInProfile!!.isSupervisor = true
                    if (amount != null) mSignInProfile!!.supervisorAmount = amount as Long
                    if (userID != null) mSignInProfile!!.supervisorID = userID as String
                } else if (designation == LedgerDefine.DESIGNATION_NORMAL) {
                    mSignInProfile!!.isNormal = true
                    if (amount != null) mSignInProfile!!.normalAmount = amount as Long
                    if (userID != null) mSignInProfile!!.normalId = userID as String
                }
            }

            var projects = document.get(LedgerDefine.ACCESSIBLE_PROJECTS)

            if (projects != null) {
                mSignInProfile!!.accesibleProjects = projects as ArrayList<String>
            }

            var isAdmin = document.get(LedgerDefine.IS_ADMIN)
            if (isAdmin != null) {
                mSignInProfile!!.isAdmin = isAdmin as Boolean
                if (userID != null) mSignInProfile!!.adminID = userID as String
            }

            var name = document.get(LedgerDefine.NAME)
            if (name != null) mSignInProfile!!.name = name as String

        }
    }
}
