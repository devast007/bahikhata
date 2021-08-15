package com.datamangement.devast007.bahikhata.ui

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.excel.MaterialsExcelSheet
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.adapter.MaterialViewAdapter
import com.datamangement.devast007.bahikhata.ui.dialog.DialogFragmentMoreInfo
import com.datamangement.devast007.bahikhata.ui.dialog.DialogPDFColumnSelectionMaterial
import com.datamangement.devast007.bahikhata.utils.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_materials_view.btn_change_image
import kotlinx.android.synthetic.main.activity_materials_view.coordinatorLayout
import kotlinx.android.synthetic.main.activity_materials_view.expandable_list_view
import kotlinx.android.synthetic.main.activity_materials_view.iv_hide_image_view
import kotlinx.android.synthetic.main.activity_materials_view.photo_view_link_image
import kotlinx.android.synthetic.main.activity_materials_view.rl_image_view
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToLong


class MaterialViewActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {


    private var mVerifyPendingMaterial: Int = -1
    var mPdfHeading: String? = ""
    var mMaterialAdapter: MaterialViewAdapter? = null
    var mMaterialsList: ArrayList<MaterialDetails> = ArrayList()
    var mMasterMaterialsList: ArrayList<MaterialDetails> = ArrayList()

    private var mSelectedMaterialDetails: MaterialDetails? = null
    private val PICK_IMAGE_REQUEST: Int = 99
    private var mProjectID: String? = null
    private var isFilterOn: Boolean = false
    private var mAmountSelectedCount: Int = 0
    private var mAmountAddition: Long = 0
    private var mQuantitySelectedCount: Int = 0
    private var mQuanityAddition: Double = 0.0
    var mContext: Context? = null
    val TAG = "MaterialViewActivity"
    var mSenderID: String? = null
    var mUsersMap: HashMap<String, String> = HashMap<String, String>()
    val mProjectsMap: HashMap<String, String> = HashMap<String, String>()
    private var mDialogForFilter: AlertDialog? = null
    var mMaterialOrService: HashMap<String, String> = HashMap<String, String>()
    var mServiceType: HashMap<String, HashMap<String, String>> =
        HashMap<String, HashMap<String, String>>()
    var mMedium: HashMap<String, String> = HashMap<String, String>()
    var mUnits: HashMap<String, String> = HashMap<String, String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_materials_view)
        mContext = this
        supportActionBar!!.setTitle(R.string.material_or_service)
        val intent = intent
        mSenderID = intent.getStringExtra(LedgerDefine.ID)
        mProjectID = intent.getStringExtra(LedgerDefine.PROJECT_ID)
        mVerifyPendingMaterial = intent.getIntExtra(LedgerDefine.TRANSACTION_VIEW_TYPE, -1)
        iv_hide_image_view.setOnClickListener(this)
        btn_change_image.setOnClickListener(this)


        getUsers()
        getProjects()
        getMaterialOrService()
        getMediumList()
        getUnits()
        getMaterials(-1, false)
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

    private fun getServiceTypeList(materialOrServiceId: String) {

        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        var db = FirestoreDataBase().db
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


    private fun getUsers() {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_USERS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        mUsersMap.put(
                            document.id.toString(),
                            document.get(LedgerDefine.NAME).toString()
                        )
                    }

                    if (!isEmpty(mSenderID)) {
                        mPdfHeading = mUsersMap[mSenderID!!.substring(2)]
                        supportActionBar!!.subtitle = mPdfHeading
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
                        mProjectsMap[document.id] =
                            document.get(LedgerDefine.NICKNAME) as String
                    }
                    if (!isEmpty(mProjectID)) {
                        mPdfHeading = mProjectsMap[mProjectID]
                        supportActionBar!!.subtitle = mPdfHeading
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
        var query: Query = getCollection()!!
        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    var queryDocs = task.result!!
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        Log.d(TAG, doc.id + " => " + doc.data)
                        setMaterials(doc)
                    }
                    registerLocalBroadcastReceiver()
                    // test-1 starts
                    if (isEmpty(mSenderID)) {
                        mSenderID = "ALL"
                    }
                    MaterialsExcelSheet(
                        mContext,
                        mSenderID,
                        mMasterMaterialsList,
                        mProjectsMap,
                        mUsersMap,
                        mMaterialOrService,
                        mServiceType,
                        mUnits,
                        mMedium
                    ).writeToSheet()

                    // test-1 ends
                    if (expandable_list_view.count <= 0) {
                        setAdapter()
                    } else {
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
        menuInflater.inflate(R.menu.menu_add_button_view, menu)
        val excel = menu!!.findItem(R.id.action_excel)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item!!.itemId) {
            R.id.action_add -> {
                startActivity(Intent(this, AddMaterialActivity::class.java));
                return true
            }
            R.id.action_excel_file -> createExcelSheet()
            R.id.action_filter -> filterDialog()
            R.id.action_add_all_visible -> addAllVisibleItems()
            R.id.action_edit_rate -> changeRate()
            R.id.action_change_subcategory -> changeData(LedgerDefine.SUBCATEGORY)
            R.id.action_change_project -> changeData(LedgerDefine.PROJECT_ID)
            R.id.action_pdf -> createPdfDialog()

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
        val batchUpdateMaterial = db.batch()

        for (details in mMaterialsList) {
            if (details.isMaterialChecked) {
                if (fieldToUpdate == LedgerDefine.PROJECT_ID) {
                    details.projectId = data
                } else if (fieldToUpdate == LedgerDefine.SUBCATEGORY) {
                    details.subCategory = data
                }

                val map = HashMap<String, Any>()
                map[fieldToUpdate] = data

                Log.d("devde", "details.materialID = " + details.materialID)
                val refs =
                    db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIALS_OR_SERVICES)
                        .document(details.materialID)
                batchUpdateMaterial.update(refs, map)
            }
        }

        batchUpdateMaterial.commit().addOnCompleteListener {
            if (it.isSuccessful) {
                mMaterialAdapter!!.notifyDataSetInvalidated()
                toast(R.string.sub_category_updated)
                Log.d("devde", "all it.exception " + it.exception)
            }
        }
    }

    private fun createPdfDialog() {

        if (isEmpty(mPdfHeading)) {
            mPdfHeading = "PDF"
        }

        var fm = supportFragmentManager
        var dFragment = DialogPDFColumnSelectionMaterial()
        // Show DialogFragment
        dFragment.show(fm, " create pdf Dialog Fragment")

    }

    /*private fun changetourl() {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

        var query: Query =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIALS_OR_SERVICES)

        query = query.whereNotEqualTo(
            LedgerDefine.IMAGE_LINK,
            ""
        )

        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    var queryDocs = task.result!!
                    mMasterMaterialsList.clear()
                    for (doc: QueryDocumentSnapshot in queryDocs) setMaterials(doc)
                    Log.d(TAG, " masterlist size = " + mMasterMaterialsList.size)
                    var storageRef = FirebaseStorage.getInstance().reference

                    var updateLInk =
                        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIALS_OR_SERVICES)
                    for (details in mMasterMaterialsList) {

                        storageRef.child(details.imageLink).downloadUrl
                            .addOnSuccessListener(
                                OnSuccessListener<Uri?> {
                                    Log.d(TAG, " it 22url on successs = " + it)
                                    *//* updateLInk.document(details.materialID)
                                         .update(LedgerDefine.IMAGE_LINK, it.toString())*//*
                                    // Got the download URL for 'users/me/profile.png'
                                }).addOnFailureListener(OnFailureListener {
                                // Handle any errors
                                Log.d(TAG, " exception it url = " + details.imageLink)
                                *//*updateLInk.document(details.materialID)
                                    .update(LedgerDefine.IMAGE_LINK, "")*//*

                            })

                    }


                    *//* for (detail in mMasterMaterialsList) {
                         Log.d(TAG, "detail.imageLink = " + mMasterMaterialsList.size)
                         storageRef.child(detail.imageLink).downloadUrl
                             .addOnSuccessListener(
                                 OnSuccessListener<Uri?> {
                                     Log.d(TAG, " it url on successs = " + it)
                                     // Got the download URL for 'users/me/profile.png'
                                 }).addOnFailureListener(OnFailureListener {
                                 // Handle any errors
                                 Log.d(TAG, " exception it url = " + it.message)
                             })
                     }*//*
                }

            })
    }
*/
    private fun changeRate() {

        if (!LedgerUtils.isHasEditPermission(mContext)) return

        val alert = AlertDialog.Builder(this)

        val editText = EditText(mContext)
        editText.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        alert.setMessage("Enter New Rate")
        alert.setView(editText)

        alert.setPositiveButton("Update",
            DialogInterface.OnClickListener { dialog, whichButton -> //What ever you want to do with the value

                validateInput(editText.text.toString())
            })

        alert.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, whichButton ->
                // what ever you want to do with No option.
            })

        alert.show()
    }

    private fun validateInput(newRate: String) {

        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        val batchUpdateMaterial = db.batch()

        if (isEmpty(newRate)) {
            toast(R.string.no_input)
            return
        }
        var oldMaterial = ""
        for (details in mMaterialsList) {
            if (details.isMaterialChecked) {
                if (isEmpty(oldMaterial)) oldMaterial = details.materialOrService

                if (oldMaterial != details.materialOrService) {
                    toast(R.string.different_in_material)
                    return
                }

                val amount =
                    (newRate.toDouble() * details.quantity.toDouble()).roundToLong()
                        .toString()
                details.rate = newRate
                details.amount = amount
                val map = HashMap<String, Any>()
                map[LedgerDefine.RATE] = newRate
                map[LedgerDefine.AMOUNT] = amount



                Log.d("devde", "details.materialID = " + details.materialID)
                val refs =
                    db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIALS_OR_SERVICES)
                        .document(details.materialID)
                batchUpdateMaterial.update(refs, map)

            }
        }

        batchUpdateMaterial.commit().addOnCompleteListener {
            if (it.isSuccessful) {
                mMaterialAdapter!!.notifyDataSetInvalidated()
                toast(R.string.rate_update_done)
                Log.d("devde", "all it.exception " + it.exception)
            }
        }


    }

    private fun addAllVisibleItems() {

        mQuanityAddition = 0.0
        mAmountSelectedCount = 0

        mQuantitySelectedCount = 0
        mAmountAddition = 0

        for (detail in mMaterialsList) {

            mAmountAddition += detail.amount.toLong()
            mAmountSelectedCount += 1

            detail.isMaterialChecked = true
            mQuanityAddition += detail.quantity.toDouble()
            mQuantitySelectedCount += 1

        }
        supportActionBar!!.title =
            "" + LedgerUtils.getRupeesFormatted(mAmountAddition) + " ($mAmountSelectedCount)"

        val strQuantity = String.format("%.2f", mQuanityAddition)
        supportActionBar!!.subtitle =
            "Quantity($mQuantitySelectedCount): $strQuantity"
        expandable_list_view.invalidateViews()
    }

    private fun filterDialog() {

        val builder = AlertDialog.Builder(mContext)
        builder.setCancelable(false) // if you want user to wait for some process to finish,
        builder.setMessage(R.string.please_wait)
        mDialogForFilter = builder.create()
        mDialogForFilter!!.show()
        isFilterOn = true
        getMaterials(mConditionForUser, true)
    }


    var mScrollListener: OnScrollListener = object : OnScrollListener {

        override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {


            if (scrollState === SCROLL_STATE_IDLE) {
                val count = expandable_list_view.count
                if (expandable_list_view.lastVisiblePosition >= count - 1) {
                    getMaterials(mConditionForUser, false)
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
            R.id.btn_verify -> btnVerifyClicked(view)
            R.id.btn_del -> btnDelClicked(view)
            R.id.tv_supplier,
            R.id.tv_reporter_id,
            R.id.tv_log_in_id -> viewUserInfo(view)
            R.id.tv_project_id -> viewProjectInfo(view)
            R.id.tv_edit_material -> editTransaction(view)
            R.id.cb_project -> addQuantity(view)

            R.id.iv_hide_image_view -> {
                mSelectedMaterialDetails!!.isMaterialSelected = false
                expandable_list_view.invalidateViews()
                rl_image_view.visibility = View.GONE
            }
            R.id.btn_change_image -> updateImage()
        }
    }

    private fun btnDelClicked(view: View) {
        var tid: String = view.getTag(R.string.tag_material_id).toString()
        if (TextUtils.isEmpty(tid)) {
            return
        }
        val dialogClickListener =
            DialogInterface.OnClickListener { _, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {

                        val db = FirestoreDataBase().db
                        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

                        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIALS_OR_SERVICES)
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
        var tid: String = view.getTag(R.string.tag_material_id).toString()
        var groupPos = view.getTag(R.string.tag_group_pos).toString().toInt()
        if (TextUtils.isEmpty(tid)) {
            return
        }
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

        val docRef =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIALS_OR_SERVICES)
                .document(tid)

        docRef
            .update(LedgerDefine.VERIFIED, true)
            .addOnSuccessListener {
                toast(R.string.transaction_verified)
                for (detail in mMasterMaterialsList) {
                    if (detail.materialID == tid) {
                        detail.verified = true
                        break
                    }
                }
                mMaterialAdapter!!.notifyDataSetChanged()
                expandable_list_view.collapseGroup(groupPos)
                if (mMaterialsList.size - 1 != groupPos)
                    expandable_list_view.expandGroup(groupPos + 1, true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
                toast(R.string.error_07)
            }
        //mMasterMaterialsList

    }

    private fun updateImage() {

        if (mSelectedMaterialDetails!!.imageLink != null && !LedgerUtils.isHasEditPermission(
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
            LedgerSharePrefManger(mContext).getCompanyID() + LedgerDefine.SLASH_MATERIALS_OR_SERVICES +
                    "/" + System.currentTimeMillis() + ".jpg"
        val riversRef: StorageReference = storage.reference.child(pathStr)

        val bmp = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 35, baos)
        val data: ByteArray = baos.toByteArray()
        val uploadTask2: UploadTask = riversRef.putBytes(data)

        if (!isEmpty(mSelectedMaterialDetails!!.imageLink)) {
            FirebaseStorage.getInstance()
                .getReferenceFromUrl(mSelectedMaterialDetails!!.imageLink!!)
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
                mSelectedMaterialDetails!!.imageLink = task.result.toString()
                expandable_list_view.invalidateViews()
                Log.d(TAG, " task.result.toString() " + task.result.toString())
                showImagePreview(mSelectedMaterialDetails!!.imageLink)
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
            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_MATERIALS_OR_SERVICES)
                .document(mSelectedMaterialDetails!!.materialID)
        docRef.update(LedgerDefine.IMAGE_LINK, mSelectedMaterialDetails!!.imageLink)
    }

    private fun showLinkedImage(view: View) {
        rl_image_view.visibility = View.VISIBLE
        photo_view_link_image.setImageResource(R.drawable.ic_bahikhata_24)
        val materialID = view.getTag(R.string.tag_material_id) as String

        if (mSelectedMaterialDetails != null) {
            mSelectedMaterialDetails!!.isMaterialSelected = false
        }
        for (details in mMaterialsList) {
            if (details.materialID == materialID) {
                mSelectedMaterialDetails = details
                mSelectedMaterialDetails!!.isMaterialSelected = true
                break
            }
        }

        expandable_list_view.invalidateViews()
        mMaterialAdapter!!.notifyDataSetInvalidated()

        showImagePreview(mSelectedMaterialDetails!!.imageLink)

    }

    private fun showImagePreview(imageLink: String) {
        Log.d(TAG, " image link " + imageLink)
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


    private fun addQuantity(view: View) {

        val checkBox: CheckBox = view as CheckBox
        val materialId = checkBox!!.getTag(R.string.tag_material_id) as String

        for (detail in mMaterialsList) {
            if (detail.materialID == materialId) {
                detail.isMaterialChecked = checkBox.isChecked
                if (checkBox.isChecked) {
                    mQuanityAddition += detail.quantity.toDouble()
                    mQuantitySelectedCount += 1

                    mAmountAddition += detail.amount.toLong()
                    mAmountSelectedCount += 1

                } else {
                    mQuanityAddition -= detail.quantity.toDouble()
                    mQuantitySelectedCount -= 1

                    mAmountAddition -= detail.amount.toLong()
                    mAmountSelectedCount -= 1
                }

                supportActionBar!!.title =
                    "Amount($mAmountSelectedCount): " + LedgerUtils.getRupeesFormatted(
                        mAmountAddition
                    )
                val strQuantity = String.format("%.2f", mQuanityAddition)
                supportActionBar!!.subtitle =
                    "Quantity($mQuantitySelectedCount): $strQuantity"
                break
            }
        }
        expandable_list_view.invalidateViews()
    }

    private fun editTransaction(view: View) {

        if (!LedgerUtils.isHasEditPermission(mContext)) return

        val transactionID = view.getTag(R.string.tag_material_id).toString()
        for (list in mMaterialsList) {
            if (transactionID == list.materialID) {
                var intent = Intent(mContext, AddMaterialActivity::class.java)
                intent.putExtra(
                    LedgerDefine.TRANSACTION_EDIT_TYPE,
                    LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY
                )
                intent.putExtra(LedgerDefine.MATERIAL_ID, list.materialID)

                intent.putExtra(LedgerDefine.MATERIAL, list.materialOrService)
                intent.putExtra(LedgerDefine.RATE, list.rate)
                intent.putExtra(LedgerDefine.QUANTITY, list.quantity)
                intent.putExtra(LedgerDefine.AMOUNT, "" + list.amount)

                intent.putExtra(LedgerDefine.PROJECT_ID, list.projectId)
                intent.putExtra(LedgerDefine.SENDER_ID, list.supplierId)
                intent.putExtra(LedgerDefine.RECEIVER_ID, list.reporterId)

                intent.putExtra(LedgerDefine.REMARK, list.remarks)
                intent.putExtra(LedgerDefine.DATE, list.date)
                intent.putExtra(LedgerDefine.SERVICE_TYPE, list.serviceType)
                intent.putExtra(LedgerDefine.SERVICE_MEDIUM, list.medium)
                intent.putExtra(LedgerDefine.VEHICLE_NO, list.vehicleNo)
                intent.putExtra(LedgerDefine.CHALLAN_NO, list.challanNo)
                intent.putExtra(LedgerDefine.UNIT, list.unit)
                intent.putExtra(LedgerDefine.SUBCATEGORY, list.subCategory)
                intent.putExtra(LedgerDefine.IMAGE_LINK, list.imageLink)

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
        var amount = document!!.get(LedgerDefine.AMOUNT)
        if (amount != null) {
            bundle.putLong(LedgerDefine.AMOUNT, amount as Long)
        }
        bundle.putString(LedgerDefine.ADDRESS, document!!.get(LedgerDefine.ADDRESS).toString())
        bundle.putString(
            LedgerDefine.TIME_STAMP,
            document!!.get(LedgerDefine.TIME_STAMP).toString()
        )
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
        bundle.putString(LedgerDefine.REMARK, document!!.get(LedgerDefine.REMARK).toString())

        var userAccounts = document!!.get(LedgerDefine.ACCOUNTS)
        if (userAccounts != null) {
            bundle.putInt(
                LedgerDefine.USER_ACCOUNT_COUNT,
                (userAccounts as ArrayList<String>).size
            )
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

    private fun getMaterials(conditionForUser: Int, isFilterRequest: Boolean) {
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
                        if (mMaterialAdapter != null) {
                            mMaterialAdapter!!.notifyDataSetChanged()
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
                            " finally call queryDocs.isEmpty = and mIsNoMoreDocs $mIsNoMoreDocs  " + queryDocs.isEmpty
                        )
                    }
                    if (mIsNoMoreDocs && isFilterRequest) {
                        showFilterDialog()
                    }

                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    val LOAD_TRANSACTIONS = 0
    val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                LOAD_TRANSACTIONS -> {
                    getMaterials(msg.arg1, msg.obj as Boolean)
                }

            }
        }
    }

    private fun showFilterDialog() {

        supportActionBar!!.title = ""

        supportActionBar!!.subtitle = ""
        expandable_list_view.invalidateViews()
        var materialList = ArrayList<String>()
        var materialListIds = ArrayList<String>()

        for (details in mMasterMaterialsList) {
            val material = mMaterialOrService[details.materialOrService]

            // to operate the filter application as per the selected ids of the material
            if (!materialListIds.contains(details.materialOrService)) {
                materialListIds.add(details.materialOrService!!)
            }

            // to show in dialog for filter values of material and service( not ids)
            if (!materialList.contains(material)) {
                materialList.add(material!!)
            }
        }


        var listItems = materialList.toTypedArray()
        val checkedItems = BooleanArray(listItems.size)
        var builder = AlertDialog.Builder(mContext)
        // set the title for the alert dialog
        builder.setTitle("Select Materials")


        // now this is the function which sets the alert dialog for multiple item selection ready
        builder.setMultiChoiceItems(listItems, checkedItems,
            OnMultiChoiceClickListener { dialog, which, isChecked ->
                checkedItems[which] = isChecked
            })


        // alert dialog shouldn't be cancellable
        builder.setCancelable(true)


        // handle the positive button of the dialog
        builder.setPositiveButton("Done") { dialog, which ->
            mMaterialsList.clear()

            for (details in mMasterMaterialsList) {
                details.isMaterialChecked = false
                for (i in checkedItems.indices) if (checkedItems[i]) {
                    if (details.materialOrService == materialListIds[i]) {
                        mMaterialsList.add(details)
                    }
                }

            }

            mDialogForFilter!!.cancel()
            setAdapter()
        }

        builder.create()

        val alertDialog = builder.create()
        alertDialog.show()
    }


    private val SENT: Int = 1
    private val RECEIVED: Int = 2

    private var mLastDoc: DocumentSnapshot? = null

    private var mConditionForUser: Int = -1

    private var LIMIT: Long = 100

    private var mIsNoLimit: Boolean = false

    private fun getCollection(): Query? {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

        var query: Query =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIALS_OR_SERVICES)

        if (!TextUtils.isEmpty(mSenderID)) {
            query = query.whereEqualTo(
                LedgerDefine.SENDER_ID,
                mSenderID
            )
        }

        if (!TextUtils.isEmpty(mProjectID)) {
            query = query.whereEqualTo(
                LedgerDefine.PROJECT_ID,
                mProjectID
            )
        }

        if (mVerifyPendingMaterial == LedgerDefine.TRANSACTION_VIEW_TYPE_PENDING) {
            query = query.whereEqualTo(
                LedgerDefine.VERIFIED,
                false
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

            for (details in mMasterMaterialsList) {
                if (details.materialID == materialID) return
            }
            var materialsDetails = MaterialDetails()
            materialsDetails.materialID = materialID
            materialsDetails.rate = document.get(LedgerDefine.RATE) as String
            materialsDetails.unit = document.get(LedgerDefine.UNIT) as String
            materialsDetails.quantity = document.get(LedgerDefine.QUANTITY) as String
            materialsDetails.amount = document.get(LedgerDefine.AMOUNT) as String
            materialsDetails.supplierId = document.get(LedgerDefine.SENDER_ID) as String
            materialsDetails.reporterId = document.get(LedgerDefine.RECEIVER_ID) as String
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

            try {
                materialsDetails.timeStamp =
                    document.getTimestamp(LedgerDefine.TIME_STAMP)!!.toDate().toString()
            } catch (e: RuntimeException) {
                materialsDetails.timeStamp = document.get(LedgerDefine.TIME_STAMP).toString()
            } catch (e: ClassCastException) {
                materialsDetails.timeStamp =
                    (document.get(LedgerDefine.TIME_STAMP) as Date).toString()
            }


            materialsDetails.loggedInID = document.get(LedgerDefine.LOGGED_IN_ID) as String

            var remark = document.get(LedgerDefine.REMARK)
            if (remark != null) materialsDetails.remarks = remark as String

            var imageLink = document.get(LedgerDefine.IMAGE_LINK)
            if (imageLink != null) materialsDetails.imageLink = imageLink as String

            var verified = document.get(LedgerDefine.VERIFIED)
            if (verified != null) materialsDetails.verified = verified as Boolean

            mMaterialsList.add(materialsDetails)
            mMasterMaterialsList.add(materialsDetails)
        }
    }

    private fun getStringFormDoc(document: QueryDocumentSnapshot?, key: String): String {
        var data: Any? = document!!.get(key) ?: return ""

        return data as String

    }

    override fun onLongClick(view: View?): Boolean {
        when (view!!.id) {
            R.id.tv_material_and_quantity -> showLinkedImage(view)
        }
        return true
    }

}







