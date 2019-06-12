package com.datamangement.devast007.bahikhata.ui

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
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
import kotlin.collections.HashMap


class TransactionViewActivity : AppCompatActivity(), View.OnClickListener {

    var mContext: Context? = null
    val TAG = "TransactionViewActivity"
    var mTransactionViewType = -1
    var mID: String? = null
    private var mUserDesignation: Long = -1
    var mBankAccountsMap: HashMap<String, String> = HashMap<String, String>()
    var mUsersMap: HashMap<String, String> = HashMap<String, String>()

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
            supportActionBar!!.subtitle = mID
        }
        getAccounts()
        getUsers()
        getTransactions(-1)
    }


    private fun dataAdded(dc: DocumentChange?) {
        if (mTransactionAdapter == null) return
        val document = dc!!.document
        val transactionID = document.get(LedgerDefine.TRANSACTION_ID).toString()
        for (details in mTransactionList) {
            if (details.transactionID == transactionID) return
        }
        Log.d(TAG, " dataAdded adding new item transactionID =  $transactionID")
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

    private fun isStoragePermissionGranted(): Boolean {
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
    private fun createExcelSheet() {

        if (!isStoragePermissionGranted()) return
        mDialog = ProgressDialog.show(
            mContext,
            getString(R.string.creating_file),
            getString(R.string.please_wait),
            false
        )

        // starts
        mIsNoLimit = true
        var query: Query = getCollection(mConditionForUser)!!
        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "task.isSuccessful")
                    var queryDocs = task.result!!
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        Log.d(TAG, doc.id + " => " + doc.data)
                        setSetTransaction(doc)
                    }
                    registerLocalBroadcastReceiver()
                    // test-1 starts
                    if (isEmpty(mID)) {
                        mID = "ALL"
                    }
                    TransactionsExcelSheet(mContext, mID, mTransactionList).writeToSheet()

                    // test-1 ends
                    if (expandable_list_view.count <= 0) {
                        setAdapter()
                    } else {
                        Log.d(TAG, "mTransactionAdapter: notifyDataSetChanged  " + mTransactionAdapter)
                        if (mTransactionAdapter != null) {
                            mTransactionAdapter!!.notifyDataSetChanged()
                            expandable_list_view.requestLayout()
                        }
                    }

                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })

        // ends

    }

    private fun registerLocalBroadcastReceiver() {
        LocalBroadcastManager.getInstance(mContext!!)
            .registerReceiver(mReceiver, IntentFilter(LedgerDefine.LOCAL_BROADCAST_INTENT))
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == LedgerDefine.LOCAL_BROADCAST_INTENT) {
                mDialog!!.dismiss()
                val filePath = intent!!.getStringExtra(LedgerDefine.INTENT_EXTRA_FILE_PATH)
                val dataType = "application/vnd.ms-excel"
                try {
                    var builder = AlertDialog.Builder(mContext);
                    builder.setMessage("File Is Downloaded...")
                        .setPositiveButton("OPEN", DialogInterface.OnClickListener { dialogInterface, i ->
                            val newIntent = Intent(Intent.ACTION_VIEW)
                            newIntent.setDataAndType(Uri.parse("file://$filePath"), dataType)
                            newIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                            mContext!!.startActivity(newIntent)
                        })
                        .setNegativeButton("SHARE", DialogInterface.OnClickListener { dialogInterface, i ->
                            var intentShareFile = Intent(Intent.ACTION_SEND);

                            intentShareFile.setType(dataType);
                            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$filePath"));

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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_transaction_view, menu)
        val received = menu!!.findItem(R.id.action_received)
        val sent = menu!!.findItem(R.id.action_send)
        val excel = menu!!.findItem(R.id.action_excel)

        if (received != null) {
            if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_USER || mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_BANK_ACCOUNT)
                received.isVisible = true
        }
        if (sent != null) {
            if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_USER || mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_BANK_ACCOUNT)
                sent.isVisible = true
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
                mLastDoc = null
                mIsNoMoreDocs = false
                setAdapter()
                getTransactions(RECEIVED)
                return true
            }
            R.id.action_send -> {
                mTransactionList.clear()
                setAdapter()
                mLastDoc = null
                mIsNoMoreDocs = false
                getTransactions(SENT)
                return true
            }
            R.id.action_excel -> createExcelSheet()
        }

        return super.onOptionsItemSelected(item)
    }


    private var mTransactionAdapter: TransactionViewAdapter? = null

    private var mTransactionList: ArrayList<TransactionDetails> = ArrayList()

    var mScrollListener: OnScrollListener = object : OnScrollListener {

        override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {


            if (scrollState === SCROLL_STATE_IDLE) {
                val count = expandable_list_view.count
                if (expandable_list_view.lastVisiblePosition >= count - 1) {
                    getTransactions(mConditionForUser)
                }
            }

        }

        override fun onScroll(
            view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int,
            totalItemCount: Int
        ) {
        }

    }

    private fun setAdapter() {
        if (mTransactionAdapter == null) {
            mTransactionAdapter = TransactionViewAdapter(this, mTransactionList)
        }
        expandable_list_view.setAdapter(mTransactionAdapter)
        expandable_list_view.setOnScrollListener(mScrollListener)
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
            R.id.tv_edit_transaction -> editTransaction(view);
        }
    }

    private fun editTransaction(view: View) {
        val transactionID = view.getTag(R.string.tag_transaction_id).toString()
        for (list in mTransactionList) {
            if (transactionID == list.transactionID) {
                var intent = Intent(mContext, AddTransactionActivity::class.java)
                intent.putExtra(LedgerDefine.TRANSACTION_EDIT_TYPE, LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY)
                intent.putExtra(LedgerDefine.TRANSACTION_ID, list.transactionID)
                intent.putExtra(LedgerDefine.PROJECT_ID, list.projectId)
                intent.putExtra(LedgerDefine.AMOUNT, "" + list.amount)
                intent.putExtra(LedgerDefine.SENDER_ID, list.senderId)
                intent.putExtra(LedgerDefine.RECEIVER_ID, list.receiverId)
                intent.putExtra(LedgerDefine.DEBIT_ACCOUNT_ID, list.debitedTo)
                intent.putExtra(LedgerDefine.CREDIT_ACCOUNT_ID, list.creditedTo)
                intent.putExtra(LedgerDefine.PAYMENT_MODE, list.paymentMode)
                intent.putExtra(LedgerDefine.REMARK, list.remarks)
                intent.putExtra(LedgerDefine.TRANSACTION_DATE, list.transactionDate)
                startActivity(intent)
                break
            }
        }

    }

    private fun viewAccountDetail(view: View) {
        var accountID: String = view.getTag(R.string.tag_account_id).toString()
        if (isEmpty(accountID)) {
            return
        }
        showSnackBar(accountID)
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
        showSnackBar(userID)
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

    private fun showSnackBar(text: String) {
        Snackbar
            .make(coordinatorLayout, "Getting Info : $text", Snackbar.LENGTH_SHORT).show()
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
        showSnackBar(projectID)
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_PROJECTS)
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
        bundle.putString(LedgerDefine.REMARK, getStringFormDoc(document, LedgerDefine.REMARK))

        bundle.putString(LedgerDefine.MB_NO, getStringFormDoc(document, LedgerDefine.MB_NO))
        bundle.putString(LedgerDefine.HEAD, getStringFormDoc(document, LedgerDefine.HEAD))
        bundle.putString(LedgerDefine.MAIN_AMOUNT, getStringFormDoc(document, LedgerDefine.MAIN_AMOUNT))
        bundle.putString(
            LedgerDefine.MAINTENANCE_1ST_YEAR_AMOUNT,
            getStringFormDoc(document, LedgerDefine.MAINTENANCE_1ST_YEAR_AMOUNT)
        )
        bundle.putString(
            LedgerDefine.MAINTENANCE_2ND_YEAR_AMOUNT,
            getStringFormDoc(document, LedgerDefine.MAINTENANCE_2ND_YEAR_AMOUNT)
        )
        bundle.putString(
            LedgerDefine.MAINTENANCE_3RD_YEAR_AMOUNT,
            getStringFormDoc(document, LedgerDefine.MAINTENANCE_3RD_YEAR_AMOUNT)
        )
        bundle.putString(
            LedgerDefine.MAINTENANCE_4TH_YEAR_AMOUNT,
            getStringFormDoc(document, LedgerDefine.MAINTENANCE_4TH_YEAR_AMOUNT)
        )
        bundle.putString(
            LedgerDefine.MAINTENANCE_5TH_YEAR_AMOUNT,
            getStringFormDoc(document, LedgerDefine.MAINTENANCE_5TH_YEAR_AMOUNT)
        )

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

    private fun launchTransactionViewActivity(view: View) {
        val projectId = view.getTag(R.string.tag_project_id)
        Toast.makeText(this, "Not Implement yet !! " + projectId, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mRegistration != null) {
            mRegistration!!.remove()
        }
        try {
            LocalBroadcastManager.getInstance(mContext!!)
                .unregisterReceiver(mReceiver)
        } catch (e: java.lang.Exception) {

        }


    }

    private var mRegistration: ListenerRegistration? = null

    private var mIsNoMoreDocs: Boolean = false

    private fun getTransactions(conditionForUser: Int) {
        Log.d(TAG, "task.isSuccessful mIsNoMoreDocs = " + mIsNoMoreDocs)
        if (mIsNoMoreDocs) {
            toast(R.string.loading_finished)
            return
        } else {
            if (mLastDoc != null) {
                toast(R.string.loading_data)
            }
        }
        var query: Query = getCollection(conditionForUser)!!
        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "task.isSuccessful")
                    var queryDocs = task.result!!
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        Log.d(TAG, doc.id + " => " + doc.data)
                        setSetTransaction(doc)
                    }
                    if (queryDocs.isEmpty) {
                        mIsNoMoreDocs = true
                    } else {
                        val size = queryDocs.size()
                        mLastDoc = queryDocs.documents[size - 1]
                    }
                    if (expandable_list_view.count <= 0) {
                        setAdapter()
                    } else {
                        Log.d(TAG, "mTransactionAdapter: notifyDataSetChanged  " + mTransactionAdapter)
                        if (mTransactionAdapter != null) {
                            mTransactionAdapter!!.notifyDataSetChanged()
                            expandable_list_view.requestLayout()
                        }
                    }
                    //setDataSetChangeListener(query)
                    // this is for skip updating duplicate amount for project or users after verification is done.
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })

    }


    private fun setDataSetChangeListener(query: Query) {
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
                    Log.d(TAG, " dc.type =  " + dc.type)
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> dataAdded(dc)
                        DocumentChange.Type.MODIFIED -> dataModified(dc)
                        DocumentChange.Type.REMOVED -> dataDeleted(dc)
                    }
                }

            });
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
        var timestamp = document.get(LedgerDefine.TIME_STAMP)
        if (timestamp != null) transactionDetail!!.timeStamp = timestamp as Date
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

    private var mLastDoc: DocumentSnapshot? = null

    private var mConditionForUser: Int = -1

    private var LIMIT: Long = 100

    private var mIsNoLimit: Boolean = false

    private fun getCollection(conditionForUser: Int): Query? {
        mConditionForUser = conditionForUser
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        Log.d(TAG, "companyID => $companyID mLastDoc = $mLastDoc")
        var query: Query = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/transactions")

        if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_BANK_ACCOUNT) {


            if (conditionForUser == SENT) {
                query = query.whereEqualTo(
                    LedgerDefine.DEBIT_ACCOUNT_ID,
                    mID
                )
            } else {
                query = query.whereEqualTo(
                    LedgerDefine.CREDIT_ACCOUNT_ID,
                    mID
                )
            }
        } else if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_PROJECT) {
            query = query.whereEqualTo(
                LedgerDefine.PROJECT_ID,
                mID
            )
        } else if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_USER) {
            if (conditionForUser == SENT) {
                query = query.whereEqualTo(
                    LedgerDefine.SENDER_ID,
                    mID
                )
            } else if (conditionForUser == RECEIVED) {
                query = query.whereEqualTo(
                    LedgerDefine.RECEIVER_ID,
                    mID
                )
            } else {
                if (mUserDesignation == LedgerDefine.DESIGNATION_NORMAL) {
                    query = query.whereEqualTo(
                        LedgerDefine.RECEIVER_ID,
                        mID
                    )
                } else {
                    query = query.whereEqualTo(
                        LedgerDefine.SENDER_ID,
                        mID
                    )
                }

            }
        } else if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_ALL) {
            //
        }
        query = query.orderBy(LedgerDefine.VERIFIED, Query.Direction.ASCENDING)
            .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
        if (mIsNoLimit) {
            mIsNoLimit = false
            // SKIP CONDITION
        } else {
            if (mLastDoc != null) {
                query = query.startAfter(mLastDoc!!).limit(LIMIT)
            } else {
                query = query.limit(LIMIT)
            }
        }
        return query
    }

    private fun getAccounts() {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        mBankAccountsMap.put(document.id.toString(), document.get(LedgerDefine.PAYEE_NAME).toString())
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getUsers() {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/users")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        mUsersMap.put(document.id.toString(), document.get(LedgerDefine.NAME).toString())
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun toast(id: Int) {
        Toast.makeText(mContext, id, Toast.LENGTH_LONG).show()
    }

    private fun setSetTransaction(document: QueryDocumentSnapshot?) {
        if (document != null) {
            val transactionID = document.get(LedgerDefine.TRANSACTION_ID).toString()

            for (details in mTransactionList) {
                if (details.transactionID == transactionID) return
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

    private fun getStringFormDoc(document: QueryDocumentSnapshot?, key: String): String {
        var data: Any? = document!!.get(key) ?: return ""

        return data as String

    }
}







