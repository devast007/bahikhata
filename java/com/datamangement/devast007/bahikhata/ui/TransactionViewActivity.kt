package com.datamangement.devast007.bahikhata.ui

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AbsListView.OnScrollListener
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.excel.TransactionsExcelSheet
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.adapter.TransactionViewAdapter
import com.datamangement.devast007.bahikhata.ui.dialog.DialogFragmentMoreInfo
import com.datamangement.devast007.bahikhata.ui.dialog.DialogFragmentPDFColumnSelection
import com.datamangement.devast007.bahikhata.ui.dialog.DialogFragmentRelatedTransactions
import com.datamangement.devast007.bahikhata.ui.dialog.DialogFragmentTransactionFilter
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.TransactionDetails
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_transaction_view.btn_change_image
import kotlinx.android.synthetic.main.activity_transaction_view.coordinatorLayout
import kotlinx.android.synthetic.main.activity_transaction_view.expandable_list_view
import kotlinx.android.synthetic.main.activity_transaction_view.iv_hide_image_view
import kotlinx.android.synthetic.main.activity_transaction_view.photo_view_link_image
import kotlinx.android.synthetic.main.activity_transaction_view.rl_image_view
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class TransactionViewActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener,
    AdapterView.OnItemLongClickListener, View.OnLongClickListener {

    private var mSelectedTransactionForPreview: TransactionDetails? = null
    private val PICK_IMAGE_REQUEST: Int = 99
    private var mIsForPayments: Boolean = false
    private var mEditGroupId: Int = 0
    private var mEditTrasactionId: String = ""
    private var mIsEditOn: Boolean = false
    private var mDialogForFilter: AlertDialog? = null
    private var isFilterOn: Boolean = false
    private var mTransactionAdapter: TransactionViewAdapter? = null

    var mRelatedTransactionsList: ArrayList<TransactionDetails> = ArrayList()
    var mTransactionList: ArrayList<TransactionDetails> = ArrayList()
    var mMasterTransactionList: ArrayList<TransactionDetails> = ArrayList()
    private var mMenuPdf: MenuItem? = null
    private var mMenuChangeSubCategory: MenuItem? = null
    private var mMenuChangeProject: MenuItem? = null


    var mPdfheading: String = "TRANSACTION"
    var mSwitchProjectOrSender: Int = 0

    private var mMenuRelateTransactions: MenuItem? = null
    private var mMenuSwitch: MenuItem? = null
    private var mdragDownAdd: MenuItem? = null
    public var mShowCheckBox: Boolean = false
    private var mCancelAddition: MenuItem? = null
    private var mAddTransaction: MenuItem? = null
    private var mExcel: MenuItem? = null
    private var mFilterMenu: MenuItem? = null
    private var mSelectedCount: Int = 0
    private var mAddition: Long = 0
    var mContext: Context? = null
    val TAG = "TransactionViewActivity"
    var mTransactionViewType = -1
    var mID: String? = null
    private var mUserDesignation: Long = -1
    var mBankAccountsMap: HashMap<String, String> = HashMap<String, String>()
    var mUsersMap: HashMap<String, String> = HashMap<String, String>()
    var mProjectsMap: HashMap<String, String> = HashMap<String, String>()

    var mSenderUsersMapForFilter: HashMap<String, String> = HashMap<String, String>()
    var mReceiverUsersMapForFilter: HashMap<String, String> = HashMap<String, String>()
    var mProjectsMapForFilter: HashMap<String, String> = HashMap<String, String>()
    var mSubcategoryForFilter: HashMap<String, String> = HashMap<String, String>()


    var mFilterListSender: ArrayList<String> = ArrayList<String>()
    var mFilterListReceiver: ArrayList<String> = ArrayList<String>()
    var mFilterListWork: ArrayList<String> = ArrayList<String>()
    var mFilterListSubcategory: ArrayList<String> = ArrayList<String>()
    var mTransactionSentOrReceived = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_view)
        mContext = this
        supportActionBar!!.setTitle(R.string.transactions)
        val intent = intent
        mTransactionViewType = intent.getIntExtra(LedgerDefine.TRANSACTION_VIEW_TYPE, -1)
        mID = intent.getStringExtra(LedgerDefine.ID)
        mIsForPayments = intent.getBooleanExtra(LedgerDefine.IS_FOR_PAYMENT, false)
        mUserDesignation = intent.getLongExtra(LedgerDefine.DESIGNATION, -1)
        mTransactionSentOrReceived =
            intent.getIntExtra(LedgerDefine.TRANSACTION_SENT_OR_RECEIVED, -1)

        iv_hide_image_view.setOnClickListener(this)
        btn_change_image.setOnClickListener(this)
        getAccounts()
        getUsers()
        getProjects()


        var condition = -1
        if (mTransactionViewType != -1 && mID != null) {
            supportActionBar!!.subtitle = mID
        }
        getTransactions(false)


    }

    override fun onResume() {
        super.onResume()
        if (mIsEditOn) {
            mIsEditOn = false
            if (!isEmpty(mEditTrasactionId)) {
                val db = FirestoreDataBase().db
                val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
                var query: Query =
                    db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
                        .whereEqualTo(LedgerDefine.TRANSACTION_ID, mEditTrasactionId)
                query.get()
                    .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                        if (task.isSuccessful) {
                            var queryDocs = task.result!!
                            for (document: QueryDocumentSnapshot in queryDocs) {

                                for (details in mTransactionList) {
                                    if (details.transactionID == mEditTrasactionId) {
                                        val doc = LedgerUtils.getTransactionDetails(document)
                                        details.senderId = doc.senderId
                                        details.receiverId = doc.receiverId
                                        details.amount = doc.amount
                                        details.roundOff = doc.roundOff
                                        details.projectId = doc.projectId
                                        details.transactionDate = doc.transactionDate
                                        details.timeStamp = doc.timeStamp
                                        details.transactionID = doc.transactionID
                                        details.transactionType = doc.transactionType
                                        details.loggedInID = doc.loggedInID
                                        details.verified = doc.verified
                                        details.subCategory = doc.subCategory
                                        details.remarks = doc.remarks
                                        details.debitedTo = doc.debitedTo
                                        details.creditedTo = doc.creditedTo
                                        details.paymentMode = doc.paymentMode
                                        details.isTrackingOn = doc.isTrackingOn
                                        details.relatedTransactionsIds = doc.relatedTransactionsIds
                                        break
                                    }
                                }

                                mTransactionAdapter!!.notifyDataSetChanged()
                                expandable_list_view.collapseGroup(mEditGroupId)
                                expandable_list_view.expandGroup(mEditGroupId, true)


                            }
                        }
                    })
            }
        }
    }


    private fun getProjects() {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this.mContext).getCompanyID()
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
        mMasterTransactionList.clear()
        // starts
        mIsNoLimit = true
        var query: Query = getCollection()!!
        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    var queryDocs = task.result!!
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        setTransaction(doc)
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
                                newIntent.setDataAndType(
                                    Uri.parse("file://$filePath"),
                                    dataType
                                )
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

                                startActivity(
                                    Intent.createChooser(
                                        intentShareFile,
                                        "Share File"
                                    )
                                );
                            }).show()
                } catch (e: Exception) {
                    toast(R.string.file_not_found)
                }

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_transaction_view, menu)
        mExcel = menu!!.findItem(R.id.action_excel)
        mFilterMenu = menu!!.findItem(R.id.action_filter)
        mAddTransaction = menu!!.findItem(R.id.action_add)
        mCancelAddition = menu!!.findItem(R.id.action_cancel_addition)
        mdragDownAdd = menu!!.findItem(R.id.action_drag_down_for_addition)
        mMenuSwitch = menu!!.findItem(R.id.action_switch)
        mMenuPdf = menu!!.findItem(R.id.action_pdf)
        mMenuChangeSubCategory = menu!!.findItem(R.id.action_change_subcategory)
        mMenuChangeProject = menu!!.findItem(R.id.action_change_project)

        mMenuRelateTransactions = menu!!.findItem(R.id.action_relate_transactions)


        //if (excel != null) {
        //  excel.isVisible = LedgerUtils.signInProfile!!.isAdmin
        //}

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item!!.itemId) {
            R.id.action_add -> {
                startActivity(Intent(this, AddTransactionActivity::class.java));
                return true
            }

            R.id.action_excel -> createExcelSheet()
            R.id.action_cancel_addition -> setNormalMenu(true)
            R.id.action_drag_down_for_addition -> dragDownAddition()
            R.id.action_switch -> switchBtnClicked()
            R.id.action_pdf -> createPdfDialog()
            R.id.action_change_subcategory -> changeData(LedgerDefine.SUBCATEGORY)
            R.id.action_change_project -> changeData(LedgerDefine.PROJECT_ID)
            R.id.action_filter -> filterDialog()
            R.id.action_relate_transactions -> relateTransaction()
        }

        return super.onOptionsItemSelected(item)
    }


    private fun changeData(fieldToUpdate: String) {

        if (!LedgerUtils.isHasEditPermission(mContext) || !LedgerUtils.signInProfile!!.isAdmin) return

        val alert = AlertDialog.Builder(this)

        val editText = EditText(mContext)
        editText.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        alert.setMessage("Enter New Data")
        alert.setView(editText)

        alert.setPositiveButton("Update",
            DialogInterface.OnClickListener { dialog, whichButton -> //What ever you want to do with the value

                validateInputAndUpdateTxns(fieldToUpdate, editText.text.toString())
            })

        alert.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, whichButton ->
                // what ever you want to do with No option.
            })

        alert.show()
    }

    private fun validateInputAndUpdateTxns(fieldToUpdate: String, data: String) {


        if (fieldToUpdate == LedgerDefine.PROJECT_ID) {

            val db = FirestoreDataBase().db
            val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_PROJECTS)
                .whereEqualTo(LedgerDefine.PROJECT_ID, data)
                .get()
                .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                    if (task.isSuccessful && task.result!!.size() == 1) {
                        updateDate(fieldToUpdate, data)
                    }
                })
        } else {
            updateDate(fieldToUpdate, data)
        }

    }

    private fun updateDate(fieldToUpdate: String, data: String) {

        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        val batchUpdateTransactions = db.batch()

        for (details in mTransactionList) {
            if (details.isChecked) {
                if (fieldToUpdate == LedgerDefine.PROJECT_ID) {
                    details.projectId = data
                } else if (fieldToUpdate == LedgerDefine.SUBCATEGORY) {
                    details.subCategory = data
                }

                val map = HashMap<String, Any>()
                map[fieldToUpdate] = data

                Log.d("devde", "details.transactionID = " + details.transactionID)
                val refs =
                    db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
                        .document(details.transactionID)
                batchUpdateTransactions.update(refs, map)
            }
        }

        batchUpdateTransactions.commit().addOnCompleteListener {
            if (it.isSuccessful) {
                mTransactionAdapter!!.notifyDataSetInvalidated()
                toast(R.string.sub_category_updated)
                Log.d("devde", "all it.exception " + it.exception)
            }
        }
    }

    private fun relateTransaction() {
        setNormalMenu(false)

        val snackbar = Snackbar
            .make(
                coordinatorLayout,
                R.string.click_ok_to_set_relation,
                Snackbar.LENGTH_INDEFINITE
            )

            .setAction(R.string.ok) {
                var relatedTransactionIDs = ArrayList<String>()
                for (details in mTransactionList) {
                    if (details.isChecked) {
                        relatedTransactionIDs.add(details.transactionID)
                    }
                }
                setRelations(relatedTransactionIDs)
            }
        snackbar.setActionTextColor(Color.BLUE)
        val sbView = snackbar.view
        sbView.setBackgroundColor(Color.GRAY)
        val textView =
            sbView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.BLACK)
        snackbar.show()
    }

    private fun setRelations(relatedTransactionIDs: ArrayList<String>) {
        if (relatedTransactionIDs.size <= 1 || relatedTransactionIDs.size > 10) {
            toast(R.string.select_two_to_ten_items)
            return
        }

        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        val batchTransaction = db.batch()


        for (transactionsId in relatedTransactionIDs) {
            var map = HashMap<String, Any>()
            map[LedgerDefine.RELATED_TRANSACTIONS_IDS] =
                FieldValue.arrayUnion(transactionsId)

            for (tid in relatedTransactionIDs) {
                val refs =
                    db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
                        .document(tid)
                batchTransaction.update(refs, map)
            }
        }

        batchTransaction.commit().addOnCompleteListener {
            if (it.isSuccessful) {
                toast(R.string.relations_set)
            }
        }
    }


    private fun resetFields() {
        mFilterListReceiver.clear()
        mFilterListSender.clear()
        mFilterListWork.clear()
        mFilterListSubcategory.clear()
        isFilterOn = false

        mMasterTransactionList.clear()
        mTransactionList.clear()

        mLastDoc = null
        mIsNoMoreDocs = false
        setAdapter()
    }

    private fun filterDialog() {

        val builder = AlertDialog.Builder(mContext)
        builder.setCancelable(false) // if you want user to wait for some process to finish,
        builder.setMessage(R.string.please_wait)
        mDialogForFilter = builder.create()
        mDialogForFilter!!.show()
        isFilterOn = true
        getTransactions(true)
    }

    private fun createPdfDialog() {

        if (isEmpty(mID)) {
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
            mShowCheckBox = false
            mExcel!!.isVisible = true
            mAddTransaction!!.isVisible = true
            mMenuSwitch!!.isVisible = true
            mFilterMenu!!.isVisible = true
            mMenuRelateTransactions!!.isVisible = true

            mdragDownAdd!!.isVisible = false
            mCancelAddition!!.isVisible = false
            mMenuPdf!!.isVisible = false
            mMenuChangeSubCategory!!.isVisible = false
            mMenuChangeProject!!.isVisible = false

            for (detail in mTransactionList) {
                detail.isChecked = false
            }
        } else {
            mShowCheckBox = true
            mExcel!!.isVisible = false
            mAddTransaction!!.isVisible = false
            mMenuSwitch!!.isVisible = false
            mFilterMenu!!.isVisible = false
            mMenuRelateTransactions!!.isVisible = false

            mdragDownAdd!!.isVisible = true
            mCancelAddition!!.isVisible = true
            mMenuPdf!!.isVisible = true
            mMenuChangeSubCategory!!.isVisible = true
            mMenuChangeProject!!.isVisible = true
        }
        mAddition = 0
        mSelectedCount = 0
        supportActionBar!!.title =
            "" + LedgerUtils.getRupeesFormatted(mAddition) + " [$mSelectedCount]"
        expandable_list_view.invalidateViews()
    }


    var mScrollListener: OnScrollListener = object : OnScrollListener {

        override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {


            if (scrollState === SCROLL_STATE_IDLE) {
                val count = expandable_list_view.count
                if (expandable_list_view.lastVisiblePosition >= count - 1) {
                    getTransactions(false)
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
                TransactionViewAdapter(this, mTransactionList, mShowCheckBox)
        }
        expandable_list_view.setAdapter(mTransactionAdapter)
        expandable_list_view.setOnScrollListener(mScrollListener)

        expandable_list_view.onItemLongClickListener = this
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
            R.id.tv_edit_transaction -> editTransaction(view)
            R.id.cb_for_addition -> addTransactions(view)
            R.id.switch_transaction_tracking -> setTransactionTracking(view)
            R.id.btn_related_transactions -> showRelatedTransactions(view)
            R.id.iv_hide_image_view -> {
                mSelectedTransactionForPreview!!.isTransactionSelected = false
                expandable_list_view.invalidateViews()
                rl_image_view.visibility = View.GONE
            }
            R.id.btn_change_image -> updateImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            var filePath = data.data
            try {
                // val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                uploadImageToFireStore(filePath)
                //imageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImageToFireStore(filePath: Uri?) {
        val storage = FirebaseStorage.getInstance();
        val pathStr =
            LedgerSharePrefManger(mContext).getCompanyID() + LedgerDefine.SLASH_TRANSACTIONS +
                    "/" + System.currentTimeMillis() + ".jpg"
        val riversRef: StorageReference = storage.reference.child(pathStr)

        val bmp = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 35, baos)
        val data: ByteArray = baos.toByteArray()
        val uploadTask2: UploadTask = riversRef.putBytes(data)

        if (!isEmpty(mSelectedTransactionForPreview!!.imageLink)) {
            FirebaseStorage.getInstance()
                .getReferenceFromUrl(mSelectedTransactionForPreview!!.imageLink!!)
                .delete()
        }

        uploadTask2.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            riversRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mSelectedTransactionForPreview!!.imageLink = task.result.toString()
                expandable_list_view.invalidateViews()
                Log.d(TAG, " task.result.toString() " + task.result.toString())
                showImagePreview(mSelectedTransactionForPreview!!.imageLink)
                updateLinkForMaterial()
            } else {
                // Handle failures
                // ...
            }
        }
    }

    private fun updateLinkForMaterial() {
        val db = FirebaseFirestore.getInstance()
        val companyName = LedgerSharePrefManger(mContext).getCompanyID()
        var docRef =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_TRANSACTIONS)
                .document(mSelectedTransactionForPreview!!.transactionID)
        docRef.update(LedgerDefine.IMAGE_LINK, mSelectedTransactionForPreview!!.imageLink)
    }

    private fun updateImage() {
        if (mSelectedTransactionForPreview!!.imageLink != null && !LedgerUtils.isHasEditPermission(
                mContext
            )
        ) return

        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission is  not granted");
            //File write logic here
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            );
            return;
        }
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            PICK_IMAGE_REQUEST
        )
        //
    }

    private fun showLinkedImage(view: View) {
        rl_image_view.visibility = View.VISIBLE
        photo_view_link_image.setImageResource(R.drawable.ic_bahikhata_24)
        val txnId = view.getTag(R.string.tag_transaction_id) as String

        if (mSelectedTransactionForPreview != null) {
            mSelectedTransactionForPreview!!.isTransactionSelected = false
        }
        for (details in mTransactionList) {
            if (details.transactionID == txnId) {
                mSelectedTransactionForPreview = details
                mSelectedTransactionForPreview!!.isTransactionSelected = true
                break
            }
        }

        expandable_list_view.invalidateViews()
        mTransactionAdapter!!.notifyDataSetInvalidated()

        showImagePreview(mSelectedTransactionForPreview!!.imageLink)

    }

    private fun showImagePreview(imageLink: String) {
        if (isEmpty(imageLink)) {
            toast(R.string.no_image_found)
            return
        }
        val islandRef: StorageReference =
            FirebaseStorage.getInstance().getReferenceFromUrl(imageLink)

        val ONE_MEGABYTE = (1024 * 1024).toLong()
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            // Data for "images/island.jpg" is returns, use this as needed
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            photo_view_link_image.setImageBitmap(bmp)
        }.addOnFailureListener {
            // Handle any errors
        }

    }

    private fun showRelatedTransactions(view: View) {
        mRelatedTransactionsList.clear()
        val txnId = view.getTag(R.string.tag_transaction_id) as String
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        var query: Query =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
                .whereArrayContains(LedgerDefine.RELATED_TRANSACTIONS_IDS, txnId)
        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    var queryDocs = task.result!!
                    for (document in queryDocs) {

                        val transactionID = document.get(LedgerDefine.TRANSACTION_ID).toString()

                        var transactionDetail = TransactionDetails()
                        transactionDetail.senderId = document.get(LedgerDefine.SENDER_ID) as String
                        transactionDetail.receiverId =
                            document.get(LedgerDefine.RECEIVER_ID) as String
                        transactionDetail.amount = document.get(LedgerDefine.AMOUNT) as Long
                        var roundOff = document!!.get(LedgerDefine.ROUND_OFF)
                        if (roundOff != null) {
                            transactionDetail.roundOff = roundOff as Long
                        }
                        var projectID = document.get(LedgerDefine.PROJECT_ID)
                        if (projectID != null) transactionDetail.projectId = projectID as String
                        transactionDetail.transactionDate =
                            document.get(LedgerDefine.TRANSACTION_DATE) as String

                        try {
                            transactionDetail.timeStamp =
                                document.getTimestamp(LedgerDefine.TIME_STAMP)!!.toDate().toString()
                        } catch (e: RuntimeException) {
                            transactionDetail.timeStamp =
                                document.get(LedgerDefine.TIME_STAMP).toString()
                        } catch (e: ClassCastException) {
                            transactionDetail.timeStamp =
                                (document.get(LedgerDefine.TIME_STAMP) as Date).toString()
                        }

                        transactionDetail.transactionID = transactionID
                        transactionDetail.transactionType =
                            document.get(LedgerDefine.TRANSACTION_TYPE) as Long
                        transactionDetail.loggedInID =
                            document.get(LedgerDefine.LOGGED_IN_ID) as String
                        transactionDetail.verified = document.get(LedgerDefine.VERIFIED) as Boolean


                        var subCategory = document.get(LedgerDefine.SUBCATEGORY)
                        if (subCategory != null) transactionDetail.subCategory =
                            subCategory as String


                        var remark = document.get(LedgerDefine.REMARK)
                        if (remark != null) transactionDetail.remarks = remark as String

                        var debitAccount = document.get(LedgerDefine.DEBIT_ACCOUNT_ID)
                        if (debitAccount != null) transactionDetail.debitedTo =
                            debitAccount.toString()

                        var creditAccount = document.get(LedgerDefine.CREDIT_ACCOUNT_ID)
                        if (creditAccount != null) transactionDetail.creditedTo =
                            creditAccount.toString()

                        var paymentMode = document.get(LedgerDefine.PAYMENT_MODE)
                        if (paymentMode != null) transactionDetail.paymentMode =
                            paymentMode.toString()

                        var isTrackingOn = document.get(LedgerDefine.IS_TRACKING_ON)
                        if (isTrackingOn != null) transactionDetail.isTrackingOn =
                            isTrackingOn as Boolean

                        var relatedTransactionIds =
                            document.get(LedgerDefine.RELATED_TRANSACTIONS_IDS)
                        if (relatedTransactionIds != null) transactionDetail.relatedTransactionsIds =
                            relatedTransactionIds as ArrayList<String>
                        mRelatedTransactionsList.add(transactionDetail)

                    }

                    var fm = supportFragmentManager
                    var dFragment = DialogFragmentRelatedTransactions()
                    // Show DialogFragment
                    dFragment.show(fm, "Transaction Related Dialog Fragment")

                }
            })
    }

    private fun setTransactionTracking(view: View) {
        val switch: Switch = view as Switch


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
            .update(
                LedgerDefine.IS_TRACKING_ON, switch.isChecked
            )
            .addOnSuccessListener {
                toast(R.string.transaction_tracking_changed)
                for (transaction in mTransactionList) {
                    if (tid == transaction.transactionID) {
                        transaction.isTrackingOn = switch.isChecked
                        expandable_list_view.deferNotifyDataSetChanged()
                        break
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
                toast(R.string.error_07)
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

        if (!LedgerUtils.isHasEditPermission(mContext)) return
        mIsEditOn = true
        mEditTrasactionId = view.getTag(R.string.tag_transaction_id).toString()
        mEditGroupId = view.getTag(R.string.tag_group_pos).toString().toInt()
        var intent = Intent(mContext, AddTransactionActivity::class.java)
        intent.putExtra(
            LedgerDefine.TRANSACTION_EDIT_TYPE,
            LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY
        )
        intent.putExtra(LedgerDefine.TRANSACTION_ID, mEditTrasactionId)
        startActivity(intent)

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

        var timestamp = try {
            document.getTimestamp(LedgerDefine.TIME_STAMP)!!.toDate().toString()
        } catch (e: RuntimeException) {
            document.get(LedgerDefine.TIME_STAMP).toString()
        } catch (e: ClassCastException) {
            (document.get(LedgerDefine.TIME_STAMP) as Date).toString()
        }

        bundle.putString(LedgerDefine.TIME_STAMP, timestamp)

        bundle.putString(
            LedgerDefine.IFSC_CODE,
            document!!.get(LedgerDefine.IFSC_CODE).toString()
        )
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


        var timestamp = try {
            document.getTimestamp(LedgerDefine.TIME_STAMP)!!.toDate().toString()
        } catch (e: RuntimeException) {
            document.get(LedgerDefine.TIME_STAMP).toString()
        } catch (e: ClassCastException) {
            (document.get(LedgerDefine.TIME_STAMP) as Date).toString()
        }

        bundle.putString(LedgerDefine.TIME_STAMP, timestamp)

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
            bundle.putInt(
                LedgerDefine.USER_ACCOUNT_COUNT,
                (userAccounts as ArrayList<String>).size
            )
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
        bundle.putString(
            LedgerDefine.NICKNAME,
            document!!.get(LedgerDefine.NICKNAME).toString()
        )

        var amount = document!!.get(LedgerDefine.AMOUNT)
        if (amount != null) bundle.putLong(LedgerDefine.AMOUNT, amount as Long)
        bundle.putString(LedgerDefine.ADDRESS, document!!.get(LedgerDefine.ADDRESS).toString())
        bundle.putString(
            LedgerDefine.DIVISION,
            document!!.get(LedgerDefine.DIVISION).toString()
        )
        bundle.putString(
            LedgerDefine.START_DATE,
            document!!.get(LedgerDefine.START_DATE).toString()
        )
        bundle.putString(
            LedgerDefine.END_DATE,
            document!!.get(LedgerDefine.END_DATE).toString()
        )
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

        var timestamp = try {
            document.getTimestamp(LedgerDefine.TIME_STAMP)!!.toDate().toString()
        } catch (e: RuntimeException) {
            document.get(LedgerDefine.TIME_STAMP).toString()
        } catch (e: ClassCastException) {
            (document.get(LedgerDefine.TIME_STAMP) as Date).toString()
        }

        bundle.putString(LedgerDefine.TIME_STAMP, timestamp)

        dFragment.arguments = bundle
        // Show DialogFragment
        dFragment.show(fm, "Project More Info ")
    }

    private fun btnDelClicked(view: View) {
        var tid: String = view.getTag(R.string.tag_transaction_id).toString()
        if (TextUtils.isEmpty(tid)) {
            return
        }
        val dialogClickListener =
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {

                        val db = FirestoreDataBase().db
                        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

                        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
                            .document(tid)
                            .delete()
                            .addOnSuccessListener(OnSuccessListener<Void> {
                                toast(R.string.transaction_deleted)
                            })
                            .addOnFailureListener(OnFailureListener { _ ->
                            })

                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                    }
                }
            }

        val builder = AlertDialog.Builder(mContext)
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()


    }

    private fun btnVerifyClicked(view: View) {
        var tid: String = view.getTag(R.string.tag_transaction_id).toString()
        var groupPos = view.getTag(R.string.tag_group_pos).toString().toInt()
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
                for (detail in mTransactionList) {
                    if (detail.transactionID == tid) {
                        detail.verified = true
                        break
                    }
                }
                mTransactionAdapter!!.notifyDataSetChanged()
                expandable_list_view.collapseGroup(groupPos)
                if (mTransactionList.size - 1 != groupPos)
                    expandable_list_view.expandGroup(groupPos + 1, true)


            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
                toast(R.string.error_07)
            }
        //mMasterTransactionList

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
    var count = 0

    val LOAD_TRANSACTIONS = 0
    val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                LOAD_TRANSACTIONS -> {
                    getTransactions(msg.obj as Boolean)
                }

            }
        }
    }


    /* var handler: Handler = object : Handler() {
         var mActivity: MainActivity? = null
         override fun handleMessage(msg: Message?) {
             //TODO: Handle different types of messages
             if (mActivity != null) {
                 mActivity.mCamera.takePicture(null, null, mActivity)
             }
         }
     }*/
    private fun getTransactions(isFilterRequest: Boolean) {

        Log.d(
            TAG,
            " first count ......  filterRequest = $isFilterRequest and mIsNoMoreDocs $mIsNoMoreDocs  " + count++
        )

        // Log.d(TAG, "filterRequest = $filterRequest and mIsNoMoreDocs $mIsNoMoreDocs")
        if (mIsNoMoreDocs) {
            toast(R.string.loading_finished)
            if (isFilterRequest) {
                showFilterDialog()
            }
            return
        } else {
            if (mLastDoc != null) {
                toast(R.string.loading_data)
            }
        }
        var query: Query = getCollection()!!
        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    var queryDocs = task.result!!
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        setTransaction(doc)
                    }

                    if (queryDocs.isEmpty) {
                        mIsNoMoreDocs = true
                    } else {
                        val size = queryDocs.size()
                        mLastDoc = queryDocs.documents[size - 1]
                    }
                    Log.d(
                        TAG,
                        " finalll call queryDocs.isEmpty = and mIsNoMoreDocs $mIsNoMoreDocs  " + queryDocs.isEmpty
                    )
                    if (expandable_list_view.count <= 0) {
                        setAdapter()
                    } else {
                        if (mTransactionAdapter != null) {
                            mTransactionAdapter!!.notifyDataSetChanged()
                            expandable_list_view.requestLayout()
                        }

                    }

                    if (!mIsNoMoreDocs && isFilterOn) {
                        val message = Message.obtain()
                        message.what = LOAD_TRANSACTIONS
                        message.arg1 = mConditionForUser
                        message.obj = isFilterRequest
                        mHandler.sendMessage(message)
                        //getTransactions(mConditionForUser, filterRequest)
                        Log.d(
                            TAG,
                            " finalll call queryDocs.isEmpty = and mIsNoMoreDocs $mIsNoMoreDocs  " + queryDocs.isEmpty
                        )
                    }
                    if (mIsNoMoreDocs && isFilterRequest) {
                        showFilterDialog()
                    }
                    // Log.d(TAG, " .............................. mIsNoMoreDocs $mIsNoMoreDocs  " +queryDocs.isEmpty )
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })

        Log.d(
            TAG,
            " last count ......$count filterRequest = $isFilterRequest and mIsNoMoreDocs $mIsNoMoreDocs"
        )
    }

    private fun showFilterDialog() {
        Log.d(TAG, "Error getting documents: showFilterDialog ")
        mSenderUsersMapForFilter.clear()
        mReceiverUsersMapForFilter.clear()
        mProjectsMapForFilter.clear()
        mSubcategoryForFilter.clear()
        for (transaction in mMasterTransactionList) {
            val senderID = transaction.senderId.substring(
                2
            )
            if (!mSenderUsersMapForFilter.containsKey(senderID)) {
                mSenderUsersMapForFilter[senderID] = mUsersMap[senderID].toString()
            }

            val receiverID = transaction.receiverId.substring(
                2
            )
            if (!mReceiverUsersMapForFilter.containsKey(receiverID)) {
                mReceiverUsersMapForFilter[receiverID] = mUsersMap[receiverID].toString()
            }

            val projectID = transaction.projectId
            if (projectID.isNotEmpty() && !mProjectsMapForFilter.containsKey(projectID)) {
                mProjectsMapForFilter[projectID] = mProjectsMap[projectID].toString()
            }

            val subcategory = transaction.subCategory
            if (subcategory.isNotEmpty() && !mSubcategoryForFilter.containsKey(projectID)) {
                mSubcategoryForFilter[subcategory] = subcategory
            }

        }
        mDialogForFilter!!.cancel()

        var fm = supportFragmentManager
        var dFragment = DialogFragmentTransactionFilter()
        // Show DialogFragment
        dFragment.show(fm, "Transaction Filter Dialog Fragment")
    }


    private var mLastDoc: DocumentSnapshot? = null

    private var mConditionForUser: Int = -1

    private var LIMIT: Long = 100

    private var mIsNoLimit: Boolean = false

    private fun getCollection(): Query? {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        var query: Query =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)

        if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_BANK_ACCOUNT) {


            if (mTransactionSentOrReceived == LedgerDefine.SENT_TRANSACTION) {
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
            if (mIsForPayments)
                query = query.whereEqualTo(
                    LedgerDefine.RECEIVER_ID,
                    LedgerDefine.PREFIX_ADMIN + LedgerDefine.ADMIN_ID
                )
        } else if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_USER) {
            if (mTransactionSentOrReceived == LedgerDefine.SENT_TRANSACTION) {
                query = query.whereEqualTo(
                    LedgerDefine.SENDER_ID,
                    mID
                )
            } else {
                query = query.whereEqualTo(
                    LedgerDefine.RECEIVER_ID,
                    mID
                )
            }
        } else if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_TRACKING) {
            query = query.whereEqualTo(
                LedgerDefine.IS_TRACKING_ON,
                true
            )
        } else if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_PENDING) {
            query = query.whereEqualTo(
                LedgerDefine.VERIFIED,
                false
            )
        }

        if (mTransactionViewType == LedgerDefine.TRANSACTION_VIEW_TYPE_PENDING) {
            query = query.orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
        } else {
            query = query.orderBy(LedgerDefine.VERIFIED, Query.Direction.ASCENDING)
                .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
        }

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
                            supportActionBar!!.subtitle =
                                document.get(LedgerDefine.NAME).toString()
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private var tempToast: Toast? = null
    private fun toast(id: Int) {

        if (tempToast != null) tempToast!!.cancel()
        tempToast = Toast.makeText(mContext, id, Toast.LENGTH_SHORT)
        tempToast!!.show()
    }

    private fun setTransaction(document: QueryDocumentSnapshot?) {
        if (document != null) {
            val getDoc = LedgerUtils.getTransactionDetails(document)
            mMasterTransactionList.add(getDoc)
            mTransactionList.add(getDoc)
        }
    }

