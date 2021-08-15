package com.datamangement.devast007.bahikhata.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.SqlDBFile
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_add_bank_account.*
import kotlinx.android.synthetic.main.activity_add_bank_account.btn_save
import kotlinx.android.synthetic.main.activity_add_bank_account.coordinatorLayout


class AddBankAccountActivity : AppCompatActivity(), View.OnClickListener,
    LedgerUtils.Companion.StatusListner {


    private var mAccountAddType: Int = LedgerDefine.BANK_ACCOUNT_ADD
    private var mLaunchedFrom: Int = -1
    val TAG = "AddBankAccountActivity"
    val mContext = this
    val mDB = FirebaseFirestore.getInstance()
    var mCompanyID: String? = null
    var mAccountID: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bank_account)
        supportActionBar!!.setTitle(R.string.add_bank_account)
        btn_save.setOnClickListener(this)

        mCompanyID = LedgerSharePrefManger(mContext).getCompanyID()

        mAccountAddType = intent.getIntExtra(
            LedgerDefine.BANK_ACCOUNT_ADD_TYPE, -1
        )

        mLaunchedFrom = intent.getIntExtra(
            LedgerDefine.LAUNCHED_FROM, -1
        )

        if (mAccountAddType == LedgerDefine.BANK_ACCOUNT_MODIFY) {
            mAccountID = intent.getStringExtra(LedgerDefine.BANK_ACCOUNT_ID)
            getAccountDetails()

        } else {
            fetchAccountID()
        }

        findViewById<EditText>(R.id.et_bank_account_number).addTextChangedListener(object :
            TextWatcher {
            override fun onTextChanged(cs: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
            }

            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
            }

            override fun afterTextChanged(arg0: Editable) {

                val account = arg0.toString()
                Log.d(TAG, " afterTextChanged Error getting documents: $account ")
                if (!isEmpty(account)) {
                    et_bank_account_number.setTextColor(Color.BLACK)
                    val db = FirestoreDataBase().db
                    db.collection(LedgerDefine.COMPANIES_SLASH + mCompanyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
                        .whereEqualTo(LedgerDefine.BANK_ACCOUNT_NUMBER, account)
                        .get()
                        .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                            if (task.isSuccessful) {
                                if (task.result!!.size() > 0) {
                                    et_bank_account_number.setTextColor(Color.RED)
                                    Toast.makeText(
                                        applicationContext,
                                        "Duplicate Account Number $account",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.exception)
                                Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG)
                            }
                        })
                }

            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (mLaunchedFrom == LedgerDefine.LAUNCHED_FROM_VIEW_LIST_ACTIVITY) {
            val newIntent = Intent(mContext, BankAccountViewActivity::class.java)
            newIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            mContext!!.startActivity(newIntent)

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
            tv_bank_account_id.text =
                "Update Account : " + document.get(LedgerDefine.BANK_ACCOUNT_ID) as String
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
        var docRef =
            mDB.collection(LedgerDefine.COMPANIES_SLASH + mCompanyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
                .orderBy(LedgerDefine.BANK_ACCOUNT_ID, Query.Direction.DESCENDING).limit(1)
        docRef.get()
            .addOnSuccessListener(OnSuccessListener<QuerySnapshot> {
                var tempId: Long = 99
                if (it.documents.count() > 0) {
                    tempId =
                        it.documents.get(0).get(LedgerDefine.BANK_ACCOUNT_ID).toString().toLong()
                }

                if (tempId!! >= BASE_ACCOUNT_ID) {
                    mAccountID = "" + (tempId + 1)
                } else {
                    mAccountID = "" + BASE_ACCOUNT_ID
                }

                tv_bank_account_id.setText(
                    String.format(
                        getString(R.string.new_user_id),
                        mAccountID
                    )
                )

            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w(
                    TAG,
                    "Error writing document",
                    e
                )
            })
    }

    private fun saveAccounts() {
        var accountNumber = et_bank_account_number.text.toString()
        var payeeName = et_bank_account_payee_name.text.toString()
        var ifscCode = et_bank_account_ifsc_code.text.toString()
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

        if (ifscCode.length != 11) {
            Toast.makeText(mContext, R.string.give_bank_account_ifsc_code, Toast.LENGTH_LONG).show()
            return
        }

        payeeName = payeeName.toUpperCase().trim()
        ifscCode = ifscCode.toUpperCase().trim()
        accountNumber = accountNumber.toUpperCase().trim()

        btn_save.isEnabled = false
        var accounts =
            mDB.collection(LedgerDefine.COMPANIES_SLASH + mCompanyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
        var docRefNor = accounts.document(mAccountID!!)
        // [START set_document]
        val accountMap = HashMap<String, Any>()
        accountMap[LedgerDefine.BANK_ACCOUNT_ID] = mAccountID!!
        accountMap[LedgerDefine.BANK_ACCOUNT_NUMBER] = accountNumber
        accountMap[LedgerDefine.PAYEE_NAME] = payeeName
        accountMap[LedgerDefine.IFSC_CODE] = ifscCode
        accountMap[LedgerDefine.BANK_ACCOUNT_BRANCH_NAME] = branch
        accountMap[LedgerDefine.REMARK] = remarks
        accountMap[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()

        LedgerUtils.setstatusListner(this)
        if (mAccountAddType == LedgerDefine.BANK_ACCOUNT_MODIFY) {
            LedgerUtils.setDataToFirestore(
                mContext!!,
                accountMap[LedgerDefine.BANK_ACCOUNT_ID]!!,
                SqlDBFile.CONTENT_URI_TABLE_BANK_ACCOUNTS,
                LedgerDefine.UPDATE_DATA,
                docRefNor,
                accountMap
            )
        } else {
            LedgerUtils.setDataToFirestore(
                mContext!!,
                accountMap[LedgerDefine.BANK_ACCOUNT_ID]!!,
                SqlDBFile.CONTENT_URI_TABLE_BANK_ACCOUNTS,
                LedgerDefine.SET_DATA,
                docRefNor,
                accountMap
            )

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
        val textView = sbView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setTextColor(Color.parseColor("#FF0B3E0D"))
        snackBar.show()

    }

    fun isEmpty(str: String?): Boolean {
        return TextUtils.isEmpty(str)
    }

    override fun onComplete(status: Boolean) {

        if (status) {
            if (mAccountAddType == LedgerDefine.BANK_ACCOUNT_MODIFY) {
                tv_bank_account_id.append(" Updated Successfully !!")
                btn_save.setText(R.string.update_done)

            } else {
                tv_bank_account_id.append(" Done !!");
                showSnackBar()
            }

        } else {
            Log.w(
                TAG,
                "Error writing document"

            )
            Toast.makeText(mContext, "Error writing document", Toast.LENGTH_LONG).show()


        }


    }
}