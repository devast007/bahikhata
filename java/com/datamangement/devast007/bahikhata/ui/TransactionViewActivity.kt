package com.datamangement.devast007.bahikhata.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.adapter.TransactionViewAdapter
import com.datamangement.devast007.bahikhata.ui.fragment.DialogFragmentMoreInfo
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.TransactionDetails
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_transaction_view.*
import java.util.*


class TransactionViewActivity : AppCompatActivity(), View.OnClickListener {

    var mContext: Context? = null
    val TAG = "TransactionViewActivity"
    var mTransactionViewType = -1
    var mID: String? = null
    private var mUserDesignation: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_view)
        mContext = this
        supportActionBar!!.setTitle(R.string.transactions)
        val intent = intent
        mTransactionViewType = intent.getIntExtra(LedgerDefine.TRANSACTION_VIEW_TYPE, -1)
        mID = intent.getStringExtra(LedgerDefine.ID)
        mUserDesignation = intent.getLongExtra(LedgerDefine.DESIGNATION, -1)

        if (mTransactionViewType != -1 && mID != null) {
            supportActionBar!!.setSubtitle(mID)
        }

    }

    override fun onStart() {
        super.onStart()
        mTransactionList.clear()
        getTransactions(-1)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_transaction_view, menu)
        val received = menu!!.findItem(R.id.action_received)
        val sent = menu!!.findItem(R.id.action_send)
        if (received != null) {
            received.isVisible = mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_USER
        }
        if (sent != null) {
            sent.isVisible = mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_USER
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item!!.itemId) {
            R.id.action_add -> {
                startActivity(Intent(this, AddTransactionActivity::class.java));
                return true
            }
            R.id.action_received -> {
                mTransactionList.clear()
                setAdapter()
                getTransactions(RECEIVED)
                return true
            }
            R.id.action_send -> {
                mTransactionList.clear()
                setAdapter()
                getTransactions(SENT)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private var mTransactionAdapter: TransactionViewAdapter? = null

    private var mTransactionList: ArrayList<TransactionDetails> = ArrayList()

    private fun setAdapter() {
        if (mTransactionAdapter == null) {
            mTransactionAdapter = TransactionViewAdapter(this, mTransactionList)
        }

        expadable_list_view.setAdapter(mTransactionAdapter)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tv_sender_id,
            R.id.tv_receiver_id,
            R.id.tv_log_in_id -> viewUserInfo(view)

            R.id.tv_project_id -> viewProjectInfo(view)
            R.id.btn_verify -> btnVerifyClicked(view)
            R.id.btn_del -> btnDelClicked(view)

            R.id.tv_transaction_debited_to,
            R.id.tv_transaction_credited_to -> viewAccountDetail(view)
        }
    }

    private fun viewAccountDetail(view: View) {
        var accountID: String = view.getTag(R.string.tag_account_id).toString()
        if (isEmpty(accountID)) {
            return
        }

        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
            .whereEqualTo(LedgerDefine.BANK_ACCOUNT_ID, accountID)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        showBankAccountMoreInfoDialog(document)
                        break
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG)
                }
            })

    }

    private fun viewUserInfo(view: View) {
        var userID: String = view.getTag(R.string.tag_user_id).toString()
        if (isEmpty(userID)) {
            return
        }

        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/users").whereEqualTo(LedgerDefine.USER_ID, userID)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        Log.d(TAG, " document.get(\"name\")+ => " + document.get("name"))

                        showUserMoreInfoDialog(document)
                        break
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG)
                }
            })

    }

    private fun showBankAccountMoreInfoDialog(document: QueryDocumentSnapshot?) {
        var fm = supportFragmentManager
        var dFragment = DialogFragmentMoreInfo()
        var bundle = Bundle()
        bundle.putInt(LedgerDefine.MORE_INFO_TYPE, LedgerDefine.MORE_INFO_TYPE_ACCOUNT)

        bundle.putString(LedgerDefine.BANK_ACCOUNT_ID, document!!.get(LedgerDefine.BANK_ACCOUNT_ID).toString())
        bundle.putString(LedgerDefine.BANK_ACCOUNT_NUMBER, document!!.get(LedgerDefine.BANK_ACCOUNT_NUMBER).toString())
        var amount = document!!.get(LedgerDefine.AMOUNT)
        if (amount != null) {
            bundle.putLong(LedgerDefine.AMOUNT, amount as Long)
        }
        bundle.putString(LedgerDefine.TIME_STAMP, document!!.get(LedgerDefine.TIME_STAMP).toString())
        bundle.putString(LedgerDefine.IFSC_CODE, document!!.get(LedgerDefine.IFSC_CODE).toString())
        bundle.putString(LedgerDefine.PAYEE_NAME, document!!.get(LedgerDefine.PAYEE_NAME).toString())
        bundle.putString(
            LedgerDefine.BANK_ACCOUNT_BRANCH_NAME,
            document!!.get(LedgerDefine.BANK_ACCOUNT_BRANCH_NAME).toString()
        )
        bundle.putString(LedgerDefine.REMARK, document!!.get(LedgerDefine.REMARK).toString())

        dFragment.arguments = bundle
        // Show DialogFragment
        dFragment.show(fm, "Account More Info ")
    }

    private fun showUserMoreInfoDialog(document: QueryDocumentSnapshot?) {
        var fm = supportFragmentManager
        var dFragment = DialogFragmentMoreInfo()
        var bundle = Bundle()
        bundle.putInt(LedgerDefine.MORE_INFO_TYPE, LedgerDefine.MORE_INFO_TYPE_USER)

        bundle.putString(LedgerDefine.USER_ID, document!!.get(LedgerDefine.USER_ID).toString())
        bundle.putString(LedgerDefine.NAME, document!!.get(LedgerDefine.NAME).toString())
        var amount = document!!.get(LedgerDefine.AMOUNT)
        if (amount != null) {
            bundle.putLong(LedgerDefine.AMOUNT, amount as Long)
        }
        bundle.putString(LedgerDefine.ADDRESS, document!!.get(LedgerDefine.ADDRESS).toString())
        bundle.putString(LedgerDefine.TIME_STAMP, document!!.get(LedgerDefine.TIME_STAMP).toString())
        bundle.putString(LedgerDefine.PHONE_NUMBER, document!!.get(LedgerDefine.PHONE_NUMBER).toString())
        bundle.putString(LedgerDefine.EMAIL, document!!.get(LedgerDefine.EMAIL).toString())
        var designation = document!!.get(LedgerDefine.DESIGNATION)
        if (designation != null) {
            bundle.putLong(LedgerDefine.DESIGNATION, designation as Long)
        }
        bundle.putString(LedgerDefine.REMARK, document!!.get(LedgerDefine.REMARK).toString())

        dFragment.arguments = bundle
        // Show DialogFragment
        dFragment.show(fm, "User More Info ")
    }

    private fun viewProjectInfo(view: View) {
        var projectID: String = view.getTag(R.string.tag_project_id).toString()
        if (isEmpty(projectID)) {
            return
        }
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/projects")
            .whereEqualTo(LedgerDefine.PROJECT_ID, projectID)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        Log.d(TAG, " document.get(\"name\")+ => " + document.get("name"))

                        showProjectMoreInfoDialog(document)
                        break
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG)
                }
            })
    }

    private fun showProjectMoreInfoDialog(document: QueryDocumentSnapshot?) {
        var fm = supportFragmentManager
        var dFragment = DialogFragmentMoreInfo()
        var bundle = Bundle()
        bundle.putInt(LedgerDefine.MORE_INFO_TYPE, LedgerDefine.MORE_INFO_TYPE_PROJECT)

        bundle.putString(LedgerDefine.PROJECT_ID, document!!.get(LedgerDefine.PROJECT_ID).toString())
        bundle.putString(LedgerDefine.NAME, document!!.get(LedgerDefine.NAME).toString())

        var amount = document!!.get(LedgerDefine.AMOUNT)
        if (amount != null) bundle.putLong(LedgerDefine.AMOUNT, amount as Long)
        bundle.putString(LedgerDefine.ADDRESS, document!!.get(LedgerDefine.ADDRESS).toString())
        bundle.putString(LedgerDefine.DIVISION, document!!.get(LedgerDefine.DIVISION).toString())
        bundle.putString(LedgerDefine.START_DATE, document!!.get(LedgerDefine.START_DATE).toString())
        bundle.putString(LedgerDefine.END_DATE, document!!.get(LedgerDefine.END_DATE).toString())
        bundle.putString(LedgerDefine.REMARK, document!!.get(LedgerDefine.REMARK).toString())

        dFragment.arguments = bundle
        // Show DialogFragment
        dFragment.show(fm, "Project More Info ")
    }

    private fun btnDelClicked(view: View) {
        var tid: String = view.getTag(R.string.tag_transaction_id).toString()
        if (TextUtils.isEmpty(tid)) {
            return
        }
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        Log.d(TAG, "companyID => " + companyID)

        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions").document(tid)
            .delete()
            .addOnSuccessListener(OnSuccessListener<Void> {
                Log.d(
                    TAG,
                    "DocumentSnapshot successfully deleted!"
                )
                toast(R.string.transaction_deleted)
            })
            .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error deleting document", e) })
    }

    private fun btnVerifyClicked(view: View) {
        var tid: String = view.getTag(R.string.tag_transaction_id).toString()
        if (TextUtils.isEmpty(tid)) {
            return
        }
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()

        val docRef = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions").document(tid)

        docRef
            .update(LedgerDefine.VERIFIED, true)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully updated!")
                toast(R.string.transaction_verified)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
                toast(R.string.error_07)
            }

    }

    private fun isEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }

    private fun launchTransactionviewActivity(view: View) {
        val projectId = view.getTag(R.string.tag_project_id)
        Toast.makeText(this, "Not Implement yet !! " + projectId, Toast.LENGTH_LONG).show()
    }

    private fun getTransactions(conditionForUser: Int): Boolean {

        getCollection(conditionForUser)!!.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "task.isSuccessful")
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        setSetTransaction(document);
                    }
                    setAdapter()
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
        return true;
    }

    private val SENT: Int = 1
    private val RECEIVED: Int = 2

    private fun getCollection(conditionForUser: Int): Query? {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        Log.d(TAG, "companyID => " + companyID)

        if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_PROJECT) {
            return db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions").whereEqualTo(
                LedgerDefine.PROJECT_ID,
                mID
            ).orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)

        } else if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_USER) {
            if (conditionForUser == SENT) {
                return db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions").whereEqualTo(
                    LedgerDefine.SENDER_ID,
                    mID
                ).orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
            } else if (conditionForUser == RECEIVED) {
                return db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions").whereEqualTo(
                    LedgerDefine.RECEIVER_ID,
                    mID
                ).orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
            } else {
                if (mUserDesignation == LedgerDefine.DESIGNATION_NORMAL) {
                    return db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions").whereEqualTo(
                        LedgerDefine.RECEIVER_ID,
                        mID
                    ).orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
                } else {
                    return db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions").whereEqualTo(
                        LedgerDefine.SENDER_ID,
                        mID
                    ).orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
                }

            }
        } else if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_ALL) {
            return db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions")
                .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
        }
        return null
    }

    private fun toast(id: Int) {
        Toast.makeText(mContext, id, Toast.LENGTH_LONG).show()
    }

    private fun setSetTransaction(document: QueryDocumentSnapshot?) {
        if (document != null) {
            var transactionDetail: TransactionDetails = TransactionDetails()
            transactionDetail.senderId = document.get(LedgerDefine.SENDER_ID) as String
            transactionDetail.receiverId = document.get(LedgerDefine.RECEIVER_ID) as String
            transactionDetail.amount = document.get(LedgerDefine.AMOUNT) as Long
            var projectID = document.get(LedgerDefine.PROJECT_ID)
            if (projectID != null) transactionDetail.projectId = projectID as String
            transactionDetail.transactionDate = document.get(LedgerDefine.TRANSACTION_DATE) as String
            transactionDetail.timeStamp = document.get(LedgerDefine.TIME_STAMP) as Date
            transactionDetail.transactionID = document.get(LedgerDefine.TRANSACTION_ID) as String
            transactionDetail.transactionType = document.get(LedgerDefine.TRANSACTION_TYPE) as Long
            transactionDetail.loggedInID = document.get(LedgerDefine.LOGGED_IN_ID) as String
            transactionDetail.verified = document.get(LedgerDefine.VERIFIED) as Boolean

            var remark = document.get(LedgerDefine.REMARK)
            if (remark != null) transactionDetail.remarks = remark as String

            var debitAccount = document.get(LedgerDefine.DEBIT_ACCOUNT_ID)
            if (debitAccount != null) transactionDetail.debitedTo = debitAccount.toString()

            var creditAccount = document.get(LedgerDefine.CREDIT_ACCOUNT_ID)
            if (creditAccount != null) transactionDetail.creditedTo = creditAccount.toString()

            var paymentMode = document.get(LedgerDefine.PAYMENT_MODE)
            if (paymentMode != null) transactionDetail.paymentMode = paymentMode.toString()

            mTransactionList.add(transactionDetail)
        }
    }
}






