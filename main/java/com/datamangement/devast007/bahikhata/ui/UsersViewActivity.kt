package com.datamangement.devast007.bahikhata.ui

import android.app.ProgressDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ExpandableListView.OnGroupClickListener
import android.widget.Toast
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.adapter.UsersViewAdapter
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.UserDetails
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_users_view.*
import java.util.*


class UsersViewActivity : AppCompatActivity(), View.OnClickListener {

    val mContext: Context = this
    val TAG = "ProjectsViewActivity"
    val mUserList: ArrayList<UserDetails> = ArrayList<UserDetails>()
    val mSearchedUserList: ArrayList<UserDetails> = ArrayList<UserDetails>()
    var mUsersAdapter: UsersViewAdapter? = null

    private var mPersonalSentAmount: Long = 0
    private var mPersonalReceivedAmount: Long = 0
    private var mMasterSentAmount: Long = 0
    private var mMasterReceivedAmount: Long = 0
    private var mPersonalGSTAmount: Long = 0
    private var mPersonalMaterialAmount: Long = 0

    private var LIMIT: Long = 500
    var mLastDoc: DocumentSnapshot? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_view)
        toolbar.setTitle(R.string.users)
        setSupportActionBar(toolbar)
        getUsers()
    }

    override fun onStart() {
        super.onStart()
        //mUserList.clear()
    }

    var queryTextListener: SearchView.OnQueryTextListener =
        object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                matchTextInList(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {

                return true
            }
        }

    private fun matchTextInList(newText: String) {

        if (!TextUtils.isEmpty(newText)) {
            mSearchedUserList.clear()
            for (userDetails in mUserList) {

                if (userDetails.name.toLowerCase().contains(newText.toLowerCase())) {
                    mSearchedUserList.add(userDetails)
                }
            }
            mUsersAdapter = UsersViewAdapter(this, mSearchedUserList)
            expandable_list_view.setAdapter(mUsersAdapter)
        } else {
            mUsersAdapter = UsersViewAdapter(this, mUserList)
            expandable_list_view.setAdapter(mUsersAdapter)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_user_view, menu)

        val searchItem: MenuItem? = menu?.findItem(R.id.action_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView: SearchView? = searchItem?.actionView as SearchView

        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView?.setOnQueryTextListener(queryTextListener)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item!!.itemId == R.id.action_add) {
            startActivity(Intent(this, AddUsersActivity::class.java))
            return true
        } else if (item!!.itemId == R.id.action_search) {

            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getUsers(): Boolean {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_USERS)
            .orderBy(LedgerDefine.NAME, Query.Direction.ASCENDING).get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        setUsers(document)
                    }
                    //FirestoreDataBase().setUsers(mUserList,mContext)
                    setAdapter()
                } else {
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
        expandable_list_view.setOnGroupClickListener(OnGroupClickListener { parent, v, groupPosition, id ->
            LedgerUtils.signInProfile!!.isHasLimitedAccess
        })

        var pos = 0
        for (user in mUserList) {
            if (user.userID == LedgerUtils.signInProfile!!.userID) {
                expandable_list_view.setSelection(pos)
                expandable_list_view.expandGroup(pos)
                break
            }
            pos++
        }
    }


    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tv_personal_payment, R.id.tv_master_amount -> launchTransactionViewActivity(view)
            R.id.tv_edit_user_info -> modifyUserInfo(view)
            R.id.tv_personal_gst -> launchGstViewActivity(view)
            R.id.tv_personal_material_cost -> launchMaterialViewActivity(view)
            R.id.tv_update_user_info -> updateUserInfo(view)

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
        //val designation: Long = view.getTag(R.string.tag_user_designation) as Long
        val intent = Intent(mContext, TransactionViewActivity::class.java)
        intent.putExtra(LedgerDefine.TRANSACTION_VIEW_TYPE, LedgerDefine.TRANSACTION_VIEW_TYPE_USER)
        intent.putExtra(LedgerDefine.ID, userId)
        //intent.putExtra(LedgerDefine.DESIGNATION, designation)
        startActivity(intent)
        //Toast.makeText(this, "Not Implement yet !! " + userId, Toast.LENGTH_LONG).show()
    }

    private fun launchGstViewActivity(view: View) {
        val userId: String = view.getTag(R.string.tag_user_id) as String
        //val designation: Long = view.getTag(R.string.tag_user_designation) as Long
        val intent = Intent(mContext, GstViewActivity::class.java)
        intent.putExtra(LedgerDefine.TRANSACTION_VIEW_TYPE, LedgerDefine.TRANSACTION_VIEW_TYPE_USER)
        intent.putExtra(LedgerDefine.ID, userId)
        //intent.putExtra(LedgerDefine.DESIGNATION, designation)
        startActivity(intent)
        //Toast.makeText(this, "Not Implement yet !! " + userId, Toast.LENGTH_LONG).show()
    }

    private fun launchMaterialViewActivity(view: View) {
        val userId: String = view.getTag(R.string.tag_user_id) as String
        //val designation: Long = view.getTag(R.string.tag_user_designation) as Long
        val intent = Intent(mContext, MaterialViewActivity::class.java)
        intent.putExtra(LedgerDefine.TRANSACTION_VIEW_TYPE, LedgerDefine.TRANSACTION_VIEW_TYPE_USER)
        intent.putExtra(LedgerDefine.ID, userId)
        //intent.putExtra(LedgerDefine.DESIGNATION, designation)
        startActivity(intent)
        //Toast.makeText(this, "Not Implement yet !! " + userId, Toast.LENGTH_LONG).show()
    }

    private fun setUsers(document: QueryDocumentSnapshot?) {
        if (document != null) {
            var userDetails: UserDetails = UserDetails()
            var userID: Any? = document.get(LedgerDefine.USER_ID)
            try {
                userID = (userID as Long).toString()
            } catch (e: ClassCastException) {
                userID = userID as String
            }
            userDetails.userID = userID.toString()
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

            var account = document.get(LedgerDefine.ACCOUNTS)
            if (account != null) {
                userDetails.userAccounts = (account as ArrayList<String>)
            }

            // personal
            temp = document.get(LedgerDefine.P_MATERIAL_COST)
            if (temp != null) userDetails.p_MaterialCost = temp as Long
            temp = document.get(LedgerDefine.P_PAYMENT)
            if (temp != null) userDetails.p_Payment = temp as Long
            temp = document.get(LedgerDefine.P_GST_BILL)
            if (temp != null) userDetails.p_gstBill = temp as Long
            // master
            temp = document.get(LedgerDefine.M_AMOUNT)
            if (temp != null) userDetails.m_Amount = temp as Long


            temp = document.get(LedgerDefine.REMARK)
            if (temp != null) userDetails.remarks = temp as String

            mUserList.add(userDetails)
        }
    }

    var mDialog: ProgressDialog? = null
    private fun updateUserInfo(view: View) {


        var userId = view.getTag(R.string.tag_user_id).toString()

        if (userId == LedgerDefine.ADMIN_ID) {
            userId = LedgerDefine.PREFIX_ADMIN + userId
        } else {
            userId = LedgerDefine.PREFIX_PERSONAL + userId
        }

        mPersonalSentAmount = 0
        mPersonalReceivedAmount = 0
        mMasterSentAmount = 0
        mMasterReceivedAmount = 0
        mPersonalGSTAmount = 0
        mPersonalMaterialAmount = 0
        mDialog = ProgressDialog.show(
            mContext,
            getString(R.string.updating_data),
            getString(R.string.please_wait),
            false
        )
        updateData(userId, LedgerDefine.SENDER_ID, LedgerDefine.SLASH_TRANSACTIONS)
    }

    private fun updateData(userID: String, where: String, type: String?) {

        mLastDoc = null
        if (type == null) {
            setDataToFirebase(userID.substring(2))

        } else if (type == LedgerDefine.SLASH_GST) {
            updateGSTAmount(userID, where)
        } else if (type == LedgerDefine.SLASH_TRANSACTIONS) {
            updateTransactionAmount(userID, where)
        } else if (type == LedgerDefine.SLASH_MATERIALS) {
            updateMaterialAmount(userID, where)
        }
    }

    private fun setDataToFirebase(userId: String) {

        var pos = 0
        for (item in mUserList) {
            if (item.userID == userId) {
                break
            }
            pos++
        }

        val personalFinalAmount = mPersonalReceivedAmount - mPersonalSentAmount
        val masterFinalAmount = mMasterReceivedAmount - mMasterSentAmount

        var userMap: HashMap<String, Any> = HashMap<String, Any>()

        userMap[LedgerDefine.P_PAYMENT] = personalFinalAmount
        mUserList[pos].p_Payment = personalFinalAmount
        if (!masterFinalAmount.equals(0)) {
            userMap[LedgerDefine.M_AMOUNT] = masterFinalAmount
            mUserList[pos].m_Amount = masterFinalAmount
        }

        if (!mPersonalGSTAmount.equals(0)) {
            userMap[LedgerDefine.P_GST_BILL] = mPersonalGSTAmount
            mUserList[pos].p_gstBill = mPersonalGSTAmount
        }

        if (!mPersonalMaterialAmount.equals(0)) {
            userMap[LedgerDefine.P_MATERIAL_COST] = mPersonalMaterialAmount
            mUserList[pos].p_MaterialCost = mPersonalMaterialAmount
        }


        val db = FirebaseFirestore.getInstance()
        val companyName = LedgerSharePrefManger(mContext).getCompanyID()
        var docRef =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_USERS)
                .document(userId)
        docRef.update(userMap)
            .addOnSuccessListener(OnSuccessListener<Void> {
                val firstVisiblePos = expandable_list_view.firstVisiblePosition
                expandable_list_view.deferNotifyDataSetChanged()
                expandable_list_view.setSelection(firstVisiblePos)
                expandable_list_view.collapseGroup(firstVisiblePos)
                expandable_list_view.expandGroup(firstVisiblePos)

                if (mDialog != null) mDialog!!.dismiss()
                Toast.makeText(mContext, R.string.update_done, Toast.LENGTH_LONG).show()
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
                if (mDialog != null) mDialog!!.dismiss()
                Toast.makeText(mContext, R.string.update_failed, Toast.LENGTH_LONG).show()
            })

    }

    private fun updateTransactionAmount(userID: String, where: String) {

        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(mContext).getCompanyID()
        var query: Query =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)

        query = query.whereEqualTo(
            where,
            userID
        )

        if (mLastDoc != null) {
            query = query.startAfter(mLastDoc!!).limit(LIMIT)
        } else {
            query = query.limit(LIMIT)
        }
        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {

                    var queryDocs = task.result!!
                    var preFix = userID!!.substring(0, 2)
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        Log.d(TAG, doc.id + " => " + doc.data)
                        val amount = doc.get(LedgerDefine.AMOUNT) as Long
                        if (preFix == LedgerDefine.PREFIX_PERSONAL || preFix == LedgerDefine.PREFIX_ADMIN) {
                            if (where == LedgerDefine.SENDER_ID) {
                                mPersonalSentAmount += amount
                            } else {
                                mPersonalReceivedAmount += amount
                            }
                        } else {
                            if (where == LedgerDefine.SENDER_ID) {
                                mMasterSentAmount += amount
                            } else {
                                mMasterReceivedAmount += amount
                            }
                        }

                    }
                    val size = queryDocs.size()
                    if (queryDocs.isEmpty || size < LIMIT) {

                        var preFix = userID!!.substring(0, 2)
                        if (preFix == LedgerDefine.PREFIX_PERSONAL || preFix == LedgerDefine.PREFIX_ADMIN) {
                            if (where == LedgerDefine.SENDER_ID) {
                                updateTransactionAmount(userID, LedgerDefine.RECEIVER_ID)
                            } else if (where == LedgerDefine.RECEIVER_ID) {
                                val masterUserID = LedgerDefine.PREFIX_MASTER + userID.substring(2)
                                updateData(
                                    masterUserID,
                                    LedgerDefine.SENDER_ID,
                                    LedgerDefine.SLASH_TRANSACTIONS
                                )
                            }
                        } else {
                            if (where == LedgerDefine.SENDER_ID) {
                                updateTransactionAmount(userID, LedgerDefine.RECEIVER_ID)
                            } else if (where == LedgerDefine.RECEIVER_ID) {
                                val personalUserID =
                                    LedgerDefine.PREFIX_PERSONAL + userID.substring(2)
                                updateData(
                                    personalUserID,
                                    LedgerDefine.SENDER_ID,
                                    LedgerDefine.SLASH_MATERIALS
                                )
                            }
                        }
                    } else {
                        mLastDoc = queryDocs.documents[size - 1]
                        updateTransactionAmount(userID, where)
                    }
                }
            })
    }

    private fun updateMaterialAmount(userID: String, where: String) {

        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(mContext).getCompanyID()
        var query: Query =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIALS)

        query = query.whereEqualTo(
            where,
            userID
        )

        if (mLastDoc != null) {
            query = query.startAfter(mLastDoc!!).limit(LIMIT)
        } else {
            query = query.limit(LIMIT)
        }
        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {

                    var queryDocs = task.result!!
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        Log.d(TAG, doc.id + " => " + doc.data)
                        val amount = (doc.get(LedgerDefine.AMOUNT) as String).toLong()
                        mPersonalMaterialAmount += amount
                    }
                    val size = queryDocs.size()
                    if (queryDocs.isEmpty || size < LIMIT) {

                        updateData(
                            userID,
                            LedgerDefine.SENDER_ID,
                            LedgerDefine.SLASH_GST
                        )
                    } else {
                        mLastDoc = queryDocs.documents[size - 1]
                        updateMaterialAmount(userID, where)
                    }
                }
            })
    }

    private fun updateGSTAmount(userID: String, where: String) {

        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(mContext).getCompanyID()
        var query: Query =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_GST)

        query = query.whereEqualTo(
            where,
            userID
        )

        if (mLastDoc != null) {
            query = query.startAfter(mLastDoc!!).limit(LIMIT)
        } else {
            query = query.limit(LIMIT)
        }
        query.get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    var queryDocs = task.result!!
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        val billAmount = (doc.get(LedgerDefine.GST_BILL_AMOUNT) as String).toLong()
                        mPersonalGSTAmount += billAmount
                    }
                    val size = queryDocs.size()
                    if (queryDocs.isEmpty || size < LIMIT) {

                        updateData(
                            userID,
                            LedgerDefine.SENDER_ID,
                            null
                        )
                    } else {
                        mLastDoc = queryDocs.documents[size - 1]
                        updateGSTAmount(userID, where)
                    }
                }
            })
    }
}
