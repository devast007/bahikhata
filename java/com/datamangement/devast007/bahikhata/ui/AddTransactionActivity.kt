package com.datamangement.devast007.bahikhata.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.fragment.DialogFragmentToSelectProject
import com.datamangement.devast007.bahikhata.utils.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_add_transaction.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class AddTransactionActivity : AppCompatActivity(), View.OnClickListener {


    private val TAG = "AddTransactionActivity"
    private var mContext: Context? = null
    var mSignInProfile: SignInProfile? = null

    var mSelectedProject: ProjectDetails? = null
    var mSelectedSender: UserDetails? = null
    var mSelectedReceiver: UserDetails? = null
    var mSelectedDebitAccount: BankAccountDetail? = null
    var mSelectedCreditAccount: BankAccountDetail? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        mContext = this
        toolbar.setTitle(R.string.create_transaction)
        setSupportActionBar(toolbar)
        tv_project_id.setOnClickListener(this)
        tv_sender_id.setOnClickListener(this)
        tv_receiver_id.setOnClickListener(this)
        tv_debit_account.setOnClickListener(this)
        tv_credit_account.setOnClickListener(this)
        btn_save.setOnClickListener(this)


        mSignInProfile = LedgerUtils.signInProfile
        getUsersList()
        if (mSignInProfile!!.isAdmin) {
            getProjectList()
            getAccountList()
            tv_debit_account.visibility = View.VISIBLE
            tv_credit_account.visibility = View.VISIBLE
            rg_transaction_mode.visibility = View.VISIBLE
            tv_sender_id.isEnabled = true
        } else {
            tv_debit_account.visibility = View.GONE
            tv_credit_account.visibility = View.GONE
            rg_transaction_mode.visibility = View.GONE
            tv_sender_id.isEnabled = false
        }

        setDefaultDate()
    }

    private fun getAccountList() {
        var projects: ArrayList<String> = ArrayList<String>()

        var db = FirestoreDataBase().db

        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()

        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    manageAccounts(task)
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })

    }

    private fun manageProjects(task: Task<QuerySnapshot>) {

        for (document in task.result!!) {
            var project: ProjectDetails = ProjectDetails()
            project.projectID = document.get(LedgerDefine.PROJECT_ID) as String
            project.name = document.get(LedgerDefine.NAME) as String
            mProjectList!!.add(project)
        }
    }

    private fun manageAccounts(task: Task<QuerySnapshot>) {

        for (document in task.result!!) {
            var account: BankAccountDetail = BankAccountDetail()
            account.id = document.get(LedgerDefine.BANK_ACCOUNT_ID) as String
            account.accountNo = document.get(LedgerDefine.BANK_ACCOUNT_NUMBER) as String
            account.payee = document.get(LedgerDefine.PAYEE_NAME) as String
            mBankAccountList!!.add(account)
        }
    }

    private fun setDefaultDate() {
        val date = Calendar.getInstance().time
        et_day.setText(SimpleDateFormat("dd").format(date))
        et_month.setText(SimpleDateFormat("MM").format(date))
        et_year.setText(SimpleDateFormat("yyyy").format(date))
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tv_project_id -> openDialog(LedgerDefine.SELECTION_TYPE_PROJECT)
            R.id.tv_sender_id -> openDialog(LedgerDefine.SELECTION_TYPE_SENDER)
            R.id.tv_receiver_id -> openDialog(LedgerDefine.SELECTION_TYPE_RECEIVER)
            R.id.tv_debit_account -> openDialog(LedgerDefine.SELECTION_TYPE_DEBIT_ACCOUNT)
            R.id.tv_credit_account -> openDialog(LedgerDefine.SELECTION_TYPE_CREDIT_ACCOUNT)
            R.id.btn_save -> saveBtnClicked()
        }
    }

    private fun saveBtnClicked() {
        hideKeyboard()
        var amount = et_amount.text.toString()
        var remarks = et_remarks.text.toString()

        if (!mSignInProfile!!.isAdmin && (mSelectedProject == null || isEmpty(mSelectedProject!!.projectID!!))) {
            toast(R.string.project_name_empty)
            return
        }

        if (mSelectedSender == null) {
            toast(R.string.sender_name_is_empty)
            return
        }

        if (mSelectedReceiver == null) {
            toast(R.string.receiver_name_is_empty)
            return
        }

        if (isEmpty(amount)) {
            toast(R.string.amount_is_empty)
            return
        }


        if (mSelectedReceiver!!.userID == mSelectedSender!!.userID) {
            toast(R.string.sender_and_receiver_same)
            return
        }

        if (mSelectedDebitAccount != null || mSelectedCreditAccount != null) {
            if (mSelectedDebitAccount == null || mSelectedCreditAccount == null) {
                toast(R.string.error_10)
                return
            }
        }
        var date: String? = getDateFormatted(et_day.text.toString(), et_month.text.toString(), et_year.text.toString())
        // elvis operator if(date == null ) return null
            ?: return

        var transactionMap: HashMap<String, Any> = HashMap<String, Any>()

        // loginId
        var loggedInID: String? = null
        if (mSignInProfile!!.isAdmin) {
            loggedInID = mSignInProfile!!.adminID
            transactionMap[LedgerDefine.VERIFIED] = true
        } else if (mSignInProfile!!.isSupervisor) {
            loggedInID = mSignInProfile!!.supervisorID
            transactionMap[LedgerDefine.VERIFIED] = false
        } else {
            toast(R.string.error_07)
            return
        }
        transactionMap[LedgerDefine.LOGGED_IN_ID] = loggedInID
        // amount
        transactionMap[LedgerDefine.AMOUNT] = amount.toLong()

        // debit/credit account
        if (mSelectedDebitAccount != null) {
            val debitAccountID = mSelectedDebitAccount!!.id
            val creditAccountID = mSelectedCreditAccount!!.id
            var checkedButton = rg_transaction_mode.checkedRadioButtonId
            var transactionMode = findViewById<RadioButton>(checkedButton).text.toString()
            transactionMap[LedgerDefine.DEBIT_ACCOUNT_ID] = debitAccountID
            transactionMap[LedgerDefine.CREDIT_ACCOUNT_ID] = creditAccountID
            transactionMap[LedgerDefine.PAYMENT_MODE] = transactionMode

        }
        //project
        if (mSelectedProject != null && !isEmpty(mSelectedProject!!.projectID)) {
            transactionMap[LedgerDefine.PROJECT_ID] = mSelectedProject!!.projectID
        }
        //sender
        transactionMap[LedgerDefine.SENDER_ID] = mSelectedSender!!.userID

        //receiver
        transactionMap[LedgerDefine.RECEIVER_ID] = mSelectedReceiver!!.userID

        // transaction date
        transactionMap[LedgerDefine.TRANSACTION_DATE] = date!!

        // timestamp
        transactionMap[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()

        // transaction type
        var transactionType: Int = -1
        var senderDesignation = mSelectedSender!!.designation
        var receiverDesignation = mSelectedReceiver!!.designation
        if (senderDesignation == LedgerDefine.DESIGNATION_ADMIN) {
            if (receiverDesignation == LedgerDefine.DESIGNATION_SUPERVISOR) {
                transactionType = LedgerDefine.TRANSACTION_TYPE_ADMIN
                transactionMap[LedgerDefine.PROJECT_ID] = ""
            } else if (receiverDesignation == LedgerDefine.DESIGNATION_NORMAL) {
                transactionType = LedgerDefine.TRANSACTION_TYPE_NORMAL
                if (mSelectedProject == null || isEmpty(mSelectedProject!!.projectID)) {
                    toast(R.string.project_name_empty)
                    return
                }
            } else {
                toast(R.string.error_08)
                return
            }

        } else if (senderDesignation == LedgerDefine.DESIGNATION_SUPERVISOR) {
            if (receiverDesignation == LedgerDefine.DESIGNATION_NORMAL) {
                transactionType = LedgerDefine.TRANSACTION_TYPE_NORMAL
            } else if (receiverDesignation == LedgerDefine.DESIGNATION_SUPERVISOR) {
                transactionType = LedgerDefine.TRANSACTION_TYPE_SUPERVISOR
                transactionMap[LedgerDefine.PROJECT_ID] = ""
            } else if (receiverDesignation == LedgerDefine.DESIGNATION_ADMIN) {
                transactionType = LedgerDefine.TRANSACTION_TYPE_ADMIN
                transactionMap[LedgerDefine.PROJECT_ID] = ""
            } else {
                toast(R.string.error_08)
                return
            }

        } else if (senderDesignation == LedgerDefine.DESIGNATION_NORMAL) {
            if (receiverDesignation == LedgerDefine.DESIGNATION_ADMIN) {
                transactionType = LedgerDefine.TRANSACTION_TYPE_NORMAL
                if (mSelectedProject == null || isEmpty(mSelectedProject!!.projectID)) {
                    toast(R.string.project_name_empty)
                    return
                }
            } else {
                toast(R.string.error_08)
                return
            }

        }
        transactionMap[LedgerDefine.TRANSACTION_TYPE] = transactionType
        transactionMap[LedgerDefine.REMARK] = remarks

        //verified todo
        saveDataToFireStore(transactionMap)


    }

    private fun saveDataToFireStore(transactionMap: HashMap<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        val companyName = LedgerSharePrefManger(mContext).getCompanyName()
        btn_save.isEnabled = false
        btn_save.setTextColor(Color.YELLOW)
        btn_save.setText(R.string.saving)
        var docRef = db.collection(LedgerDefine.COMPANIES_SLASH + companyName + "/transactions").document()
        transactionMap[LedgerDefine.TRANSACTION_ID] = docRef.id
        docRef.set(transactionMap)
            .addOnSuccessListener(OnSuccessListener<Void> {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                btn_save.setTextColor(Color.parseColor("#FF7C7B7B"))
                btn_save.setText(R.string.saved)
                showSnackBar()

            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
                toast(R.string.error_09)
                btn_save.setTextColor(Color.RED)
                btn_save.setText(R.string.failed)
            })

    }

    private fun showSnackBar() {
        val snackbar = Snackbar
            .make(coordinatorLayout, R.string.saved_successfully, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.add_more) {
                btn_save.isEnabled = true
                btn_save.setTextColor(Color.BLACK)
                btn_save.setText(R.string.save)
            }
        snackbar.setActionTextColor(Color.BLUE)
        val sbView = snackbar.view
        sbView.setBackgroundColor(Color.RED)
        val textView = sbView.findViewById<View>(android.support.design.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.parseColor("#FF0B3E0D"))
        snackbar.show()
    }

    private fun getDateFormatted(dd: String, mm: String, yyyy: String): String? {
        if (isEmpty(dd) || isEmpty(mm) || isEmpty(yyyy)) {
            toast(R.string.date_month_year_is_empty)
            return null
        }

        var day: Int = dd.toInt()
        var month: Int = mm.toInt()
        var year: Int = yyyy.toInt()

        if (day > 31 || day <= 0) {
            toast(R.string.day_should_be_between_1_to_31)
            return null
        }
        if (month > 12 || day <= 0) {
            toast(R.string.month_should_be_between_1_to_12)
            return null
        }
        if (year <= 1900) {
            toast(R.string.year_should_be_more_than_1900)
            return null
        }
        var time = SimpleDateFormat("HHmmss").format(Calendar.getInstance().time)

        var dayStr = day.toString()
        if (day < 10) {
            dayStr = "0$day";
        }

        var monthStr = month.toString()
        if (month < 10) {
            monthStr = "0$monthStr";
        }
        return (year.toString() + monthStr + dayStr + time)
    }

    private fun toast(id: Int) {
        Toast.makeText(mContext, id, Toast.LENGTH_LONG).show()
    }

    private fun isEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }

    private fun openDialog(type: Int) {
        var fm = getSupportFragmentManager()
        var dFragment = DialogFragmentToSelectProject()
        var bundle = Bundle()
        bundle.putInt(LedgerDefine.KEY_SELECTION_TYPE, type)
        dFragment.arguments = bundle
        // Show DialogFragment
        dFragment.show(fm, " project select Dialog Fragment")
    }

    private fun getProjectList() {
        var db = FirestoreDataBase().db

        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()

        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/projects")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    manageProjects(task)
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getUsersList() {
        var projects: ArrayList<String> = ArrayList<String>()

        var db = FirestoreDataBase().db

        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()

        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/users")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    manageUserData(task)
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun manageUserData(task: Task<QuerySnapshot>): Boolean {
        if (mSignInProfile == null) {
            finish()
            return false
        }
        val isAdmin = mSignInProfile!!.isAdmin
        val isSuperVisor = mSignInProfile!!.isSupervisor
        val isNormalUser = mSignInProfile!!.isNormal
        val accessedProjects = mSignInProfile!!.accesibleProjects


        for (document in task.result!!) {
            Log.d(TAG, document.id + " => " + document.data)
            Log.d(TAG, " document.get(\"name\")+ => " + document.get("name"))
            // senderList

            val tempName = document.get(LedgerDefine.NAME)
            val tempId = document.get(LedgerDefine.USER_ID)
            val tempDesignation = document.get(LedgerDefine.DESIGNATION)
            val tempIsAdmin = document.get(LedgerDefine.IS_ADMIN)
            val tempAccessedProjects = document.get(LedgerDefine.ACCESSIBLE_PROJECTS)

            val userDetails = UserDetails()

            if (tempName != null) userDetails.name = tempName as String

            if (tempId != null) userDetails.userID = tempId as String

            if (tempDesignation != null) userDetails.designation = tempDesignation as Long

            if (tempIsAdmin != null) {
                if (tempIsAdmin as Boolean) userDetails.designation = LedgerDefine.DESIGNATION_ADMIN
            }

            if (tempAccessedProjects != null) {
                userDetails.accesibleProjectsList = tempAccessedProjects as ArrayList<String>
            }

            if (isAdmin) {
                mReceiverList!!.add(userDetails)
                mSenderList!!.add(userDetails)

            } else if (isSuperVisor) {
                addToReceiverList(userDetails, accessedProjects!!)
            }
        }
        if (isSuperVisor) {
            val senderData = UserDetails()
            senderData.userID = mSignInProfile!!.supervisorID
            senderData.name = mSignInProfile!!.name
            senderData.designation = LedgerDefine.DESIGNATION_SUPERVISOR
            mSenderList!!.add(senderData)
            tv_sender_id.setText(senderData.userID + "\n" + senderData.name)
            tv_sender_id.setTextColor(Color.BLUE)
            mSelectedSender = senderData
            for (id in mSignInProfile!!.accesibleProjects!!) {
                var projectDetails: ProjectDetails = ProjectDetails()
                projectDetails.name = ""
                projectDetails.projectID = id
                mProjectList!!.add(projectDetails)
            }
        }
        // ReceiverList
        return true
    }

    private fun addToReceiverList(userDetail: UserDetails, projectAccess: ArrayList<String>) {

        if (userDetail.accesibleProjectsList != null) {
            for (projectID in projectAccess) {
                for (tempProjectID in userDetail.accesibleProjectsList!!) {
                    if (projectID == tempProjectID) {
                        // doubt to == operator
                        mReceiverList!!.add(userDetail)
                        break
                    }
                }
            }

        }
    }

    var mSenderList: ArrayList<UserDetails>? = ArrayList<UserDetails>()
    var mReceiverList: ArrayList<UserDetails>? = ArrayList<UserDetails>()
    var mBankAccountList: ArrayList<BankAccountDetail>? = ArrayList<BankAccountDetail>()
    var mProjectList: ArrayList<ProjectDetails>? = ArrayList<ProjectDetails>()

    private fun hideKeyboard() {
        val imm = mContext!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = getCurrentFocus()
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(mContext);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
