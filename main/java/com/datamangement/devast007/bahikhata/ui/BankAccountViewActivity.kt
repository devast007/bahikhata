package com.datamangement.devast007.bahikhata.ui

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.excel.BankAccountsExcelSheet
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.adapter.BankAccountViewAdapter
import com.datamangement.devast007.bahikhata.utils.BankAccountDetail
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.Query
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
        menuInflater.inflate(R.menu.menu_add_button_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item!!.itemId == R.id.action_add) {
            startActivity(Intent(this, AddBankAccountActivity::class.java));
            return true
        }else if (item!!.itemId == R.id.action_excel_file) {
            createExcelSheet()
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
        expandable_list_view.setAdapter(mAccountAdapter)
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
            .orderBy(LedgerDefine.PAYEE_NAME, Query.Direction.ASCENDING).get()
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

    private fun isStoragePermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true
            } else {

                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                return false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true
        }
    }

    var mDialog: ProgressDialog? = null
    fun createExcelSheet() {

        if (!isStoragePermissionGranted()) return
        mDialog = ProgressDialog.show(
            mContext,
            getString(R.string.creating_file),
            getString(R.string.please_wait),
            false
        )
        val filepath = BankAccountsExcelSheet(mContext, mAccountsList).getFilePath()
        mDialog!!.dismiss()
        val dataType = "application/vnd.ms-excel"
        if (filepath != null) {
            try {
                var builder = AlertDialog.Builder(mContext);
                builder.setMessage("File Is Downloaded...")
                    .setPositiveButton("OPEN", DialogInterface.OnClickListener { dialogInterface, i ->
                        val newIntent = Intent(Intent.ACTION_VIEW)
                        newIntent.setDataAndType(Uri.parse("file://$filepath"), dataType)
                        newIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                        mContext!!.startActivity(newIntent)
                    })
                    .setNegativeButton("SHARE", DialogInterface.OnClickListener { dialogInterface, i ->
                        var intentShareFile = Intent(Intent.ACTION_SEND);

                        intentShareFile.setType(dataType);
                        intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$filepath"));

                        intentShareFile.putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Sharing File..."
                        );
                        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

                        startActivity(Intent.createChooser(intentShareFile, "Share File"));
                    }).show()
            } catch (e: Exception) {
                toast(R.string.file_not_found)
            }
        }
    }

    private fun toast(id: Int) {
        Toast.makeText(mContext, id, Toast.LENGTH_LONG).show()
    }
}
