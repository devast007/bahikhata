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
import android.widget.*
import android.widget.AbsListView.OnScrollListener
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.excel.TransactionsExcelSheet
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.adapter.TransactionViewAdapter
import com.datamangement.devast007.bahikhata.ui.fragment.DialogFragmentMoreInfo
import com.datamangement.devast007.bahikhata.ui.fragment.DialogFragmentPDFColumnSelection
import com.datamangement.devast007.bahikhata.ui.fragment.DialogFragmentTransactionFilter
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.TransactionDetails
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_transaction_view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class TransactionViewActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener,
    AdapterView.OnItemLongClickListener {

    private var isFilterOn: Boolean = false
    private var mTransactionAdapter: TransactionViewAdapter? = null

    var mTransactionList: ArrayList<TransactionDetails> = ArrayList()
    var mMasterTransactionList: ArrayList<TransactionDetails> = ArrayList()
    private var mMenuPdf: MenuItem? = null
    var mPdfheading: String = "TRANSACTION"
    var mSwitchProjectOrSender: Int = 0
    private var mMenuSwitch: MenuItem? = null
    private var mdragDownAdd: MenuItem? = null
    public var mLongClickedForAddition: Boolean = false
    private var mCancelAddition: MenuItem? = null
    private var mAddTransaction: MenuItem? = null
    private var mExcel: MenuItem? = null
    private var mFilterMenu: MenuItem? = null
    private var mSelectedCount: Int = 0
    private var mAddition: Long = 0
    private var mSentMenuItem: MenuItem? = null
    private var mReceivedMenuItem: MenuItem? = null
    var mContext: Context? = null
    val TAG = "TransactionViewActivity"
    var mTransactionViewType = -1
    var mID: String? = null
    private var mUserDesignation: Long = -1
    var mBankAccountsMap: HashMap<String, String> = HashMap<String, String>()
    var mUsersMap: HashMap<String, String> = HashMap<String, String>()
    var mProjectsMap: HashMap<String, String> = HashMap<String, String>()
    var mFilterListSender: ArrayList<String> = ArrayList<String>()
    var mFilterListReceiver: ArrayList<String> = ArrayList<String>()
    var mFilterListWork: ArrayList<String> = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_view)
        mContext = this
        supportActionBar!!.setTitle(R.string.transactions)
        val intent = intent
        mTransactionViewType = intent.getIntExtra(LedgerDefine.TRANSACTION_VIEW_TYPE, -1)
        mID = intent.getStringExtra(LedgerDefine.ID)
        mUserDesignation = intent.getLongExtra(LedgerDefine.DESIGNATION, -1)

        getAccounts()
        getUsers()
        getProjects()

        var condition = -1
        if (mTransactionViewType != -1 && mID != null) {
            supportActionBar!!.subtitle = mID
            condition = if (mTransactionViewType === LedgerDefine.TRANSACTION_VIEW_TYPE_USER) {
                var preFix = mID!!.substring(0, 2)
                if (preFix == LedgerDefine.PREFIX_PERSONAL) {
                    RECEIVED
                } else {
                    SENT
                }
            } else {
                RECEIVED
            }
        }
        getTransactions(condition)


    }

    private fun getProjects() {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_PROJECTS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val id = document.id.toString()
                        mProjectsMap[id] =
                            document.get(LedgerDefine.NICKNAME).toString()
                        if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_PROJECT && id == mID) {
                            supportActionBar!!.subtitle =
                                document.get(LedgerDefine.NICKNAME).toString()
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }


    override fun onStart() {
        super.onStart()

    }

    private fun isStoragePermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true
            } else {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
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
                    var queryDocs = task.result!!
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        setSetTransaction(doc)
                    }
                    registerLocalBroadcastReceiver()
                    // test-1 starts
                    if (isEmpty(mID)) {
                        mID = "ALL"
                    }
                    TransactionsExcelSheet(mContext, mID, mMasterTransactionList).writeToSheet()

                    // test-1 ends
                    if (expandable_list_view.count <= 0) {
                        setAdapter()
                    } else {
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
                        .setPositiveButton(
                            "OPEN",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                val newIntent = Intent(Intent.ACTION_VIEW)
                                newIntent.setDataAndType(Uri.parse("file://$filePath"), dataType)
                                newIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                                mContext!!.startActivity(newIntent)
                            })
                        .setNegativeButton(
                            "SHARE",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                var intentShareFile = Intent(Intent.ACTION_SEND);

                                intentShareFile.setType(dataType);
                                intentShareFile.putExtra(
                                    Intent.EXTRA_STREAM,
                                    Uri.parse("file://$filePath")
                                );

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
        mReceivedMenuItem = menu!!.findItem(R.id.action_received)
        mSentMenuItem = menu!!.findItem(R.id.action_send)
        mExcel = menu!!.findItem(R.id.action_excel)
        mFilterMenu = menu!!.findItem(R.id.action_filter)
        mAddTransaction = menu!!.findItem(R.id.action_add)
        mCancelAddition = menu!!.findItem(R.id.action_cancel_addition)
        mdragDownAdd = menu!!.findItem(R.id.action_drag_down_for_addition)
        mMenuSwitch = menu!!.findItem(R.id.action_switch)
        mMenuPdf = menu!!.findItem(R.id.action_pdf)


        if (mReceivedMenuItem != null) {
            if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_USER || mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_BANK_ACCOUNT)
                mReceivedMenuItem!!.isVisible = true
        }
        if (mSentMenuItem != null) {
            if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_USER || mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_BANK_ACCOUNT)
                mSentMenuItem!!.isVisible = true
        }

        //if (excel != null) {
        //  excel.isVisible = LedgerUtils.signInProfile!!.isAdmin
        //}

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item!!.itemId) {
            R.id.action_add -> {
                startActivity(Intent(this, AddTransactionActivity::class.java));
                return true
            }
            R.id.action_received -> {
                resetFields()

                getTransactions(RECEIVED)
                //item.setIcon(R.mipmap.icon_incoming_disabled)
                //mSentMenuItem!!.setIcon(R.mipmap.icon_outgoing_red_arrow)
                return true
            }
            R.id.action_send -> {
                resetFields()
                getTransactions(SENT)
               // item.setIcon(R.mipmap.icon_outgoing_disabled)
               // mReceivedMenuItem!!.setIcon(R.mipmap.icon_incoming_green_arrow)
                return true
            }
            R.id.action_excel -> createExcelSheet()
            R.id.action_cancel_addition -> setNormalMenu(true)
            R.id.action_drag_down_for_addition -> dragDownAddition()
            R.id.action_switch -> switchBtnClicked()
            R.id.action_pdf -> createPdfDialog()
            R.id.action_pdf -> createPdfDialog()
            R.id.action_filter -> filterDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun resetFields() {
        mFilterListReceiver.clear()
        mFilterListSender.clear()
        mFilterListWork.clear()
        isFilterOn = false

        mMasterTransactionList.clear()
        mTransactionList.clear()

        mLastDoc = null
        mIsNoMoreDocs = false
        setAdapter()
    }

    private fun filterDialog() {
        var fm = supportFragmentManager
        var dFragment = DialogFragmentTransactionFilter()
        // Show DialogFragment
        dFragment.show(fm, "Transaction Filter Dialog Fragment")

    }

    private fun createPdfDialog() {

        if (mID == null) {
            mPdfheading = "ALL"
        } else {

            mPdfheading = when (mTransactionViewType) {
                LedgerDefine.TRANSACTION_VIEW_TYPE_PROJECT -> {
                    mProjectsMap[mID!!]!!
                }
                LedgerDefine.TRANSACTION_VIEW_TYPE_BANK_ACCOUNT -> {
                    mBankAccountsMap[mID!!]!!
                }
                else -> {
                    mUsersMap[mID!!.substring(2)]!!
                }
            }
        }
        var fm = supportFragmentManager
        var dFragment = DialogFragmentPDFColumnSelection()
        // Show DialogFragment
        dFragment.show(fm, " create pdf Dialog Fragment")

    }

    private fun switchBtnClicked() {
        mSwitchProjectOrSender = when (mSwitchProjectOrSender) {
            0 -> {
                1
            }
            1 -> {
                2
            }
            else -> {
                0
            }
        }
        val firstVisiblePos = expandable_list_view.firstVisiblePosition
        setAdapter()
        expandable_list_view.setSelection(firstVisiblePos)
    }

    private fun dragDownAddition() {

        // looking for 2nd checked item in visible zone
        val firstVisiblePos = expandable_list_view.firstVisiblePosition
        val lastVisiblePos = expandable_list_view.lastVisiblePosition

        var lastCheckedPos = lastVisiblePos
        var firstCheckedPos = 0
        var lastItemCheckedID: String? = null
        while (lastCheckedPos >= firstVisiblePos) {
            if (mTransactionList[lastCheckedPos].isChecked) {
                lastItemCheckedID = mTransactionList[lastCheckedPos].transactionID
                break
            }
            lastCheckedPos--
        }

        if (lastItemCheckedID == null) {
            Toast.makeText(this, "last position not found", Toast.LENGTH_LONG).show()
            return
        }



        for (detail in mTransactionList) {
            var firstItemCheckedId: String? = null
            if (detail.isChecked) {
                if (firstItemCheckedId == null) {
                    firstItemCheckedId = detail.transactionID

                }
                if (firstItemCheckedId == lastItemCheckedID) {
                    Toast.makeText(this, "Only One selection", Toast.LENGTH_LONG).show()
                    return
                } else if (firstItemCheckedId != null) {
                    break
                }
            }
            firstCheckedPos++
        }

        var totalAmount: Long = 0
        // get position of first checked item
        var count = 1
        var isAdditionDone = false
        var size = mTransactionList.size - firstCheckedPos
        while (firstCheckedPos < size) {
            var detail = mTransactionList[firstCheckedPos]

            if (isAdditionDone) {
                detail.isChecked = false
            } else {
                detail.isChecked = true
                totalAmount += detail.amount
            }
            if (detail.transactionID == lastItemCheckedID) {
                mAddition = totalAmount
                mSelectedCount = count
                Log.d("TEST", " total amount" + totalAmount)
                supportActionBar!!.title =
                    "" + LedgerUtils.getRupeesFormatted(mAddition) + " [$mSelectedCount]"
                setAdapter()
                expandable_list_view.setSelection(firstVisiblePos)
                isAdditionDone = true
            }

            count++
            firstCheckedPos++
        }

    }

    private fun setNormalMenu(isNormal: Boolean) {
        if (isNormal) {
            mLongClickedForAddition = false
            mReceivedMenuItem!!.isVisible = true
            mSentMenuItem!!.isVisible = true
            mExcel!!.isVisible = true
            mAddTransaction!!.isVisible = true
            mMenuSwitch!!.isVisible = true
            mFilterMenu!!.isVisible = true

            mdragDownAdd!!.isVisible = false
            mCancelAddition!!.isVisible = false
            mMenuPdf!!.isVisible = false

            for (detail in mTransactionList) {
                detail.isChecked = false
            }
        } else {
            mLongClickedForAddition = true
            mReceivedMenuItem!!.isVisible = false
            mSentMenuItem!!.isVisible = false
            mExcel!!.isVisible = false
            mAddTransaction!!.isVisible = false
            mMenuSwitch!!.isVisible = false
            mFilterMenu!!.isVisible = false

            mdragDownAdd!!.isVisible = true
            mCancelAddition!!.isVisible = true
            mMenuPdf!!.isVisible = true
        }
        mAddition = 0
        mSelectedCount = 0
        supportActionBar!!.title =
            "" + LedgerUtils.getRupeesFormatted(mAddition) + " [$mSelectedCount]"
        setAdapter()
    }


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
            mTransactionAdapter =
                TransactionViewAdapter(this, mTransactionList, mLongClickedForAddition)
        }
        expandable_list_view.setAdapter(mTransactionAdapter)
        expandable_list_view.setOnScrollListener(mScrollListener)

        expandable_list_view.setOnItemLongClickListener(this)
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
            R.id.cb_for_addition -> addTransactions(view)
        }
    }

    private fun addTransactions(view: View) {
        val checkBox: CheckBox = view as CheckBox
        val amount = checkBox!!.getTag(R.string.tag_amount) as Long
        val transactionID = checkBox!!.getTag(R.string.tag_transaction_id) as String
        if (checkBox.isChecked) {
            mAddition += amount
            mSelectedCount += 1
        } else {
            mAddition -= amount
            mSelectedCount -= 1
        }

        for (detail in mTransactionList) {
            if (detail.transactionID == transactionID) {
                detail.isChecked = checkBox.isChecked
                break
            }
        }
        supportActionBar!!.title =
            "" + LedgerUtils.getRupeesFormatted(mAddition) + " [$mSelectedCount]"

    }

    private fun editTransaction(view: View) {
        val transactionID = view.getTag(R.string.tag_transaction_id).toString()
        for (list in mTransactionList) {
            if (transactionID == list.transactionID) {
                var intent = Intent(mContext, AddTransactionActivity::class.java)
                intent.putExtra(
                    LedgerDefine.TRANSACTION_EDIT_TYPE,
                    LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY
                )
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
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
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
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_USERS)
            .whereEqualTo(LedgerDefine.USER_ID, userID.toLong())
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
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

        bundle.putString(
            LedgerDefine.BANK_ACCOUNT_ID,
            document!!.get(LedgerDefine.BANK_ACCOUNT_ID).toString()
        )
        bundle.putString(
            LedgerDefine.BANK_ACCOUNT_NUMBER,
            document!!.get(LedgerDefine.BANK_ACCOUNT_NUMBER).toString()
        )
        var amount = document!!.get(LedgerDefine.AMOUNT)
        if (amount != null) {
            bundle.putLong(LedgerDefine.AMOUNT, amount as Long)
        }


        var timestamp = document.get(LedgerDefine.TIME_STAMP)
        if (timestamp != null) {
            try {
                timestamp = timestamp as String
            } catch (e: java.lang.ClassCastException) {
                timestamp = (timestamp as Date).toString()
            }
        }
        bundle.putString(LedgerDefine.TIME_STAMP, timestamp.toString())
        bundle.putString(LedgerDefine.IFSC_CODE, document!!.get(LedgerDefine.IFSC_CODE).toString())
        bundle.putString(
            LedgerDefine.PAYEE_NAME,
            document!!.get(LedgerDefine.PAYEE_NAME).toString()
        )
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
        // personal
        var materialCost = document.get(LedgerDefine.P_MATERIAL_COST)
        if (materialCost != null) materialCost = materialCost as Long
        var payment = document.get(LedgerDefine.P_PAYMENT)
        if (payment != null) payment = payment as Long
        var gstBill = document.get(LedgerDefine.P_GST_BILL)
        if (gstBill != null) gstBill = gstBill as Long
        // master
        var masterAmount = document.get(LedgerDefine.M_AMOUNT)
        if (masterAmount != null) masterAmount = masterAmount as Long

        if (materialCost != null) {
            bundle.putLong(LedgerDefine.P_MATERIAL_COST, materialCost as Long)
        }

        if (payment != null) {
            bundle.putLong(LedgerDefine.P_PAYMENT, payment as Long)
        }

        if (gstBill != null) {
            bundle.putLong(LedgerDefine.P_GST_BILL, gstBill as Long)
        }

        if (masterAmount != null) {
            bundle.putLong(LedgerDefine.M_AMOUNT, masterAmount as Long)
        }





        bundle.putString(LedgerDefine.ADDRESS, document!!.get(LedgerDefine.ADDRESS).toString())

        var timestamp = document.get(LedgerDefine.TIME_STAMP)
        if (timestamp != null) {
            try {
                timestamp = timestamp as String
            } catch (e: java.lang.ClassCastException) {
                timestamp = (timestamp as Date).toString()
            }
        }

        bundle.putString(LedgerDefine.TIME_STAMP, timestamp.toString())
        bundle.putString(
            LedgerDefine.PHONE_NUMBER,
            document!!.get(LedgerDefine.PHONE_NUMBER).toString()
        )
        bundle.putString(LedgerDefine.EMAIL, document!!.get(LedgerDefine.EMAIL).toString())
        var designation = document!!.get(LedgerDefine.DESIGNATION)
        if (designation != null) {
            bundle.putLong(LedgerDefine.DESIGNATION, designation as Long)
        }

        var projects = document!!.get(LedgerDefine.ACCESSIBLE_PROJECTS)
        if (projects != null) {
            bundle.putString(
                LedgerDefine.ACCESSIBLE_PROJECTS,
                (projects as ArrayList<String>).toString()
            )
        }

        var userAccounts = document!!.get(LedgerDefine.ACCOUNTS)
        if (userAccounts != null) {
            bundle.putInt(LedgerDefine.USER_ACCOUNT_COUNT, (userAccounts as ArrayList<String>).size)
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
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_PROJECTS)
            .whereEqualTo(LedgerDefine.PROJECT_ID, projectID)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {

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

        bundle.putString(
            LedgerDefine.PROJECT_ID,
            document!!.get(LedgerDefine.PROJECT_ID).toString()
        )
        bundle.putString(LedgerDefine.NAME, document!!.get(LedgerDefine.NAME).toString())
        bundle.putString(LedgerDefine.NICKNAME, document!!.get(LedgerDefine.NICKNAME).toString())

        var amount = document!!.get(LedgerDefine.AMOUNT)
        if (amount != null) bundle.putLong(LedgerDefine.AMOUNT, amount as Long)
        bundle.putString(LedgerDefine.ADDRESS, document!!.get(LedgerDefine.ADDRESS).toString())
        bundle.putString(LedgerDefine.DIVISION, document!!.get(LedgerDefine.DIVISION).toString())
        bundle.putString(
            LedgerDefine.START_DATE,
            document!!.get(LedgerDefine.START_DATE).toString()
        )
        bundle.putString(LedgerDefine.END_DATE, document!!.get(LedgerDefine.END_DATE).toString())
        bundle.putString(LedgerDefine.REMARK, getStringFormDoc(document, LedgerDefine.REMARK))

        bundle.putString(LedgerDefine.MB_NO, getStringFormDoc(document, LedgerDefine.MB_NO))
        bundle.putString(LedgerDefine.HEAD, getStringFormDoc(document, LedgerDefine.HEAD))
        bundle.putString(
            LedgerDefine.MAIN_AMOUNT,
            getStringFormDoc(document, LedgerDefine.MAIN_AMOUNT)
        )
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

        var timestamp = document.get(LedgerDefine.TIME_STAMP)

        if (timestamp != null) {
            try {
                timestamp = timestamp as String
            } catch (e: java.lang.ClassCastException) {
                timestamp = (timestamp as Date).toString()
            }
        }
        bundle.putString(LedgerDefine.TIME_STAMP, timestamp.toString())

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
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
            .document(tid)
            .delete()
            .addOnSuccessListener(OnSuccessListener<Void> {
                toast(R.string.transaction_deleted)
            })
            .addOnFailureListener(OnFailureListener { e ->
            })
    }

    private fun btnVerifyClicked(view: View) {
        var tid: String = view.getTag(R.string.tag_transaction_id).toString()
        if (TextUtils.isEmpty(tid)) {
            return
        }
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

        val docRef =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
                .document(tid)

        docRef
            .update(LedgerDefine.VERIFIED, true)
            .addOnSuccessListener {
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
        Toast.makeText(this, "Not Implement yet !! $projectId", Toast.LENGTH_LONG).show()
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
                    var queryDocs = task.result!!
                    for (doc: QueryDocumentSnapshot in queryDocs) {
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
                        if (mTransactionAdapter != null) {
                            mTransactionAdapter!!.notifyDataSetChanged()
                            expandable_list_view.requestLayout()
                        }
                    }
                    if(!mIsNoMoreDocs && isFilterOn){
                        getTransactions(mConditionForUser)
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })

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
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        var query: Query =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)

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
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val id = document.id.toString()
                        mBankAccountsMap[id] =
                            document.get(LedgerDefine.PAYEE_NAME).toString()

                        if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_BANK_ACCOUNT && id == mID) {
                            supportActionBar!!.subtitle =
                                document.get(LedgerDefine.PAYEE_NAME).toString()
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getUsers() {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_USERS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        val id = document.id.toString()
                        mUsersMap[id] =
                            document.get(LedgerDefine.NAME).toString()
                        if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_USER && id == mID!!.substring(
                                2
                            )
                        ) {
                            supportActionBar!!.subtitle = document.get(LedgerDefine.NAME).toString()
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private var tempToast:Toast? =null
    private fun toast(id: Int) {

        if(tempToast != null ) tempToast!!.cancel()
        tempToast = Toast.makeText(mContext, id, Toast.LENGTH_SHORT)
        tempToast!!.show()
    }

    private fun setSetTransaction(document: QueryDocumentSnapshot?) {
        if (document != null) {
            val transactionID = document.get(LedgerDefine.TRANSACTION_ID).toString()

            for (details in mMasterTransactionList) {
                if (details.transactionID == transactionID) return
            }
            var transactionDetail: TransactionDetails = TransactionDetails()
            transactionDetail.senderId = document.get(LedgerDefine.SENDER_ID) as String
            transactionDetail.receiverId = document.get(LedgerDefine.RECEIVER_ID) as String
            transactionDetail.amount = document.get(LedgerDefine.AMOUNT) as Long
            var projectID = document.get(LedgerDefine.PROJECT_ID)
            if (projectID != null) transactionDetail.projectId = projectID as String
            transactionDetail.transactionDate =
                document.get(LedgerDefine.TRANSACTION_DATE) as String

            var timestamp = document.get(LedgerDefine.TIME_STAMP)
            if (timestamp != null) {
                try {
                    timestamp = timestamp as String
                } catch (e: java.lang.ClassCastException) {
                    timestamp = (timestamp as Date).toString()
                }
            }

            transactionDetail.timeStamp = timestamp.toString()
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

            mMasterTransactionList.add(transactionDetail)

            addToTempList(transactionDetail)

        }
    }

    private fun addToTempList(transactionDetail: TransactionDetails) {
        if (isFilterOn) {
            val isNeedToAddSender = mFilterListSender.isEmpty() || mFilterListSender.contains(
                transactionDetail.senderId.substring(
                    2
                )
            )


            var isNeedToAddReceiver = mFilterListReceiver.isEmpty() || mFilterListReceiver.contains(
                transactionDetail.receiverId.substring(
                    2
                )
            )

            var isNeedToAddWork = mFilterListWork.isEmpty() || mFilterListWork.contains(
                transactionDetail.projectId
            )

            if (isNeedToAddSender && isNeedToAddReceiver && isNeedToAddWork) mTransactionList.add(
                transactionDetail
            )
        } else {
            mTransactionList.add(transactionDetail)
        }
    }

    private fun getStringFormDoc(document: QueryDocumentSnapshot?, key: String): String {
        var data: Any? = document!!.get(key) ?: return ""

        return data as String

    }

    override fun onCheckedChanged(checkBox: CompoundButton?, isChecked: Boolean) {

    }

    public fun setFilter() {
        isFilterOn = true
        mTransactionList.clear()

        for (transactionDetail in mMasterTransactionList) {


            val isNeedToAddSender = mFilterListSender.isEmpty() || mFilterListSender.contains(
                transactionDetail.senderId.substring(
                    2
                )
            )


            var isNeedToAddReceiver = mFilterListReceiver.isEmpty() || mFilterListReceiver.contains(
                transactionDetail.receiverId.substring(
                    2
                )
            )

            var isNeedToAddWork = mFilterListWork.isEmpty() || mFilterListWork.contains(
                transactionDetail.projectId
            )

            if (isNeedToAddSender && isNeedToAddReceiver && isNeedToAddWork) mTransactionList.add(
                transactionDetail
            )

        }
        setAdapter()

        if (mTransactionList.size < LIMIT)
            getTransactions(mConditionForUser)

    }

    override fun onItemLongClick(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ): Boolean {
        setNormalMenu(false)
        return true
    }
}







