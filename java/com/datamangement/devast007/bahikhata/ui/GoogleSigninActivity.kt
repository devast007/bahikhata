package com.datamangement.devast007.bahikhata.ui

//import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.os.StrictMode
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_google_signin.*
import java.util.*


class GoogleSigninActivity : AppCompatActivity(), View.OnClickListener,
    GoogleApiClient.OnConnectionFailedListener {

    private var mIntentType: String? = ""
    private var mIntentAction: String? = ""
    private var mFirebaseDB: FirebaseFirestore? = null
    private var isFABOpen: Boolean = false
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

    private var mLlFabTransaction: LinearLayout? = null
    private var mLlFabProjects: LinearLayout? = null
    private var mLlFabUsers: LinearLayout? = null
    private var mLlFabMaterial: LinearLayout? = null
    private var mLlFabGST: LinearLayout? = null
    private var mLlFabBankAccoount: LinearLayout? = null

    private var mFabBtnTransaction: FloatingActionButton? = null
    private var mFabBtnUsers: FloatingActionButton? = null
    private var mFabBtnProjects: FloatingActionButton? = null
    private var mFabBtnMaterials: FloatingActionButton? = null
    private var mFabBtnGST: FloatingActionButton? = null
    private var mFabBtnBankAccounts: FloatingActionButton? = null
    private var mFabBtnAdd: FloatingActionButton? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_signin)
        mContext = this

        mIntentAction = intent.action
        mIntentType = intent.type

        supportActionBar!!.setTitle(R.string.app_name)
        supportActionBar!!.hide()
        btn_sign_in.setOnClickListener(this)

        tv_admin_all_projects.setOnClickListener(this)
        tv_admin_all_users.setOnClickListener(this)
        tv_admin_all_transactions.setOnClickListener(this)
        tv_admin_all_accounts.setOnClickListener(this)
        tv_admin_all_material.setOnClickListener(this)
        tv_admin_all_gst.setOnClickListener(this)
        tv_admin_manage_material_and_services.setOnClickListener(this)
        tv_admin_all_transactions_tracking.setOnClickListener(this)
        tv_account_statement.setOnClickListener(this)
        tv_verify_txn.setOnClickListener(this)
        tv_verify_material.setOnClickListener(this)


        tv_users_normal.setOnClickListener(this)
        tv_users_supervisor.setOnClickListener(this)

        mLlFabTransaction = findViewById(R.id.ll_add_transaction)
        mLlFabUsers = findViewById(R.id.ll_add_user)
        mLlFabProjects = findViewById(R.id.ll_add_projects)
        mLlFabMaterial = findViewById(R.id.ll_add_material)
        mLlFabGST = findViewById(R.id.ll_add_gst)
        mLlFabBankAccoount = findViewById(R.id.ll_add_bank_accounts)


        fab_add_transaction.setOnClickListener(this)
        fab_add_projects.setOnClickListener(this)
        fab_add_user.setOnClickListener(this)
        fab_add_material.setOnClickListener(this)
        fab_add_gst.setOnClickListener(this)
        fab_add_bank_accounts.setOnClickListener(this)

        mFabBtnAdd = findViewById(R.id.fab_add_plus)
        fab_add_plus.setOnClickListener(this)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(
                this /* FragmentActivity */,
                this /* OnConnectionFailedListener */
            )
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        mAuth = FirebaseAuth.getInstance()
        mCurrentUserInfo = mAuth!!.currentUser
        if (mCurrentUserInfo != null) {
            mCurrentType = TYPE_SIGNED_DATA_FETCHING
        }
        mFirebaseDB = FirestoreDataBase().db
        var mode = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(mode.build())


    }


    override fun onResume() {
        super.onResume()
        // Check if user is signed in (non-null) and update UI accordingly.
        when (mCurrentType) {
            TYPE_NOT_SIGNED -> {
                et_company_id.setText(LedgerSharePrefManger(mContext).getCompanyID())
                updateUI(TYPE_NOT_SIGNED)
            }
            TYPE_SIGNED_DATA_FETCHING -> {
                updateUI(TYPE_SIGNED_DATA_FETCHING)
                mHandler.sendEmptyMessage(MSG_USER_PROFILE)
            }
            TYPE_SIGNED -> {
                //showAddDialog()
                updateUI(TYPE_SIGNED)
            }
        }
    }

    private fun showAddDialog() {
        val dialog: AlertDialog = AlertDialog.Builder(mContext).setTitle("Add Below Items")
            .setSingleChoiceItems(arrayOf(
                "TRANSACTIONS",
                "MATERIALS",
                "USERS",
                "PROJECTS",
                "BANK ACCOUNTS",
                "GST"
            ), 0,
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        0 -> startActivity(Intent(mContext, AddTransactionActivity::class.java))
                        1 -> startActivity(Intent(mContext, AddMaterialActivity::class.java))
                        2 -> startActivity(Intent(mContext, AddUsersActivity::class.java))
                        3 -> startActivity(Intent(mContext, AddProjectActivity::class.java))
                        4 -> startActivity(Intent(mContext, AddBankAccountActivity::class.java))
                        5 -> startActivity(Intent(mContext, AddGSTActivity::class.java))
                    }
                    dialog.dismiss()
                }).setNegativeButton("Close", null).create()
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_signout_button_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

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
            R.id.tv_admin_all_material -> launchMaterialView()
            R.id.tv_admin_all_gst -> launchGstView()
            R.id.tv_admin_manage_material_and_services -> launchMaterialAndServices()
            R.id.tv_users_supervisor -> launchTransactionView(LedgerDefine.DESIGNATION_SUPERVISOR)
            R.id.tv_users_normal -> launchTransactionView(LedgerDefine.DESIGNATION_NORMAL)
            R.id.fab_add_plus -> fabPlusBtnClicked()
            R.id.tv_account_statement -> startActivity(
                Intent(
                    mContext,
                    StatementActivity::class.java
                )
            )
            R.id.tv_verify_txn -> {
                val intent = Intent(
                    mContext,
                    TransactionViewActivity::class.java
                )
                intent.putExtra(
                    LedgerDefine.TRANSACTION_VIEW_TYPE,
                    LedgerDefine.TRANSACTION_VIEW_TYPE_PENDING
                )
                startActivity(
                    intent
                )
            }
            R.id.tv_verify_material -> {
                val intent = Intent(
                    mContext,
                    MaterialViewActivity::class.java
                )
                intent.putExtra(
                    LedgerDefine.TRANSACTION_VIEW_TYPE,
                    LedgerDefine.TRANSACTION_VIEW_TYPE_PENDING
                )
                startActivity(
                    intent
                )
            }
            R.id.fab_add_transaction -> startActivity(
                Intent(
                    mContext,
                    AddTransactionActivity::class.java
                )
            )
            R.id.fab_add_projects -> startActivity(
                Intent(
                    mContext,
                    AddProjectActivity::class.java
                )
            )
            R.id.fab_add_user -> {
                startActivity(Intent(mContext, AddUsersActivity::class.java))
            }
            R.id.fab_add_material -> startActivity(
                Intent(
                    mContext,
                    AddMaterialActivity::class.java
                )
            )
            R.id.fab_add_gst -> startActivity(Intent(mContext, AddGSTActivity::class.java))

            R.id.fab_add_bank_accounts -> startActivity(
                Intent(
                    mContext,
                    AddBankAccountActivity::class.java
                )
            )
            R.id.tv_admin_all_transactions_tracking -> {
                val intent = Intent(mContext, TransactionViewActivity::class.java)
                intent.putExtra(
                    LedgerDefine.TRANSACTION_VIEW_TYPE,
                    LedgerDefine.TRANSACTION_VIEW_TYPE_TRACKING
                )
                startActivity(intent)
            }

        }
    }

    private fun launchMaterialAndServices() {
        if (!LedgerUtils.isHasEditPermission(mContext)) return

        val intent = Intent(mContext, MaterialAndServiceViewActivity::class.java)
        startActivity(intent)
    }

    private fun fabPlusBtnClicked() {
        if (!isFABOpen) {

            mLlFabTransaction!!.visibility = View.VISIBLE
            mLlFabProjects!!.visibility = View.VISIBLE
            mLlFabUsers!!.visibility = View.VISIBLE
            mLlFabMaterial!!.visibility = View.VISIBLE
            mLlFabGST!!.visibility = View.VISIBLE
            mLlFabBankAccoount!!.visibility = View.VISIBLE

            mFabBtnAdd!!.animate().rotation(45F)

            mLlFabTransaction!!.animate().translationY(5f)
            mLlFabUsers!!.animate().translationY(10f)
            mLlFabProjects!!.animate().translationY(15f)
            mLlFabBankAccoount!!.animate().translationY(20f)
            mLlFabMaterial!!.animate().translationY(25f)
            mLlFabGST!!.animate().translationY(30f)

        } else {
            mFabBtnAdd!!.animate().rotation(0F)

            mLlFabTransaction!!.animate().translationY(0f)
            mLlFabProjects!!.animate().translationY(0f)
            mLlFabUsers!!.animate().translationY(0f)
            mLlFabMaterial!!.animate().translationY(0f)
            mLlFabBankAccoount!!.animate().translationY(0f)
            mLlFabGST!!.animate().translationY(0f).setListener(object : AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    if (!isFABOpen) {
                        mLlFabTransaction!!.visibility = View.GONE
                        mLlFabProjects!!.visibility = View.GONE
                        mLlFabUsers!!.visibility = View.GONE
                        mLlFabMaterial!!.visibility = View.GONE
                        mLlFabGST!!.visibility = View.GONE
                        mLlFabBankAccoount!!.visibility = View.GONE
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }

            })


        }

        isFABOpen = !isFABOpen
    }

    private fun launchGstView() {
        val intent = Intent(mContext, GstViewActivity::class.java)
        startActivity(intent)
    }

    private fun launchMaterialView() {
        val intent = Intent(mContext, MaterialViewActivity::class.java)
        startActivity(intent)
    }

    private fun launchAccountView() {
        if (!mSignInProfile!!.isHasLimitedAccess) {
            val intent = Intent(mContext, BankAccountViewActivity::class.java)
            startActivity(intent)
        }
    }

    private fun launchProjectView() {
        if (!mSignInProfile!!.isHasLimitedAccess) {
            val intent = Intent(mContext, ProjectsViewActivity::class.java)
            startActivity(intent)
        }
    }

    private fun launchUserView() {
        val intent = Intent(mContext, UsersViewActivity::class.java)
        startActivity(intent)
    }

    private fun launchTransactionView(type: Long) {
        val intent = Intent(mContext, TransactionViewActivity::class.java)
        var viewType = LedgerDefine.TRANSACTION_VIEW_TYPE_ALL
        if (mSignInProfile!!.isHasLimitedAccess) {
            viewType = LedgerDefine.TRANSACTION_VIEW_TYPE_USER

            if (mSignInProfile!!.userAccounts!!.size > 1) {
                intent.putExtra(
                    LedgerDefine.ID,
                    LedgerDefine.PREFIX_MASTER + mSignInProfile!!.userID
                )
            } else {
                intent.putExtra(
                    LedgerDefine.ID,
                    LedgerDefine.PREFIX_PERSONAL + mSignInProfile!!.userID
                )
            }
        }
        intent.putExtra(LedgerDefine.TRANSACTION_VIEW_TYPE, viewType)
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

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    mCurrentUserInfo = mAuth!!.currentUser
                    updateUI(TYPE_SIGNED_DATA_FETCHING)
                    mHandler.sendEmptyMessage(MSG_USER_PROFILE)
                } else {
                    // Sign in fails
                    Toast.makeText(
                        applicationContext, "Authentication failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(TYPE_NOT_SIGNED)
                }
            }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Toast.makeText(applicationContext, "Google Play Services error.", Toast.LENGTH_SHORT)
            .show();
    }

    private fun signIn() {
        var companyID = et_company_id.text.toString()
        if (TextUtils.isEmpty(companyID)) return
        LedgerSharePrefManger(this!!.mContext!!).setCompanyID(companyID)
        val intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(intent, REQUEST_CODE_SIGN_IN)
    }

    private fun signOut() {
        // sign out Firebase
        mAuth!!.signOut()

        // sign out Google
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
            .setResultCallback { updateUI(TYPE_NOT_SIGNED) }
    }


    /*this method will update for 3 case
        *  1. signin
        *  2. fetching the data from server
        *  3. after fetching update the data.
        * */
    private fun updateUI(type: Int) {
        Log.d(TAG, "type =  $type")
        mCurrentType = type
        when (type) {
            TYPE_NOT_SIGNED -> {
                layout_01_sign_in.visibility = View.VISIBLE
                layout_02_loading.visibility = View.GONE
                layout_03_admin.visibility = View.GONE
                layout_03_normal.visibility = View.GONE
                supportActionBar!!.show()
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
                supportActionBar!!.show()
                if (mSignInProfile == null) {
                    Toast.makeText(mContext, R.string.error_02, Toast.LENGTH_LONG)
                    signOut()
                    return
                }
                if (!isNeedToLaunchIntent()) {
                    tv_company_info.visibility = View.VISIBLE
                    tv_company_info.text = LedgerSharePrefManger(mContext).getCompanyName()
                    layout_01_sign_in.visibility = View.GONE
                    layout_02_loading.visibility = View.GONE
                    // if (mSignInProfile!!.isAdmin) {
                    layout_03_admin.visibility = View.VISIBLE
                    layout_03_normal.visibility = View.GONE
                    tv_user_name.text = mSignInProfile!!.name
                    tv_admin_title.text = mSignInProfile!!.name
                    showAddDialog()
                }

            }
        }
    }

    private fun isNeedToLaunchIntent(): Boolean {
        if (TextUtils.isEmpty(mIntentAction) || TextUtils.isEmpty(mIntentType) ||
            Intent.ACTION_SEND != mIntentAction || !mIntentType!!.startsWith("image/")
        ) {
            return false
        } else {
            //
            var intentAct = Intent(mContext, AddMaterialActivity::class.java)
            intentAct.putExtra(
                Intent.EXTRA_STREAM,
                intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
            )
            startActivity(intentAct)
            mIntentAction = null
            return true
        }

    }

    fun getUnderLineText(str: String): SpannableString {
        val content = SpannableString(str)
        content.setSpan(UnderlineSpan(), 0, str.length, 0)
        content.setSpan(
            ForegroundColorSpan(Color.BLUE),
            0,
            str.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
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

        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        mFirebaseDB!!.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_USERS)
            .whereEqualTo("email", mCurrentUserInfo!!.email)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    mSignInProfile = null
                    for (document in task.result!!) {
                        setProfileInfo(document)
                    }
                    getCompanyProfile(mFirebaseDB!!, companyID)
                    LedgerUtils.signInProfile = mSignInProfile
                    syncData()
                    Handler().postDelayed(Runnable { updateUI(TYPE_SIGNED) }, 200)
                } else {
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG)
                }
            })
        return true;
    }

    private fun syncData() {
        syncUsers()

    }

    private fun syncUsers() {


    }

    private fun getCompanyProfile(db: FirebaseFirestore, companyID: String) {
        db.collection("companies")
            .whereEqualTo("companyID", companyID)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                var companyName = ""
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        companyName = document.get("Name") as String

                        break
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG)
                }
                LedgerSharePrefManger(mContext).setCompanyName(companyName)
            })
    }


    private fun setProfileInfo(document: QueryDocumentSnapshot?) {
        if (document != null) {
            if (mSignInProfile == null) {
                mSignInProfile = SignInProfile()
            }

            var userID = document.get(LedgerDefine.USER_ID)
            var designation = document.get(LedgerDefine.DESIGNATION)

            var projects = document.get(LedgerDefine.ACCESSIBLE_PROJECTS)

            if (projects != null) {
                mSignInProfile!!.accesibleProjects = projects as ArrayList<String>
            }

            var accounts = document.get(LedgerDefine.ACCOUNTS)

            if (accounts != null) {
                mSignInProfile!!.userAccounts = accounts as ArrayList<String>
            }

            var isAdmin = document.get(LedgerDefine.IS_ADMIN)
            if (isAdmin != null) {
                mSignInProfile!!.isAdmin = isAdmin as Boolean
            }

            var isLimitedAccess = document.get(LedgerDefine.IS_HAS_LIMITED_ACCESS)
            if (isLimitedAccess != null) {
                mSignInProfile!!.isHasLimitedAccess = isLimitedAccess as Boolean
            }

            var isHasEditPermission = document.get(LedgerDefine.IS_HAS_EDIT_PERMISSION)
            if (isHasEditPermission != null) {
                mSignInProfile!!.isHasEditPermission = isHasEditPermission as Boolean
            }

            var name = document.get(LedgerDefine.NAME)
            if (name != null) mSignInProfile!!.name = name as String
            try {
                userID = (userID as Long).toString()
            } catch (e: ClassCastException) {
                userID = userID as String
            }
            if (userID != null) mSignInProfile!!.userID = userID.toString()


        }
    }
}
