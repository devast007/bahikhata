package com.datamangement.devast007.bahikhata.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.StatementActivity
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.UserDetails
import kotlinx.android.synthetic.main.activity_statement.*


class DialogFragmentToSelectUserOrProjectForStatement : DialogFragment(),
    AdapterView.OnItemClickListener {


    private val TAG = "UserOrProjectStatement"
    private var statementActivity: StatementActivity? = null
    private lateinit var mBottomSheetDialog: Dialog
    private var simpleAdapter: SimpleAdapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        statementActivity = activity as StatementActivity
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

        mBottomSheetDialog.findViewById<Button>(R.id.btn_add_new_item).visibility = View.GONE
        titleSearchView.queryHint = getString(R.string.title_sender)

        var itemDataList: ArrayList<Map<String, String>> = ArrayList<Map<String, String>>()
        for (user in statementActivity!!.mUserDetails!!) {
            val listItemMap = HashMap<String, String>()
            listItemMap.put(LedgerDefine.USER_ID, user.userID)
            listItemMap.put(LedgerDefine.NAME, user.name)
            itemDataList.add(listItemMap)
        }
        simpleAdapter = SimpleAdapter(
            statementActivity,
            itemDataList,
            android.R.layout.simple_list_item_2,
            arrayOf(LedgerDefine.USER_ID, LedgerDefine.NAME),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        listView.adapter = simpleAdapter

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

        var map = clickItemObj as HashMap<String, String>
        var id = map.get(LedgerDefine.USER_ID)
        for (user in statementActivity!!.mUserDetails!!) {
            if (id == user.userID) {
                statementActivity!!.mSelectedSupplier = user
                if (user.userAccounts!!.size > 1) {
                    showChooseUserDialog(user)
                } else {
                    var str = user.userAccounts!![0] + "\n" + user.name
                    statementActivity!!.btn_supplier_id.text = str
                }

                break
            }
        }

        mBottomSheetDialog.dismiss()
    }

    private fun showChooseUserDialog(user: UserDetails) {
        AlertDialog.Builder(context)
            .setTitle(user.userID)
            .setMessage("Select Account !!")
            .setCancelable(true)
            .setPositiveButton(
                R.string.personal,
                DialogInterface.OnClickListener { dialog, which ->
                    val PERSONAL_ACCOUNT = LedgerDefine.PREFIX_PERSONAL + user.userID
                    var str = PERSONAL_ACCOUNT + "\n" + user.name
                    statementActivity!!.btn_supplier_id.text = str
                })

            .setNegativeButton(R.string.master,
                DialogInterface.OnClickListener { dialog, which ->
                    val MASTERL_ACCOUNT = LedgerDefine.PREFIX_MASTER + user.userID
                    var str = MASTERL_ACCOUNT + "\n" + user.name
                    statementActivity!!.btn_supplier_id.text = str

                })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()

    }

}
