package com.datamangement.devast007.bahikhata.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
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
import com.datamangement.devast007.bahikhata.ui.AddGSTActivity
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.UserDetails
import kotlinx.android.synthetic.main.activity_add_transaction.*


class DialogFragmentToSelectUserOrProjectForGST : DialogFragment(), AdapterView.OnItemClickListener {


    private val TAG = "DialogFragmentToSelectUserOrProjectForGST"
    private var addGstActivity: AddGSTActivity? = null
    private lateinit var mBottomSheetDialog: Dialog
    private var mType: Int = -1
    private var simpleAdapter: SimpleAdapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        addGstActivity = activity as AddGSTActivity
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
                for (user in addGstActivity!!.mSenderList!!) {
                    val listItemMap = HashMap<String, String>()
                    listItemMap.put(LedgerDefine.USER_ID, user.userID)
                    listItemMap.put(LedgerDefine.NAME, user.name)
                    itemDataList.add(listItemMap)
                }
                simpleAdapter = SimpleAdapter(
                    addGstActivity, itemDataList, android.R.layout.simple_list_item_2,
                    arrayOf(LedgerDefine.USER_ID, LedgerDefine.NAME), intArrayOf(android.R.id.text1, android.R.id.text2)
                )
                listView.adapter = simpleAdapter

            }
            LedgerDefine.SELECTION_TYPE_RECEIVER -> {
                titleSearchView.queryHint = getString(R.string.title_receiver)

                var itemDataList: ArrayList<Map<String, String>> = ArrayList<Map<String, String>>()
                for (user in addGstActivity!!.mReceiverList!!) {
                    val listItemMap = HashMap<String, String>()
                    listItemMap.put(LedgerDefine.USER_ID, user.userID)
                    listItemMap.put(LedgerDefine.NAME, user.name)
                    itemDataList.add(listItemMap)
                }
                simpleAdapter = SimpleAdapter(
                    addGstActivity, itemDataList, android.R.layout.simple_list_item_2,
                    arrayOf(LedgerDefine.USER_ID, LedgerDefine.NAME), intArrayOf(android.R.id.text1, android.R.id.text2)
                )
                listView.adapter = simpleAdapter
            }
            LedgerDefine.SELECTION_TYPE_PROJECT -> {
                titleSearchView.queryHint = getString(R.string.title_project)

                var itemDataList: ArrayList<Map<String, String>> = ArrayList<Map<String, String>>()
                for (project in addGstActivity!!.mProjectList!!) {
                    val listItemMap = HashMap<String, String>()
                    listItemMap.put(LedgerDefine.PROJECT_ID, project.projectID)
                    listItemMap.put(LedgerDefine.NAME, project.name)
                    itemDataList.add(listItemMap)
                }

                simpleAdapter = SimpleAdapter(
                    addGstActivity,
                    itemDataList,
                    android.R.layout.simple_list_item_2,
                    arrayOf(LedgerDefine.PROJECT_ID, LedgerDefine.NAME),
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
                        addGstActivity!!.tv_sender_id.text = str
                    } else {
                        addGstActivity!!.tv_receiver_id.text = str
                    }
                })

            .setNegativeButton(R.string.master,
                DialogInterface.OnClickListener { dialog, which ->
                    val MASTERL_ACCOUNT = LedgerDefine.PREFIX_MASTER + user.userID
                    var str = MASTERL_ACCOUNT + "\n" + user.name
                    if (type == LedgerDefine.SELECTION_TYPE_SENDER) {
                        addGstActivity!!.tv_sender_id.text = str
                    } else {
                        addGstActivity!!.tv_receiver_id.text = str
                    }

                })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()

    }

    override fun onItemClick(adapterView: AdapterView<*>?, view: View?, index: Int, p3: Long) {

        var clickItemObj = adapterView!!.getAdapter().getItem(index)

        when (mType) {
            LedgerDefine.SELECTION_TYPE_SENDER -> {
                var map = clickItemObj as HashMap<String, String>
                var id = map.get(LedgerDefine.USER_ID)
                for (user in addGstActivity!!.mSenderList!!) {
                    if (id == user.userID) {
                        addGstActivity!!.mSelectedSender = user
                        if (user.userAccounts!!.size > 1) {
                            showChooseUserDialog(user, mType)
                        } else {
                            var str = user.userAccounts!![0] + "\n" + user.name
                            addGstActivity!!.tv_sender_id.text = str
                        }

                        break
                    }
                }
            }
            LedgerDefine.SELECTION_TYPE_RECEIVER -> {
                var map = clickItemObj as HashMap<String, String>
                var id = map.get(LedgerDefine.USER_ID)
                for (user in addGstActivity!!.mReceiverList!!) {
                    if (id == user.userID) {
                        addGstActivity!!.mSelectedReceiver = user
                        if (user.userAccounts!!.size > 1) {
                            showChooseUserDialog(user, mType)
                        } else {
                            var str = user.userAccounts!![0] + "\n" + user.name
                            addGstActivity!!.tv_receiver_id.text = str
                        }

                        break

                    }
                }
            }
            LedgerDefine.SELECTION_TYPE_PROJECT -> {
                var map = clickItemObj as HashMap<String, String>
                var id = map.get(LedgerDefine.PROJECT_ID)
                for (project in addGstActivity!!.mProjectList!!) {
                    if (id == project.projectID) {
                        addGstActivity!!.mSelectedProject = project
                        var str = project.projectID + "\n" + project.name
                        addGstActivity!!.tv_project_id.setText(str)
                        break
                    }
                }
            }
        }

        mBottomSheetDialog.dismiss()
    }
}
