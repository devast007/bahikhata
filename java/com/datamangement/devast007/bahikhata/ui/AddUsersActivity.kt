package com.datamangement.devast007.bahikhata.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.SqlDBFile
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_add_users.*
import java.util.*

class AddUsersActivity : AppCompatActivity(), View.OnClickListener,
    LedgerUtils.Companion.StatusListner {


    private var mUserId: Long = -1
    val TAG = "AddUsersActivity"
    val mContext = this
    val mDB = FirebaseFirestore.getInstance()
    var mCompanyName: String? = null
    private var mUserAddType = LedgerDefine.USER_ADD_TYPE_NEW
    private var mLaunchedFrom: Int = -1
    override fun onBackPressed() {
        super.onBackPressed()
        if (mLaunchedFrom == LedgerDefine.LAUNCHED_FROM_VIEW_LIST_ACTIVITY) {
            val newIntent = Intent(mContext, UsersViewActivity::class.java)
            newIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            mContext!!.startActivity(newIntent)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_users)
        supportActionBar!!.setTitle(R.string.title_activity_add_users)
        btn_save.setOnClickListener(this)
        btn_project_access.setOnClickListener(this)

        mCompanyName = LedgerSharePrefManger(mContext).getCompanyID()
        mUserAddType = intent.getIntExtra(
            LedgerDefine.USER_ADD_TYPE, -1
        )
        mLaunchedFrom = intent.getIntExtra(
            LedgerDefine.LAUNCHED_FROM, -1
        )
        if (mUserAddType == LedgerDefine.USER_ADD_TYPE_MODIFY) {
            var userID = intent.getStringExtra(LedgerDefine.USER_ID)
            getUserDetails(userID!!)
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
        val textView = sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setTextColor(Color.parseColor("#FF0B3E0D"))
        snackbar.show()

    }

    private fun getUserDetails(userID: String) {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_USERS)
            .whereEqualTo(LedgerDefine.USER_ID, userID.toLong())
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
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
                var id: Any? = document.get(LedgerDefine.USER_ID)
                try {
                    id = id as String
                } catch (e: ClassCastException) {
                    id = id as Long
                }
                mUserId = id.toString().toLong()

                tv_user_id.text = "update Info For $mUserId";
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
                var userAccounts = document.get(LedgerDefine.ACCOUNTS)

                if (userAccounts != null) {
                    if ((userAccounts as ArrayList<String>).size >= 2) {
                        checkbox_supervisor.isEnabled = false
                        checkbox_supervisor.isChecked = true
                        checkbox_supervisor.text = "Already Have Master Account"
                    } else {
                        checkbox_supervisor.isEnabled = true
                    }
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
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (REQUEST_CODE == requestCode) {
                var bundle = data!!.extras
                mProjectIDArrayList = bundle!!.getStringArrayList(LedgerDefine.ALL_PROJECTS)
                btn_project_access.setText(mProjectIDArrayList.toString())
            }
        }
    }

    private fun fetchUserID() {
        var docRef =
            mDB.collection(LedgerDefine.COMPANIES_SLASH + mCompanyName + LedgerDefine.SLASH_USERS)
                .orderBy(LedgerDefine.USER_ID, Query.Direction.DESCENDING).limit(1)
        docRef.get()
            .addOnSuccessListener(OnSuccessListener<QuerySnapshot> {
                var id: Any? = it.documents[0].get(LedgerDefine.USER_ID)
                try {
                    id = id as String
                } catch (e: ClassCastException) {
                    id = id as Long
                }
                var tempUserId: Long = id.toString().toLong()
                mUserId = tempUserId + 1

                tv_user_id.setText(String.format(getString(R.string.new_user_id), mUserId))

            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w(
                    TAG,
                    "Error writing document",
                    e
                )
            })
    }

    private fun saveUser() {
        val userName = et_user_name.text.toString()
        val userAddress = et_user_address.text.toString()
        val userEmail = et_user_email.text.toString()
        val userPhone = et_user_phone.text.toString()
        val remarks = et_user_remarks.text.toString()

        if (mUserId.toInt() == -1) {
            Toast.makeText(mContext, R.string.error_03, Toast.LENGTH_LONG).show()
            return
        }
        if (isEmpty(userName)) {
            et_user_name.error = getString(R.string.error_06)
            Toast.makeText(mContext, R.string.error_06, Toast.LENGTH_LONG).show()
            return
        }

        var users =
            mDB.collection(LedgerDefine.COMPANIES_SLASH + mCompanyName + LedgerDefine.SLASH_USERS);
        var docRefNor = users.document(mUserId.toString())
        // [START set_document]
        val userMap = HashMap<String, Any>()
        userMap[LedgerDefine.USER_ID] = mUserId
        userMap[LedgerDefine.NAME] = userName.toUpperCase().trim()
        userMap[LedgerDefine.ADDRESS] = userAddress
        userMap[LedgerDefine.EMAIL] = userEmail
        userMap[LedgerDefine.PHONE_NUMBER] = userPhone
        userMap[LedgerDefine.REMARK] = remarks
        userMap[LedgerDefine.DESIGNATION] = LedgerDefine.DESIGNATION_NORMAL
        userMap[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()

        FieldValue.serverTimestamp()
        userMap[LedgerDefine.ACCESSIBLE_PROJECTS] = this!!.mProjectIDArrayList!!
        btn_save.isEnabled = false
        LedgerUtils.setstatusListner(this)
        if (mUserAddType == LedgerDefine.USER_ADD_TYPE_MODIFY) {
            if (checkbox_supervisor.isEnabled && checkbox_supervisor.isChecked) {
                userMap[LedgerDefine.DESIGNATION] = LedgerDefine.DESIGNATION_SUPERVISOR
                var userAccounts: ArrayList<String> = ArrayList<String>()
                userAccounts.add(LedgerDefine.PREFIX_PERSONAL + mUserId)
                userAccounts.add(LedgerDefine.PREFIX_MASTER + mUserId)
                userMap[LedgerDefine.ACCOUNTS] = userAccounts
            }

            LedgerUtils.setDataToFirestore(
                mContext!!, userMap[LedgerDefine.USER_ID]!!,
                SqlDBFile.CONTENT_URI_TABLE_USERS, LedgerDefine.UPDATE_DATA, docRefNor, userMap
            )
            /*docRefNor.update(user).addOnSuccessListener(OnSuccessListener {
                tv_user_id.append(" Updated Successfully !!");
            }).addOnFailureListener(OnFailureListener { e ->
                Log.w(
                    TAG,
                    "Error writing document",
                    e
                )
            })*/
        } else {
            var userAccounts: ArrayList<String> = ArrayList<String>()
            userAccounts.add(LedgerDefine.PREFIX_PERSONAL + mUserId)
            if (checkbox_supervisor.isChecked) {
                userMap[LedgerDefine.DESIGNATION] = LedgerDefine.DESIGNATION_SUPERVISOR
                userAccounts.add(LedgerDefine.PREFIX_MASTER + mUserId)
            }
            userMap[LedgerDefine.ACCOUNTS] = userAccounts
            LedgerUtils.setDataToFirestore(
                mContext!!, userMap[LedgerDefine.USER_ID]!!,
                SqlDBFile.CONTENT_URI_TABLE_USERS, LedgerDefine.SET_DATA, docRefNor, userMap
            )
            /*docRefNor.set(user)
                .addOnSuccessListener(OnSuccessListener<Void> {
                    tv_user_id.append(" Done !!");
                    Log.d("it.toString()", "it.toString() = "+it.toString())
                    showSnackBar()
                })
                .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error writing document", e) })*/
        }
    }

    private fun toast(id: Int) {
        Toast.makeText(mContext, id, Toast.LENGTH_LONG).show()
    }

    fun isEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }

    override fun onComplete(status: Boolean) {
        if (status) {
            if (mUserAddType == LedgerDefine.USER_ADD_TYPE_MODIFY) {
                tv_user_id.append(" Updated Successfully !!");
            } else {
                tv_user_id.append(" Done !!");
                showSnackBar()
            }

        } else {


        }
    }
}
