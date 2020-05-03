package com.datamangement.devast007.bahikhata.ui

import android.content.Context
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
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_add_project.*
import java.util.*

class AddProjectActivity : AppCompatActivity(), View.OnClickListener {
    private val mContext: Context? = this
    private val TAG: String = "AddProjectActivity"

    private var mAuth: FirebaseAuth? = null

    private lateinit var mCompanyName: String

    private var mProjectIDToUpdate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)
        setSupportActionBar(toolbar)

        val mDB = FirebaseFirestore.getInstance()
        mCompanyName = LedgerSharePrefManger(mContext).getCompanyID()
        btn_save.setOnClickListener(this)
        mAuth = FirebaseAuth.getInstance()
        mProjectIDToUpdate = intent.getStringExtra(LedgerDefine.PROJECT_ID)
        if (isEmpty(mProjectIDToUpdate)) {
            // create new project
            fetchProjectIDPrefix()
        } else {
            supportActionBar!!.subtitle = mProjectIDToUpdate
            getDataFromProjectID()
        }

    }

    private fun getDataFromProjectID() {
        var docRef =
            FirestoreDataBase().db.collection(LedgerDefine.COMPANIES_SLASH + mCompanyName + LedgerDefine.SLASH_PROJECTS)
                .whereEqualTo(LedgerDefine.PROJECT_ID, mProjectIDToUpdate)

        docRef.get()
            .addOnSuccessListener(OnSuccessListener<QuerySnapshot> {
                if (it.size() > 0) {
                    var doc = it.documents.get(0)

                    tv_project_id.text = getStringFormDoc(doc, LedgerDefine.PROJECT_ID)
                    et_project_name.setText(getStringFormDoc(doc, LedgerDefine.NAME))
                    et_project_nickname.setText(getStringFormDoc(doc, LedgerDefine.NICKNAME))
                    et_project_address.setText(getStringFormDoc(doc, LedgerDefine.ADDRESS))
                    et_project_division.setText(getStringFormDoc(doc, LedgerDefine.DIVISION))
                    et_remarks.setText(getStringFormDoc(doc, LedgerDefine.REMARK))
                    et_project_start_date.setText(getStringFormDoc(doc, LedgerDefine.START_DATE))
                    et_project_end_date.setText(getStringFormDoc(doc, LedgerDefine.END_DATE))
                    et_project_mb_no.setText(getStringFormDoc(doc, LedgerDefine.MB_NO))
                    et_project_head.setText(getStringFormDoc(doc, LedgerDefine.HEAD))
                    et_project_main_amount.setText(getStringFormDoc(doc, LedgerDefine.MAIN_AMOUNT))
                    et_project_1st_maintenance.setText(
                        getStringFormDoc(
                            doc,
                            LedgerDefine.MAINTENANCE_1ST_YEAR_AMOUNT
                        )
                    )
                    et_project_2nd_maintenance.setText(
                        getStringFormDoc(
                            doc,
                            LedgerDefine.MAINTENANCE_2ND_YEAR_AMOUNT
                        )
                    )
                    et_project_3rd_maintenance.setText(
                        getStringFormDoc(
                            doc,
                            LedgerDefine.MAINTENANCE_3RD_YEAR_AMOUNT
                        )
                    )
                    et_project_4th_maintenance.setText(
                        getStringFormDoc(
                            doc,
                            LedgerDefine.MAINTENANCE_4TH_YEAR_AMOUNT
                        )
                    )
                    et_project_5th_maintenance.setText(
                        getStringFormDoc(
                            doc,
                            LedgerDefine.MAINTENANCE_5TH_YEAR_AMOUNT
                        )
                    )

                }
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w(
                    TAG,
                    "Error writing document",
                    e
                )
            })
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
        val projectName = et_project_name.text.toString()
        val projectNickName = et_project_nickname.text.toString()
        val projectAddress = et_project_address.text.toString()
        val projectDiv = et_project_division.text.toString()
        val projectNote = et_remarks.text.toString()
        val projectStartDate = et_project_start_date.text.toString()
        val projectEndDate = et_project_end_date.text.toString()
        val projectMBNo = et_project_mb_no.text.toString()
        val projectHead = et_project_head.text.toString()
        val projectMainAmount = et_project_main_amount.text.toString()
        val projectMaintenance1stYearAmount = et_project_1st_maintenance.text.toString()
        val projectMaintenance2ndYearAmount = et_project_2nd_maintenance.text.toString()
        val projectMaintenance3rdYearAmount = et_project_3rd_maintenance.text.toString()
        val projectMaintenance4thYearAmount = et_project_4th_maintenance.text.toString()
        val projectMaintenance5thYearAmount = et_project_5th_maintenance.text.toString()



        if (isEmpty(mProjectIDToUpdate) == null && isEmpty(mNewProjectID)) {
            tv_project_id.error = getString(R.string.error_04)
            Toast.makeText(mContext, R.string.error_03, Toast.LENGTH_LONG).show()
            return
        }
        if (isEmpty(projectName)) {
            et_project_name.error = getString(R.string.error_05)
            return
        }

        if (isEmpty(projectNickName)) {
            et_project_nickname.error = getString(R.string.error_05)
            return
        }

        // [START set_document]
        val project = HashMap<String, Any>()
        project[LedgerDefine.NAME] = projectName
        project[LedgerDefine.NICKNAME] = projectNickName
        project[LedgerDefine.ADDRESS] = projectAddress
        project[LedgerDefine.DIVISION] = projectDiv
        project[LedgerDefine.REMARK] = projectNote
        project[LedgerDefine.START_DATE] = projectStartDate
        project[LedgerDefine.END_DATE] = projectEndDate
        project[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()
        project[LedgerDefine.MB_NO] = projectMBNo
        project[LedgerDefine.HEAD] = projectHead
        project[LedgerDefine.MAIN_AMOUNT] = projectMainAmount
        project[LedgerDefine.MAINTENANCE_1ST_YEAR_AMOUNT] = projectMaintenance1stYearAmount
        project[LedgerDefine.MAINTENANCE_2ND_YEAR_AMOUNT] = projectMaintenance2ndYearAmount
        project[LedgerDefine.MAINTENANCE_3RD_YEAR_AMOUNT] = projectMaintenance3rdYearAmount
        project[LedgerDefine.MAINTENANCE_4TH_YEAR_AMOUNT] = projectMaintenance4thYearAmount
        project[LedgerDefine.MAINTENANCE_5TH_YEAR_AMOUNT] = projectMaintenance5thYearAmount

        btn_save.isEnabled = false
        val db = FirebaseFirestore.getInstance()
        val companyName = LedgerSharePrefManger(mContext).getCompanyID()
        if (isEmpty(mProjectIDToUpdate)) {
            project[LedgerDefine.AMOUNT] = 0
            project[LedgerDefine.PROJECT_ID] = mNewProjectID!!
            var docRef =
                db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_PROJECTS)
                    .document(mNewProjectID!!)
            Log.d(TAG, "docRef CRAETRE  $docRef")

            docRef.set(project)
                .addOnSuccessListener(OnSuccessListener<Void> {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                    showSnackBar()
                })
                .addOnFailureListener(OnFailureListener { e ->
                    Log.w(TAG, "Error writing document", e)
                    btn_save.setText(R.string.failed)
                })
        } else {
            var docRef =
                db.collection(LedgerDefine.COMPANIES_SLASH + companyName + LedgerDefine.SLASH_PROJECTS)
                    .document(mProjectIDToUpdate!!)
            Log.d(TAG, "docRef for update $docRef")

            docRef.update(project)
                .addOnSuccessListener(OnSuccessListener<Void> {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                    btn_save.setText(R.string.update_done)
                })
                .addOnFailureListener(OnFailureListener { e ->
                    Log.w(TAG, "Error writing document", e)
                    btn_save.setText(R.string.update_failed)
                })
        }

    }

    fun isEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }

/*    override fun onBackPressed() {
        val intent = Intent(this, GoogleSigninActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }*/

    private var mNewProjectID: String? = null

    private fun fetchProjectIDPrefix() {
        var docRef =
            FirestoreDataBase().db.collection(LedgerDefine.COMPANIES_SLASH + mCompanyName + LedgerDefine.SLASH_PROJECTS)
                .orderBy(LedgerDefine.PROJECT_ID, Query.Direction.DESCENDING).limit(1)
        docRef.get()
            .addOnSuccessListener(OnSuccessListener<QuerySnapshot> {
                if (it.size() <= 0) {
                    mNewProjectID = "001"
                } else {
                    var tempUserId: String =
                        it.documents.get(0).get(LedgerDefine.PROJECT_ID).toString()
                    var pre = tempUserId.substring(0, 3).toInt() + 1
                    if (pre < 10) {
                        mNewProjectID = "00$pre"
                    } else if (pre < 100) {
                        mNewProjectID = "0$pre"
                    }


                }
                mNewProjectID += "_PROJECT"
                tv_project_id.text = mNewProjectID
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w(
                    TAG,
                    "Error writing document",
                    e
                )
            })
    }

    fun getStringFormDoc(document: DocumentSnapshot?, key: String): String {
        var data: Any? = document!!.get(key) ?: return ""

        return data as String

    }
}