/*  private fun addToTempList(transactionDetail: TransactionDetails) {
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
  }*/

    private fun getStringFormDoc(document: QueryDocumentSnapshot?, key: String): String {
        var data: Any? = document!!.get(key) ?: return ""

        return data as String

    }

    override fun onCheckedChanged(checkBox: CompoundButton?, isChecked: Boolean) {

    }

    public fun setFilter() {
        mTransactionList.clear()

        for (transactionDetail in mMasterTransactionList) {


            val isNeedToAddSender = mFilterListSender.isEmpty() || mFilterListSender.contains(
                transactionDetail.senderId.substring(
                    2
                )
            )


            var isNeedToAddReceiver =
                mFilterListReceiver.isEmpty() || mFilterListReceiver.contains(
                    transactionDetail.receiverId.substring(
                        2
                    )
                )

            var isNeedToAddWork = mFilterListWork.isEmpty() || mFilterListWork.contains(
                transactionDetail.projectId
            )

            var isNeedToAddSubcategory =
                mFilterListSubcategory.isEmpty() || mFilterListSubcategory.contains(
                    transactionDetail.subCategory
                )

            if (isNeedToAddSender && isNeedToAddReceiver && isNeedToAddWork && isNeedToAddSubcategory) mTransactionList.add(
                transactionDetail
            )

        }
        setAdapter()

        /*  if (mTransactionList.size < LIMIT)
              getTransactions(mConditionForUser,false)*/

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

    override fun onLongClick(view: View?): Boolean {
        when (view!!.id) {
            R.id.tv_transaction_amount -> showLinkedImage(view)
        }
        return true
    }
}







