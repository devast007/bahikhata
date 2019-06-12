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
import com.datamangement.devast007.bahikhata.excel.MaterialsExcelSheet
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.adapter.MaterialViewAdapter
import com.datamangement.devast007.bahikhata.ui.fragment.DialogFragmentMoreInfo
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.MaterialDetails
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_transaction_view.*
import java.util.*
import kotlin.collections.ArrayList


class MaterialViewActivity : AppCompatActivity(), View.OnClickListener {

    var mContext: Context? = null
    val TAG = "MaterialViewActivity"
    var mID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materials_view)
        mContext = this
        supportActionBar!!.setTitle(R.string.material)
        val intent = intent
        mID = intent.getStringExtra(LedgerDefine.ID)

        supportActionBar!!.subtitle = mID
        getMaterials(-1)
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
                    Log.d(TAG, "task.isSuccessful")
                    var queryDocs = task.result!!
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        Log.d(TAG, doc.id + " => " + doc.data)
                        setMaterials(doc)
                    }
                    registerLocalBroadcastReceiver()
                    // test-1 starts
                    if (isEmpty(mID)) {
                        mID = "ALL"
                    }
                   MaterialsExcelSheet(mContext, mID, mMaterialsList).writeToSheet()

                    // test-1 ends
                    if (expandable_list_view.count <= 0) {
                        setAdapter()
                    } else {
                        Log.d(TAG, "mMaterialAdapter: notifyDataSetChanged  " + mMaterialAdapter)
                        if (mMaterialAdapter != null) {
                            mMaterialAdapter!!.notifyDataSetChanged()
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item!!.itemId) {
            R.id.action_add -> {
                startActivity(Intent(this, AddMaterialActivity::class.java));
                return true
            }
           R.id.action_excel_file -> createExcelSheet()
        }

        return super.onOptionsItemSelected(item)
    }


    private var mMaterialAdapter: MaterialViewAdapter? = null

    private var mMaterialsList: ArrayList<MaterialDetails> = ArrayList()

    var mScrollListener: OnScrollListener = object : OnScrollListener {

        override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {


            if (scrollState === SCROLL_STATE_IDLE) {
                val count = expandable_list_view.count
                if (expandable_list_view.lastVisiblePosition >= count - 1) {
                    getMaterials(mConditionForUser)
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
        if (mMaterialAdapter == null) {
            mMaterialAdapter = MaterialViewAdapter(this, mMaterialsList)
        }
        expandable_list_view.setAdapter(mMaterialAdapter)
        expandable_list_view.setOnScrollListener(mScrollListener)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tv_sender_id,
            R.id.tv_receiver_id,
            R.id.tv_log_in_id -> viewUserInfo(view)
            R.id.tv_project_id -> viewProjectInfo(view)
            R.id.tv_edit_material -> editTransaction(view)
        }
    }

    private fun editTransaction(view: View) {
        val transactionID = view.getTag(R.string.tag_material_id).toString()
        for (list in mMaterialsList) {
            if (transactionID == list.materialID) {
                var intent = Intent(mContext, AddMaterialActivity::class.java)
                intent.putExtra(LedgerDefine.TRANSACTION_EDIT_TYPE, LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY)
                intent.putExtra(LedgerDefine.MATERIAL_ID, list.materialID)

                intent.putExtra(LedgerDefine.MATERIAL, list.material)
                intent.putExtra(LedgerDefine.RATE, list.rate)
                intent.putExtra(LedgerDefine.QUANTITY, list.quantity)
                intent.putExtra(LedgerDefine.AMOUNT, "" + list.amount)

                intent.putExtra(LedgerDefine.PROJECT_ID, list.projectId)
                intent.putExtra(LedgerDefine.SENDER_ID, list.senderId)
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

    private fun getMaterials(conditionForUser: Int) {
        Log.d(TAG, "task.isSuccessful mIsNoMoreDocs = " + mIsNoMoreDocs)
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
                    Log.d(TAG, "task.isSuccessful")
                    var queryDocs = task.result!!
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        Log.d(TAG, doc.id + " => " + doc.data)
                        setMaterials(doc)
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
                        Log.d(TAG, "mMaterialAdapter: notifyDataSetChanged  " + mMaterialAdapter)
                        if (mMaterialAdapter != null) {
                            mMaterialAdapter!!.notifyDataSetChanged()
                            expandable_list_view.requestLayout()
                        }
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

    private fun getCollection(): Query? {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        Log.d(TAG, "companyID => " + companyID + " mLastDoc = " + mLastDoc)
        var query: Query = db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIALS)

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

    private fun setMaterials(document: QueryDocumentSnapshot?) {
        if (document != null) {
            val materialID = document.get(LedgerDefine.MATERIAL_ID).toString()

            for (details in mMaterialsList) {
                if (details.materialID == materialID) return
            }
            var materialsDetails = MaterialDetails()
            materialsDetails.materialID = materialID
            materialsDetails.material = document.get(LedgerDefine.MATERIAL) as String
            materialsDetails.rate = document.get(LedgerDefine.RATE) as String
            materialsDetails.quantity = document.get(LedgerDefine.QUANTITY) as String
            materialsDetails.amount = document.get(LedgerDefine.AMOUNT) as String
            materialsDetails.senderId = document.get(LedgerDefine.SENDER_ID) as String
            materialsDetails.receiverId = document.get(LedgerDefine.RECEIVER_ID) as String
            materialsDetails.projectId = document.get(LedgerDefine.PROJECT_ID) as String
            materialsDetails.date = document.get(LedgerDefine.DATE) as String
            materialsDetails.timeStamp = document.get(LedgerDefine.TIME_STAMP) as Date
            materialsDetails.loggedInID = document.get(LedgerDefine.LOGGED_IN_ID) as String

            var remark = document.get(LedgerDefine.REMARK)
            if (remark != null) materialsDetails.remarks = remark as String

            mMaterialsList.add(materialsDetails)
        }
    }

    private fun getStringFormDoc(document: QueryDocumentSnapshot?, key: String): String {
        var data: Any? = document!!.get(key) ?: return ""

        return data as String

    }
}







