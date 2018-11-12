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

    }

    override fun onStart() {
        super.onStart()
        mProjectsList.clear()
        getProjects()
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
        expadable_list_view.setAdapter(mProjectAdapter)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tv_project_benefits -> launchTransactionviewActivity(view)
        }
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
                    setAdapter()
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
        return true;
    }

    private fun setSetProject(document: QueryDocumentSnapshot?) {
        if (document != null) {
            var projectDetails: ProjectDetails = ProjectDetails()
            projectDetails.projectID = document.get(LedgerDefine.PROJECT_ID) as String
            projectDetails.name = document.get(LedgerDefine.NAME) as String
            projectDetails.address = document.get(LedgerDefine.ADDRESS) as String
            projectDetails.division = document.get(LedgerDefine.DIVISION) as String
            projectDetails.startDate = document.get(LedgerDefine.START_DATE) as String
            projectDetails.endDate = document.get(LedgerDefine.END_DATE) as String
            projectDetails.workAmount = "" + document.get(LedgerDefine.WORK_AMOUNT)
            projectDetails.amount = document.get(LedgerDefine.AMOUNT) as Long
            projectDetails.remarks = document.get(LedgerDefine.REMARK) as String
            mProjectsList.add(projectDetails)
        }
    }
}

