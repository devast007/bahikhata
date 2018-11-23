package com.datamangement.devast007.bahikhata.ui

import android.content.Context
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
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_project.*
import java.util.*

class AddProjectActivity : AppCompatActivity(), View.OnClickListener {
    private val mContext: Context? = this
    private val TAG: String = "AddProjectActivity"

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)
        setSupportActionBar(toolbar)

        btn_save.setOnClickListener(this)
        mAuth = FirebaseAuth.getInstance()
        Log.d(TAG, "mAuth 1 " + mAuth)
    }

    override fun onResume() {
        super.onResume()

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

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.btn_save -> saveProject()
        }
    }


    private fun saveProject() {
        Log.d(TAG, "saveProject ")
        val projectId = tv_project_id.text.toString()
        val projectName = et_project_name.text.toString()
        val projectAddress = et_project_address.text.toString()
        val projectDiv = et_project_division.text.toString()
        val projectNote = et_remarks.text.toString()
        val projectStartDate = et_project_start_date.text.toString()
        val projectEndDate = et_project_end_date.text.toString()

        if (isEmpty(projectId)) {
            tv_project_id.error = getString(R.string.error_04)
            Toast.makeText(mContext, R.string.error_03, Toast.LENGTH_LONG).show()
            return
        }
        if (isEmpty(projectName)) {
            et_project_name.error = getString(R.string.error_05)
            Toast.makeText(mContext, R.string.error_03, Toast.LENGTH_LONG).show()
            return
        }
        val db = FirebaseFirestore.getInstance()
        val companyName = LedgerSharePrefManger(mContext).getCompanyName()
        var docRef = db.collection(LedgerDefine.COMPANIES_SLASH + companyName + "/projects").document(projectId)
        Log.d(TAG, "docRef " + docRef)
        // [START set_document]
        val project = HashMap<String, Any>()
        project.put(LedgerDefine.PROJECT_ID, projectId)
        project.put(LedgerDefine.NAME, projectName)
        project.put(LedgerDefine.ADDRESS, projectAddress)
        project.put(LedgerDefine.DIVISION, projectDiv)
        project.put(LedgerDefine.REMARK, projectNote)
        project.put(LedgerDefine.START_DATE, projectStartDate)
        project.put(LedgerDefine.END_DATE, projectEndDate)
        project.put(LedgerDefine.AMOUNT, 0)

        btn_save.isEnabled = false
        docRef.set(project)
            .addOnSuccessListener(OnSuccessListener<Void> {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                showSnackBar()
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
                btn_save.setText(R.string.saved_successfully)
            })
    }

    fun isEmpty(str: String): Boolean {
        return TextUtils.isEmpty(str)
    }
}
