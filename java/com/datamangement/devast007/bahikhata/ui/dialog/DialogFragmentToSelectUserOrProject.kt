package com.datamangement.devast007.bahikhata.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.AddBankAccountActivity
import com.datamangement.devast007.bahikhata.ui.AddTransactionActivity
import com.datamangement.devast007.bahikhata.ui.AddUsersActivity
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.SqlDBFile
import com.datamangement.devast007.bahikhata.utils.UserDetails
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
            requireActivity(),
            R.style.MaterialDialogSheet
        )
        mBottomSheetDialog.setContentView(R.layout.dialog_project_selection)
        mBottomSheetDialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mBottomSheetDialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        lp.verticalMargin = 5.0f
        lp.gravity = Gravity.BOTTOM
        lp.windowAnimations = R.style.DialogAnimation
        mBottomSheetDialog.window!!.attributes = lp

        var titleSearchView = mBottomSheetDialog.findViewById<SearchView>(R.id.sv_dialog_title)
        var listView = mBottomSheetDialog.findViewById<ListView>(R.id.listview_project)
        listView.onItemClickListener = this
        mType = requireArguments().get(LedgerDefine.KEY_SELECTION_TYPE) as Int

        mBottomSheetDialog.findViewById<Button>(R.id.btn_add_new_item).setOnClickListener {
            if (mType == LedgerDefine.SELECTION_TYPE_CREDIT_ACCOUNT || mType ==
                LedgerDefine.SELECTION_TYPE_DEBIT_ACCOUNT
            ) {
                startActivityForResult(
                    Intent(
                        addTransactionActivity,
                        AddBankAccountActivity::class.java
                    ), LedgerDefine.REQUEST_CODE_BANK_ACCOUNT_ADDED
                )
                mBottomSheetDialog.dismiss()
            }else if (mType == LedgerDefine.SELECTION_TYPE_SENDER || mType ==
                LedgerDefine.SELECTION_TYPE_RECEIVER
            ) {
                startActivityForResult(
                    Intent(
                        addTransactionActivity,
                        AddUsersActivity::class.java
                    ), LedgerDefine.REQUEST_CODE_BANK_ACCOUNT_ADDED
                )
                mBottomSheetDialog.dismiss()
            }
        }
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
                    addTransactionActivity,
                    itemDataList,
                    android.R.layout.simple_list_item_2,
                    arrayOf(LedgerDefine.USER_ID, LedgerDefine.NAME),
                    intArrayOf(android.R.id.text1, android.R.id.text2)
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
                    addTransactionActivity,
                    itemDataList,
                    android.R.layout.simple_list_item_2,
                    arrayOf(LedgerDefine.USER_ID, LedgerDefine.NAME),
                    intArrayOf(android.R.id.text1, android.R.id.text2)
                )
                listView.adapter = simpleAdapter
            }
            LedgerDefine.SELECTION_TYPE_PROJECT -> {
                titleSearchView.queryHint = getString(R.string.title_project)

                var itemDataList: ArrayList<Map<String, String>> = ArrayList<Map<String, String>>()
                for (project in addTransactionActivity!!.mProjectList!!) {
                    val listItemMap = HashMap<String, String>()
                    listItemMap[LedgerDefine.PROJECT_ID] = project.projectID!!
                    listItemMap[LedgerDefine.NAME] = project.name
                    listItemMap[LedgerDefine.SUBCATEGORY] = project.subCategory
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

        Handler().postDelayed({
            titleSearchView.onActionViewExpanded();
            //titleSearchView.setIconified(false);
            titleSearchView.requestFocus()
        }, 800)

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
                        if (user.userAccounts!!.size > 1) {
                            showChooseUserDialog(user, mType)
                        } else {
                            var str = user.userAccounts!![0] + "\n" + user.name
                            addTransactionActivity!!.tv_sender_id.text = str
                        }

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
                        if (user.userAccounts!!.size > 1) {
                            showChooseUserDialog(user, mType)
                        } else {
                            var str = user.userAccounts!![0] + "\n" + user.name
                            addTransactionActivity!!.tv_receiver_id.text = str
                            setPreferenceFromDB(user.userAccounts!![0])

                        }

                        break

                    }
                }
            }
            LedgerDefine.SELECTION_TYPE_PROJECT -> {
                var map = clickItemObj as HashMap<String, String>
                var id = map[LedgerDefine.PROJECT_ID]
                for (project in addTransactionActivity!!.mProjectList!!) {
                    if (id == project.projectID) {
                        addTransactionActivity!!.mSelectedProject = project
                        var str = project.projectID + "\n" + project.name
                        addTransactionActivity!!.tv_project_id.text = str
                        addTransactionActivity!!.mSelectedReceiver
                        addTransactionActivity!!.setSubCategory(project.subCategory)
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
                            addTransactionActivity!!.tv_debit_account.text = str
                        } else {
                            addTransactionActivity!!.mSelectedCreditAccount = bankAccountDetail
                            addTransactionActivity!!.tv_credit_account.text = str
                        }
                        addTransactionActivity!!.rb_online.isChecked = true

                    }
                }
            }
        }

        mBottomSheetDialog.dismiss()
    }

    private fun showChooseUserDialog(user: UserDetails, type: Int) {
        AlertDialog.Builder(context)
            .setTitle(user.userID)
            .setMessage("Select Account !!")
            .setCancelable(true)
            .setPositiveButton(
                R.string.personal,
                DialogInterface.OnClickListener { dialog, which ->
                    val PERSONAL_ACCOUNT = LedgerDefine.PREFIX_PERSONAL + user.userID
                    var str = PERSONAL_ACCOUNT + "\n" + user.name
                    if (type == LedgerDefine.SELECTION_TYPE_SENDER) {
                        addTransactionActivity!!.tv_sender_id.text = str
                    } else {
                        addTransactionActivity!!.tv_receiver_id.text = str
                        setPreferenceFromDB(PERSONAL_ACCOUNT)
                    }
                })

            .setNegativeButton(R.string.master,
                DialogInterface.OnClickListener { dialog, which ->
                    val MASTERL_ACCOUNT = LedgerDefine.PREFIX_MASTER + user.userID
                    var str = MASTERL_ACCOUNT + "\n" + user.name
                    if (type == LedgerDefine.SELECTION_TYPE_SENDER) {
                        addTransactionActivity!!.tv_sender_id.text = str
                    } else {
                        addTransactionActivity!!.tv_receiver_id.text = str
                        setPreferenceFromDB(MASTERL_ACCOUNT)
                    }

                })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()

    }

    private fun setPreferenceFromDB(id: String) {
        val cursor = addTransactionActivity!!.contentResolver.query(
            SqlDBFile.CONTENT_URI_TABLE_SUGGESTION,
            null,
            LedgerDefine.RECEIVER_ID + "=?",
            Array<String>(1) { id },
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val tempProject = cursor.getString(cursor.getColumnIndex(LedgerDefine.PROJECT_ID))
                val creditAccount =
                    cursor.getString(cursor.getColumnIndex(LedgerDefine.CREDIT_ACCOUNT_ID))
                val remarks = cursor.getString(cursor.getColumnIndex(LedgerDefine.REMARK))

                for (project in addTransactionActivity!!.mProjectList!!) {
                    if (tempProject == project.projectID) {
                        addTransactionActivity!!.mSelectedProject = project
                        var str = project.projectID + "\n" + project.name
                        addTransactionActivity!!.tv_project_id.text = str
                        addTransactionActivity!!.setSubCategory(project.subCategory)
                        break
                    }
                }
                var isCreditAccountFound = false
                for (bankAccountDetail in addTransactionActivity!!.mBankAccountList!!) {
                    if (creditAccount == bankAccountDetail.id) {
                        var str = bankAccountDetail.accountNo + "\n" + bankAccountDetail.payee
                        addTransactionActivity!!.mSelectedCreditAccount = bankAccountDetail
                        addTransactionActivity!!.tv_credit_account.text = str
                        addTransactionActivity!!.rb_online.isChecked = true
                        isCreditAccountFound = true
                        break
                    }
                }
                if (!isCreditAccountFound) {
                    addTransactionActivity!!.mSelectedCreditAccount = null
                    addTransactionActivity!!.tv_credit_account.text = null
                    addTransactionActivity!!.rb_cash.isChecked = true
                }
                //addTransactionActivity!!.et_remarks.setText(remarks)
            }
            cursor.close()
        }
    }
}
