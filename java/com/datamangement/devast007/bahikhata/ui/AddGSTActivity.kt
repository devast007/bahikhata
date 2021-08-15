package com.datamangement.devast007.bahikhata.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.R.layout.activity_add_gst
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.dialog.DialogFragmentToSelectUserOrProjectForGST
import com.datamangement.devast007.bahikhata.utils.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_add_gst.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class AddGSTActivity : AppCompatActivity(), View.OnClickListener,
    LedgerUtils.Companion.StatusListner {

    private val TAG = "AddGSTActivity"
    private var mContext: Context? = null
    var mSignInProfile: SignInProfile? = null

    var mSelectedProject: ProjectDetails? = null
    var mSelectedSender: UserDetails? = null
    var mSelectedReceiver: UserDetails? = null
    var mRate: Long = 0
    var mQualtity: Long = 0


    private var mEditType: Int = -1


    private val MATERIALS: Array<String> = arrayOf(
        "SAND(BALU)",
        "GITTI(METAL)",
        "CEMENT",
        "ROD(CHHAD)",
        "DIESEL",
        "BENTONITE POWDER",
        "BITUMINOUS",
        "GSB"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_add_gst)
        mContext = this
        supportActionBar!!.setTitle(R.string.add_gst)
        tv_project_id.setOnClickListener(this)
        tv_sender_id.setOnClickListener(this)
        tv_receiver_id.setOnClickListener(this)
        btn_save.setOnClickListener(this)


        setDataForMaterialType()
        mSignInProfile = LedgerUtils.signInProfile
        getUsersList()
        getProjectList()
        mEditType = intent.getIntExtra(LedgerDefine.TRANSACTION_EDIT_TYPE, -1)
        if (mEditType == LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY) {
            setDataToUpdate()
        } else {
            setDefaultDate()
        }
    }


    private fun setDataForMaterialType() {

        var adapter = ArrayAdapter<String>(
            mContext!!,
            android.R.layout.simple_dropdown_item_1line, MATERIALS
        )
        auto_tv_material.threshold = 1
        auto_tv_material.setAdapter(adapter)
        auto_tv_material.onFocusChangeListener =
            View.OnFocusChangeListener { v, hasFocus -> if (hasFocus) auto_tv_material.showDropDown() }
    }

    override fun onResume() {
        super.onResume()
        et_gst_amount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!et_gst_amount.isFocused) {
                    return
                }
                et_gst_percentage.setText("")
                if (s != null && s.isNotEmpty()) {
                    val gstAmount = s.toString().toFloat()
                    if (et_amount.text.isNotEmpty()) {
                        val billAmount = et_amount.text.toString().toFloat()
                        val percentage: Float = ((gstAmount * 100) / (billAmount - gstAmount))
                        et_gst_percentage.setText(String.format("%.2f", percentage))
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        et_gst_percentage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!et_gst_percentage.isFocused) {
                    return
                }
                et_gst_amount.setText("")
                if (s != null && s.isNotEmpty()) {
                    val percentage = s.toString().toFloat()
                    if (et_amount.text.isNotEmpty()) {
                        val billAmount = et_amount.text.toString().toFloat()
                        val taxableAmount = (billAmount * 100) / (100 + percentage)
                        val gstAmount: Float = (billAmount - taxableAmount)
                        et_gst_amount.setText(String.format("%.2f", gstAmount))
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        et_amount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!et_amount.isFocused) {
                    return
                }
                et_gst_percentage.setText("")
                et_gst_amount.setText("")
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

    }

    private var mGSTIdToUpdate: String? = null

    private var mSenderIdToUpdate: String? = null

    private fun setDataToUpdate() {
        mGSTIdToUpdate = intent.getStringExtra(LedgerDefine.GST_ID)
        var material = intent.getStringExtra(LedgerDefine.MATERIAL)
        var billAmount = intent.getStringExtra(LedgerDefine.GST_BILL_AMOUNT)
        var gstAmount = intent.getStringExtra(LedgerDefine.GST_TAX_AMOUNT)
        var gstPercentage = intent.getStringExtra(LedgerDefine.GST_TAX_PERCENTAGE)
        var projectID = intent.getStringExtra(LedgerDefine.PROJECT_ID)
        var senderId = intent.getStringExtra(LedgerDefine.SENDER_ID)
        var receiverId = intent.getStringExtra(LedgerDefine.RECEIVER_ID)
        var remarks = intent.getStringExtra(LedgerDefine.REMARK)
        var date = intent.getStringExtra(LedgerDefine.DATE)

        mSelectedProject = ProjectDetails()
        mSelectedProject!!.projectID = projectID
        tv_project_id.text = projectID

        auto_tv_material.setText(material)
        et_gst_amount.setText(gstAmount)
        et_gst_percentage.setText(gstPercentage)
        et_amount.setText(billAmount)


        mSelectedSender = UserDetails()
        mSelectedSender!!.userID = senderId!!
        mSenderIdToUpdate = senderId
        tv_sender_id.text = senderId
        tv_receiver_id.text = receiverId

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
        }
    }

    private fun saveBtnClicked() {
        hideKeyboard()
        var material = auto_tv_material.text.toString()
        var billAmount = et_amount.text.toString()
        var gstAmount = et_gst_amount.text.toString()
        var gstPercentage = et_gst_percentage.text.toString()
        var remarks = et_remarks.text.toString()

        val senderID = LedgerUtils.getUserAccount(tv_sender_id.text.toString())
        val receiverID = LedgerUtils.getUserAccount(tv_receiver_id.text.toString())


        if (isEmpty(material)) {
            toast(R.string.material_is_empty)
            return
        }

        if (isEmpty(billAmount)) {
            toast(R.string.amount_is_empty)
            return
        }

        if (isEmpty(gstAmount)) {
            toast(R.string.gst_amount_is_empty)
            return
        }

        if (mSelectedProject == null || isEmpty(mSelectedProject!!.projectID!!)) {
            toast(R.string.project_name_empty)
            return
        }

        if (senderID == null) {
            toast(R.string.sender_name_is_empty)
            return
        }

        if (receiverID == null) {
            toast(R.string.receiver_name_is_empty)
            return
        }


        var date: String? = getDateFormatted(
            et_day.text.toString(),
            et_month.text.toString(),
            et_year.text.toString()
        )
        // elvis operator if(date == null ) return null
            ?: return

        var gstMap: HashMap<String, Any> = HashMap<String, Any>()

        // loginId
        var loggedInID = mSignInProfile!!.userID

        gstMap[LedgerDefine.LOGGED_IN_ID] = loggedInID


        // material
        gstMap[LedgerDefine.MATERIAL] = material

        // rate
        gstMap[LedgerDefine.GST_BILL_AMOUNT] = billAmount

        // quantity
        gstMap[LedgerDefine.GST_TAX_AMOUNT] = gstAmount

        // amount
        gstMap[LedgerDefine.GST_TAX_PERCENTAGE] = gstPercentage


        //project
        gstMap[LedgerDefine.PROJECT_ID] = mSelectedProject!!.projectID!!

        //sender
        gstMap[LedgerDefine.SENDER_ID] = senderID

        //receiver
        gstMap[LedgerDefine.RECEIVER_ID] = receiverID

        //  date
        gstMap[LedgerDefine.DATE] = date!!

        // timestamp
        gstMap[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()


        //remarks
        gstMap[LedgerDefine.REMARK] = remarks

        //verified todo
        if (mEditType == LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY) {
            gstMap[LedgerDefine.GST_ID] = mGSTIdToUpdate!!
            updateGSTData(gstMap)
        } else {
            saveDataToFireStore(gstMap)
        }

    }

    private fun updateGSTData(transactionMap: HashMap<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        val companyName = LedgerSharePrefManger(mContext).getCompanyID()
        btn_save.isEnabled = false
        btn_save.setTextColor(Color.YELLOW)
        btn_save.setText(R.string.saving)
        var docRef =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_GST)
                .document(mGSTIdToUpdate!!)
        /* docRef.update(transactionMap)
             .addOnSuccessListener(OnSuccessListener<Void> {
                 btn_save.setTextColor(Color.parseColor("#FF7C7B7B"))
                 btn_save.setText(R.string.update_done)

             })
             .addOnFailureListener(OnFailureListener { e ->
                 Log.w(TAG, "Error writing document", e)
                 toast(R.string.error_09)
                 btn_save.setTextColor(Color.RED)
                 btn_save.setText(R.string.failed)
             })*/

        LedgerUtils.setstatusListner(this)
        LedgerUtils.setDataToFirestore(
            mContext!!, transactionMap[LedgerDefine.GST_ID]!!, SqlDBFile.CONTENT_URI_TABLE_GST,
            LedgerDefine.UPDATE_DATA,
            docRef,
            transactionMap
        )

    }

    private fun saveDataToFireStore(gstMap: HashMap<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        val companyName = LedgerSharePrefManger(mContext).getCompanyID()
        btn_save.isEnabled = false
        btn_save.setTextColor(Color.YELLOW)
        btn_save.setText(R.string.saving)
        var docRef =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_GST)
                .document()
        gstMap[LedgerDefine.GST_ID] = docRef.id
        /*  docRef.set(gstMap)
              .addOnSuccessListener(OnSuccessListener<Void> {
                  btn_save.setTextColor(Color.parseColor("#FF7C7B7B"))
                  btn_save.setText(R.string.saved)
                  updateGstAmountForUser(
                      gstMap[LedgerDefine.SENDER_ID],
                      gstMap[LedgerDefine.GST_BILL_AMOUNT]
                  )
                  showSnackBar()

              })
              .addOnFailureListener(OnFailureListener { e ->
                  Log.w(TAG, "Error writing document", e)
                  toast(R.string.error_09)
                  btn_save.setTextColor(Color.RED)
                  btn_save.setText(R.string.failed)
              })*/

        LedgerUtils.setstatusListner(this)
        LedgerUtils.setDataToFirestore(
            mContext!!,
            gstMap[LedgerDefine.GST_ID]!!,
            SqlDBFile.CONTENT_URI_TABLE_GST,
            LedgerDefine.SET_DATA,
            docRef,
            gstMap
        )
        updateGstAmountForUser(gstMap[LedgerDefine.SENDER_ID], gstMap[LedgerDefine.GST_BILL_AMOUNT])

    }

