package com.datamangement.devast007.bahikhata.ui

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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_add_bank_account.*

class AddBankAccountActivity : AppCompatActivity(), View.OnClickListener {


    private var mAccountAddType: Int = LedgerDefine.BANK_ACCOUNT_ADD
    val TAG = "AddBankAccountActivity"
    val mContext = this
    val mDB = FirebaseFirestore.getInstance()
    var mCompanyID: String? = null
    var mAccountID: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bank_account)
        toolbar.setTitle(R.string.add_bank_account)
        setSupportActionBar(toolbar)
        btn_save.setOnClickListener(this)

        mCompanyID = LedgerSharePrefManger(mContext).getCompanyName()

        mAccountAddType = intent.getIntExtra(
            LedgerDefine.BANK_ACCOUNT_ADD_TYPE, -1
        )
        if (mAccountAddType == LedgerDefine.BANK_ACCOUNT_MODIFY) {
            mAccountID = intent.getStringExtra(LedgerDefine.BANK_ACCOUNT_ID)
            getAccountDetails()

        } else {
            fetchAccountID()
        }
    }

    private fun getAccountDetails() {
        val db = FirestoreDataBase().db
        db.collection(LedgerDefine.COMPANIES_SLASH + mCompanyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
            .whereEqualTo(LedgerDefine.BANK_ACCOUNT_ID, mAccountID)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        Log.d(TAG, " document.get(\"name\")+ => " + document.get("name"))
                        setAccountInfo(document)
                        break
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG)
                }
            })
    }

    private fun setAccountInfo(document: QueryDocumentSnapshot?) {

        if (document != null) {
            tv_bank_account_id.text = "Update Account : " + document.get(LedgerDefine.BANK_ACCOUNT_ID) as String
            et_bank_account_payee_name.setText(document.get(LedgerDefine.PAYEE_NAME) as String)
            et_bank_account_number.setText(document.get(LedgerDefine.BANK_ACCOUNT_NUMBER) as String)
            et_bank_account_ifsc_code.setText(document.get(LedgerDefine.IFSC_CODE) as String)
            et_bank_account_branch_name.setText(document.get(LedgerDefine.BANK_ACCOUNT_BRANCH_NAME) as String)
            et_bank_account_remarks.setText(document.get(LedgerDefine.REMARK) as String)
        }

    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.btn_save -> saveAccounts()
        }
    }


    private val BASE_ACCOUNT_ID: Long = 100

    private fun fetchAccountID() {
        var docRef = mDB.collection(LedgerDefine.COMPANIES_SLASH + mCompanyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
            .orderBy(LedgerDefine.BANK_ACCOUNT_ID, Query.Direction.DESCENDING).limit(1)
        docRef.get()
            .addOnSuccessListener(OnSuccessListener<QuerySnapshot> {
                Log.d(
                    TAG,
                    "DataFetched data =  " + it
                )
                var tempId: Long = 99
                if (it.documents.count() > 0) {
                    tempId = it.documents.get(0).get(LedgerDefine.BANK_ACCOUNT_ID).toString().toLong()
                }
                Log.d(
                    TAG,
                    "DataFetched tempUserId =  " + tempId
                )

                if (tempId!! >= BASE_ACCOUNT_ID) {
                    mAccountID = "" + (tempId + 1)
                } else {
                    mAccountID = "" + BASE_ACCOUNT_ID
                }

                tv_bank_account_id.setText(String.format(getString(R.string.new_user_id), mAccountID))

            })
            .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error writing document", e) })
    }

    private fun saveAccounts() {
        Log.d(TAG, "saveAccounts ")
        val accountNumber = et_bank_account_number.text.toString()
        var payeeName = et_bank_account_payee_name.text.toString()
        val ifscCode = et_bank_account_ifsc_code.text.toString()
        val branch = et_bank_account_branch_name.text.toString()
        val remarks = et_bank_account_remarks.text.toString()


        if (isEmpty(mAccountID)) {
            Toast.makeText(mContext, R.string.accout_id_is_empty, Toast.LENGTH_LONG).show()
            return
        }


        if (isEmpty(accountNumber)) {
            et_bank_account_number.error = getString(R.string.account_no_empty)
            Toast.makeText(mContext, R.string.account_no_empty, Toast.LENGTH_LONG).show()
            return
        }

        if (isEmpty(payeeName)) {
            Toast.makeText(mContext, R.string.payee_name_is_empty, Toast.LENGTH_LONG).show()
            return
        }
        payeeName = payeeName.toUpperCase().trim()
        btn_save.isEnabled = false
        var accounts = mDB.collection(LedgerDefine.COMPANIES_SLASH + mCompanyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
        var docRefNor = accounts.document(mAccountID!!)
        Log.d(TAG, "docRef " + docRefNor)
        // [START set_document]
        val account = HashMap<String, Any>()
        account[LedgerDefine.BANK_ACCOUNT_ID] = mAccountID!!
        account[LedgerDefine.BANK_ACCOUNT_NUMBER] = accountNumber
        account[LedgerDefine.PAYEE_NAME] = payeeName
        account[LedgerDefine.IFSC_CODE] = ifscCode
        account[LedgerDefine.BANK_ACCOUNT_BRANCH_NAME] = branch
        account[LedgerDefine.REMARK] = remarks
        account[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()

        if (mAccountAddType == LedgerDefine.BANK_ACCOUNT_MODIFY) {
            docRefNor.update(account).addOnSuccessListener(OnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully updated!")
                tv_bank_account_id.append(" Updated Successfully !!");
            }).addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error writing document", e) })
        } else {
            docRefNor.set(account)
                .addOnSuccessListener(OnSuccessListener<Void> {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                    tv_bank_account_id.append(" Done !!");
                    showSnackBar()
                })
                .addOnFailureListener(OnFailureListener { e -> Log.w(TAG, "Error writing document", e) })
        }
    }

    private fun showSnackBar() {
        val snackBar = Snackbar
            .make(coordinatorLayout, R.string.saved_successfully, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.add_more) {
                finish()
                startActivity(intent)
            }
        snackBar.setActionTextColor(Color.BLUE)
        val sbView = snackBar.view
        sbView.setBackgroundColor(Color.RED)
        val textView = sbView.findViewById<TextView>(android.support.design.R.id.snackbar_text)
        textView.setTextColor(Color.parseColor("#FF0B3E0D"))
        snackBar.show()

    }

    fun isEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }
}