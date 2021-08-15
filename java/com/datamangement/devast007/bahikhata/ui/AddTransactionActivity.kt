package com.datamangement.devast007.bahikhata.ui

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.dialog.DialogFragmentForTransationAmount
import com.datamangement.devast007.bahikhata.ui.dialog.DialogFragmentToSelectUserOrProject
import com.datamangement.devast007.bahikhata.ui.dialog.DialogTransactionRemarksSubcategroy
import com.datamangement.devast007.bahikhata.utils.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_add_transaction.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class AddTransactionActivity : AppCompatActivity(), View.OnClickListener,
    LedgerUtils.Companion.StatusListner, View.OnLongClickListener {


    private var mDocumentToBeUpdated: TransactionDetails? = null
    val mRemarksArrayList: ArrayList<String>? = ArrayList<String>()
    val msubCategoryArrayList: ArrayList<String>? = ArrayList<String>()
    private val SENDER_ACCOUNT: Int = 1
    private val RECEIVER_ACCOUNT: Int = 2
    private val TAG = "AddTransactionActivity"
    private var mContext: Context? = null
    var mSignInProfile: SignInProfile? = null

    var mSelectedProject: ProjectDetails? = null
    var mSelectedSender: UserDetails? = null
    var mSelectedReceiver: UserDetails? = null
    var mSelectedDebitAccount: BankAccountDetail? = null
    var mSelectedCreditAccount: BankAccountDetail? = null

    private var mEditType: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        mContext = this
        supportActionBar!!.setTitle(R.string.create_transaction)
        tv_project_id.setOnClickListener(this)
        tv_sender_id.setOnClickListener(this)
        tv_receiver_id.setOnClickListener(this)
        tv_debit_account.setOnClickListener(this)
        tv_credit_account.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        img_btn_clear_debit_account.setOnClickListener(this)
        img_btn_clear_credit_account.setOnClickListener(this)
        btn_suggestion_amount.setOnClickListener(this)
        img_btn_suggestion_remarks.setOnClickListener(this)
        img_btn_suggestion_remarks.setOnLongClickListener(this)
        img_btn_suggestion_subcategroy.setOnClickListener(this)
        img_btn_suggestion_subcategroy.setOnLongClickListener(this)
        findViewById<ImageButton>(R.id.img_btn_auto_fetch_bank_accounts).setOnClickListener(this)

        et_amount.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                cs: CharSequence,
                arg1: Int,
                arg2: Int,
                arg3: Int
            ) {
            }

            override fun beforeTextChanged(
                arg0: CharSequence,
                arg1: Int,
                arg2: Int,
                arg3: Int
            ) {

            }

            override fun afterTextChanged(arg0: Editable?) {
                if (arg0 != null) {
                    try {

                        tv_digit_in_word.text =
                            CurrenyToWord.convertToIndianCurrency(arg0.toString())
                    } catch (e: Exception) {
                        tv_digit_in_word.text = ""
                    }

                }
            }
        })

        mSignInProfile = LedgerUtils.signInProfile
        getUsersList()
        getProjectList()
        getAccountList()
        if (mSignInProfile!!.isAdmin) {
            /* tv_debit_account.visibility = View.VISIBLE
             tv_credit_account.visibility = View.VISIBLE
             rg_transaction_mode.visibility = View.VISIBLE*/
            et_day.isEnabled = true
            et_month.isEnabled = true
            et_year.isEnabled = true
        } else {
            /*tv_debit_account.visibility = View.GONE
            tv_credit_account.visibility = View.GONE
            rg_transaction_mode.visibility = View.GONE*/
            /* et_day.isEnabled = false
             et_month.isEnabled = false
             et_year.isEnabled = false*/
        }

    }

    private fun initApp() {
        mEditType = intent.getIntExtra(LedgerDefine.TRANSACTION_EDIT_TYPE, -1)
        if (mEditType == LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY) {
            setDataToUpdate()
        } else {
            setDefaultDate()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private var mTransactionIdToUpdate: String? = null

    private var mReceiverIdToUpdate: String? = null
    private var mSenderIdToUpdate: String? = null

    private fun setDataToUpdate() {
        mTransactionIdToUpdate = intent.getStringExtra(LedgerDefine.TRANSACTION_ID)

        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        var query: Query =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
                .whereEqualTo(LedgerDefine.TRANSACTION_ID, mTransactionIdToUpdate)
        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    var queryDocs = task.result!!
                    for (document: QueryDocumentSnapshot in queryDocs) {
                        mDocumentToBeUpdated = LedgerUtils.getTransactionDetails(document)
                        val receiverId = mDocumentToBeUpdated!!.receiverId

                        mSelectedReceiver = UserDetails()
                        mSelectedReceiver!!.userID = receiverId!!
                        mReceiverIdToUpdate = receiverId
                        tv_receiver_id.text = receiverId

                        val senderId = mDocumentToBeUpdated!!.senderId
                        mSelectedSender = UserDetails()
                        mSelectedSender!!.userID = senderId!!
                        mSenderIdToUpdate = senderId
                        tv_sender_id.text = senderId

                        et_amount.setText("" + mDocumentToBeUpdated!!.amount)
                        et_round_off.setText("" + mDocumentToBeUpdated!!.roundOff)
                        var projectID = mDocumentToBeUpdated!!.projectId
                        if (!isEmpty(projectID)) {
                            mSelectedProject = ProjectDetails()
                            mSelectedProject!!.projectID = projectID
                            tv_project_id.text = projectID
                        }

                        val date = SimpleDateFormat("yyyyMMddHHmmss")
                        date.parse(mDocumentToBeUpdated!!.transactionDate)

                        et_day.setText("" + date.calendar.get(Calendar.DAY_OF_MONTH))
                        et_month.setText("" + (date.calendar.get(Calendar.MONTH) + 1))
                        et_year.setText("" + date.calendar.get(Calendar.YEAR))

                        et_remarks.setText(mDocumentToBeUpdated!!.remarks)
                        et_auto_complete_subcategory.setText(mDocumentToBeUpdated!!.subCategory)

                        val debitedAccount = mDocumentToBeUpdated!!.debitedTo
                        if (!isEmpty(debitedAccount)) {
                            mSelectedDebitAccount = BankAccountDetail()
                            mSelectedDebitAccount!!.id = debitedAccount!!
                            tv_debit_account.text = debitedAccount
                        }

                        val creditedAccount = mDocumentToBeUpdated!!.creditedTo
                        if (!isEmpty(creditedAccount)) {
                            mSelectedCreditAccount = BankAccountDetail()
                            mSelectedCreditAccount!!.id = creditedAccount!!
                            tv_credit_account.text = creditedAccount
                        }
                        switch_trackings.isChecked =
                            mDocumentToBeUpdated!!.isTrackingOn

                        val paymentMode = mDocumentToBeUpdated!!.paymentMode
                        rb_cash.isChecked = true
                        if (!isEmpty(paymentMode)) {
                            if (paymentMode != LedgerDefine.OTHER && paymentMode != LedgerDefine.CASH) {
                                rb_online.isChecked = true
                            }
                        }

                        break
                    }
                }
            })

    }

    private fun getAccountList() {

        var db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
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
            var text = document.get(LedgerDefine.SUBCATEGORY)
            if (text != null) project.subCategory = text as String
            var amount = document.get(LedgerDefine.AMOUNT)
            if (amount != null)
                project.amount = amount as Long
            mProjectList!!.add(project)
        }
        initApp()
    }


    private fun manageAccounts(task: Task<QuerySnapshot>) {

        for (document in task.result!!) {
            var account: BankAccountDetail = BankAccountDetail()
            account.id = document.get(LedgerDefine.BANK_ACCOUNT_ID) as String
            account.accountNo = document.get(LedgerDefine.BANK_ACCOUNT_NUMBER) as String
            account.payee = document.get(LedgerDefine.PAYEE_NAME) as String

            var amount = document.get(LedgerDefine.AMOUNT)
            if (amount != null)
                account.amount = amount as Long

            mBankAccountList!!.add(account)
        }
    }

    fun setSubCategory(categories: String) {
        if (categories.isEmpty()) return

        categories.split(" ")

        var adapter = ArrayAdapter<String>(
            mContext!!,
            android.R.layout.simple_dropdown_item_1line, categories.split(" ")
        )
        et_auto_complete_subcategory.threshold = 1
        et_auto_complete_subcategory.setAdapter(adapter)
        et_auto_complete_subcategory.onFocusChangeListener =
            View.OnFocusChangeListener { v, hasFocus -> if (hasFocus) et_auto_complete_subcategory.showDropDown() }
    }


    private fun setDefaultDate() {
        val date = Calendar.getInstance().time
        et_day.setText(SimpleDateFormat("dd").format(date))
        et_month.setText(SimpleDateFormat("MM").format(date))
        et_year.setText(SimpleDateFormat("yyyy").format(date))
    }

    override fun onClick(view: View?) {
        //hideKeyboard()
        findViewById<EditText>(R.id.et_amount).clearFocus()
        findViewById<EditText>(R.id.et_auto_complete_subcategory).clearFocus()
        findViewById<EditText>(R.id.et_remarks).clearFocus()
        when (view!!.id) {
            R.id.tv_project_id -> openDialog(LedgerDefine.SELECTION_TYPE_PROJECT)
            R.id.tv_sender_id -> openDialog(LedgerDefine.SELECTION_TYPE_SENDER)
            R.id.tv_receiver_id -> openDialog(LedgerDefine.SELECTION_TYPE_RECEIVER)
            R.id.tv_debit_account -> openDialog(LedgerDefine.SELECTION_TYPE_DEBIT_ACCOUNT)
            R.id.tv_credit_account -> openDialog(LedgerDefine.SELECTION_TYPE_CREDIT_ACCOUNT)
            R.id.btn_save -> saveBtnClicked()
            R.id.img_btn_clear_debit_account -> clearDebitAccount()
            R.id.img_btn_clear_credit_account -> clearCreditAccount()
            R.id.btn_suggestion_amount -> openDialogForAmount()
            R.id.img_btn_suggestion_remarks -> openDialogForRemarks()
            R.id.img_btn_suggestion_subcategroy -> openDialogForSubcategory()
            R.id.img_btn_auto_fetch_bank_accounts -> fetchAccountDetailsForUser()

        }
    }


    private fun fetchAccountDetailsForUser() {


        val receiverID = LedgerUtils.getUserAccount(tv_receiver_id.text.toString())
        if (isEmpty(receiverID)) return

        Toast.makeText(mContext, R.string.fetching_bank_account, Toast.LENGTH_SHORT).show()

        var db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(mContext).getCompanyID()

        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
            .whereEqualTo(LedgerDefine.RECEIVER_ID, receiverID)
            .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING).limit(50)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {

                    mRemarksArrayList!!.clear()
                    for (document in task.result!!) {
                        var debitAccountID =
                            document!!.get(LedgerDefine.DEBIT_ACCOUNT_ID).toString()
                        var creditAccountID =
                            document!!.get(LedgerDefine.CREDIT_ACCOUNT_ID).toString()

                        if (!isEmpty(creditAccountID)) {
                            for (bankAccountDetail in mBankAccountList!!) {
                                if (creditAccountID == bankAccountDetail.id) {
                                    mSelectedCreditAccount = bankAccountDetail
                                    var str =
                                        bankAccountDetail.accountNo + "\n" + bankAccountDetail.payee
                                    tv_credit_account.text = str
                                    rb_online.isChecked = true
                                    break

                                }
                            }
                        } else {
                            continue
                        }

                        if (!isEmpty(debitAccountID)) {
                            for (bankAccountDetail in mBankAccountList!!) {
                                if (debitAccountID == bankAccountDetail.id) {
                                    mSelectedDebitAccount = bankAccountDetail
                                    var str =
                                        bankAccountDetail.accountNo + "\n" + bankAccountDetail.payee
                                    tv_debit_account.text = str
                                    break

                                }
                            }
                        }
                        Toast.makeText(mContext, R.string.bank_account_fetched, Toast.LENGTH_SHORT)
                            .show()
                        break
                    }

                    Toast.makeText(mContext, R.string.search_finished, Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }


            })
    }


    private fun openDialogForSubcategory() {

        val receiverID = LedgerUtils.getUserAccount(tv_receiver_id.text.toString())

        if (!isEmpty(receiverID)) {

            var db = FirestoreDataBase().db

            val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
                .whereEqualTo(LedgerDefine.RECEIVER_ID, receiverID)
                .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
                .limit(15)
                .get()
                .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                    if (task.isSuccessful) {
                        msubCategoryArrayList!!.clear()
                        for (document in task.result!!) {
                            var tempSubcategory =
                                document!!.get(LedgerDefine.SUBCATEGORY).toString()
                            if (!isEmpty(tempSubcategory) && !msubCategoryArrayList.contains(
                                    tempSubcategory
                                )
                            ) msubCategoryArrayList.add(tempSubcategory)
                        }
                        if (msubCategoryArrayList.isNotEmpty()) {

                            var fm = supportFragmentManager
                            var dFragment = DialogTransactionRemarksSubcategroy()
                            var bundle = Bundle()
                            bundle.putInt(
                                LedgerDefine.KEY_SELECTION_TYPE,
                                LedgerDefine.SELECTION_TYPE_SUBCATEGORY
                            )
                            dFragment.arguments = bundle
                            // Show DialogFragment
                            dFragment.show(
                                fm,
                                " subcategory for transaction select Dialog Fragment"
                            )


                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.exception)
                        Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG)
                            .show()
                    }
                })


        } else {
            toast(R.string.receiver_name_is_empty)
        }


    }

    private fun openDialogForRemarks() {

        val receiverID = LedgerUtils.getUserAccount(tv_receiver_id.text.toString())

        if (!isEmpty(receiverID)) {

            var db = FirestoreDataBase().db

            val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
                .whereEqualTo(LedgerDefine.RECEIVER_ID, receiverID)
                .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
                .limit(15)
                .get()
                .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                    if (task.isSuccessful) {

                        mRemarksArrayList!!.clear()
                        for (document in task.result!!) {
                            var tempRemarks = document!!.get(LedgerDefine.REMARK).toString()

                            if (!isEmpty(tempRemarks)) mRemarksArrayList.add(tempRemarks)
                        }
                        if (mRemarksArrayList.isNotEmpty()) {
                            var fm = supportFragmentManager
                            var dFragment = DialogTransactionRemarksSubcategroy()
                            var bundle = Bundle()
                            bundle.putInt(
                                LedgerDefine.KEY_SELECTION_TYPE,
                                LedgerDefine.SELECTION_TYPE_REMARKS
                            )
                            dFragment.arguments = bundle
                            // Show DialogFragment
                            dFragment.show(
                                fm,
                                " subcategory for transaction select Dialog Fragment"
                            )
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.exception)
                        Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG)
                            .show()
                    }
                })


        } else {
            toast(R.string.receiver_name_is_empty)
        }


    }

    private fun openDialogForAmount() {
        var fm = supportFragmentManager
        var dFragment = DialogFragmentForTransationAmount()
        // Show DialogFragment
        dFragment.show(fm, " Amount select Dialog Fragment")
    }

    private fun clearCreditAccount() {
        mSelectedCreditAccount = null
        tv_credit_account.text = null
        rb_cash.isChecked = true
    }

    private fun clearDebitAccount() {
        mSelectedDebitAccount = null
        tv_debit_account.text = null
    }

    private fun saveBtnClicked() {
        hideKeyboard()
        var amount = et_amount.text.toString()
        var roundOff = et_round_off.text.toString()
        var remarks = et_remarks.text.toString()
        var subcategory = et_auto_complete_subcategory.text.toString()

        if (!mSignInProfile!!.isAdmin && (mSelectedProject == null || isEmpty(
                mSelectedProject!!.projectID!!
            ))
        ) {
            toast(R.string.project_name_empty)
            return
        }

        val senderID = LedgerUtils.getUserAccount(tv_sender_id.text.toString())
        val receiverID = LedgerUtils.getUserAccount(tv_receiver_id.text.toString())
        if (isEmpty(senderID)) {
            toast(R.string.sender_name_is_empty)
            return
        }

        if (isEmpty(receiverID)) {
            toast(R.string.receiver_name_is_empty)
            return
        }

        if (isEmpty(amount)) {
            toast(R.string.amount_is_empty)
            return
        }


        if (senderID == receiverID) {
            toast(R.string.sender_and_receiver_same)
            return
        }

        var date: String? = getDateFormatted(
            et_day.text.toString(),
            et_month.text.toString(),
            et_year.text.toString()
        )
        // elvis operator if(date == null ) return null
            ?: return

        var transactionMap: HashMap<String, Any> = HashMap<String, Any>()

        // loginId
        var loggedInID: String? = mSignInProfile!!.userID
        transactionMap[LedgerDefine.VERIFIED] = mSignInProfile!!.isAdmin

        transactionMap[LedgerDefine.LOGGED_IN_ID] = loggedInID!!
        // amount
        transactionMap[LedgerDefine.AMOUNT] = amount.toLong()

        transactionMap[LedgerDefine.ROUND_OFF] = 0 // default value
        if (!isEmpty(roundOff))
            transactionMap[LedgerDefine.ROUND_OFF] = roundOff.toLong()

        // debit/credit account
        if (mSelectedDebitAccount != null) {
            val debitAccountID = mSelectedDebitAccount!!.id
            transactionMap[LedgerDefine.DEBIT_ACCOUNT_ID] = debitAccountID

        } else {
            transactionMap[LedgerDefine.DEBIT_ACCOUNT_ID] = ""
        }

        if (mSelectedCreditAccount != null) {
            val creditAccountID = mSelectedCreditAccount!!.id
            transactionMap[LedgerDefine.CREDIT_ACCOUNT_ID] = creditAccountID
        } else {
            transactionMap[LedgerDefine.CREDIT_ACCOUNT_ID] = ""
        }

        var checkedButton = rg_transaction_mode.checkedRadioButtonId
        var transactionMode = findViewById<RadioButton>(checkedButton).text.toString()
        transactionMap[LedgerDefine.PAYMENT_MODE] = transactionMode

        //project
        if (mSelectedProject != null && !isEmpty(mSelectedProject!!.projectID)) {
            transactionMap[LedgerDefine.PROJECT_ID] = mSelectedProject!!.projectID!!

            if (subcategory.isEmpty()) {
                for (project in mProjectList!!) {
                    if (project.projectID == mSelectedProject!!.projectID!!) {
                        if (project.subCategory.isNotEmpty()) {
                            toast(R.string.subcategory_is_empty)
                            return
                        }
                    }
                }
            }
        }
        //sender
        transactionMap[LedgerDefine.SENDER_ID] = senderID!!

        //receiver
        transactionMap[LedgerDefine.RECEIVER_ID] = receiverID!!

        // transaction date
        transactionMap[LedgerDefine.TRANSACTION_DATE] = date!!

        // timestamp
        transactionMap[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()

        // transaction type
        var transactionType: Int = -1
        var senderDesignation = getUserAccountDesignation(senderID)
        var receiverDesignation = getUserAccountDesignation(receiverID)
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
                if (mSelectedProject == null || isEmpty(mSelectedProject!!.projectID)) {
                    toast(R.string.project_name_empty)
                    return
                }
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
        transactionMap[LedgerDefine.SUBCATEGORY] = subcategory
        transactionMap[LedgerDefine.IS_TRACKING_ON] = switch_trackings.isChecked

        //verified todo
        if (mEditType == LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY) {
            updateTransactionData(transactionMap)
        } else {
            saveDataToFireStore(transactionMap)
        }

        var projectID: String? = ""
        if (mSelectedProject != null) projectID = mSelectedProject!!.projectID
        var creditID: String = ""
        if (mSelectedCreditAccount != null) creditID = mSelectedCreditAccount!!.id
        savePreferenceToDB(
            projectID,
            receiverID,
            creditID,
            remarks
        )

    }

    private fun getUserAccountDesignation(userAccount: String): Long {
        var type: Long = LedgerDefine.DESIGNATION_NORMAL
        var prefix: String = userAccount.substring(0, 1)
        if (prefix == "A") {
            type = LedgerDefine.DESIGNATION_ADMIN
        } else if (prefix == "M") {
            type = LedgerDefine.DESIGNATION_SUPERVISOR
        }
        Log.d(TAG, "getUserAccountDesignation prefix : $prefix ,type = $type")
        return type
    }


    private fun savePreferenceToDB(
        projectID: String?,
        userID: String,
        creditAccount: String,
        remarks: String
    ) {
        var values = ContentValues()
        values.put(LedgerDefine.RECEIVER_ID, userID)
        values.put(LedgerDefine.PROJECT_ID, projectID)
        values.put(LedgerDefine.CREDIT_ACCOUNT_ID, creditAccount)
        values.put(LedgerDefine.REMARK, remarks)
        val selectionArgs = Array<String>(1) { userID }
        val count = mContext!!.contentResolver.update(
            SqlDBFile.CONTENT_URI_TABLE_SUGGESTION,
            values,
            LedgerDefine.RECEIVER_ID + " =?",
            selectionArgs
        )
        Log.d(TAG, "savePreferenceToDB update count = " + count)
        if (count <= 0) {
            mContext!!.contentResolver.insert(
                SqlDBFile.CONTENT_URI_TABLE_SUGGESTION,
                values
            )
            Log.d(TAG, "savePreferenceToDB inserted ")
        }
    }

    private fun updateTransactionData(transactionMap: HashMap<String, Any>) {
        setHistory(transactionMap)
        val db = FirebaseFirestore.getInstance()
        val companyName = LedgerSharePrefManger(mContext).getCompanyID()
        btn_save.isEnabled = false
        btn_save.setTextColor(Color.YELLOW)
        btn_save.setText(R.string.saving)
        transactionMap[LedgerDefine.TRANSACTION_ID] = mTransactionIdToUpdate!!
        var docRef =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_TRANSACTIONS)
                .document(mTransactionIdToUpdate!!)
        /*docRef.update(transactionMap)
            .addOnSuccessListener(OnSuccessListener<Void> {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                btn_save.setTextColor(Color.parseColor("#FF7C7B7B"))
                btn_save.setText(R.string.update_done)
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
                toast(R.string.error_09)
                btn_save.setTextColor(Color.RED)
                btn_save.setText(R.string.failed)
            })*/
        LedgerUtils.setstatusListner(this)
        LedgerUtils.setDataToFirestore(
            mContext!!, transactionMap[LedgerDefine.TRANSACTION_ID]!!,
            SqlDBFile.CONTENT_URI_TABLE_TRANSACTIONS,
            LedgerDefine.UPDATE_DATA,
            docRef,
            transactionMap
        )

    }

    private fun setHistory(transactionMap: HashMap<String, Any>) {
        val history = HashMap<String, Any>()
        if (mDocumentToBeUpdated != null) {
            // sender
            if (mDocumentToBeUpdated!!.senderId != transactionMap[LedgerDefine.SENDER_ID])
                history[LedgerDefine.SENDER_ID] = mDocumentToBeUpdated!!.senderId
            // receiver
            if (mDocumentToBeUpdated!!.receiverId != transactionMap[LedgerDefine.RECEIVER_ID])
                history[LedgerDefine.RECEIVER_ID] = mDocumentToBeUpdated!!.receiverId
            //txndate
            val originalFormat = SimpleDateFormat("yyyyMMddHHmmss")
            val old = originalFormat.parse(mDocumentToBeUpdated!!.transactionDate)
            val new = originalFormat.parse(transactionMap[LedgerDefine.TRANSACTION_DATE].toString())

            val targetFormat = SimpleDateFormat("dd-MM-yyyy")
            Log.d(TAG, " != targetFormat old " + mDocumentToBeUpdated!!.transactionDate)
            Log.d(TAG, " != targetFormat new" + transactionMap[LedgerDefine.TRANSACTION_DATE])
            if (targetFormat.format(old).toString() != targetFormat.format(new).toString()) {
                history[LedgerDefine.TRANSACTION_DATE] = mDocumentToBeUpdated!!.transactionDate
            }

            // project
            if (mDocumentToBeUpdated!!.projectId != transactionMap[LedgerDefine.PROJECT_ID])
                history[LedgerDefine.PROJECT_ID] = mDocumentToBeUpdated!!.projectId

            // amount
            if (mDocumentToBeUpdated!!.amount != transactionMap[LedgerDefine.AMOUNT])
                history[LedgerDefine.AMOUNT] = mDocumentToBeUpdated!!.amount

            // subcategory
            if (mDocumentToBeUpdated!!.subCategory != transactionMap[LedgerDefine.SUBCATEGORY])
                history[LedgerDefine.SUBCATEGORY] = mDocumentToBeUpdated!!.subCategory

            // debitaccount
            if (mDocumentToBeUpdated!!.debitedTo != transactionMap[LedgerDefine.DEBIT_ACCOUNT_ID])
                history[LedgerDefine.DEBIT_ACCOUNT_ID] = mDocumentToBeUpdated!!.debitedTo

            // creditaccount
            if (mDocumentToBeUpdated!!.creditedTo != transactionMap[LedgerDefine.CREDIT_ACCOUNT_ID])
                history[LedgerDefine.CREDIT_ACCOUNT_ID] = mDocumentToBeUpdated!!.creditedTo

            // remarks
            if (mDocumentToBeUpdated!!.remarks != transactionMap[LedgerDefine.REMARK])
                history[LedgerDefine.REMARK] = mDocumentToBeUpdated!!.remarks

            //roundoff
            if (mDocumentToBeUpdated!!.roundOff != transactionMap[LedgerDefine.ROUND_OFF])
                history[LedgerDefine.ROUND_OFF] = mDocumentToBeUpdated!!.roundOff

            // txnTracking
            if (mDocumentToBeUpdated!!.isTrackingOn != transactionMap[LedgerDefine.IS_TRACKING_ON])
                history[LedgerDefine.IS_TRACKING_ON] = mDocumentToBeUpdated!!.isTrackingOn

            // current time
            history[LedgerDefine.MODIFIED_DATE] = LocalDateTime.now()

            // modified by
            history[LedgerDefine.MODIFIER_LOGIN_ID] =
                transactionMap[LedgerDefine.LOGGED_IN_ID].toString()

            history[LedgerDefine.LOGGED_IN_ID] = mDocumentToBeUpdated!!.loggedInID
            // setting the value of history
            transactionMap[LedgerDefine.HISTORY] = FieldValue.arrayUnion(history.toString())
        }
    }

    private fun saveDataToFireStore(transactionMap: HashMap<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        val companyName = LedgerSharePrefManger(mContext).getCompanyID()
        btn_save.isEnabled = false
        btn_save.setTextColor(Color.YELLOW)
        btn_save.setText(R.string.saving)
        var docRef =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_TRANSACTIONS)
                .document()
        transactionMap[LedgerDefine.TRANSACTION_ID] = docRef.id
        /*docRef.set(transactionMap)
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
            })*/

        updateTransactionAmountForUser(
            transactionMap[LedgerDefine.SENDER_ID],
            transactionMap[LedgerDefine.RECEIVER_ID],
            transactionMap[LedgerDefine.PROJECT_ID],
            transactionMap[LedgerDefine.DEBIT_ACCOUNT_ID],
            transactionMap[LedgerDefine.CREDIT_ACCOUNT_ID],
            transactionMap[LedgerDefine.AMOUNT]
        )

        LedgerUtils.setstatusListner(this)
        LedgerUtils.setDataToFirestore(
            mContext!!, transactionMap[LedgerDefine.TRANSACTION_ID]!!,
            SqlDBFile.CONTENT_URI_TABLE_TRANSACTIONS,
            LedgerDefine.SET_DATA,
            docRef,
            transactionMap
        )

    }

    private fun updateTransactionAmountForUser(
        senderUser: Any?,
        receiverUser: Any?,
        project: Any?,
        debitAccount: Any?,
        creditAccount: Any?,
        payment: Any?
    ) {
        var senderID = senderUser.toString().substring(2)
        var receiverID = receiverUser.toString().substring(2)

        var senderPreFix = senderUser.toString().substring(0, 2)
        var receiverPreFix = receiverUser.toString().substring(0, 2)

        var senderMap = java.util.HashMap<String, Any>()
        var receiverMap = java.util.HashMap<String, Any>()

        var tempAmount = payment as Long
        var senderAmount: Long = tempAmount
        var receiverAmount: Long = tempAmount


        for (user in mSenderList!!) {
            if (senderID == user.userID) {
                if (senderPreFix == LedgerDefine.PREFIX_MASTER) {
                    senderAmount = user.m_Amount - senderAmount
                    senderMap[LedgerDefine.M_AMOUNT] = senderAmount
                } else {
                    senderAmount = user.p_Payment - senderAmount
                    senderMap[LedgerDefine.P_PAYMENT] = senderAmount
                }

                break
            }
        }

        for (user in mReceiverList!!) {
            if (receiverID == user.userID) {

                if (receiverPreFix == LedgerDefine.PREFIX_MASTER) {
                    receiverAmount += user.m_Amount
                    receiverMap[LedgerDefine.M_AMOUNT] = receiverAmount
                } else {
                    receiverAmount += user.p_Payment
                    receiverMap[LedgerDefine.P_PAYMENT] = receiverAmount
                }
                break
            }
        }

        Log.d(TAG, "sender AMount " + senderAmount)
        val companyName = LedgerSharePrefManger(mContext).getCompanyID()
        val db = FirebaseFirestore.getInstance()
        val batchUpdateUsers = db.batch()
        var docRefSender =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_USERS)
                .document(senderID)
        var docRefReceiver =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_USERS)
                .document(receiverID)


        batchUpdateUsers.update(docRefSender, senderMap)

        batchUpdateUsers.update(docRefReceiver, receiverMap)

        batchUpdateUsers.commit().addOnCompleteListener {
            // ...
            toast(R.string.user_payment_updated)
        }


        var debitAccountAmount: Long = tempAmount
        var debitID = debitAccount.toString()
        var debitMap = java.util.HashMap<String, Any>()
        val batchUpdateBankAccounts = db.batch()
        if (!isEmpty(debitID)) {
            for (details in mBankAccountList!!) {
                if (debitID == details.id) {
                    debitAccountAmount = details.amount - debitAccountAmount
                    debitMap[LedgerDefine.AMOUNT] = debitAccountAmount
                    var docRefBankAccount =
                        db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_BANK_ACCOUNTS)
                            .document(debitID)
                    batchUpdateBankAccounts.update(docRefBankAccount, debitMap)
                    break
                }

            }
        }

        var creditAccountAmount: Long = tempAmount
        var creditID = creditAccount.toString()
        var creditMap = java.util.HashMap<String, Any>()

        if (!isEmpty(creditID)) {
            for (details in mBankAccountList!!) {
                if (creditID == details.id) {
                    creditAccountAmount += details.amount
                    creditMap[LedgerDefine.AMOUNT] = creditAccountAmount
                    var docRefBankAccount =
                        db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_BANK_ACCOUNTS)
                            .document(creditID)
                    batchUpdateBankAccounts.update(docRefBankAccount, creditMap)
                    break
                }

            }
        }

        if (!isEmpty(creditID) || !isEmpty(debitID)) {
            batchUpdateBankAccounts.commit().addOnCompleteListener {
                // ...
                toast(R.string.bank_accounts_updated)
            }
        }
        if (project != null) {
            var projectID = project.toString()
            if (!isEmpty(projectID)) {
                for (details in mProjectList!!) {
                    if (projectID == details.projectID) {
                        var basicAmount = details.amount

                        if (receiverID == LedgerDefine.ADMIN_ID) {
                            basicAmount += payment
                        } else {
                            basicAmount -= payment
                        }
                        var docRefProject =
                            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_PROJECTS)
                                .document(projectID)

                        docRefProject.update(LedgerDefine.AMOUNT, basicAmount)
                            .addOnSuccessListener(OnSuccessListener<Void> {
                                toast(R.string.project_amount_updated)
                            })
                            .addOnFailureListener(OnFailureListener { e ->
                                Log.w(TAG, "Error writing document", e)
                                toast(R.string.error_09)
                            })

                        break
                    }

                }

            }
        }
    }

