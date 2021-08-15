package com.datamangement.devast007.bahikhata.ui

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.excel.GstExcelSheet
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.adapter.GstViewAdapter
import com.datamangement.devast007.bahikhata.ui.dialog.DialogFragmentMoreInfo
import com.datamangement.devast007.bahikhata.utils.GstDetails
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_transaction_view.*
import java.util.*
import kotlin.collections.ArrayList

class GstViewActivity : AppCompatActivity() , View.OnClickListener{

    var mContext: Context? = null
    val TAG = "MaterialViewActivity"
    var mID: String? = null
    var mUsersMap: HashMap<String, String> = HashMap<String, String>()
    val mProjectsMap: HashMap<String, String> = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gst_view)
        mContext = this
        supportActionBar!!.setTitle(R.string.gst)
        val intent = intent
        mID = intent.getStringExtra(LedgerDefine.ID)

        supportActionBar!!.subtitle = mID
        getUsers()
        getGstData(-1)
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
                        mUsersMap.put(document.id.toString(), document.get(LedgerDefine.NAME).toString())
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
                        Log.d(TAG, document.id + " => " + document.data)
                        mProjectsMap[document.id.toString()] =
                            document.get(LedgerDefine.NICKNAME).toString()
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
        var query: Query = getCollection()!!
        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    var queryDocs = task.result!!
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        setGstDetails(doc)
                    }
                    registerLocalBroadcastReceiver()
                    // test-1 starts
                    if (isEmpty(mID)) {
                        mID = "ALL"
                    }
                    GstExcelSheet(mContext, mID, mGstLists).writeToSheet()

                    // test-1 ends
                    if (expandable_list_view.count <= 0) {
                        setAdapter()
                    } else {
                        if (mGstAdapter != null) {
                            mGstAdapter!!.notifyDataSetChanged()
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
        menuInflater.inflate(R.menu.menu_add_button_view, menu)
        val excel = menu!!.findItem(R.id.action_excel)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item!!.itemId) {
            R.id.action_add -> {
                startActivity(Intent(this, AddGSTActivity::class.java));
                return true
            }
            R.id.action_excel_file -> createExcelSheet()
        }

        return super.onOptionsItemSelected(item)
    }


    private var mGstAdapter: GstViewAdapter? = null

    private var mGstLists: ArrayList<GstDetails> = ArrayList()

    var mScrollListener: AbsListView.OnScrollListener = object : AbsListView.OnScrollListener {

        override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {


            if (scrollState === AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                val count = expandable_list_view.count
                if (expandable_list_view.lastVisiblePosition >= count - 1) {
                    getGstData(mConditionForUser)
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
        if (mGstAdapter == null) {
            mGstAdapter = GstViewAdapter(this, mGstLists)
        }
        expandable_list_view.setAdapter(mGstAdapter)
        expandable_list_view.setOnScrollListener(mScrollListener)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tv_sender_id,
            R.id.tv_receiver_id,
            R.id.tv_log_in_id -> viewUserInfo(view)
            R.id.tv_project_id -> viewProjectInfo(view)
            R.id.tv_edit_gst -> editTransaction(view)
        }
    }

    private fun editTransaction(view: View) {
        val transactionID = view.getTag(R.string.tag_gst_id).toString()
        for (list in mGstLists) {
            if (transactionID == list.gstId) {
                var intent = Intent(mContext, AddGSTActivity::class.java)
                intent.putExtra(LedgerDefine.TRANSACTION_EDIT_TYPE, LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY)
                intent.putExtra(LedgerDefine.GST_ID, list.gstId)

                intent.putExtra(LedgerDefine.MATERIAL, list.material)
                intent.putExtra(LedgerDefine.GST_TAX_AMOUNT, list.gstTax)
                intent.putExtra(LedgerDefine.GST_TAX_PERCENTAGE, list.gstTaxPercent)
                intent.putExtra(LedgerDefine.GST_BILL_AMOUNT, "" + list.billAmount)

                intent.putExtra(LedgerDefine.PROJECT_ID, list.projectId)
                intent.putExtra(LedgerDefine.SENDER_ID, list.supplierID)
                intent.putExtra(LedgerDefine.RECEIVER_ID, list.receiverId)

                intent.putExtra(LedgerDefine.REMARK, list.remarks)
                intent.putExtra(LedgerDefine.DATE, list.date)
                startActivity(intent)
                break
            }
        }

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


    private fun showUserMoreInfoDialog(document: QueryDocumentSnapshot?) {
        var fm = supportFragmentManager
        var dFragment = DialogFragmentMoreInfo()
        var bundle = Bundle()
        bundle.putInt(LedgerDefine.MORE_INFO_TYPE, LedgerDefine.MORE_INFO_TYPE_USER)

        bundle.putString(LedgerDefine.USER_ID, document!!.get(LedgerDefine.USER_ID).toString())
        bundle.putString(LedgerDefine.NAME, document!!.get(LedgerDefine.NAME).toString())
        var m_amount = document!!.get(LedgerDefine.M_AMOUNT)
        if (m_amount != null) {
            bundle.putLong(LedgerDefine.M_AMOUNT, m_amount as Long)
        }

        var p_gst_bill = document!!.get(LedgerDefine.P_GST_BILL)
        if (p_gst_bill != null) {
            bundle.putLong(LedgerDefine.P_GST_BILL, p_gst_bill as Long)
        }

        var p_payment = document!!.get(LedgerDefine.P_PAYMENT)
        if (p_payment != null) {
            bundle.putLong(LedgerDefine.P_MATERIAL_COST, p_payment as Long)
        }

        var p_materialCost = document!!.get(LedgerDefine.P_MATERIAL_COST)
        if (p_materialCost != null) {
            bundle.putLong(LedgerDefine.AMOUNT, p_materialCost as Long)
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

        var userAccounts = document!!.get(LedgerDefine.ACCOUNTS)
        if (userAccounts != null) {
            bundle.putInt(LedgerDefine.USER_ACCOUNT_COUNT, (userAccounts as ArrayList<String>).size)
        }

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

        bundle.putString(LedgerDefine.PROJECT_ID, document!!.get(LedgerDefine.PROJECT_ID).toString())
        bundle.putString(LedgerDefine.NAME, document!!.get(LedgerDefine.NAME).toString())
        bundle.putString(LedgerDefine.NICKNAME, document!!.get(LedgerDefine.NICKNAME).toString())

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

    private fun isEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(mContext!!)
                .unregisterReceiver(mReceiver)
        } catch (e: java.lang.Exception) {

        }


    }


    private var mIsNoMoreDocs: Boolean = false

    private fun getGstData(conditionForUser: Int) {
        if (mIsNoMoreDocs) {
            toast(R.string.loading_finished)
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
                        Log.d(TAG, doc.id + " => " + doc.data)
                        setGstDetails(doc)
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
                        if (mGstAdapter != null) {
                            mGstAdapter!!.notifyDataSetChanged()
                            expandable_list_view.requestLayout()
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })

    }


    private var mLastDoc: DocumentSnapshot? = null

    private var mConditionForUser: Int = -1

    private var LIMIT: Long = 100

    private var mIsNoLimit: Boolean = false

    private fun getCollection(): Query? {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        var query: Query = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_GST)

        if (!TextUtils.isEmpty(mID)) {
            query = query.whereEqualTo(
                LedgerDefine.SENDER_ID,
                mID
            )
        }
        query = query
            .orderBy(LedgerDefine.DATE, Query.Direction.DESCENDING)
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


    private fun toast(id: Int) {
        Toast.makeText(mContext, id, Toast.LENGTH_LONG).show()
    }

    private fun setGstDetails(document: QueryDocumentSnapshot?) {
        if (document != null) {
            val gstID = document.get(LedgerDefine.GST_ID).toString()

            for (details in mGstLists) {
                if (details.gstId == gstID) return
            }
            var gstDetails = GstDetails()
            gstDetails.gstId = gstID
            gstDetails.material = document.get(LedgerDefine.MATERIAL) as String
            gstDetails.billAmount = document.get(LedgerDefine.GST_BILL_AMOUNT) as String
            gstDetails.gstTax = document.get(LedgerDefine.GST_TAX_AMOUNT) as String
            gstDetails.gstTaxPercent = document.get(LedgerDefine.GST_TAX_PERCENTAGE) as String
            gstDetails.supplierID = document.get(LedgerDefine.SENDER_ID) as String
            gstDetails.receiverId = document.get(LedgerDefine.RECEIVER_ID) as String
            gstDetails.projectId = document.get(LedgerDefine.PROJECT_ID) as String
            gstDetails.date = document.get(LedgerDefine.DATE) as String

            try {
                gstDetails.timeStamp =
                    document.getTimestamp(LedgerDefine.TIME_STAMP)!!.toDate().toString()
            } catch (e: RuntimeException) {
                gstDetails.timeStamp = document.get(LedgerDefine.TIME_STAMP).toString()
            } catch (e: ClassCastException) {
                gstDetails.timeStamp =
                    (document.get(LedgerDefine.TIME_STAMP) as Date).toString()
            }

            gstDetails.loggedInID = document.get(LedgerDefine.LOGGED_IN_ID) as String

            var remark = document.get(LedgerDefine.REMARK)
            if (remark != null) gstDetails.remarks = remark as String

            mGstLists.add(gstDetails)
        }
    }

    private fun getStringFormDoc(document: QueryDocumentSnapshot?, key: String): String {
        var data: Any? = document!!.get(key) ?: return ""

        return data as String

    }
}







