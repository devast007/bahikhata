package com.datamangement.devast007.bahikhata.ui

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.excel.TransactionsExcelSheet
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.adapter.TransactionViewAdapter
import com.datamangement.devast007.bahikhata.ui.fragment.DialogFragmentMoreInfo
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.TransactionDetails
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import kotlinx.android.synthetic.main.activity_transaction_view.*
import java.util.*
import kotlin.collections.ArrayList


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
        getTransactions(-1)
    }


    private fun dataAdded(dc: DocumentChange?) {
        if (mTransactionAdapter == null) return
        val document = dc!!.document
        val transactionID = document.get(LedgerDefine.TRANSACTION_ID).toString()

        for (details in mTransactionList) {
            if (details.transactionID === transactionID) return
        }
        var transactionDetail: TransactionDetails = TransactionDetails()

        transactionDetail.senderId = document.get(LedgerDefine.SENDER_ID) as String
        transactionDetail.receiverId = document.get(LedgerDefine.RECEIVER_ID) as String
        transactionDetail.amount = document.get(LedgerDefine.AMOUNT) as Long
        var projectID = document.get(LedgerDefine.PROJECT_ID)
        if (projectID != null) transactionDetail.projectId = projectID as String
        transactionDetail.transactionDate = document.get(LedgerDefine.TRANSACTION_DATE) as String
        var timestamp = document.get(LedgerDefine.TIME_STAMP)
        if (timestamp != null) transactionDetail.timeStamp = timestamp as Date

        transactionDetail.transactionID = transactionID as String
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
        mTransactionList.add(0, transactionDetail)
        setAdapter()
    }

    override fun onStart() {
        super.onStart()

    }

    fun isStoragePermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true
            } else {

                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                return false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true
        }
    }

    var mDialog: ProgressDialog? = null
    fun createExcelSheet() {

        if (!isStoragePermissionGranted()) return
        mDialog = ProgressDialog.show(
            mContext,
            getString(R.string.creating_file),
            getString(R.string.please_wait),
            false
        )
        if (isEmpty(mID)) {
            mID = "ALL"
        }
        val filepath = TransactionsExcelSheet(mContext, mID, mTransactionList).getFilePath()
        mDialog!!.dismiss()
        val dataType = "application/vnd.ms-excel"
        if (filepath != null) {
            try {
                var builder = AlertDialog.Builder(mContext);
                builder.setMessage("File Is Downloaded...")
                    .setPositiveButton("OPEN", DialogInterface.OnClickListener { dialogInterface, i ->
                        val newIntent = Intent(Intent.ACTION_VIEW)
                        newIntent.setDataAndType(Uri.parse("file://$filepath"), dataType)
                        newIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                        mContext!!.startActivity(newIntent)
                    })
                    .setNegativeButton("SHARE", DialogInterface.OnClickListener { dialogInterface, i ->
                        var intentShareFile = Intent(Intent.ACTION_SEND);

                        intentShareFile.setType(dataType);
                        intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$filepath"));

                        intentShareFile.putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Sharing File..."
                        );
                        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

                        startActivity(Intent.createChooser(intentShareFile, "Share File"));
                    }).show()
            } catch (e: Exception) {
                toast(R.string.file_not_found)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_transaction_view, menu)
        val received = menu!!.findItem(R.id.action_received)
        val sent = menu!!.findItem(R.id.action_send)
        val excel = menu!!.findItem(R.id.action_excel)

        if (received != null) {
            received.isVisible = mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_USER
        }
        if (sent != null) {
            sent.isVisible = mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_USER
        }

        if (excel != null) {
            excel.isVisible = LedgerUtils.signInProfile!!.isAdmin
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
            R.id.action_excel -> createExcelSheet()
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
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/users")
            .whereEqualTo(LedgerDefine.USER_ID, userID)
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
        bundle.putString(
            LedgerDefine.BANK_ACCOUNT_NUMBER,
            document!!.get(LedgerDefine.BANK_ACCOUNT_NUMBER).toString()
        )
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

        var projects = document!!.get(LedgerDefine.ACCESSIBLE_PROJECTS)
        if (projects != null) {
            bundle.putString(LedgerDefine.ACCESSIBLE_PROJECTS, (projects as ArrayList<String>).toString())
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

    override fun onDestroy() {
        super.onDestroy()
        if (mRegistration != null) {
            mRegistration!!.remove()
        }
    }

    private var mRegistration: ListenerRegistration? = null

    private fun getTransactions(conditionForUser: Int): Boolean {

        var query: Query = getCollection(conditionForUser)!!
        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "task.isSuccessful")
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        setSetTransaction(document)
                    }
                    setAdapter()
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })

        if (mRegistration != null) {
            mRegistration!!.remove()
        }
        mRegistration = query
            .addSnapshotListener(EventListener<QuerySnapshot>() { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException != null) {
                    Log.w(TAG, "listen:error", firebaseFirestoreException);
                    return@EventListener;
                }

                for (dc in querySnapshot!!.documentChanges) {
                    when (dc.getType()) {
                        DocumentChange.Type.ADDED -> dataAdded(dc)
                        DocumentChange.Type.MODIFIED -> dataModified(dc)
                        DocumentChange.Type.REMOVED -> dataDeleted(dc)
                    }
                }

            });
        return true;
    }

    private fun dataDeleted(dc: DocumentChange?) {

        val document = dc!!.document
        if (document != null) {
            val transactionID = document.get(LedgerDefine.TRANSACTION_ID).toString()

            for (details in mTransactionList) {
                if (details.transactionID === transactionID) {
                    mTransactionList.remove(details)
                    break
                }
            }
            setAdapter()
        }

    }

    private fun dataModified(dc: DocumentChange?) {

        var isIDFound = false
        var transactionDetail: TransactionDetails? = null
        val document = dc!!.document
        val transactionID = document.get(LedgerDefine.TRANSACTION_ID).toString()
        for (detail in mTransactionList) {
            if (detail.transactionID == transactionID) {
                isIDFound = true
                transactionDetail = detail
                break
            }
        }
        if (!isIDFound) return



        transactionDetail!!.senderId = document.get(LedgerDefine.SENDER_ID) as String
        transactionDetail!!.receiverId = document.get(LedgerDefine.RECEIVER_ID) as String
        transactionDetail!!.amount = document.get(LedgerDefine.AMOUNT) as Long
        var projectID = document.get(LedgerDefine.PROJECT_ID)
        if (projectID != null) transactionDetail.projectId = projectID as String
        transactionDetail!!.transactionDate = document.get(LedgerDefine.TRANSACTION_DATE) as String
        transactionDetail!!.timeStamp = document.get(LedgerDefine.TIME_STAMP) as Date
        transactionDetail!!.transactionID = transactionID as String
        transactionDetail!!.transactionType = document.get(LedgerDefine.TRANSACTION_TYPE) as Long
        transactionDetail!!.loggedInID = document.get(LedgerDefine.LOGGED_IN_ID) as String
        transactionDetail!!.verified = document.get(LedgerDefine.VERIFIED) as Boolean

        var remark = document.get(LedgerDefine.REMARK)
        if (remark != null) transactionDetail!!.remarks = remark as String

        var debitAccount = document.get(LedgerDefine.DEBIT_ACCOUNT_ID)
        if (debitAccount != null) transactionDetail!!.debitedTo = debitAccount.toString()

        var creditAccount = document.get(LedgerDefine.CREDIT_ACCOUNT_ID)
        if (creditAccount != null) transactionDetail!!.creditedTo = creditAccount.toString()

        var paymentMode = document.get(LedgerDefine.PAYMENT_MODE)
        if (paymentMode != null) transactionDetail!!.paymentMode = paymentMode.toString()

        mTransactionAdapter!!.notifyDataSetChanged()

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
            ).orderBy(LedgerDefine.VERIFIED, Query.Direction.ASCENDING)
                .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)

        } else if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_USER) {
            if (conditionForUser == SENT) {
                return db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions").whereEqualTo(
                    LedgerDefine.SENDER_ID,
                    mID
                ).orderBy(LedgerDefine.VERIFIED, Query.Direction.ASCENDING)
                    .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
            } else if (conditionForUser == RECEIVED) {
                return db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions").whereEqualTo(
                    LedgerDefine.RECEIVER_ID,
                    mID
                ).orderBy(LedgerDefine.VERIFIED, Query.Direction.ASCENDING)
                    .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
            } else {
                if (mUserDesignation == LedgerDefine.DESIGNATION_NORMAL) {
                    return db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions").whereEqualTo(
                        LedgerDefine.RECEIVER_ID,
                        mID
                    ).orderBy(LedgerDefine.VERIFIED, Query.Direction.ASCENDING)
                        .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
                } else {
                    return db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions").whereEqualTo(
                        LedgerDefine.SENDER_ID,
                        mID
                    ).orderBy(LedgerDefine.VERIFIED, Query.Direction.ASCENDING)
                        .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
                }

            }
        } else if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_ALL) {
            return db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions")
                .orderBy(LedgerDefine.VERIFIED, Query.Direction.ASCENDING)
                .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
        }
        return null
    }

    private fun toast(id: Int) {
        Toast.makeText(mContext, id, Toast.LENGTH_LONG).show()
    }

    private fun setSetTransaction(document: QueryDocumentSnapshot?) {
        if (document != null) {
            val transactionID = document.get(LedgerDefine.TRANSACTION_ID).toString()

            for (details in mTransactionList) {
                if (details.transactionID === transactionID) return
            }
            var transactionDetail: TransactionDetails = TransactionDetails()
            transactionDetail.senderId = document.get(LedgerDefine.SENDER_ID) as String
            transactionDetail.receiverId = document.get(LedgerDefine.RECEIVER_ID) as String
            transactionDetail.amount = document.get(LedgerDefine.AMOUNT) as Long
            var projectID = document.get(LedgerDefine.PROJECT_ID)
            if (projectID != null) transactionDetail.projectId = projectID as String
            transactionDetail.transactionDate = document.get(LedgerDefine.TRANSACTION_DATE) as String
            transactionDetail.timeStamp = document.get(LedgerDefine.TIME_STAMP) as Date
            transactionDetail.transactionID = transactionID
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







