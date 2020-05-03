package com.datamangement.devast007.bahikhata.ui

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.SearchManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
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

    private val mAccountsList: ArrayList<BankAccountDetail> = ArrayList<BankAccountDetail>()
    private val mSearchedAccountsList: ArrayList<BankAccountDetail> = ArrayList<BankAccountDetail>()

    val TAG = "BankAccountViewActivity"
    var mContext: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank_account_view)
        mContext = this
        toolbar.setTitle(R.string.bank_accounts)
        setSupportActionBar(toolbar)
        getAccounts()


    }

    override fun onStart() {
        super.onStart()
    }

    var queryTextListener: SearchView.OnQueryTextListener =
        object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                matchTextInList(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {

                return true
            }
        }

    private fun matchTextInList(newText: String) {
        mSearchedAccountsList.clear()
        if (!TextUtils.isEmpty(newText)) {

            for (account in mAccountsList) {

                if (account.payee.toLowerCase()
                        .contains(newText.toLowerCase()) || account.accountNo.contains(
                        newText
                    )
                ) {
                    mSearchedAccountsList.add(account)
                }
            }
            mAccountAdapter = BankAccountViewAdapter(this, mSearchedAccountsList)
        } else {
            mAccountAdapter = BankAccountViewAdapter(this, mAccountsList)
        }
        expandable_list_view.setAdapter(mAccountAdapter)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bank_accounts_view, menu)

        val searchItem: MenuItem? = menu?.findItem(R.id.action_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView: SearchView? = searchItem?.actionView as SearchView

        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView?.setOnQueryTextListener(queryTextListener)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item!!.itemId == R.id.action_add) {
            startActivity(Intent(this, AddBankAccountActivity::class.java));
            return true
        } else if (item!!.itemId == R.id.action_excel_file) {
            createExcelSheet()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private var mAccountAdapter: BankAccountViewAdapter? = null

    private fun setAdapter() {
        if (mAccountAdapter == null) {
            mAccountAdapter = BankAccountViewAdapter(this, mAccountsList)
        } else {
            mAccountAdapter!!.notifyDataSetChanged()
        }
        expandable_list_view.setAdapter(mAccountAdapter)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tv_edit_bank_account_info -> modifyBankAccountInfo(view)
            R.id.tv_bank_account_amount -> launchTransactionViewActivity(view)
            R.id.iv_share_account -> shareDetails(view)
        }
    }

    private fun modifyBankAccountInfo(view: View) {
        var accountID = view.getTag(R.string.tag_account_id).toString()
        var intent = Intent(this, AddBankAccountActivity::class.java)
        intent.putExtra(LedgerDefine.BANK_ACCOUNT_ADD_TYPE, LedgerDefine.BANK_ACCOUNT_MODIFY)
        intent.putExtra(LedgerDefine.BANK_ACCOUNT_ID, accountID)
        startActivity(intent)
    }

    private fun launchTransactionViewActivity(view: View) {
        val accountID: String = view.getTag(R.string.tag_account_id) as String
        val intent = Intent(mContext, TransactionViewActivity::class.java)
        intent.putExtra(
            LedgerDefine.TRANSACTION_VIEW_TYPE,
            LedgerDefine.TRANSACTION_VIEW_TYPE_BANK_ACCOUNT
        )
        intent.putExtra(LedgerDefine.ID, accountID)
        startActivity(intent)
    }

    private fun getAccounts(): Boolean {

        mAccountsList.clear()
        val db = FirestoreDataBase().db
        val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
        db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
            .orderBy(LedgerDefine.PAYEE_NAME, Query.Direction.ASCENDING).get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        setSetAccount(document);
                    }
                    // FirestoreDataBase().setBankAccounts(mAccountsList, mContext)
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

            var timestamp = document.get(LedgerDefine.TIME_STAMP)
            if (timestamp != null) {
                try {
                    accountDetails.timestamp = timestamp as String
                } catch (e: java.lang.ClassCastException) {
                    accountDetails.timestamp = (timestamp as Date).toString()
                }
            }

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

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                return false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true
        }
    }

    var mDialog: ProgressDialog? = null
    private fun createExcelSheet() {

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
                    .setPositiveButton(
                        "OPEN",
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            val newIntent = Intent(Intent.ACTION_VIEW)
                            newIntent.setDataAndType(Uri.parse("file://$filepath"), dataType)
                            newIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                            mContext!!.startActivity(newIntent)
                        })
                    .setNegativeButton(
                        "SHARE",
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            var intentShareFile = Intent(Intent.ACTION_SEND);

                            intentShareFile.setType(dataType);
                            intentShareFile.putExtra(
                                Intent.EXTRA_STREAM,
                                Uri.parse("file://$filepath")
                            );

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

    fun shareDetails(view: View) {
        val position = view.getTag(R.string.tag_position) as Int
        var accountDetails: BankAccountDetail? = null
        if (mSearchedAccountsList.size != 0) {
            accountDetails = mSearchedAccountsList[position]
        } else {
            accountDetails = mAccountsList[position]
        }
        val accountInfo = accountDetails.payee + "\n" + accountDetails.accountNo + "\n" +
                accountDetails.ifscCode + "\n" + accountDetails.branch

        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, accountInfo)
        startActivity(
            Intent.createChooser(
                sharingIntent,
                resources.getString(R.string.share_using)
            )
        )
    }
}