/*
    override fun onBackPressed() {
        val intent = Intent(this, GoogleSigninActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }*/

    private fun showSnackBar() {
        val snackbar = Snackbar
            .make(
                coordinatorLayout,
                R.string.saved_successfully,
                Snackbar.LENGTH_INDEFINITE
            )
            .setAction(R.string.add_more) {
                btn_save.isEnabled = true
                btn_save.setTextColor(Color.BLACK)
                btn_save.setText(R.string.save)
                et_amount.text.clear()
                et_round_off.text.clear()
                if (switch_suggestion.isChecked)
                    openDialog(LedgerDefine.SELECTION_TYPE_RECEIVER)
            }
        snackbar.setActionTextColor(Color.BLUE)
        val sbView = snackbar.view
        sbView.setBackgroundColor(Color.GREEN)
        val textView =
            sbView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.BLACK)
        snackbar.show()
    }

    private fun getDateFormatted(
        dd: String,
        mm: String,
        yyyy: String
    ): String? {
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

        // leap year check
        if (month == 2) {
            if (day > 29) {
                toast(R.string.date_is_wrong)
            } else if (day == 29) {
                if (year % 4 != 0) {
                    toast(R.string.date_is_wrong)
                }
            }
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
        var fm = supportFragmentManager
        var dFragment = DialogFragmentToSelectUserOrProject()
        var bundle = Bundle()
        bundle.putInt(LedgerDefine.KEY_SELECTION_TYPE, type)
        dFragment.arguments = bundle
        // Show DialogFragment
        dFragment.show(fm, " project select Dialog Fragment")
    }

    private fun getProjectList() {
        var db = FirestoreDataBase().db

        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_PROJECTS)
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

        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_USERS)
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
        /*val isAdmin = mSignInProfile!!.isAdmin
        val isSuperVisor = mSignInProfile!!.isSupervisor
        val isNormalUser = mSignInProfile!!.isNormal
        val accessedProjects = mSignInProfile!!.accesibleProjects*/


        for (document in task.result!!) {
            Log.d(TAG, document.id + " => " + document.data)
            // senderList
            val disable = document.get(LedgerDefine.IS_USER_DISABLE)
            if (disable != null) {
                if (disable as Boolean) continue
            }
            val tempName = document.get(LedgerDefine.NAME)
            var tempId = document.get(LedgerDefine.USER_ID)
            val tempDesignation = document.get(LedgerDefine.DESIGNATION)
            val tempIsAdmin = document.get(LedgerDefine.IS_ADMIN)
            val tempAccessedProjects = document.get(LedgerDefine.ACCESSIBLE_PROJECTS)
            val UserAccounts = document.get(LedgerDefine.ACCOUNTS)
            val userDetails = UserDetails()

            if (tempName != null) userDetails.name = tempName as String

            try {
                tempId = (tempId as Long).toString()
            } catch (e: ClassCastException) {
                tempId = tempId as String
            }

            if (tempId != null) userDetails.userID = tempId.toString()

            if (tempDesignation != null) userDetails.designation = tempDesignation as Long

            if (tempIsAdmin != null) {
                if (tempIsAdmin as Boolean) userDetails.designation =
                    LedgerDefine.DESIGNATION_ADMIN
            }

            if (tempAccessedProjects != null) {
                userDetails.accesibleProjectsList =
                    tempAccessedProjects as ArrayList<String>
            }

            if (UserAccounts != null) {
                userDetails.userAccounts = UserAccounts as ArrayList<String>
            }

            var p_payment = document.get(LedgerDefine.P_PAYMENT)
            if (p_payment != null) {
                userDetails.p_Payment = p_payment as Long
            }
            var m_amount = document.get(LedgerDefine.M_AMOUNT)
            if (m_amount != null) {
                userDetails.m_Amount = m_amount as Long
            }

            //if (isAdmin) {
            if (mEditType === LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY) {
                if (tempId == mReceiverIdToUpdate) {
                    mSelectedReceiver = userDetails

                    tv_receiver_id.text = userDetails.userID + "\n" + userDetails.name
                } else if (tempId == mSenderIdToUpdate) {
                    mSelectedSender = userDetails
                    tv_sender_id.text = userDetails.userID + "\n" + userDetails.name
                }
            }
            mReceiverList!!.add(userDetails)
            mSenderList!!.add(userDetails)

            /*} else if (isSuperVisor) {
                addToReceiverList(userDetails, accessedProjects!!)
            }*/
        }
        /*if (isSuperVisor) {
            val senderData = UserDetails()
            senderData.userID = mSignInProfile!!.userID
            senderData.name = mSignInProfile!!.name
            senderData.designation = LedgerDefine.DESIGNATION_SUPERVISOR
            mSenderList!!.add(senderData)
            tv_sender_id.text = senderData.userID + "\n" + senderData.name
            tv_sender_id.setTextColor(Color.BLUE)
            mSelectedSender = senderData
            for (id in mSignInProfile!!.accesibleProjects!!) {
                var projectDetails: ProjectDetails = ProjectDetails()
                projectDetails.name = ""
                projectDetails.projectID = id
                mProjectList!!.add(projectDetails)
            }
        }*/
        // ReceiverList
        return true
    }

