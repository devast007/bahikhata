package com.datamangement.devast007.bahikhata.ui

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.excel.StatementExcelSheet
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.dialog.DialogFragmentToSelectUserOrProjectForStatement
import com.datamangement.devast007.bahikhata.utils.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_statement.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class StatementActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {

    var mMaterialOrService: HashMap<String, String> = HashMap<String, String>()
    var mServiceType: HashMap<String, HashMap<String, String>> =
        HashMap<String, HashMap<String, String>>()
    var mMedium: HashMap<String, String> = HashMap<String, String>()
    var mUserDetails: ArrayList<UserDetails>? = ArrayList<UserDetails>()
    var mUnits: HashMap<String, String> = HashMap<String, String>()
    private val mBankAccountsMap: HashMap<String, String> = HashMap()
    private val mProjectsMap: HashMap<String, String> = HashMap()
    var mUsersMap: HashMap<String, String> = HashMap()

    private var mDataLoadingCount: Int = 0
    private var mDialog: ProgressDialog? = null
    private val mMasterTransactionList: ArrayList<MaterialDetails> = ArrayList()
    private val LIMIT: Long = 300
    private var mLastDocForRecieved: DocumentSnapshot? = null
    private var mLastDocForSent: DocumentSnapshot? = null

    private var mLastDocForMaterial: DocumentSnapshot? = null
    private var mIsNoMoreDocsForSent: Boolean = false
    private var mIsNoMoreDocsForReceived: Boolean = false
    private var mIsNoMoreDocsForMaterial: Boolean = false
    private val LOAD_RECEIVED_AMOUNT: Int = 1
    private val LOAD_SENT_AMOUNT: Int = 2
    private val LOAD_MATERIAL_AMOUNT: Int = 3
    private val SHOW_DIALOG: Int = 5
    private val DATA_LOADING_DONE: Int = 6
    private var mContext: Context? = null
    var mSelectedSupplier: UserDetails? = null
    private var fromDatePickerDialog: DatePickerDialog? = null
    private var toDatePickerDialog: DatePickerDialog? = null
    val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.US);
    val TAG = "StatementActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statement)
        mContext = this
        supportActionBar!!.setTitle(R.string.generate_account_statement)
        findViewById<Button>(R.id.btn_supplier_id).setOnClickListener(this)
        findViewById<Button>(R.id.btn_excel).setOnClickListener(this)
        setDateTimeField()
        getUsers()
        getProjects()
        getAccounts()

        getMaterialOrService()
        getMediumList()
        getUnits()

    }

    private fun getUnits() {
        var db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_UNITS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val id = document.get(LedgerDefine.ID) as String
                        val name = document.get(LedgerDefine.NAME) as String
                        mUnits[id] = name
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getMediumList() {
        var db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MEDIUM)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val id = document.get(LedgerDefine.ID) as String
                        val name = document.get(LedgerDefine.NAME) as String
                        mMedium[id] = name
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getMaterialOrService() {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIAL_AND_SERVICES_SETTINGS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val id = document.get(LedgerDefine.MATERIAL_OR_SERVICE_ID) as String
                        val name =
                            document.get(LedgerDefine.MATERIAL_OR_SERVICE_NAME) as String
                        mMaterialOrService[id] = name
                        getServiceTypeList(id)
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getServiceTypeList(materialOrServiceId: String) {

        var db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(
            LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIAL_AND_SERVICES_SETTINGS
                    + "/$materialOrServiceId" + LedgerDefine.SLASH_SERVICE_TYPES
        )
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    var map: HashMap<String, String> = HashMap()
                    for (document in task.result!!) {
                        val id = document.get(LedgerDefine.ID) as String
                        val name = document.get(LedgerDefine.NAME) as String
                        map[id] = name
                        mServiceType[materialOrServiceId] = map
                    }
                    Log.d(
                        TAG,
                        "  mServiceType[materialOrServiceId]" + mServiceType[materialOrServiceId]
                    )
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
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

                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
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
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }


    private fun setDateTimeField() {
        var fromDateTxt = findViewById<Button>(R.id.btn_from_date)
        var toDateTxt = findViewById<Button>(R.id.btn_to_date)
        fromDateTxt.setOnClickListener(this)
        toDateTxt.setOnClickListener(this)
        fromDateTxt.setOnLongClickListener(this)
        toDateTxt.setOnLongClickListener(this)
        val newCalendar: Calendar = Calendar.getInstance()
        fromDatePickerDialog = DatePickerDialog(
            this,
            { view, year, monthOfYear, dayOfMonth ->
                val newDate: Calendar = Calendar.getInstance()
                newDate.set(year, monthOfYear, dayOfMonth)
                fromDateTxt.setText(dateFormatter.format(newDate.getTime()))
            },
            newCalendar.get(Calendar.YEAR),
            newCalendar.get(Calendar.MONTH),
            newCalendar.get(Calendar.DAY_OF_MONTH)
        )
        toDatePickerDialog = DatePickerDialog(
            this,
            { view, year, monthOfYear, dayOfMonth ->
                val newDate: Calendar = Calendar.getInstance()
                newDate.set(year, monthOfYear, dayOfMonth)
                toDateTxt.setText(dateFormatter.format(newDate.getTime()))
            },
            newCalendar.get(Calendar.YEAR),
            newCalendar.get(Calendar.MONTH),
            newCalendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun getUsers() {
        var projects: ArrayList<String> = ArrayList<String>()

        var db = FirestoreDataBase().db

        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_USERS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val userDetails = UserDetails()
                        val tempName = document.get(LedgerDefine.NAME)
                        var tempId = document.get(LedgerDefine.USER_ID)
                        try {
                            tempId = (tempId as Long).toString()
                        } catch (e: ClassCastException) {
                            tempId = tempId as String
                        }

                        if (tempId != null) userDetails.userID = tempId.toString()
                        if (tempName != null) userDetails.name = tempName as String
                        val UserAccounts = document.get(LedgerDefine.ACCOUNTS)
                        userDetails.userAccounts = UserAccounts as ArrayList<String>?
                        mUserDetails!!.add(userDetails)
                        mUsersMap[tempId.toString()] = tempName.toString()
                    }

                } else {
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    fun getReceivedAmount() {

        if (mSelectedSupplier == null) {
            Toast.makeText(mContext, R.string.supplier_name_empty, Toast.LENGTH_LONG).show()
            return
        }

        var fromDate = btn_from_date.text
        var toDate = btn_to_date.text

        val id = LedgerUtils.getUserAccount("" + btn_supplier_id.text)
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        var query: Query =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
                .whereEqualTo(LedgerDefine.RECEIVER_ID, id)
        if (!TextUtils.isEmpty(fromDate)) {
            var str = fromDate.split("-")
            var date = str[2] + str[1] + str[0] + "000000"
            query = query.whereGreaterThanOrEqualTo(LedgerDefine.TRANSACTION_DATE, date)
        }
        if (!TextUtils.isEmpty(toDate)) {
            var str = toDate.split("-")
            var date = str[2] + str[1] + str[0] + "999999"
            query = query.whereLessThanOrEqualTo(LedgerDefine.TRANSACTION_DATE, date)
        }

        query = query
            .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)

        if (mLastDocForRecieved != null) {
            query = query.startAfter(mLastDocForRecieved!!).limit(LIMIT)
        } else {
            query = query.limit(LIMIT)
        }

        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    var queryDocs = task.result!!
                    var i = 0
                    Log.d(TAG, " queryDocs.size() = " + queryDocs.size())
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        setReceivedAmount(doc)
                    }
                    Log.d(TAG, " queryDocs.empty() = " + queryDocs.isEmpty)
                    if (queryDocs.isEmpty) {
                        mIsNoMoreDocsForReceived = true
                        mHandler.sendEmptyMessage(DATA_LOADING_DONE)
                    } else {
                        val size = queryDocs.size()
                        mLastDocForRecieved = queryDocs.documents[size - 1]
                    }

                    if (!mIsNoMoreDocsForReceived) {
                        mHandler.sendEmptyMessage(LOAD_RECEIVED_AMOUNT)
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun setReceivedAmount(document: QueryDocumentSnapshot) {
        var details = MaterialDetails()
        details.paidBy = document.get(LedgerDefine.SENDER_ID) as String
        details.payment = document.get(LedgerDefine.AMOUNT) as Long
        var roundOff = document!!.get(LedgerDefine.ROUND_OFF)
        if (roundOff != null) {
            details.roundOff = roundOff as Long
        }
        var projectID = document.get(LedgerDefine.PROJECT_ID)
        if (projectID != null) details.projectId = projectID as String
        details.date =
            document.get(LedgerDefine.TRANSACTION_DATE) as String
        var subCategory = document.get(LedgerDefine.SUBCATEGORY)
        if (subCategory != null) details.subCategory = subCategory as String

        var remark = document.get(LedgerDefine.REMARK)
        if (remark != null) details.remarks = remark as String

        var creditAccount = document.get(LedgerDefine.CREDIT_ACCOUNT_ID)
        if (creditAccount != null) details.bankAccount = creditAccount.toString()
        mMasterTransactionList.add(details)

    }

    fun startDataLoading() {

        if (!isStoragePermissionGranted()) return
        mLastDocForRecieved = null
        mLastDocForSent = null
        mLastDocForMaterial = null
        mIsNoMoreDocsForSent = false
        mIsNoMoreDocsForReceived = false
        mIsNoMoreDocsForMaterial = false

        mDataLoadingCount = 3
        if (mSelectedSupplier == null) return
        mDialog = ProgressDialog.show(
            mContext,
            getString(R.string.excel_file),
            getString(R.string.please_wait),
            false
        )

        mMasterTransactionList.clear()
        mHandler.sendEmptyMessage(LOAD_RECEIVED_AMOUNT)
        mHandler.sendEmptyMessage(LOAD_SENT_AMOUNT)
        mHandler.sendEmptyMessage(LOAD_MATERIAL_AMOUNT)
    }


    val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                LOAD_RECEIVED_AMOUNT -> getReceivedAmount()
                LOAD_SENT_AMOUNT -> getSentAmount()
                LOAD_MATERIAL_AMOUNT -> getMaterialAmount()

                DATA_LOADING_DONE -> {
                    mDataLoadingCount--
                    Log.d(TAG, " mDataLoadingCount = $mDataLoadingCount")
                    if (mDataLoadingCount == 0) {
                        createExcelFile()
                    }


                }

            }
        }
    }


    private fun createExcelFile() {
        registerLocalBroadcastReceiver()

        Collections.sort(mMasterTransactionList, Comparator { a, b ->
            var cmp0: Int = a.date.compareTo(b.date)
            if (cmp0 != 0) {
                return@Comparator cmp0
            }

            if (a.payment < b.payment) {
                return@Comparator -1
            } else if (a.payment == b.payment) {
                return@Comparator -0
            } else {
                return@Comparator 1
            }
        })



        StatementExcelSheet(
            mContext,
            btn_supplier_id.text.toString(),
            mMasterTransactionList,
            mProjectsMap,
            mUsersMap,
            mBankAccountsMap,
            mMaterialOrService,
            mServiceType,
            mUnits,
            mMedium
        ).writeToSheet()

    }

    private fun registerLocalBroadcastReceiver() {
        LocalBroadcastManager.getInstance(mContext!!)
            .registerReceiver(mReceiver, IntentFilter(LedgerDefine.LOCAL_BROADCAST_INTENT))
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == LedgerDefine.LOCAL_BROADCAST_INTENT) {

                if (mDialog != null) mDialog!!.dismiss()
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
                    Log.d(TAG, " exception  message " + e.message)
                }

            }
        }
    }

    fun getSentAmount() {


        if (mSelectedSupplier == null) {
            Toast.makeText(mContext, R.string.supplier_name_empty, Toast.LENGTH_LONG).show()
            return
        }

        var fromDate = btn_from_date.text
        var toDate = btn_to_date.text

        val id = LedgerUtils.getUserAccount("" + btn_supplier_id.text)
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        var query: Query =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
                .whereEqualTo(LedgerDefine.SENDER_ID, id)
        if (!TextUtils.isEmpty(fromDate)) {
            var str = fromDate.split("-")
            var date = str[2] + str[1] + str[0] + "000000"
            query = query.whereGreaterThanOrEqualTo(LedgerDefine.TRANSACTION_DATE, date)
        }
        if (!TextUtils.isEmpty(toDate)) {
            var str = toDate.split("-")
            var date = str[2] + str[1] + str[0] + "999999"
            query = query.whereLessThanOrEqualTo(LedgerDefine.TRANSACTION_DATE, date)
        }

        query = query
            .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)

        if (mLastDocForSent != null) {
            query = query.startAfter(mLastDocForSent!!).limit(LIMIT)
        } else {
            query = query.limit(LIMIT)
        }

        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    var queryDocs = task.result!!
                    var i = 0
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        setSentAmount(doc)
                    }
                    if (queryDocs.isEmpty) {

                        mIsNoMoreDocsForSent = true
                        mHandler.sendEmptyMessage(DATA_LOADING_DONE)
                    } else {
                        val size = queryDocs.size()
                        mLastDocForSent = queryDocs.documents[size - 1]
                    }

                    if (!mIsNoMoreDocsForSent) {
                        mHandler.sendEmptyMessage(LOAD_SENT_AMOUNT)
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun setSentAmount(document: QueryDocumentSnapshot) {
        var details = MaterialDetails()
        details.paidTo = document.get(LedgerDefine.RECEIVER_ID) as String
        details.amount = (document.get(LedgerDefine.AMOUNT) as Long).toString()
        var roundOff = document!!.get(LedgerDefine.ROUND_OFF)
        if (roundOff != null) {
            details.roundOff = roundOff as Long
        }
        var projectID = document.get(LedgerDefine.PROJECT_ID)
        if (projectID != null) details.projectId = projectID as String
        details.date =
            document.get(LedgerDefine.TRANSACTION_DATE) as String
        var subCategory = document.get(LedgerDefine.SUBCATEGORY)
        if (subCategory != null) details.subCategory = subCategory as String

        var remark = document.get(LedgerDefine.REMARK)
        if (remark != null) details.remarks = remark as String

        mMasterTransactionList.add(details)

    }

    fun getMaterialAmount() {

        if (mSelectedSupplier == null) {
            Toast.makeText(mContext, R.string.supplier_name_empty, Toast.LENGTH_LONG).show()
            return
        }

        var fromDate = btn_from_date.text
        var toDate = btn_to_date.text

        val id = LedgerUtils.getUserAccount("" + btn_supplier_id.text)
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        var query: Query =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIALS_OR_SERVICES)
                .whereEqualTo(LedgerDefine.SENDER_ID, id)
        if (!TextUtils.isEmpty(fromDate)) {
            var str = fromDate.split("-")
            var date = str[2] + str[1] + str[0] + "000000"
            query = query.whereGreaterThanOrEqualTo(LedgerDefine.DATE, date)
        }
        if (!TextUtils.isEmpty(toDate)) {
            var str = toDate.split("-")
            var date = str[2] + str[1] + str[0] + "999999"
            query = query.whereLessThanOrEqualTo(LedgerDefine.DATE, date)
        }

        query = query
            .orderBy(LedgerDefine.DATE, Query.Direction.DESCENDING)

        if (mLastDocForMaterial != null) {
            query = query.startAfter(mLastDocForMaterial!!).limit(LIMIT)
        } else {
            query = query.limit(LIMIT)
        }

        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    var queryDocs = task.result!!
                    var i = 0
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        setMaterial(doc)
                    }
                    if (queryDocs.isEmpty) {
                        mIsNoMoreDocsForMaterial = true
                        mHandler.sendEmptyMessage(DATA_LOADING_DONE)
                    } else {
                        val size = queryDocs.size()
                        mLastDocForMaterial = queryDocs.documents[size - 1]
                    }

                    if (!mIsNoMoreDocsForMaterial) {
                        mHandler.sendEmptyMessage(LOAD_MATERIAL_AMOUNT)
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun setMaterial(document: QueryDocumentSnapshot) {
        if (document != null) {
            var materialsDetails = MaterialDetails()
            materialsDetails.rate = document.get(LedgerDefine.RATE) as String
            materialsDetails.unit = document.get(LedgerDefine.UNIT) as String
            materialsDetails.quantity = document.get(LedgerDefine.QUANTITY) as String
            materialsDetails.amount = document.get(LedgerDefine.AMOUNT) as String
            materialsDetails.projectId = document.get(LedgerDefine.PROJECT_ID) as String
            materialsDetails.date = document.get(LedgerDefine.DATE) as String

            materialsDetails.materialOrService = document.get(LedgerDefine.MATERIAL) as String

            var temp = document.get(LedgerDefine.SERVICE_TYPE)
            if (temp != null) materialsDetails.serviceType = temp as String

            temp = document.get(LedgerDefine.SERVICE_MEDIUM)
            if (temp != null) materialsDetails.medium = temp as String

            temp = document.get(LedgerDefine.VEHICLE_NO)
            if (temp != null) materialsDetails.vehicleNo = temp as String

            temp = document.get(LedgerDefine.CHALLAN_NO)
            if (temp != null) materialsDetails.challanNo = temp as String

            temp = document.get(LedgerDefine.SUBCATEGORY)
            if (temp != null) materialsDetails.subCategory = temp as String

            temp = document.get(LedgerDefine.REMARK)
            if (temp != null) materialsDetails.remarks = temp as String

            mMasterTransactionList.add(materialsDetails)
        }
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.btn_supplier_id -> {
                mMasterTransactionList.clear()
                openDailogForSupplier()
            }
            R.id.btn_from_date -> {
                mMasterTransactionList.clear()
                fromDatePickerDialog!!.show()
            }
            R.id.btn_to_date -> {
                mMasterTransactionList.clear()
                toDatePickerDialog!!.show()
            }

            R.id.btn_excel -> startDataLoading()
        }
    }

    private fun openDailogForSupplier() {
        var fm = supportFragmentManager
        var dFragment = DialogFragmentToSelectUserOrProjectForStatement()
        dFragment.show(fm, " project select Dialog Fragment")
    }

    override fun onLongClick(p0: View?): Boolean {
        when (p0!!.id) {
            R.id.btn_from_date -> btn_from_date.text = ""
            R.id.btn_to_date -> btn_to_date.text = ""
        }

        return true

    }

    private fun toast(id: Int) {
        Toast.makeText(mContext, id, Toast.LENGTH_LONG).show()
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
}
