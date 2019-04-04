package com.datamangement.devast007.bahikhata.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SearchView
import android.widget.SimpleAdapter
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.AddTransactionActivity
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.SqlDBFile
import kotlinx.android.synthetic.main.activity_add_transaction.*


class DialogFragmentToSelectUserOrProject : DialogFragment(), AdapterView.OnItemClickListener {


    private val TAG = "DialogSelectUserOrProject"
    private var addTransactionActivity: AddTransactionActivity? = null
    private lateinit var mBottomSheetDialog: Dialog
    private var mType: Int = -1
    private var simpleAdapter: SimpleAdapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        addTransactionActivity = activity as AddTransactionActivity
        mBottomSheetDialog = Dialog(
            activity!!,
            R.style.MaterialDialogSheet
        )
        mBottomSheetDialog.setContentView(R.layout.dialog_project_selection)
        mBottomSheetDialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mBottomSheetDialog.getWindow()!!.getAttributes())
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        lp.verticalMargin = 5.0f
        lp.gravity = Gravity.BOTTOM
        lp.windowAnimations = R.style.DialogAnimation
        mBottomSheetDialog.getWindow()!!.setAttributes(lp)

        var titleSearchView = mBottomSheetDialog.findViewById<SearchView>(R.id.sv_dialog_title)
        var listView = mBottomSheetDialog.findViewById<ListView>(R.id.listview_project)
        listView.onItemClickListener = this

        mType = arguments!!.get(LedgerDefine.KEY_SELECTION_TYPE) as Int
        when (mType) {
            LedgerDefine.SELECTION_TYPE_SENDER -> {
                titleSearchView.queryHint = getString(R.string.title_sender)

                var itemDataList: ArrayList<Map<String, String>> = ArrayList<Map<String, String>>()
                for (user in addTransactionActivity!!.mSenderList!!) {
                    val listItemMap = HashMap<String, String>()
                    listItemMap.put(LedgerDefine.USER_ID, user.userID)
                    listItemMap.put(LedgerDefine.NAME, user.name)
                    itemDataList.add(listItemMap)
                }
                simpleAdapter = SimpleAdapter(
                    addTransactionActivity, itemDataList, android.R.layout.simple_list_item_2,
                    arrayOf(LedgerDefine.USER_ID, LedgerDefine.NAME), intArrayOf(android.R.id.text1, android.R.id.text2)
                )
                listView.adapter = simpleAdapter

            }
            LedgerDefine.SELECTION_TYPE_RECEIVER -> {
                titleSearchView.queryHint = getString(R.string.title_receiver)

                var itemDataList: ArrayList<Map<String, String>> = ArrayList<Map<String, String>>()
                for (user in addTransactionActivity!!.mReceiverList!!) {
                    val listItemMap = HashMap<String, String>()
                    listItemMap.put(LedgerDefine.USER_ID, user.userID)
                    listItemMap.put(LedgerDefine.NAME, user.name)
                    itemDataList.add(listItemMap)
                }
                simpleAdapter = SimpleAdapter(
                    addTransactionActivity, itemDataList, android.R.layout.simple_list_item_2,
                    arrayOf(LedgerDefine.USER_ID, LedgerDefine.NAME), intArrayOf(android.R.id.text1, android.R.id.text2)
                )
                listView.adapter = simpleAdapter
            }
            LedgerDefine.SELECTION_TYPE_PROJECT -> {
                titleSearchView.queryHint = getString(R.string.title_project)

                var itemDataList: ArrayList<Map<String, String>> = ArrayList<Map<String, String>>()
                for (project in addTransactionActivity!!.mProjectList!!) {
                    val listItemMap = HashMap<String, String>()
                    listItemMap.put(LedgerDefine.PROJECT_ID, project.projectID)
                    listItemMap.put(LedgerDefine.NAME, project.name)
                    itemDataList.add(listItemMap)
                }

                simpleAdapter = SimpleAdapter(
                    addTransactionActivity,
                    itemDataList,
                    android.R.layout.simple_list_item_2,
                    arrayOf(LedgerDefine.PROJECT_ID, LedgerDefine.NAME),
                    intArrayOf(android.R.id.text1, android.R.id.text2)
                )
                listView.adapter = simpleAdapter
            }

            LedgerDefine.SELECTION_TYPE_CREDIT_ACCOUNT,
            LedgerDefine.SELECTION_TYPE_DEBIT_ACCOUNT -> {
                titleSearchView.queryHint = getString(R.string.title_bank_account)

                var itemDataList: ArrayList<Map<String, String>> = ArrayList<Map<String, String>>()
                for (account in addTransactionActivity!!.mBankAccountList!!) {
                    val listItemMap = HashMap<String, String>()
                    listItemMap.put(LedgerDefine.BANK_ACCOUNT_NUMBER, account.accountNo)
                    listItemMap.put(LedgerDefine.PAYEE_NAME, account.payee)
                    itemDataList.add(listItemMap)
                }

                simpleAdapter = SimpleAdapter(
                    addTransactionActivity,
                    itemDataList,
                    android.R.layout.simple_list_item_2,
                    arrayOf(LedgerDefine.BANK_ACCOUNT_NUMBER, LedgerDefine.PAYEE_NAME),
                    intArrayOf(android.R.id.text1, android.R.id.text2)
                )
                listView.adapter = simpleAdapter
            }
        }

        titleSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {

                simpleAdapter!!.filter.filter(newText)

                return true
            }
        })
       titleSearchView.onActionViewExpanded();
        //titleSearchView.setIconified(false);
        titleSearchView.requestFocus()
        return mBottomSheetDialog
    }

    override fun onItemClick(adapterView: AdapterView<*>?, view: View?, index: Int, p3: Long) {

        var clickItemObj = adapterView!!.getAdapter().getItem(index)

        when (mType) {
            LedgerDefine.SELECTION_TYPE_SENDER -> {
                var map = clickItemObj as HashMap<String, String>
                var id = map.get(LedgerDefine.USER_ID)
                for (user in addTransactionActivity!!.mSenderList!!) {
                    if (id == user.userID) {
                        addTransactionActivity!!.mSelectedSender = user
                        var str = user.userID + "\n" + user.name
                        addTransactionActivity!!.tv_sender_id.setText(str)
                        break
                    }
                }
            }
            LedgerDefine.SELECTION_TYPE_RECEIVER -> {
                var map = clickItemObj as HashMap<String, String>
                var id = map.get(LedgerDefine.USER_ID)
                for (user in addTransactionActivity!!.mReceiverList!!) {
                    if (id == user.userID) {
                        addTransactionActivity!!.mSelectedReceiver = user
                        var str = user.userID + "\n" + user.name
                        addTransactionActivity!!.tv_receiver_id.setText(str)
                        setPreferenceFromDB(id)
                        break

                    }
                }
            }
            LedgerDefine.SELECTION_TYPE_PROJECT -> {
                var map = clickItemObj as HashMap<String, String>
                var id = map.get(LedgerDefine.PROJECT_ID)
                for (project in addTransactionActivity!!.mProjectList!!) {
                    if (id == project.projectID) {
                        addTransactionActivity!!.mSelectedProject = project
                        var str = project.projectID + "\n" + project.name
                        addTransactionActivity!!.tv_project_id.setText(str)
                        break
                    }
                }
            }
            LedgerDefine.SELECTION_TYPE_DEBIT_ACCOUNT,
            LedgerDefine.SELECTION_TYPE_CREDIT_ACCOUNT -> {
                var map = clickItemObj as HashMap<String, String>
                var accountNo = map.get(LedgerDefine.BANK_ACCOUNT_NUMBER)
                for (bankAccountDetail in addTransactionActivity!!.mBankAccountList!!) {
                    if (accountNo == bankAccountDetail.accountNo) {
                        var str = bankAccountDetail.accountNo + "\n" + bankAccountDetail.payee
                        if (mType == LedgerDefine.SELECTION_TYPE_DEBIT_ACCOUNT) {
                            addTransactionActivity!!.mSelectedDebitAccount = bankAccountDetail
                            addTransactionActivity!!.tv_debit_account.setText(str)
                        } else {
                            addTransactionActivity!!.mSelectedCreditAccount = bankAccountDetail
                            addTransactionActivity!!.tv_credit_account.setText(str)
                        }

                    }
                }
            }
        }

        mBottomSheetDialog.dismiss()
    }

    private fun setPreferenceFromDB(id: String) {
        val cursor = addTransactionActivity!!.contentResolver.query(
            SqlDBFile.CONTENT_URI,
            null,
            SqlDBFile.RECEIVER_ID + "=?",
            Array<String>(1) { id },
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val tempProject = cursor.getString(cursor.getColumnIndex(SqlDBFile.PROJECT_ID))
                val creditAccount = cursor.getString(cursor.getColumnIndex(SqlDBFile.CREDIT_ACCOUNT))
                val remarks = cursor.getString(cursor.getColumnIndex(SqlDBFile.REMARKS))

                for (project in addTransactionActivity!!.mProjectList!!) {
                    if (tempProject == project.projectID) {
                        addTransactionActivity!!.mSelectedProject = project
                        var str = project.projectID + "\n" + project.name
                        addTransactionActivity!!.tv_project_id.setText(str)
                        break
                    }
                }
                var isCreditAccountFound = false
                for (bankAccountDetail in addTransactionActivity!!.mBankAccountList!!) {
                    if (creditAccount == bankAccountDetail.id) {
                        var str = bankAccountDetail.accountNo + "\n" + bankAccountDetail.payee
                        addTransactionActivity!!.mSelectedCreditAccount = bankAccountDetail
                        addTransactionActivity!!.tv_credit_account.text = str
                        isCreditAccountFound = true
                        break
                    }
                }
                if(!isCreditAccountFound) {
                    addTransactionActivity!!.mSelectedCreditAccount = null
                    addTransactionActivity!!.tv_credit_account.text = null
                }
                addTransactionActivity!!.et_remarks.setText(remarks)
            }
            cursor.close()
        }
    }
}
