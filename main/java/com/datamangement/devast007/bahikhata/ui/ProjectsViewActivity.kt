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
import android.widget.Toast
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.adapter.ProjectViewAdapter
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.ProjectDetails
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_projects_view.expandable_list_view
import kotlinx.android.synthetic.main.activity_projects_view.toolbar
import java.util.*
import kotlin.collections.ArrayList

class ProjectsViewActivity : AppCompatActivity(), View.OnClickListener {

    private val LIMIT: Long = 100
    private var mLastDoc: DocumentSnapshot? = null
    private var mProjectsReceivedAmount: Long = 0
    private var mProjectsSentAmount: Long = 0
    val mContext: Context = this
    val TAG = "ProjectsViewActivity"
    val mProjectsMap: ArrayList<ProjectDetails> = ArrayList<ProjectDetails>()
    val mSearchedProjectsList: ArrayList<ProjectDetails> = ArrayList<ProjectDetails>()

    var mProjectAdapter: ProjectViewAdapter? = null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects_view)
        toolbar.setTitle(R.string.projets)
        setSupportActionBar(toolbar)
        getProjects()

    }

    override fun onStart() {
        super.onStart()
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

        mSearchedProjectsList.clear()
        if (!TextUtils.isEmpty(newText)) {
            for (userDetails in mProjectsMap) {
                if (userDetails.name.toLowerCase().contains(newText.toLowerCase())) {
                    mSearchedProjectsList.add(userDetails)
                }
            }
            mProjectAdapter = ProjectViewAdapter(this, mSearchedProjectsList)
        } else {
            mProjectAdapter = ProjectViewAdapter(this, mProjectsMap)
        }
        expandable_list_view.setAdapter(mProjectAdapter)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_projects_view, menu)

        val searchItem: MenuItem? = menu?.findItem(R.id.action_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView: SearchView? = searchItem?.actionView as SearchView

        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView?.setOnQueryTextListener(queryTextListener)

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item!!.itemId == R.id.action_add) {
            startActivity(Intent(this, AddProjectActivity::class.java));
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setAdapter() {
        if (mProjectAdapter == null) {
            mProjectAdapter = ProjectViewAdapter(this, mProjectsMap)
        }
        expandable_list_view.setAdapter(mProjectAdapter)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tv_amount -> launchTransactionViewActivity(view)
            R.id.tv_edit_project -> launchmodifyProject(view)
            R.id.tv_update_project_info -> updateProjectInfo(view)
        }
    }

    private fun launchmodifyProject(view: View) {
        var id = view.getTag(R.string.tag_project_id).toString()
        if (TextUtils.isEmpty(id)) return

        var intent = Intent(this, AddProjectActivity::class.java)
        intent.putExtra(LedgerDefine.PROJECT_ID, id)
        startActivity(intent)
    }

    private fun launchTransactionViewActivity(view: View) {
        val projectId: String = view.getTag(R.string.tag_project_id) as String
        val intent = Intent(mContext, TransactionViewActivity::class.java)
        intent.putExtra(
            LedgerDefine.TRANSACTION_VIEW_TYPE,
            LedgerDefine.TRANSACTION_VIEW_TYPE_PROJECT
        )
        intent.putExtra(LedgerDefine.ID, projectId)
        startActivity(intent)
        //Toast.makeText(this, "Not Implement yet !! " + projectId, Toast.LENGTH_LONG).show()
    }

    private fun getProjects(): Boolean {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_PROJECTS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        setSetProject(document);
                    }
                    // setting timestamp
                    // FirestoreDataBase().setProjetcs(mProjectsList, mContext)
                    setAdapter()
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
        return true
    }

    private fun setSetProject(document: QueryDocumentSnapshot?) {
        if (document != null) {
            var projectDetails: ProjectDetails = ProjectDetails()
            projectDetails.projectID = getStringFormDoc(document, LedgerDefine.PROJECT_ID)
            projectDetails.name = getStringFormDoc(document, LedgerDefine.NAME)
            projectDetails.nickname = getStringFormDoc(document, LedgerDefine.NICKNAME)
            projectDetails.address = getStringFormDoc(document, LedgerDefine.ADDRESS)
            projectDetails.division = getStringFormDoc(document, LedgerDefine.DIVISION)
            if (document.get(LedgerDefine.AMOUNT) != null) {
                projectDetails.amount = document.get(LedgerDefine.AMOUNT) as Long
            }
            projectDetails.startDate = getStringFormDoc(document, LedgerDefine.START_DATE)
            projectDetails.endDate = getStringFormDoc(document, LedgerDefine.END_DATE)
            projectDetails.remarks = getStringFormDoc(document, LedgerDefine.REMARK)
            projectDetails.mainAmount = getStringFormDoc(document, LedgerDefine.MAIN_AMOUNT)
            projectDetails.mbNo = getStringFormDoc(document, LedgerDefine.MB_NO)
            projectDetails.head = getStringFormDoc(document, LedgerDefine.HEAD)
            var timestamp = document.get(LedgerDefine.TIME_STAMP)

            if (timestamp != null) {
                try {
                    projectDetails.timeStamp = timestamp as String
                } catch (e: java.lang.ClassCastException) {
                    projectDetails.timeStamp = (timestamp as Date).toString()
                }
            }

            projectDetails.maintenace1stYearAmount =
                getStringFormDoc(document, LedgerDefine.MAINTENANCE_1ST_YEAR_AMOUNT)
            projectDetails.maintenace2ndYearAmount =
                getStringFormDoc(document, LedgerDefine.MAINTENANCE_2ND_YEAR_AMOUNT)
            projectDetails.maintenace3rdYearAmount =
                getStringFormDoc(document, LedgerDefine.MAINTENANCE_3RD_YEAR_AMOUNT)
            projectDetails.maintenace4thYearAmount =
                getStringFormDoc(document, LedgerDefine.MAINTENANCE_4TH_YEAR_AMOUNT)
            projectDetails.maintenace5thYearAmount =
                getStringFormDoc(document, LedgerDefine.MAINTENANCE_5TH_YEAR_AMOUNT)
            mProjectsMap.add(projectDetails)
        }
    }


    var mDialog: ProgressDialog? = null
    private fun updateProjectInfo(view: View) {


        var projectId = view.getTag(R.string.tag_project_id).toString()

        mProjectsSentAmount = 0
        mProjectsReceivedAmount = 0
        mDialog = ProgressDialog.show(
            mContext,
            getString(R.string.updating_data),
            getString(R.string.please_wait),
            false
        )
        updateTransactionAmount(projectId)
    }

    private fun setDataToFirebase(projectId: String) {

        var pos = 0
        for (item in mProjectsMap) {
            if (item.projectID == projectId) {
                break
            }
            pos++
        }

        val projectAmount = mProjectsReceivedAmount - mProjectsSentAmount

        mProjectsMap[pos].amount = projectAmount

        var projectMap: HashMap<String, Any> = HashMap<String, Any>()

        projectMap[LedgerDefine.AMOUNT] = projectAmount


        val db = FirebaseFirestore.getInstance()
        val companyName = LedgerSharePrefManger(mContext).getCompanyID()
        var docRef =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_PROJECTS)
                .document(projectId)
        docRef.update(projectMap)
            .addOnSuccessListener(OnSuccessListener<Void> {
                expandable_list_view.deferNotifyDataSetChanged()
                expandable_list_view.setSelection(pos)
                expandable_list_view.collapseGroup(pos)
                expandable_list_view.expandGroup(pos)

                if (mDialog != null) mDialog!!.dismiss()
                Toast.makeText(mContext, R.string.update_done, Toast.LENGTH_LONG).show()
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
                if (mDialog != null) mDialog!!.dismiss()
                Toast.makeText(mContext, R.string.update_failed, Toast.LENGTH_LONG).show()
            })

    }


    private fun updateTransactionAmount(projectID: String) {

        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(mContext).getCompanyID()
        var query: Query =
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_TRANSACTIONS)

        query = query.whereEqualTo(
            LedgerDefine.PROJECT_ID,
            projectID
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
                    var preFix = projectID!!.substring(0, 2)
                    for (doc: QueryDocumentSnapshot in queryDocs) {
                        Log.d(TAG, doc.id + " => " + doc.data)
                        val amount = doc.get(LedgerDefine.AMOUNT) as Long
                        var senderID = doc.get(LedgerDefine.SENDER_ID) as String
                        if (senderID.contains(LedgerDefine.PREFIX_PERSONAL)) {
                            mProjectsReceivedAmount += amount
                        } else {
                            mProjectsSentAmount += amount
                        }

                    }
                    val size = queryDocs.size()
                    if (queryDocs.isEmpty || size < LIMIT) {
                        mLastDoc = null
                        setDataToFirebase(projectID)
                    } else {
                        mLastDoc = queryDocs.documents[size - 1]
                        updateTransactionAmount(projectID)
                    }
                }
            })
    }

    private fun getStringFormDoc(document: QueryDocumentSnapshot?, key: String): String {
        var data: Any? = document!!.get(key) ?: return ""

        return data as String

    }
}

