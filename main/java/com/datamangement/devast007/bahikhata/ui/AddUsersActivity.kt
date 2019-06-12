package com.datamangement.devast007.bahikhata.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_add_users.*
import java.util.*

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
    private var mUserAddType = LedgerDefine.USER_ADD_TYPE_NEW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_users)
        toolbar.setTitle(R.string.users)
        setSupportActionBar(toolbar)
        btn_save.setOnClickListener(this)
        btn_project_access.setOnClickListener(this)

        mCompanyName = LedgerSharePrefManger(mContext).getCompanyName()
        mUserAddType = intent.getIntExtra(
            LedgerDefine.USER_ADD_TYPE, -1
        )
        if (mUserAddType == LedgerDefine.USER_ADD_TYPE_MODIFY) {
            var userID1 = intent.getStringExtra(LedgerDefine.USER_ID)
            var userID2: String? = null
            if (userID1.toLong() < BASE_NORMAL_USER_ID) {
                userID2 = "" + (userID1.toLong() + DIFF_NORMAL_SUP_ID)
            } else {
                userID2 = "" + (userID1.toLong() - DIFF_NORMAL_SUP_ID)
            }
            getUserDetails(userID1)
            getUserDetails(userID2)
        } else {
            fetchUserID()
        }
    }

    private fun showSnackBar() {
        val snackbar = Snackbar
            .make(coordinatorLayout, R.string.saved_successfully, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.add_more) {
                finish()
                startActivity(intent)
            }
        snackbar.setActionTextColor(Color.BLUE)
        val sbView = snackbar.view
        sbView.setBackgroundColor(Color.RED)
        val textView = sbView.findViewById<TextView>(android.support.design.R.id.snackbar_text)
        textView.setTextColor(Color.parseColor("#FF0B3E0D"))
        snackbar.show()

    }

    private fun getUserDetails(userID: String) {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        Log.d(TAG, "companyID => " + companyID + " , userID = " + userID)
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/users").whereEqualTo(LedgerDefine.USER_ID, userID)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        Log.d(TAG, " document.get(\"name\")+ => " + document.get("name"))
                        setUserInfo(document);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG)
                }
            })
    }

    private fun setUserInfo(document: QueryDocumentSnapshot?) {
        run {
            if (document != null) {
                var userID = document.get(LedgerDefine.USER_ID) as String
                if (document.get(LedgerDefine.DESIGNATION) == LedgerDefine.DESIGNATION_SUPERVISOR) {
                    checkbox_supervisor.isChecked = true
                    checkbox_supervisor.isEnabled = false
                    mSupervisorId = userID
                    checkbox_supervisor.text = mSupervisorId
                    return
                }
                tv_user_id.text = userID
                mNormalId = userID
                if (isEmpty(mSupervisorId)) {
                    mSupervisorId = "" + (mNormalId!!.toLong() - DIFF_NORMAL_SUP_ID)
                    checkbox_supervisor.text = mSupervisorId
                }


                var temp = document.get(LedgerDefine.NAME)
                if (temp != null) et_user_name.setText(temp as String)

                temp = document.get(LedgerDefine.ADDRESS)
                if (temp != null) et_user_address.setText(temp as String)

                temp = document.get(LedgerDefine.PHONE_NUMBER)
                if (temp != null) et_user_phone.setText(temp as String)

                temp = document.get(LedgerDefine.EMAIL)
                if (temp != null) et_user_email.setText(temp as String)

                var projects = document.get(LedgerDefine.ACCESSIBLE_PROJECTS)

                if (projects != null) {
                    mProjectIDArrayList = projects as ArrayList<String>
                }
                if (mProjectIDArrayList != null) {
                    btn_project_access.text = mProjectIDArrayList.toString()
                }

                temp = document.get(LedgerDefine.REMARK)
                if (temp != null) et_user_remarks.setText(temp as String)

            }
        }


    }


    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.btn_save -> saveUser()
            R.id.btn_project_access -> launProjectSelectActivity()
        }
    }

    private val REQUEST_CODE: Int = 1

    private fun launProjectSelectActivity() {
        var intent: Intent = Intent(mContext, ProjectSelectForUserActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE)
    }

    private var mProjectIDArrayList: ArrayList<String>? = ArrayList<String>()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (REQUEST_CODE == requestCode) {
                var bundle = data!!.extras
                mProjectIDArrayList = bundle.getStringArrayList(LedgerDefine.ALL_PROJECTS)
                btn_project_access.setText(mProjectIDArrayList.toString())
            }
        }
    }

    private fun fetchUserID() {
        var docRef = mDB.collection(LedgerDefine.COMPANIES_SLASH + mCompanyName + "/users")
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

        var users = mDB.collection(LedgerDefine.COMPANIES_SLASH + mCompanyName + "/users");
        var docRefNor = users.document(this!!.mNormalId!!)
        Log.d(TAG, "docRef $docRefNor")
        // [START set_document]
        val user = HashMap<String, Any>()
        user[LedgerDefine.USER_ID] = this!!.mNormalId!!
        user[LedgerDefine.NAME] = userName.toUpperCase().trim()
        user[LedgerDefine.ADDRESS] = userAddress
        user[LedgerDefine.EMAIL] = userEmail
        user[LedgerDefine.PHONE_NUMBER] = userPhone
        user[LedgerDefine.REMARK] = remarks
        user[LedgerDefine.DESIGNATION] = LedgerDefine.DESIGNATION_NORMAL
        user[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()
        user[LedgerDefine.ACCESSIBLE_PROJECTS] = this!!.mProjectIDArrayList!!
        btn_save.isEnabled = false
        if (mUserAddType == LedgerDefine.USER_ADD_TYPE_MODIFY) {
            docRefNor.update(user).addOnSuccessListener(OnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully updated!")
                tv_user_id.append(" Updated Successfully !!");
            }).addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error writing document", e) })
        } else {
            docRefNor.set(user)
                .addOnSuccessListener(OnSuccessListener<Void> {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                    tv_user_id.append(" Done !!");
                })
                .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error writing document", e) })
        }
        if (checkbox_supervisor.isChecked) {
            if (mUserAddType == LedgerDefine.USER_ADD_TYPE_MODIFY) {
                user[LedgerDefine.DESIGNATION] = LedgerDefine.DESIGNATION_SUPERVISOR
                user[LedgerDefine.USER_ID] = this!!.mSupervisorId!!
                var docRefSup = users.document(this!!.mSupervisorId!!)
                docRefSup.update(user).addOnSuccessListener(OnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully updated!")
                    checkbox_supervisor.append(" Updated Successfully !!");
                }).addOnFailureListener(OnFailureListener { e ->
                    Log.w(TAG, "Error updating not possible but adding new user document", e)
                    docRefSup.set(user)
                        .addOnSuccessListener(OnSuccessListener<Void> {
                            Log.d(TAG, "DocumentSnapshot successfully written!")
                            checkbox_supervisor.append(" Done !!");
                            showSnackBar()
                        })
                        .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error writing document", e) })
                })
            } else {
                user[LedgerDefine.DESIGNATION] = LedgerDefine.DESIGNATION_SUPERVISOR
                user[LedgerDefine.USER_ID] = this!!.mSupervisorId!!
                var docRefSup = users.document(this!!.mSupervisorId!!)
                docRefSup.set(user)
                    .addOnSuccessListener(OnSuccessListener<Void> {
                        Log.d(TAG, "DocumentSnapshot successfully written!")
                        checkbox_supervisor.append(" Done !!");
                        showSnackBar()
                    })
                    .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error writing document", e) })
            }
        }

    }

    private fun toast(id: Int) {
        Toast.makeText(mContext, id, Toast.LENGTH_LONG).show()
    }

    fun isEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }
}