/*override fun onBackPressed() {
    val intent = Intent(this, GoogleSigninActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
    finish()
}*/

    private fun updateGstAmountForUser(userID: Any?, gstAmount: Any?) {
        var id = userID.toString().substring(2)

        var basicAmount: Long = 0
        for (user in mSenderList!!) {
            if (id == user.userID) {
                if (user.p_gstBill != null) {
                    basicAmount = user.p_gstBill
                }
                break
            }
        }
        val amount = gstAmount.toString().toLong() + basicAmount

        val db = FirebaseFirestore.getInstance()
        val companyName = LedgerSharePrefManger(mContext).getCompanyID()
        var docRef =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_USERS)
                .document(id)
        docRef.update(LedgerDefine.P_GST_BILL, amount)
            .addOnSuccessListener(OnSuccessListener<Void> {
                toast(R.string.gst_updated_for_user)
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
                toast(R.string.error_09)
            })

    }

    private fun showSnackBar() {
        val snackbar = Snackbar
            .make(coordinatorLayout, R.string.saved_successfully, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.add_more) {
                btn_save.isEnabled = true
                btn_save.setTextColor(Color.BLACK)
                btn_save.setText(R.string.save)
            }
        snackbar.setActionTextColor(Color.BLUE)
        val sbView = snackbar.view
        sbView.setBackgroundColor(Color.parseColor("#FFF851A7"))
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
        var dFragment = DialogFragmentToSelectUserOrProjectForGST()
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
        /*  val isAdmin = mSignInProfile!!.isAdmin
          val isSuperVisor = mSignInProfile!!.isSupervisor
          val isNormalUser = mSignInProfile!!.isNormal
          val accessedProjects = mSignInProfile!!.accesibleProjects*/


        for (document in task.result!!) {
            Log.d(TAG, document.id + " => " + document.data)
            // senderList

            val tempName = document.get(LedgerDefine.NAME)
            val tempId = document.get(LedgerDefine.USER_ID)
            val tempDesignation = document.get(LedgerDefine.DESIGNATION)
            val tempIsAdmin = document.get(LedgerDefine.IS_ADMIN)
            val tempAccessedProjects = document.get(LedgerDefine.ACCESSIBLE_PROJECTS)
            val userAccounts = document.get(LedgerDefine.ACCOUNTS)
            val personalGstBillAmount = document.get(LedgerDefine.P_GST_BILL)
            val userDetails = UserDetails()

            if (tempName != null) userDetails.name = tempName as String

            if (tempId != null) userDetails.userID = (tempId as Long).toString()
            if (tempDesignation != null) userDetails.designation = tempDesignation as Long

            if (tempIsAdmin != null) {
                if (tempIsAdmin as Boolean) userDetails.designation = LedgerDefine.DESIGNATION_ADMIN
            }

            if (personalGstBillAmount != null) {
                userDetails.p_gstBill = personalGstBillAmount as Long
            }
            if (tempAccessedProjects != null) {
                userDetails.accesibleProjectsList = tempAccessedProjects as ArrayList<String>
            }
            if (userAccounts != null) {
                userDetails.userAccounts = userAccounts as ArrayList<String>
            }
            // if (isAdmin) {
            if (mEditType === LedgerDefine.TRANSACTION_EDIT_TYPE_MODIFY) {
                if (tempId == mSenderIdToUpdate) {
                    mSelectedSender = userDetails
                    tv_sender_id.text = userDetails.userID + "\n" + userDetails.name
                }
            }
            mSenderList!!.add(userDetails)
            mReceiverList!!.add(userDetails)

            /*} else if (isSuperVisor) {
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
}*/

    var mSenderList: ArrayList<UserDetails>? = ArrayList<UserDetails>()
    var mReceiverList: ArrayList<UserDetails>? = ArrayList<UserDetails>()
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

}
