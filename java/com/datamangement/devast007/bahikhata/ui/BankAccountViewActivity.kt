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
import com.datamangement.devast007.bahikhata.ui.adapter.BankAccountViewAdapter
import com.datamangement.devast007.bahikhata.utils.BankAccountDetail
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_bank_account_view.*
import java.util.*

class BankAccountViewActivity : AppCompatActivity(), View.OnClickListener {

    val TAG = "BankAccountViewActivity"
    var mContext: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank_account_view)
        mContext = this
        toolbar.setTitle(R.string.bank_accounts)
        setSupportActionBar(toolbar)


    }

    override fun onStart() {
        super.onStart()
        mAccountsList.clear()
        getAccounts()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_add_button_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item!!.itemId == R.id.action_add) {
            startActivity(Intent(this, AddBankAccountActivity::class.java));
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private val mAccountsList: ArrayList<BankAccountDetail> = ArrayList<BankAccountDetail>()

    private var mAccountAdapter: BankAccountViewAdapter? = null

    private fun setAdapter() {
        if (mAccountAdapter == null) {
            mAccountAdapter = BankAccountViewAdapter(this, mAccountsList)
        }else{
            mAccountAdapter!!.notifyDataSetChanged()
        }
        expadable_list_view.setAdapter(mAccountAdapter)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tv_amount -> launchTransactionviewActivity(view)
        }
    }

    private fun launchTransactionviewActivity(view: View) {
        // todo need to launh it specific to account no
        val projectId = view.getTag(R.string.tag_project_id)
        Toast.makeText(this, "Not Implement yet !! " + projectId, Toast.LENGTH_LONG).show()
    }

    private fun getAccounts(): Boolean {
        mAccountsList.clear()
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyName()
        Log.d(TAG, "companyID => " + companyID)
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        Log.d(TAG, document.id + " => " + document.data)
                        Log.d(TAG, " document.get(\"name\")+ => " + document.get("name"))
                        setSetAccount(document);
                    }
                    setAdapter()
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
        return true;
    }

    private fun setSetAccount(document: QueryDocumentSnapshot?) {
        if (document != null) {
            var accountDetails: BankAccountDetail = BankAccountDetail()
            accountDetails.id = document.get(LedgerDefine.BANK_ACCOUNT_ID) as String
            accountDetails.payee = document.get(LedgerDefine.PAYEE_NAME) as String
            accountDetails.accountNo = document.get(LedgerDefine.BANK_ACCOUNT_NUMBER) as String
            accountDetails.ifscCode = document.get(LedgerDefine.IFSC_CODE) as String
            accountDetails.branch = document.get(LedgerDefine.BANK_ACCOUNT_BRANCH_NAME) as String
            accountDetails.timestamp = (document.get(LedgerDefine.TIME_STAMP) as Date).toString()
            val amount = document.get(LedgerDefine.AMOUNT)
            if (amount != null) accountDetails.amount = amount as Long
            accountDetails.remarks = document.get(LedgerDefine.REMARK) as String
            mAccountsList.add(accountDetails)
        }
    }
}
