package com.datamangement.devast007.bahikhata.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_projects_view.*

class ProjectsViewActivity : AppCompatActivity(), View.OnClickListener {

    val mContext: Context = this
    val TAG = "ProjectsViewActivity"
    val mProjectsList: ArrayList<ProjectDetails> = ArrayList<ProjectDetails>()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_add_button_view, menu)
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
            mProjectAdapter = ProjectViewAdapter(this, mProjectsList)
        }
        expandable_list_view.setAdapter(mProjectAdapter)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tv_amount -> launchTransactionviewActivity(view)
            R.id.tv_edit_project -> launchmodifyProject(view)
        }
    }

    private fun launchmodifyProject(view: View) {
        var id = view.getTag(R.string.tag_project_id).toString()
        if (TextUtils.isEmpty(id)) return

        var intent = Intent(this, AddProjectActivity::class.java)
        intent.putExtra(LedgerDefine.PROJECT_ID, id)
        startActivity(intent)
    }

    private fun launchTransactionviewActivity(view: View) {
        val projectId: String = view.getTag(R.string.tag_project_id) as String
        val intent = Intent(mContext, TransactionViewActivity::class.java)
        intent.putExtra(LedgerDefine.TRANSACTION_VIEW_TYPE, LedgerDefine.TRANSACTION_VIEW_TYPE_PROJECT)
        intent.putExtra(LedgerDefine.ID, projectId)
        startActivity(intent)
        //Toast.makeText(this, "Not Implement yet !! " + projectId, Toast.LENGTH_LONG).show()
    }

    private fun getProjects(): Boolean {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        Log.d(TAG, "companyID => " + companyID)
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + "/projects")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        Log.d(TAG, " document.get(\"name\")+ => " + document.get("name"))
                        setSetProject(document);
                    }
                    // updating project ID first Time
                    //FirestoreDataBase().createNewDocForProjects(mContext,mProjectsList);
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
            projectDetails.address = getStringFormDoc(document, LedgerDefine.ADDRESS)
            projectDetails.division = getStringFormDoc(document, LedgerDefine.DIVISION)
            projectDetails.amount = document.get(LedgerDefine.AMOUNT) as Long
            projectDetails.startDate = getStringFormDoc(document, LedgerDefine.START_DATE)
            projectDetails.endDate = getStringFormDoc(document, LedgerDefine.END_DATE)
            projectDetails.remarks = getStringFormDoc(document, LedgerDefine.REMARK)
            projectDetails.mainAmount = getStringFormDoc(document, LedgerDefine.MAIN_AMOUNT)
            projectDetails.mbNo = getStringFormDoc(document, LedgerDefine.MB_NO)
            projectDetails.head = getStringFormDoc(document, LedgerDefine.HEAD)
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
            mProjectsList.add(projectDetails)
        }
    }

    private fun getStringFormDoc(document: QueryDocumentSnapshot?, key: String): String {
        var data: Any? = document!!.get(key) ?: return ""

        return data as String

    }
}

