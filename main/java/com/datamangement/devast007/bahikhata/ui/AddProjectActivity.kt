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
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_add_project.*
import java.util.*

class AddProjectActivity : AppCompatActivity(), View.OnClickListener {
    private val mContext: Context? = this
    private val TAG: String = "AddProjectActivity"

    private var mAuth: FirebaseAuth? = null

    private lateinit var mCompanyName: String

    private var mProjectID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)
        setSupportActionBar(toolbar)

        val mDB = FirebaseFirestore.getInstance()
        mCompanyName = LedgerSharePrefManger(mContext).getCompanyName()
        btn_save.setOnClickListener(this)
        mAuth = FirebaseAuth.getInstance()
        mProjectID = intent.getStringExtra(LedgerDefine.PROJECT_ID)
        if (isEmpty(mProjectID)) {
            // create new project
            fetchProjectIDPrefix()
        } else {
            supportActionBar!!.subtitle = mProjectID
            getDataFromProjectID()
        }

    }

    private fun getDataFromProjectID() {
        var docRef = FirestoreDataBase().db.collection(LedgerDefine.COMPANIES_SLASH + mCompanyName + "/projects")
            .whereEqualTo(LedgerDefine.PROJECT_ID, mProjectID)

        docRef.get()
            .addOnSuccessListener(OnSuccessListener<QuerySnapshot> {
                if (it.size() > 0) {
                    var doc = it.documents.get(0)

                    tv_project_id.text = getStringFormDoc(doc, LedgerDefine.PROJECT_ID)
                    et_project_name.setText(getStringFormDoc(doc, LedgerDefine.NAME))
                    et_project_address.setText(getStringFormDoc(doc, LedgerDefine.ADDRESS))
                    et_project_division.setText(getStringFormDoc(doc, LedgerDefine.DIVISION))
                    et_remarks.setText(getStringFormDoc(doc, LedgerDefine.REMARK))
                    et_project_start_date.setText(getStringFormDoc(doc, LedgerDefine.START_DATE))
                    et_project_end_date.setText(getStringFormDoc(doc, LedgerDefine.END_DATE))
                    et_project_mb_no.setText(getStringFormDoc(doc, LedgerDefine.MB_NO))
                    et_project_head.setText(getStringFormDoc(doc, LedgerDefine.HEAD))
                    et_project_main_amount.setText(getStringFormDoc(doc, LedgerDefine.MAIN_AMOUNT))
                    et_project_1st_maintenance.setText(getStringFormDoc(doc, LedgerDefine.MAINTENANCE_1ST_YEAR_AMOUNT))
                    et_project_2nd_maintenance.setText(getStringFormDoc(doc, LedgerDefine.MAINTENANCE_2ND_YEAR_AMOUNT))
                    et_project_3rd_maintenance.setText(getStringFormDoc(doc, LedgerDefine.MAINTENANCE_3RD_YEAR_AMOUNT))
                    et_project_4th_maintenance.setText(getStringFormDoc(doc, LedgerDefine.MAINTENANCE_4TH_YEAR_AMOUNT))
                    et_project_5th_maintenance.setText(getStringFormDoc(doc, LedgerDefine.MAINTENANCE_5TH_YEAR_AMOUNT))

                }
            })
            .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error writing document", e) })
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



        if (mProjectID == null && isEmpty(mIdPrefix)) {
            tv_project_id.error = getString(R.string.error_04)
            Toast.makeText(mContext, R.string.error_03, Toast.LENGTH_LONG).show()
            return
        }
        if (isEmpty(projectName)) {
            et_project_name.error = getString(R.string.error_05)
            Toast.makeText(mContext, R.string.error_03, Toast.LENGTH_LONG).show()
            return
        }

        // [START set_document]
        val project = HashMap<String, Any>()
        project[LedgerDefine.NAME] = projectName
        project[LedgerDefine.ADDRESS] = projectAddress
        project[LedgerDefine.DIVISION] = projectDiv
        project[LedgerDefine.REMARK] = projectNote
        project[LedgerDefine.START_DATE] = projectStartDate
        project[LedgerDefine.END_DATE] = projectEndDate
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
        val companyName = LedgerSharePrefManger(mContext).getCompanyName()
        if (isEmpty(mProjectID)) {
            mProjectID = mIdPrefix + projectName.toUpperCase().replace(" ", "", true)
            project[LedgerDefine.AMOUNT] = 0
            project[LedgerDefine.PROJECT_ID] = mProjectID!!
            var docRef = db.collection(LedgerDefine.COMPANIES_SLASH + companyName + "/projects").document(mProjectID!!)
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
            var docRef = db.collection(LedgerDefine.COMPANIES_SLASH + companyName + "/projects").document(mProjectID!!)
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

    private var mIdPrefix: String? = null

    private fun fetchProjectIDPrefix() {
        var docRef = FirestoreDataBase().db.collection(LedgerDefine.COMPANIES_SLASH + mCompanyName + "/projects")
            .orderBy(LedgerDefine.PROJECT_ID, Query.Direction.DESCENDING).limit(1)
        docRef.get()
            .addOnSuccessListener(OnSuccessListener<QuerySnapshot> {
                Log.d(
                    TAG,
                    "DataFetched data =  " + it.size()
                )
                if (it.size() <= 0) {
                    mIdPrefix = "001_"
                } else {
                    var tempUserId: String = it.documents.get(0).get(LedgerDefine.PROJECT_ID).toString()
                    Log.d(
                        TAG,
                        "DataFetched data = pre get " + tempUserId.substring(0, 3)
                    )
                    var pre = tempUserId.substring(0, 3).toInt() + 1
                    Log.d(
                        TAG,
                        "DataFetched data = pre get " + pre
                    )
                    if (pre < 10) {
                        mIdPrefix = "00" + pre + "_"
                    } else if (pre < 999) {
                        mIdPrefix = "0" + pre + "_"
                    }


                }

                tv_project_id.text = mIdPrefix
            })
            .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error writing document", e) })
    }

    fun getStringFormDoc(document: DocumentSnapshot?, key: String): String {
        var data: Any? = document!!.get(key) ?: return ""

        return data as String

    }
}
