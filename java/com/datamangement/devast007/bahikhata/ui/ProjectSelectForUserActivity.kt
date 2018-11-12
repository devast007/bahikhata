package com.datamangement.devast007.bahikhata.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.*
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.ProjectDetails
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_project_select_for_user.*

class ProjectSelectForUserActivity : AppCompatActivity(), AdapterView.OnItemClickListener {
    private val TAG = "ProjectSelectForUser"
    private var mContext: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_select_for_user)
        mContext = this
        supportActionBar!!.setTitle(R.string.select_projects)
//        setSupportActionBar(toolbar)
        listview.setOnItemClickListener(this)
        getProjects()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_done_button_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item!!.itemId == R.id.action_done) {
            sendResult();
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendResult() {
        var bundle = Bundle()
        var ids: ArrayList<String> = ArrayList<String>()
        for (project in mProjectList) {
            if (project.isChecked) {
                ids.add(project.projectID)
            }
        }
        bundle.putStringArrayList(LedgerDefine.ALL_PROJECTS, ids)
        setResult(RESULT_OK, Intent().putExtras(bundle))
        finish()
    }

    private  var mAdapter: ListAdapter? = null

    private fun getProjects(): Boolean {
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        Log.d(TAG, "companyID => " + companyID)
        db.collection(LedgerDefine.COMPANIES_SLASH+ companyID + "/projects")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        Log.d(TAG, " document.get(\"name\")+ => " + document.get("name"))
                        setSetProject(document);
                    }
                    mAdapter = ListAdapter()
                    listview.adapter =mAdapter
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
        return true;
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, pos: Int, id: Long) {
        Toast.makeText(mContext, "clicked", Toast.LENGTH_LONG).show()
        mProjectList!!.get(pos).isChecked  = !mProjectList!!.get(pos).isChecked
        mAdapter!!.notifyDataSetChanged()
    }

    public val mProjectList: ArrayList<ProjectDetails> = ArrayList<ProjectDetails>()

    private fun setSetProject(document: QueryDocumentSnapshot?) {
        if (document != null) {
            var projectDetails: ProjectDetails = ProjectDetails()
            projectDetails.projectID = document.get(LedgerDefine.PROJECT_ID) as String
            projectDetails.name = document.get(LedgerDefine.NAME) as String
            mProjectList.add(projectDetails)
        }
    }

    inner class ListAdapter : BaseAdapter() {
        override fun getItem(pos: Int): ProjectDetails {
            return mProjectList!!.get(pos)
        }

        override fun getItemId(id: Int): Long {

            return id.toLong()
        }

        override fun getCount(): Int {
            return mProjectList!!.size
        }

        override fun getView(pos: Int, convertView: View?, p2: ViewGroup?): View {
            var view: View? = convertView
            if (view == null) {
                var inflater: LayoutInflater =
                    mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                view = inflater.inflate(R.layout.row_project_select_view, p2, false)
            }

            val projectDetailView = getItem(pos)
            Log.d(TAG, "view " + view)
            var cb = view!!.findViewById<CheckBox>(R.id.checkbox_project_id)
            var tv = view!!.findViewById<TextView>(R.id.tv_project_name)
            Log.d(TAG, "cb " + cb)
            Log.d(TAG, "tv " + tv)
            cb.setText(projectDetailView.projectID)
            tv.setText(projectDetailView.name)
            cb.isChecked = projectDetailView.isChecked

            return view!!
        }

    }
}
