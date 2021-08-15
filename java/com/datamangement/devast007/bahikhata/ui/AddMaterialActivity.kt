package com.datamangement.devast007.bahikhata.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.dialog.DialogFragmentToSelectUserOrProjectForMaterial
import com.datamangement.devast007.bahikhata.utils.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_material.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToLong


class AddMaterialActivity : AppCompatActivity(), View.OnClickListener,
    LedgerUtils.Companion.StatusListner, View.OnLongClickListener {

    private var mImageLink: String? = ""
    private var mImageLinkToUpdate: String? = ""
    private val PICK_IMAGE_REQUEST: Int = 777
    private var mMaterialOrServiceArrayList: ArrayAdapter<String>? = null
    private var mServiceMediumList: ArrayAdapter<String>? = null
    private var mUnitsList: ArrayAdapter<String>? = null
    private var mServiceTypesList: HashMap<String, ArrayAdapter<String>>? =
        HashMap<String, ArrayAdapter<String>>()
    private val SENDER_ACCOUNT: Int = 1
    private val RECEIVER_ACCOUNT: Int = 2
    private val TAG = "AddMaterialActivity"
    private var mContext: Context? = null
    var mSignInProfile: SignInProfile? = null

    var mSelectedProject: ProjectDetails? = null
    var mSelectedSender: UserDetails? = null
    var mSelectedReceiver: UserDetails? = null

    private var mEditType: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_material)
        mContext = this
        supportActionBar!!.setTitle(R.string.add_material)
        tv_project_id.setOnClickListener(this)
        tv_sender_id.setOnClickListener(this)
        tv_receiver_id.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        btn_material_or_service.setOnClickListener(this)
        btn_types_of_service.setOnClickListener(this)
        btn_medium_of_service.setOnClickListener(this)
        btn_rate_unit.setOnClickListener(this)
        imgBtn_remarks_history.setOnClickListener(this)
        imgBtn_vehicle_no_history.setOnClickListener(this)
        img_btn_suggestion_subcategroy.setOnClickListener(this)
        btn_upload_image.setOnClickListener(this)
        img_btn_suggestion_subcategroy.setOnLongClickListener(this)
        imgBtn_remarks_history.setOnLongClickListener(this)

        btn_medium_of_service.setOnLongClickListener(this)

        mSignInProfile = LedgerUtils.signInProfile
        getUsersList()
        getProjectList()
        getMaterialOrServiceList()
        getServiceMediumList()
        getUnitsList()
        mEditType = intent.getIntExtra(LedgerDefine.TRANSACTION_EDIT_TYPE, -1)
        if (mEditType == LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY) {
            setDataToUpdate()
        } else {
            setDefaultDate()
        }
        var filePath = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM)
        if (filePath != null) {
            uploadImageToFireStore(filePath as Uri)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (!isEmpty(mImageLink)) {
            FirebaseStorage.getInstance().reference.child(mImageLink!!).delete()
        }
    }

    private fun getUnitsList() {
        var db = FirestoreDataBase().db
        mUnitsList =
            ArrayAdapter<String>(mContext!!, android.R.layout.select_dialog_item)
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_UNITS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val id = document.get(LedgerDefine.ID) as String
                        val name = document.get(LedgerDefine.NAME) as String
                        mUnitsList!!.add("$name $id")
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getServiceMediumList() {
        var db = FirestoreDataBase().db
        mServiceMediumList =
            ArrayAdapter<String>(mContext!!, android.R.layout.select_dialog_item)
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MEDIUM)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val id = document.get(LedgerDefine.ID) as String
                        val name = document.get(LedgerDefine.NAME) as String
                        mServiceMediumList!!.add("$name $id")
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getMaterialOrServiceList() {
        var db = FirestoreDataBase().db

        mMaterialOrServiceArrayList =
            ArrayAdapter<String>(mContext!!, android.R.layout.select_dialog_item)


        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIAL_AND_SERVICES_SETTINGS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val id = document.get(LedgerDefine.MATERIAL_OR_SERVICE_ID) as String
                        val name = document.get(LedgerDefine.MATERIAL_OR_SERVICE_NAME) as String
                        mMaterialOrServiceArrayList!!.add("$name $id")
                        getServiceTypesList(id)
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getServiceTypesList(materialOrServiceId: String) {

        var db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(
            LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIAL_AND_SERVICES_SETTINGS
                    + "/$materialOrServiceId" + LedgerDefine.SLASH_SERVICE_TYPES
        )
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    var tempAdapter =
                        ArrayAdapter<String>(mContext!!, android.R.layout.select_dialog_item)
                    for (document in task.result!!) {
                        val id = document.get(LedgerDefine.ID) as String
                        val name = document.get(LedgerDefine.NAME) as String
                        tempAdapter!!.add("$name $id")
                    }
                    if (!tempAdapter.isEmpty) mServiceTypesList?.set(
                        materialOrServiceId,
                        tempAdapter!!
                    )
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun chooseMaterialOrService() {
        val builderSingle = AlertDialog.Builder(mContext)
        builderSingle.setTitle(R.string.choose_material_or_service)

        builderSingle.setNegativeButton(
            R.string.cancel
        ) { dialog, which -> dialog.dismiss() }

        builderSingle.setAdapter(
            mMaterialOrServiceArrayList
        ) { dialog, which ->
            val strName = mMaterialOrServiceArrayList!!.getItem(which)
            btn_material_or_service.text = getSpannableString(strName!!)
            val subStr = strName!!.split(" ")
            val size = subStr.size
            chooseServiceType(subStr[size - 1])

        }
        builderSingle.show()
    }

    private fun chooseServiceType(id: String) {

        var materialOrServiceID = id
        if (TextUtils.isEmpty(materialOrServiceID)) {
            val text = btn_material_or_service.text
            if (text.isEmpty()) {
                return
            }
            var subStr = text.split(" ")
            materialOrServiceID = subStr[subStr.size - 1]
        }
        btn_types_of_service.isEnabled = true
        Log.e("Exception", " mServiceTypesList = " + mServiceTypesList)
        Log.e("Exception", " mServiceTypesList keys  = " + mServiceTypesList!!.keys)

        if (mServiceTypesList!!.keys.contains(materialOrServiceID)) {
            val adapter = mServiceTypesList!![materialOrServiceID!!]
            val builderSingle = AlertDialog.Builder(mContext)
            builderSingle.setTitle(R.string.choose_service_type)

            builderSingle.setNegativeButton(
                R.string.cancel
            ) { dialog, which -> dialog.dismiss() }

            builderSingle.setAdapter(
                adapter
            ) { dialog, which ->
                val strName = adapter!!.getItem(which)
                btn_types_of_service.text = getSpannableString(strName!!)
            }
            builderSingle.show()
        } else {
            btn_types_of_service.isEnabled = false
            btn_types_of_service.text = ""
        }

    }


    override fun onResume() {
        super.onResume()
        et_rate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                tv_amount.setText("")
                if (s != null && s.isNotEmpty()) {
                    val rate = s.toString().toBigDecimal()
                    if (et_quantity.text.isNotEmpty()) {
                        val quantity = et_quantity.text.toString().toBigDecimal()
                        val amount = (rate.toDouble() * quantity.toDouble()).roundToLong()
                        tv_amount.text = "" + amount
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        et_quantity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                tv_amount.setText("")
                if (s != null && s.isNotEmpty()) {
                    val quantity = s.toString().toDouble()
                    if (et_rate.text.isNotEmpty()) {
                        val rate = et_rate.text.toString().toDouble()
                        val amount: Long = (rate.toDouble() * quantity.toDouble()).roundToLong()
                        tv_amount.text = "" + amount
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
    }

    private var mMaterialIdToUpdate: String? = null

    private var mReceiverIdToUpdate: String? = null
    private var mSenderIdToUpdate: String? = null

    private fun setDataToUpdate() {
        mMaterialIdToUpdate = intent.getStringExtra(LedgerDefine.MATERIAL_ID)
        var material = intent.getStringExtra(LedgerDefine.MATERIAL)
        var rate = intent.getStringExtra(LedgerDefine.RATE)
        var quantity = intent.getStringExtra(LedgerDefine.QUANTITY)
        var amount = intent.getStringExtra(LedgerDefine.AMOUNT)
        var projectID = intent.getStringExtra(LedgerDefine.PROJECT_ID)
        var senderId = intent.getStringExtra(LedgerDefine.SENDER_ID)
        var receiverId = intent.getStringExtra(LedgerDefine.RECEIVER_ID)

        var remarks = intent.getStringExtra(LedgerDefine.REMARK)
        var date = intent.getStringExtra(LedgerDefine.DATE)

        var serviceType = intent.getStringExtra(LedgerDefine.SERVICE_TYPE)
        var medium = intent.getStringExtra(LedgerDefine.SERVICE_MEDIUM)
        var vehicleNo = intent.getStringExtra(LedgerDefine.VEHICLE_NO)
        var challanNo = intent.getStringExtra(LedgerDefine.CHALLAN_NO)
        var unit = intent.getStringExtra(LedgerDefine.UNIT)
        var subcategroy = intent.getStringExtra(LedgerDefine.SUBCATEGORY)
        mImageLinkToUpdate = intent.getStringExtra(LedgerDefine.IMAGE_LINK)
        mSelectedProject = ProjectDetails()
        mSelectedProject!!.projectID = projectID
        tv_project_id.text = projectID


        et_rate.setText(rate)
        et_quantity.setText(quantity)
        tv_amount.setText(amount)

        btn_material_or_service.setText(material)
        btn_types_of_service.setText(serviceType)
        btn_medium_of_service.setText(medium)
        et_vehicle_no.setText(vehicleNo)
        et_challan_no.setText(challanNo)
        btn_rate_unit.setText(unit)
        et_auto_complete_subcategory.setText(subcategroy)


        mSelectedReceiver = UserDetails()
        mSelectedReceiver!!.userID = receiverId!!
        mReceiverIdToUpdate = receiverId
        tv_receiver_id.text = receiverId

        mSelectedSender = UserDetails()
        mSelectedSender!!.userID = senderId!!
        mSenderIdToUpdate = senderId
        tv_sender_id.text = senderId

        et_remarks.setText(remarks)

        et_day.setText(date!!.substring(6, 8))
        et_month.setText(date!!.substring(4, 6))
        et_year.setText(date!!.substring(0, 4))
    }

    private fun manageProjects(task: Task<QuerySnapshot>) {

        for (document in task.result!!) {
            var project: ProjectDetails = ProjectDetails()
            project.projectID = document.get(LedgerDefine.PROJECT_ID) as String
            project.name = document.get(LedgerDefine.NAME) as String
            mProjectList!!.add(project)
        }
    }

    private fun setDefaultDate() {
        val date = Calendar.getInstance().time
        et_day.setText(SimpleDateFormat("dd").format(date))
        et_month.setText(SimpleDateFormat("MM").format(date))
        et_year.setText(SimpleDateFormat("yyyy").format(date))
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tv_project_id -> openDialog(LedgerDefine.SELECTION_TYPE_PROJECT)
            R.id.tv_sender_id -> openDialog(LedgerDefine.SELECTION_TYPE_SENDER)
            R.id.tv_receiver_id -> openDialog(LedgerDefine.SELECTION_TYPE_RECEIVER)
            R.id.btn_save -> saveBtnClicked()
            R.id.btn_material_or_service -> chooseMaterialOrService()
            R.id.btn_types_of_service -> chooseServiceType("")
            R.id.btn_medium_of_service -> chooseMediumOfService()
            R.id.btn_rate_unit -> chooseUnit()
            R.id.imgBtn_remarks_history -> openAlertDilog(LedgerDefine.SELECTION_TYPE_REMARKS)
            R.id.imgBtn_vehicle_no_history -> openAlertDilog(LedgerDefine.SELECTION_TYPE_VEHICLE_NO)
            R.id.img_btn_suggestion_subcategroy -> openDialogForSubcategory()
            R.id.btn_upload_image -> chooseImageToUpload()
        }
    }

    private fun chooseImageToUpload() {

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
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
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

        /*  uploadTask2.addOnSuccessListener { //if the upload is successfull
              mImageLink = it.metadata!!.path
              btn_upload_image.text = "Uploading Done!"
          }
              .addOnFailureListener(OnFailureListener {   //if the upload is not successfull
                  btn_upload_image.text = "Uploading FAILED!!!!!!!!!"
                  btn_upload_image.setBackgroundColor(Color.RED)
              })
              .addOnProgressListener { taskSnapshot ->
                  val progress: Double =
                      100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                  btn_upload_image.text = "Uploaded $progress%"
              }*/


        uploadTask2.addOnProgressListener { taskSnapshot ->
            val progress: Double =
                100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            btn_upload_image.text = "Uploaded $progress%"
        }.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            riversRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mImageLink = task.result.toString()
                btn_upload_image.text = "Uploading Done!"
                Log.d(TAG, " task.result.toString() " + task.result.toString())
            } else {
                // Handle failures
                // ...
            }
        }

    }

    private fun openDialogForSubcategory() {

        // here supplier will act like receiver in transaction of amount
        val supplierId = LedgerUtils.getUserAccount(tv_sender_id.text.toString())

        if (!isEmpty(supplierId)) {

            var db = FirestoreDataBase().db

            val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)
                .whereEqualTo(LedgerDefine.RECEIVER_ID, supplierId)
                .orderBy(LedgerDefine.TRANSACTION_DATE, Query.Direction.DESCENDING)
                .limit(15)
                .get()
                .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                    if (task.isSuccessful) {
                        var adapter =
                            ArrayAdapter<String>(mContext!!, android.R.layout.select_dialog_item)
                        for (document in task.result!!) {
                            var text = document!!.get(LedgerDefine.SUBCATEGORY).toString()
                            if (!isEmpty(text)) {
                                adapter.remove(text)
                                adapter.add(text)
                            }
                        }
                        if (!adapter.isEmpty) {
                            val builderSingle = AlertDialog.Builder(mContext)
                            builderSingle.setTitle(R.string.sub_category)
                            builderSingle.setNegativeButton(
                                R.string.cancel
                            ) { dialog, which -> dialog.dismiss() }

                            builderSingle.setAdapter(
                                adapter
                            ) { dialog, which ->
                                val strName = adapter!!.getItem(which)
                                et_auto_complete_subcategory.setText(strName)
                            }
                            builderSingle.show()

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

    private fun openAlertDilog(type: Int) {
        var et = et_vehicle_no
        var field = LedgerDefine.VEHICLE_NO
        var title = R.string.select_vehicle_no
        when (type) {
            LedgerDefine.SELECTION_TYPE_VEHICLE_NO -> {
                et = et_vehicle_no
                field = LedgerDefine.VEHICLE_NO
                title = R.string.select_vehicle_no
            }
            LedgerDefine.SELECTION_TYPE_REMARKS -> {
                et = et_remarks
                field = LedgerDefine.REMARK
                title = R.string.select_remarks
            }
        }
        val supplier = LedgerUtils.getUserAccount(tv_sender_id.text.toString())

        if (!isEmpty(supplier)) {
            var db = FirestoreDataBase().db
            val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()

            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIALS_OR_SERVICES)
                .whereEqualTo(LedgerDefine.SENDER_ID, supplier)
                .orderBy(LedgerDefine.DATE, Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                    if (task.isSuccessful) {
                        var adapter =
                            ArrayAdapter<String>(mContext!!, android.R.layout.select_dialog_item)
                        for (document in task.result!!) {
                            var text = document!!.get(field).toString()
                            if (!isEmpty(text)) {
                                adapter.remove(text)
                                adapter.add(text)
                            }
                        }
                        if (!adapter.isEmpty) {
                            val builderSingle = AlertDialog.Builder(mContext)
                            builderSingle.setTitle(title)
                            builderSingle.setNegativeButton(
                                R.string.cancel
                            ) { dialog, which -> dialog.dismiss() }

                            builderSingle.setAdapter(
                                adapter
                            ) { dialog, which ->
                                val strName = adapter!!.getItem(which)
                                et.setText(strName)
                            }
                            builderSingle.show()

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

    private fun chooseUnit() {
        val builderSingle = AlertDialog.Builder(mContext)
        builderSingle.setTitle(R.string.choose_unit)
        builderSingle.setNegativeButton(
            R.string.cancel
        ) { dialog, which -> dialog.dismiss() }

        builderSingle.setAdapter(
            mUnitsList
        ) { dialog, which ->
            val strName = mUnitsList!!.getItem(which)
            btn_rate_unit.text = getSpannableString(strName!!)
        }
        builderSingle.show()
    }

    private fun chooseMediumOfService() {
        val builderSingle = AlertDialog.Builder(mContext)
        builderSingle.setTitle(R.string.choose_medium_of_service)
        builderSingle.setNegativeButton(
            R.string.cancel
        ) { dialog, which -> dialog.dismiss() }

        builderSingle.setAdapter(
            mServiceMediumList
        ) { dialog, which ->
            val strName = mServiceMediumList!!.getItem(which)
            btn_medium_of_service.text = getSpannableString(strName!!)
        }
        builderSingle.show()
    }

    fun getId(strName: String): String {
        val subStr = strName!!.split(" ")
        val size = subStr.size
        return subStr[size - 1]
    }

    private fun saveBtnClicked() {
        hideKeyboard()
        val senderID = LedgerUtils.getUserAccount(tv_sender_id.text.toString())
        val receiverID = LedgerUtils.getUserAccount(tv_receiver_id.text.toString())
        var materialOrService = ""
        var serviceType = ""
        var medium = ""
        var unit = ""

        var temp = "" + btn_material_or_service.text
        if (isEmpty(temp)) {
            toast(R.string.material_is_empty)
            return
        } else {
            materialOrService = getId(temp)
        }

        temp = "" + btn_types_of_service.text
        if (!isEmpty(temp)) {
            serviceType = getId(temp)
        }

        temp = "" + btn_medium_of_service.text
        if (!isEmpty(temp)) {
            medium = getId(temp)
        }

        temp = "" + btn_rate_unit.text
        if (isEmpty(temp)) {
            toast(R.string.unit_is_empty)
            return
        } else {
            unit = getId(temp)
        }


        var quantiy = et_quantity.text.toString()
        var rate = et_rate.text.toString()
        var amount = tv_amount.text.toString()
        var remarks = et_remarks.text.toString()

        var vehicleNo = et_vehicle_no.text.toString()
        var challanNo = et_challan_no.text.toString()
        var subcategory = et_auto_complete_subcategory.text.toString()


        if (isEmpty(rate)) {
            toast(R.string.rate_is_empty)
            return
        }

        if (isEmpty(quantiy)) {
            toast(R.string.quantity_is_empty)
            return
        }

        if (isEmpty(amount)) {
            toast(R.string.amount_is_empty)
            return
        }

        if (mSelectedProject == null || isEmpty(mSelectedProject!!.projectID!!)) {
            toast(R.string.project_name_empty)
            return
        }

        if (isEmpty(senderID)) {
            toast(R.string.supplier_name_empty)
            return
        }

        if (isEmpty(receiverID)) {
            toast(R.string.reproter_name_empty)
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
            ?: return

        var materialMap: HashMap<String, Any> = HashMap<String, Any>()

        // loginId
        var loggedInID = mSignInProfile!!.userID

        materialMap[LedgerDefine.LOGGED_IN_ID] = loggedInID

        // material
        materialMap[LedgerDefine.MATERIAL] = materialOrService

        //service type
        materialMap[LedgerDefine.SERVICE_TYPE] = serviceType

        //service medium
        materialMap[LedgerDefine.SERVICE_MEDIUM] = medium

        //vehicle number
        materialMap[LedgerDefine.VEHICLE_NO] = vehicleNo

        //challan no
        materialMap[LedgerDefine.CHALLAN_NO] = challanNo

        // rate
        materialMap[LedgerDefine.UNIT] = unit

        // rate
        materialMap[LedgerDefine.RATE] = rate

        // quantity
        materialMap[LedgerDefine.QUANTITY] = quantiy

        // amount
        materialMap[LedgerDefine.AMOUNT] = amount

        //project
        materialMap[LedgerDefine.PROJECT_ID] = mSelectedProject!!.projectID!!

        //subcategory
        materialMap[LedgerDefine.SUBCATEGORY] = subcategory

        //sender
        materialMap[LedgerDefine.SENDER_ID] = senderID!!

        //receiver
        materialMap[LedgerDefine.RECEIVER_ID] = receiverID!!


        //  date
        materialMap[LedgerDefine.DATE] = date!!

        // timestamp
        materialMap[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()

        //remarks
        materialMap[LedgerDefine.REMARK] = remarks

        //remarks
        materialMap[LedgerDefine.VERIFIED] = false

        if (!isEmpty(mImageLinkToUpdate) && !isEmpty(mImageLink)) {
            FirebaseStorage.getInstance().getReferenceFromUrl(mImageLinkToUpdate!!).delete()
        }

        if (isEmpty(mImageLink)) {
            mImageLink = mImageLinkToUpdate
        }
        // link of image
        if (!isEmpty(mImageLink))
            materialMap[LedgerDefine.IMAGE_LINK] = mImageLink!!

        //verified todo
        if (mEditType == LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY) {
            updateMaterialData(materialMap)
        } else {
            saveDataToFireStore(materialMap)
        }

    }

    private fun updateMaterialData(materialMap: HashMap<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        val companyName = LedgerSharePrefManger(mContext).getCompanyID()
        btn_upload_image.isEnabled = false
        btn_save.isEnabled = false
        btn_save.setTextColor(Color.YELLOW)
        btn_save.setText(R.string.saving)
        materialMap[LedgerDefine.MATERIAL_ID] = mMaterialIdToUpdate!!
        var docRef =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_MATERIALS_OR_SERVICES)
                .document(mMaterialIdToUpdate!!)

        LedgerUtils.setstatusListner(this)
        LedgerUtils.setDataToFirestore(
            mContext!!, materialMap[LedgerDefine.MATERIAL_ID]!!,
            SqlDBFile.CONTENT_URI_TABLE_MATERIALS, LedgerDefine.UPDATE_DATA, docRef, materialMap
        )

    }

    private fun saveDataToFireStore(materialMap: HashMap<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        val companyName = LedgerSharePrefManger(mContext).getCompanyID()
        btn_save.isEnabled = false
        btn_save.setTextColor(Color.YELLOW)
        btn_save.setText(R.string.saving)
        var docRef =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_MATERIALS_OR_SERVICES)
                .document()
        materialMap[LedgerDefine.MATERIAL_ID] = docRef.id

        LedgerUtils.setstatusListner(this)
        LedgerUtils.setDataToFirestore(
            mContext!!, materialMap[LedgerDefine.MATERIAL_ID]!!,
            SqlDBFile.CONTENT_URI_TABLE_MATERIALS, LedgerDefine.SET_DATA, docRef, materialMap
        )
        updateMaterialAmountForUser(
            materialMap[LedgerDefine.SENDER_ID],
            materialMap[LedgerDefine.AMOUNT]
        )

    }

    private fun updateMaterialAmountForUser(userID: Any?, materialCost: Any?) {
        var id = userID.toString().substring(2)

        var basicAmount: Long = 0
        for (user in mSenderList!!) {
            if (id == user.userID) {
                if (user.p_MaterialCost != null) {
                    basicAmount = user.p_MaterialCost
                }
                break
            }
        }
        val amount = materialCost.toString().toLong() + basicAmount

        val db = FirebaseFirestore.getInstance()
        val companyName = LedgerSharePrefManger(mContext).getCompanyID()
        var docRef =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_USERS)
                .document(id)
        docRef.update(LedgerDefine.P_MATERIAL_COST, amount)
            .addOnSuccessListener(OnSuccessListener<Void> {
                toast(R.string.material_cost_updated_for_user)
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
                toast(R.string.error_09)
            })

    }

    private fun showSnackBar() {

        et_vehicle_no.setText("")
        et_challan_no.setText("")
        et_quantity.setText("")
        tv_amount.text = ""
        et_remarks.setText("")
        mImageLink = ""
        val snackbar = Snackbar
            .make(coordinatorLayout, R.string.saved_successfully, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.add_more) {
                // btn_medium_of_service.text = ""
                btn_upload_image.isEnabled = true
                btn_save.isEnabled = true
                btn_save.setTextColor(Color.BLACK)
                btn_save.setText(R.string.save)
            }
        snackbar.setActionTextColor(Color.BLUE)
        val sbView = snackbar.view
        sbView.setBackgroundColor(Color.YELLOW)
        val textView =
            sbView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.BLACK)
        snackbar.show()
    }

    private fun getDateFormatted(dd: String, mm: String, yyyy: String): String? {
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
        var dFragment = DialogFragmentToSelectUserOrProjectForMaterial()
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
        /* val isAdmin = mSignInProfile!!.isAdmin
         val isSuperVisor = mSignInProfile!!.isSupervisor
         val isNormalUser = mSignInProfile!!.isNormal
         val accessedProjects = mSignInProfile!!.accesibleProjects*/


        for (document in task.result!!) {
            Log.d(TAG, document.id + " => " + document.data)
            Log.d(TAG, " document.get(\"name\")+ => " + document.get("name"))
            // senderList

            val tempName = document.get(LedgerDefine.NAME)
            val tempId = document.get(LedgerDefine.USER_ID)
            val tempDesignation = document.get(LedgerDefine.DESIGNATION)
            val tempIsAdmin = document.get(LedgerDefine.IS_ADMIN)
            val tempAccessedProjects = document.get(LedgerDefine.ACCESSIBLE_PROJECTS)
            val userAccounts = document.get(LedgerDefine.ACCOUNTS)
            val materialCost = document.get(LedgerDefine.P_MATERIAL_COST)
            val userDetails = UserDetails()

            if (tempName != null) userDetails.name = tempName as String

            if (tempId != null) userDetails.userID = (tempId as Long).toString()

            if (tempDesignation != null) userDetails.designation = tempDesignation as Long

            if (materialCost != null) {
                userDetails.p_MaterialCost = materialCost as Long
            }

            if (tempIsAdmin != null) {
                if (tempIsAdmin as Boolean) userDetails.designation = LedgerDefine.DESIGNATION_ADMIN
            }

            if (userAccounts != null) {
                userDetails.userAccounts = userAccounts as ArrayList<String>
            }
            if (tempAccessedProjects != null) {
                userDetails.accesibleProjectsList = tempAccessedProjects as ArrayList<String>
            }

//            if (isAdmin) {
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

            /*          } else if (isSuperVisor) {
                          addToReceiverList(userDetails, accessedProjects!!)
                      }*/
        }
        /*if (isSuperVisor) {
            val senderData = UserDetails()
            senderData.userID = mSignInProfile!!.userID
            senderData.name = mSignInProfile!!.name
            senderData.designation = LedgerDefine.DESIGNATION_SUPERVISOR
            mSenderList!!.add(senderData)
            tv_sender_id.setText(senderData.userID + "\n" + senderData.name)
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
    }
*/
    var mSenderList: ArrayList<UserDetails>? = ArrayList<UserDetails>()
    var mReceiverList: ArrayList<UserDetails>? = ArrayList<UserDetails>()
    var mBankAccountList: ArrayList<BankAccountDetail>? = ArrayList<BankAccountDetail>()
    var mProjectList: ArrayList<ProjectDetails>? = ArrayList<ProjectDetails>()

    private fun hideKeyboard() {
        val imm = mContext!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = currentFocus
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

    override fun onLongClick(p0: View?): Boolean {
        when (p0!!.id) {
            R.id.btn_medium_of_service -> btn_medium_of_service.text = ""
            R.id.imgBtn_remarks_history -> et_remarks.setText("")
            R.id.img_btn_suggestion_subcategroy -> et_auto_complete_subcategory.setText("")
            R.id.imgBtn_remarks_history -> et_remarks.setText("")

        }
        return true
    }

    private fun getSpannableString(str: String): SpannableString {

        var subStr = str.split(" ")
        var lastStr = subStr[subStr.size - 1]
        val lastIndex = str.indexOf(lastStr)

        val bold = StyleSpan(Typeface.BOLD)
        val normal = StyleSpan(Typeface.NORMAL)

        val content = SpannableString(str)

        content.setSpan(
            ForegroundColorSpan(Color.LTGRAY),
            lastIndex,
            str.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        return content
    }

}
