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
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.excel.BankAccountsExcelSheet
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.adapter.BankAccountViewAdapter
import com.datamangement.devast007.bahikhata.utils.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_bank_account_view.*
import java.lang.RuntimeException
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
        supportActionBar!!.setTitle(R.string.bank_accounts)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item!!.itemId == R.id.action_add) {
            var intent = Intent(this, AddBankAccountActivity::class.java)
            intent.putExtra(
                LedgerDefine.LAUNCHED_FROM,
                LedgerDefine.LAUNCHED_FROM_VIEW_LIST_ACTIVITY
            )
            startActivity(intent)
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
            R.id.tv_bank_account_amount_sent -> launchTransactionViewActivity(
                view,
                LedgerDefine.SENT_TRANSACTION
            )
            R.id.tv_bank_account_amount_received -> launchTransactionViewActivity(
                view,
                LedgerDefine.RECEIVED_TRANSACTIONS
            )
            R.id.iv_share_account -> shareDetails(view)
        }
    }

    private fun modifyBankAccountInfo(view: View) {
        var accountID = view.getTag(R.string.tag_account_id).toString()
        var intent = Intent(this, AddBankAccountActivity::class.java)
        intent.putExtra(LedgerDefine.BANK_ACCOUNT_ADD_TYPE, LedgerDefine.BANK_ACCOUNT_MODIFY)
        intent.putExtra(LedgerDefine.BANK_ACCOUNT_ID, accountID)
        intent.putExtra(
            LedgerDefine.LAUNCHED_FROM,
            LedgerDefine.LAUNCHED_FROM_VIEW_LIST_ACTIVITY
        )
        startActivity(intent)
    }

    private fun launchTransactionViewActivity(view: View, sentOrReceived: Int) {
        val accountID: String = view.getTag(R.string.tag_account_id) as String
        val intent = Intent(mContext, TransactionViewActivity::class.java)
        intent.putExtra(
            LedgerDefine.TRANSACTION_VIEW_TYPE,
            LedgerDefine.TRANSACTION_VIEW_TYPE_BANK_ACCOUNT
        )
        intent.putExtra(LedgerDefine.ID, accountID)
        intent.putExtra(LedgerDefine.TRANSACTION_SENT_OR_RECEIVED, sentOrReceived)
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
                        mAccountsList.add(getAccountDetails(document))
                    }
                    // FirestoreDataBase().setBankAccounts(mAccountsList, mContext)
                    setAdapter()
                    syncBankAccounts()
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                    Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                }
            })
        return true;
    }

    private fun syncBankAccounts() {

        val cr: ContentResolver = mContext!!.contentResolver
        // check data base empty status for bank accounts

        /*     var values = ContentValues()
             values.put(LedgerDefine.RECEIVER_ID, userID)
             values.put(LedgerDefine.PROJECT_ID, projectID)
             values.put(LedgerDefine.CREDIT_ACCOUNT_ID, creditAccount)
             values.put(LedgerDefine.REMARK, remarks)
             val selectionArgs = Array<String>(1) { userID }
             val count = mContext!!.contentResolver.update(
                 SqlDBFile.CONTENT_URI_TABLE_SUGGESTION,
                 values,
                 LedgerDefine.RECEIVER_ID + " =?",
                 selectionArgs
             )
             Log.d(TAG, "savePreferenceToDB update count = " + count)
             if (count <= 0) {
                 mContext!!.contentResolver.insert(SqlDBFile.CONTENT_URI_TABLE_SUGGESTION, values)
                 Log.d(TAG, "savePreferenceToDB inserted ")

                 CREATE_TABLE + TABLE_BANK_ACCOUNTS + "(" + ID + INTEGER_AUTO_INCREAMENT + LedgerDefine.BANK_ACCOUNT_ID + TEXT_UNIQUE_COMMA_SPACE +
                    LedgerDefine.PAYEE_NAME + TEXT_COMMA_SPACE + LedgerDefine.BANK_ACCOUNT_NUMBER + TEXT_COMMA_SPACE +
                    LedgerDefine.IFSC_CODE + TEXT_COMMA_SPACE + LedgerDefine.BANK_ACCOUNT_BRANCH_NAME + TEXT_COMMA_SPACE +
                    LedgerDefine.TIME_STAMP + TEXT_COMMA_SPACE + LedgerDefine.AMOUNT + TEXT_COMMA_SPACE +
                    LedgerDefine.SERVER_MILLI + INTEGER_COMMA_SPACE + LedgerDefine.REMARK + " text " + ");"
             }*/

        val query = cr.query(
            SqlDBFile.CONTENT_URI_TABLE_BANK_ACCOUNTS,
            null,
            null,
            null,
            LedgerDefine.SYSTEM_MILLI + " DESC"
        )
        // if empty then store all data from list view to sql db of bank accounts
        Log.w(
            TAG,
            "map data " + query!!.count
        )
        if (query!!.count <= 0) {
            for (accounts in mAccountsList) {

                val uri =
                    cr.insert(SqlDBFile.CONTENT_URI_TABLE_BANK_ACCOUNTS, getContentValues(accounts))
                Log.d(TAG, "cr.insert(SqlDBFile.CONTENT_URI_TABLE_BANK_ACCOUNTS, cv) = $uri")

            }
            //cr.insert()
        } else {
            query.moveToFirst()
            var systemMill = query.getLong(query.getColumnIndex(LedgerDefine.SYSTEM_MILLI))
            if (systemMill == null) systemMill = 0

            val db = FirestoreDataBase().db
            val companyID = LedgerSharePrefManger(this!!.mContext).getCompanyID()
            db.collection(LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_BANK_ACCOUNTS)
                .whereGreaterThan(LedgerDefine.SYSTEM_MILLI, systemMill)
                .orderBy(LedgerDefine.SYSTEM_MILLI, Query.Direction.ASCENDING).get()
                .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "task.result!!.size()" + task.result!!.size())
                        for (document in task.result!!) {
                            Log.d(TAG, "Error getting documents: document.data " + document.data)
                            val cv = getContentValues(getAccountDetails(document))
                            val selectionArgs =
                                Array<String>(1) { "" + cv[LedgerDefine.BANK_ACCOUNT_ID] }
                            val count = cr.update(
                                SqlDBFile.CONTENT_URI_TABLE_BANK_ACCOUNTS, cv,
                                LedgerDefine.BANK_ACCOUNT_ID + "=?", selectionArgs!!
                            )
                            Log.d(
                                TAG,
                                " update SqlDBFile.CONTENT_URI_TABLE_BANK_ACCOUNT= $count" + " name = " + cv[LedgerDefine.PAYEE_NAME]
                            )
                        }

                    } else {
                        Log.d(TAG, "Error getting documents: ", task.exception)
                        Toast.makeText(mContext, R.string.error_01, Toast.LENGTH_LONG).show()
                    }
                })

        }

        query.close()
        // if not, get the highest servermilli and get the all documents from server greater than the sql db servermill
        // then update the same if any

    }

    private fun getContentValues(accounts: BankAccountDetail): ContentValues {
        val cv = ContentValues()
        cv.put(LedgerDefine.BANK_ACCOUNT_ID, accounts.id!!)
        cv.put(LedgerDefine.PAYEE_NAME, accounts.payee!!)
        cv.put(LedgerDefine.BANK_ACCOUNT_NUMBER, accounts.accountNo!!)
        cv.put(LedgerDefine.IFSC_CODE, accounts.ifscCode!!)
        cv.put(LedgerDefine.BANK_ACCOUNT_BRANCH_NAME, accounts.branch!!)
        cv.put(LedgerDefine.TIME_STAMP, accounts.timestamp!!)
        cv.put(LedgerDefine.AMOUNT, accounts.amount!!)
        cv.put(LedgerDefine.SYSTEM_MILLI, accounts.systemMill!!)
        cv.put(LedgerDefine.REMARK, accounts.remarks!!)

        return cv

    }

    private fun getAccountDetails(document: QueryDocumentSnapshot?): BankAccountDetail {
        var accountDetails: BankAccountDetail = BankAccountDetail()
        if (document != null) {
            accountDetails.id = document.get(LedgerDefine.BANK_ACCOUNT_ID) as String
            accountDetails.payee = document.get(LedgerDefine.PAYEE_NAME) as String
            accountDetails.accountNo = document.get(LedgerDefine.BANK_ACCOUNT_NUMBER) as String
            accountDetails.ifscCode = document.get(LedgerDefine.IFSC_CODE) as String
            accountDetails.branch = document.get(LedgerDefine.BANK_ACCOUNT_BRANCH_NAME) as String


            try {
                accountDetails.timestamp =
                    document.getTimestamp(LedgerDefine.TIME_STAMP)!!.toDate().toString()
            } catch (e: RuntimeException) {
                accountDetails.timestamp = document.get(LedgerDefine.TIME_STAMP).toString()
            } catch (e: ClassCastException) {
                accountDetails.timestamp =
                    (document.get(LedgerDefine.TIME_STAMP) as Date).toString()
            }

            val amount = document.get(LedgerDefine.AMOUNT)
            if (amount != null) accountDetails.amount = amount as Long
            accountDetails.remarks = document.get(LedgerDefine.REMARK) as String

            val systemMilli = document.get(LedgerDefine.SYSTEM_MILLI)
            if (systemMilli != null) accountDetails.systemMill = systemMilli as Long

        }
        return accountDetails
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