/*private fun addToReceiverList(userDetail: UserDetails, projectAccess: ArrayList<String>) {

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
}*/

    var mSenderList: ArrayList<UserDetails>? = ArrayList<UserDetails>()
    var mReceiverList: ArrayList<UserDetails>? = ArrayList<UserDetails>()
    var mBankAccountList: ArrayList<BankAccountDetail>? = ArrayList<BankAccountDetail>()
    var mProjectList: ArrayList<ProjectDetails>? = ArrayList<ProjectDetails>()

    private fun hideKeyboard() {
        val imm =
            mContext!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = getCurrentFocus()
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(mContext);
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0);
    }

    override fun onComplete(status: Boolean) {
        if (status) {
            if (mEditType == LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY) {
                btn_save.setTextColor(Color.parseColor("#FF7C7B7B"))
                btn_save.setText(R.string.update_done)
                finish()

            } else {
                btn_save.setTextColor(Color.parseColor("#FF7C7B7B"))
                btn_save.setText(R.string.saved)
                showSnackBar()
            }

        } else {
            if (mEditType == LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY) {
                toast(R.string.error_09)
                btn_save.setTextColor(Color.RED)
                btn_save.setText(R.string.failed)
            } else {
                toast(R.string.error_09)
                btn_save.setTextColor(Color.RED)
                btn_save.setText(R.string.failed)
            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "resultCode == $resultCode")
        // if (resultCode == RESULT_OK) {
        mBankAccountList!!.clear()
        mReceiverList!!.clear()
        mSenderList!!.clear()
        getAccountList()
        getUsersList()
        // }
    }

    override fun onLongClick(p0: View?): Boolean {
        when (p0!!.id) {
            R.id.img_btn_suggestion_remarks -> et_remarks.setText("")
            R.id.img_btn_suggestion_subcategroy -> et_auto_complete_subcategory.setText("")
        }

        return true
    }

}
