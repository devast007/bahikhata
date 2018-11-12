package com.datamangement.devast007.bahikhata.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_add_users.*

class AddUsersActivity : AppCompatActivity(), View.OnClickListener {


    val TAG = "AddUsersActivity"
    val mContext = this
    var accessProjects: ArrayList<String>? = null
    var mNormalId: String? = null
    var mSupervisorId: String? = null
    val mDB = FirebaseFirestore.getInstance()
    var mCompanyName: String? = null
    private val BASE_NORMAL_USER_ID: Long = 6010000
    private val DIFF_NORMAL_SUP_ID: Long = 1000000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_users)
        toolbar.setTitle(R.string.users)
        setSupportActionBar(toolbar)
        btn_save.setOnClickListener(this)
        btn_project_access.setOnClickListener(this)

        mCompanyName = LedgerSharePrefManger(mContext).getCompanyName()
        fetchUserID()
    }


    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.btn_save -> saveUser()
            R.id.btn_project_access -> launProjectSelectActivity()
        }
    }

    private val REQUEST_CODE: Int = 1 ;

    private fun launProjectSelectActivity() {
        var intent:Intent = Intent(mContext, ProjectSelectForUserActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE)
    }

    private var mProjectIDArrayList: ArrayList<String>? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            if(REQUEST_CODE == requestCode){
                var bundle = data!!.extras
                mProjectIDArrayList = bundle.getStringArrayList(LedgerDefine.ALL_PROJECTS)
                btn_project_access.setText(mProjectIDArrayList.toString())
            }
        }
    }
    private fun fetchUserID() {
        var docRef = mDB.collection(LedgerDefine.COMPANIES_SLASH+mCompanyName+"/users")
            .orderBy(LedgerDefine.USER_ID, Query.Direction.DESCENDING).limit(1)
        docRef.get()
            .addOnSuccessListener(OnSuccessListener<QuerySnapshot> {
                Log.d(
                    TAG,
                    "DataFetched data =  " + it
                )
                var tempUserId: Long = it.documents.get(0).get(LedgerDefine.USER_ID).toString().toLong()
                Log.d(
                    TAG,
                    "DataFetched tempUserId =  " + tempUserId
                )



                if (tempUserId!! >= BASE_NORMAL_USER_ID) {
                    mNormalId = "" + (tempUserId + 1)
                    mSupervisorId = "" + (tempUserId + 1 - DIFF_NORMAL_SUP_ID)
                } else {
                    mNormalId = "" + BASE_NORMAL_USER_ID
                    mSupervisorId = "" + (BASE_NORMAL_USER_ID - DIFF_NORMAL_SUP_ID)
                }

                tv_user_id.setText(String.format(getString(R.string.new_user_id), mNormalId))
                checkbox_supervisor.setText(String.format(getString(R.string.new_user_id), mSupervisorId))

            })
            .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error writing document", e) })
    }

    private fun saveUser() {
        Log.d(TAG, "saveUser ")
        val userName = et_user_name.text.toString()
        val userAddress = et_user_address.text.toString()
        val userEmail = et_user_email.text.toString()
        val userPhone = et_user_phone.text.toString()
        val remarks = et_user_remarks.text.toString()

        if (isEmpty(mNormalId)) {
            Toast.makeText(mContext, R.string.error_03, Toast.LENGTH_LONG).show()
            return
        }
        if (isEmpty(userName)) {
            et_user_name.error = getString(R.string.error_06)
            Toast.makeText(mContext, R.string.error_06, Toast.LENGTH_LONG).show()
            return
        }

        var users = mDB.collection(LedgerDefine.COMPANIES_SLASH+mCompanyName+"/users");
        var docRefNor = users.document(this!!.mNormalId!!)
        Log.d(TAG, "docRef " + docRefNor)
        // [START set_document]
        val user = HashMap<String, Any>()
        user.put(LedgerDefine.USER_ID, this!!.mNormalId!!)
        user.put(LedgerDefine.NAME, userName)
        user.put(LedgerDefine.ADDRESS, userAddress)
        user.put(LedgerDefine.EMAIL, userEmail)
        user.put(LedgerDefine.PHONE_NUMBER, userPhone)
        user.put(LedgerDefine.REMARK, remarks)
        user.put(LedgerDefine.DESIGNATION, LedgerDefine.DESIGNATION_NORMAL)
        user.put(LedgerDefine.TIME_STAMP, FieldValue.serverTimestamp())
        user.put(LedgerDefine.ACCESSIBLE_PROJECTS, this!!.mProjectIDArrayList!!)

        docRefNor.set(user)
            .addOnSuccessListener(OnSuccessListener<Void> {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                tv_user_id.append(" Done !!");
            })
            .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error writing document", e) })

        if (checkbox_supervisor.isChecked) {
            user.put(LedgerDefine.DESIGNATION, LedgerDefine.DESIGNATION_SUPERVISOR)
            user.put(LedgerDefine.USER_ID, this!!.mSupervisorId!!)
            var docRefSup = users.document(this!!.mSupervisorId!!)
            docRefSup.set(user)
                .addOnSuccessListener(OnSuccessListener<Void> {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                    checkbox_supervisor.append(" Done !!");
                })
                .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error writing document", e) })
        }

    }

    fun isEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }
}
