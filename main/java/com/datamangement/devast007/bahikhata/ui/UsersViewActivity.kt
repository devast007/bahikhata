package com.datamangement.devast007.bahikhata.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.adapter.UsersViewAdapter
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.UserDetails
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_users_view.*
import java.util.*

class UsersViewActivity : AppCompatActivity(), View.OnClickListener {

    val mContext: Context = this
    val TAG = "ProjectsViewActivity"
    val mUserList: ArrayList<UserDetails> = ArrayList<UserDetails>()
    var mUsersAdapter: UsersViewAdapter? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_view)
        toolbar.setTitle(R.string.users)
        setSupportActionBar(toolbar)
    }

    override fun onStart() {
        super.onStart()
        mUserList.clear()
        getUsers()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_add_button_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item!!.itemId == R.id.action_add) {
            startActivity(Intent(this, AddUsersActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getUsers(): Boolean {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        Log.d(TAG, "companyID => " + companyID)
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/users")
            .orderBy(LedgerDefine.NAME, Query.Direction.ASCENDING).get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        Log.d(TAG, " document.get(\"name\")+ => " + document.get("name"))
                        setUsers(document);
                    }
                    setAdapter()
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
        return true;
    }

    private fun setAdapter() {
        if (mUsersAdapter == null) {
            mUsersAdapter = UsersViewAdapter(this, mUserList)
        }
        expandable_list_view.setAdapter(mUsersAdapter)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tv_amount -> launchTransactionViewActivity(view)
            R.id.tv_edit_user_info -> modifyUserInfo(view)
        }
    }

    private fun modifyUserInfo(view: View) {
        var userId = view.getTag(R.string.tag_user_id).toString()
        var intent = Intent(this, AddUsersActivity::class.java)
        intent.putExtra(LedgerDefine.USER_ADD_TYPE, LedgerDefine.USER_ADD_TYPE_MODIFY)
        intent.putExtra(LedgerDefine.USER_ID, userId)
        startActivity(intent)
    }

    private fun launchTransactionViewActivity(view: View) {
        val userId: String = view.getTag(R.string.tag_user_id) as String
        val designation: Long = view.getTag(R.string.tag_user_designation) as Long
        val intent = Intent(mContext, TransactionViewActivity::class.java)
        intent.putExtra(LedgerDefine.TRANSACTION_VIEW_TYPE, LedgerDefine.TRANSACTION_VIEW_TYPE_USER)
        intent.putExtra(LedgerDefine.ID, userId)
        intent.putExtra(LedgerDefine.DESIGNATION, designation)
        startActivity(intent)
        //Toast.makeText(this, "Not Implement yet !! " + userId, Toast.LENGTH_LONG).show()
    }

    private fun setUsers(document: QueryDocumentSnapshot?) {
        if (document != null) {
            var userDetails: UserDetails = UserDetails()
            userDetails.userID = document.get(LedgerDefine.USER_ID) as String
            var temp = document.get(LedgerDefine.NAME)
            if (temp != null) userDetails.name = temp as String

            temp = document.get(LedgerDefine.ADDRESS)
            if (temp != null) userDetails.address = temp as String

            temp = document.get(LedgerDefine.PHONE_NUMBER)
            if (temp != null) userDetails.phone = temp as String


            temp = document.get(LedgerDefine.DESIGNATION)
            if (temp != null) userDetails.designation = temp as Long

            temp = document.get(LedgerDefine.TIME_STAMP)
            if (temp != null) userDetails.userCreatedDate = (temp as Date).toString()


            temp = document.get(LedgerDefine.EMAIL)
            if (temp != null) userDetails.email = temp as String

            var projects = document.get(LedgerDefine.ACCESSIBLE_PROJECTS)

            if (projects != null) {
                userDetails.accesibleProjectsStr = (projects as ArrayList<String>).toString()
            }

            temp = document.get(LedgerDefine.AMOUNT)
            if (temp != null) userDetails.amount = temp as Long


            temp = document.get(LedgerDefine.REMARK)
            if (temp != null) userDetails.remarks = temp as String

            mUserList.add(userDetails)
        }
    }

}
