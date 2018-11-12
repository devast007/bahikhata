package com.datamangement.devast007.bahikhata.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.TransactionViewActivity
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerUtils


class DialogFragmentMoreInfo : DialogFragment() {


    private val TAG = "DialogFragmentToSelectProject"
    private var transactionViewActivity: TransactionViewActivity? = null
    private lateinit var mBottomSheetDialog: Dialog
    private var mType: Int = -1;

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        transactionViewActivity = activity as TransactionViewActivity
        mBottomSheetDialog = Dialog(
            activity!!,
            R.style.MaterialDialogSheet
        )
        mBottomSheetDialog.setContentView(R.layout.dialog_more_info)
        mBottomSheetDialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mBottomSheetDialog.getWindow()!!.getAttributes())
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.BOTTOM
        lp.windowAnimations = R.style.DialogAnimation
        mBottomSheetDialog.getWindow()!!.setAttributes(lp)

        var type = arguments!!.getInt(LedgerDefine.MORE_INFO_TYPE)

        var projectView = mBottomSheetDialog.findViewById<View>(R.id.include_project_info)
        var userView = mBottomSheetDialog.findViewById<View>(R.id.include_user_info)
        var accountView = mBottomSheetDialog.findViewById<View>(R.id.include_account_info)

        if (type == LedgerDefine.MORE_INFO_TYPE_USER) {
            projectView.visibility = View.GONE
            userView.visibility = View.VISIBLE
            accountView.visibility = View.GONE
            setUserView(mBottomSheetDialog)
        } else if (type == LedgerDefine.MORE_INFO_TYPE_PROJECT) {
            projectView.visibility = View.VISIBLE
            userView.visibility = View.GONE
            accountView.visibility = View.GONE
            setProjectView(mBottomSheetDialog)
        } else if (type == LedgerDefine.MORE_INFO_TYPE_ACCOUNT) {
            projectView.visibility = View.GONE
            userView.visibility = View.GONE
            accountView.visibility = View.VISIBLE
            setAccountView(mBottomSheetDialog)
        }

        return mBottomSheetDialog
    }

    private fun setAccountView(view: Dialog) {

        var tvPayee = view.findViewById<TextView>(R.id.tv_bank_account_payee)
        var tvBranch = view.findViewById<TextView>(R.id.tv_bank_account_branch)
        val tvAccountNo = view.findViewById<TextView>(R.id.tv_bank_account_number)
        var tvIFSCCode: TextView = view.findViewById<TextView>(R.id.tv_bank_account_ifsc_code)
        var tvID: TextView = view.findViewById<TextView>(R.id.tv_bank_account_id)
        var remark: TextView = view.findViewById<TextView>(R.id.tv_bank_account_remarks)
        var timestamp: TextView = view.findViewById<TextView>(R.id.tv_bank_account_timestamp)
        var amount: TextView = view.findViewById<TextView>(R.id.tv_bank_account_amount)
        var title = view.findViewById<TextView>(R.id.tv_dialog_title)

        title.setText(arguments!!.getString(LedgerDefine.BANK_ACCOUNT_ID))
        tvPayee.setText(arguments!!.getString(LedgerDefine.PAYEE_NAME))
        tvAccountNo.setText(arguments!!.getString(LedgerDefine.BANK_ACCOUNT_NUMBER))
        tvBranch.setText(arguments!!.getString(LedgerDefine.BANK_ACCOUNT_BRANCH_NAME))
        tvIFSCCode.setText(arguments!!.getString(LedgerDefine.IFSC_CODE))
        tvID.setText(arguments!!.getString(LedgerDefine.BANK_ACCOUNT_ID))
        remark.setText(arguments!!.getString(LedgerDefine.REMARK))
        timestamp.setText(arguments!!.getString(LedgerDefine.TIME_STAMP))
        amount.setText(LedgerUtils.getRupeesFormatted(arguments!!.getLong(LedgerDefine.AMOUNT)))
    }

    private fun setProjectView(view: Dialog) {
        var title = view.findViewById<TextView>(R.id.tv_dialog_title)
        var tvId = view.findViewById<TextView>(R.id.tv_project_id)
        val tvName = view.findViewById<TextView>(R.id.tv_project_name)
        val tvAmount = view.findViewById<TextView>(R.id.tv_project_benefits)
        var tvAddress = view.findViewById<TextView>(R.id.tv_project_address)
        var tvDivision = view.findViewById<TextView>(R.id.tv_project_div)
        val tvStartDate = view.findViewById<TextView>(R.id.tv_project_start_date)
        val tvEndDate = view.findViewById<TextView>(R.id.tv_project_end_date)
        val tvRemarks = view.findViewById<TextView>(R.id.tv_project_remarks)

        title.setText(arguments!!.getString(LedgerDefine.PROJECT_ID))
        tvId.setText(arguments!!.getString(LedgerDefine.PROJECT_ID))
        tvName.setText(arguments!!.getString(LedgerDefine.NAME))
        tvAmount.setText(LedgerUtils.getRupeesFormatted(arguments!!.getLong(LedgerDefine.AMOUNT)))
        tvAddress.setText(arguments!!.getString(LedgerDefine.ADDRESS))
        tvDivision.setText(arguments!!.getString(LedgerDefine.DIVISION))
        tvStartDate.setText(arguments!!.getString(LedgerDefine.START_DATE))
        tvEndDate.setText(arguments!!.getString(LedgerDefine.END_DATE))
        tvRemarks.setText(arguments!!.getString(LedgerDefine.REMARK))

    }

    private fun setUserView(view: Dialog) {
        var title = view.findViewById<TextView>(R.id.tv_dialog_title)
        var tvId = view.findViewById<TextView>(R.id.tv_user_id)
        val tvName = view.findViewById<TextView>(R.id.tv_user_name)
        val tvAmount = view.findViewById<TextView>(R.id.tv_user_benefits)
        var tvAddress = view.findViewById<TextView>(R.id.tv_user_address)
        var tvUserCreatedDate = view.findViewById<TextView>(R.id.tv_user_created_date)
        val tvPhone = view.findViewById<TextView>(R.id.tv_user_phone)
        val tvEmail = view.findViewById<TextView>(R.id.tv_user_email)
        val tvAccessibleProjects = view.findViewById<TextView>(R.id.tv_user_project_access)
        val tvRemarks = view.findViewById<TextView>(R.id.tv_user_remarks)
        val designation = view.findViewById<TextView>(R.id.tv_user_designation)

        title.setText(arguments!!.getString(LedgerDefine.USER_ID))

        tvId.setText(arguments!!.getString(LedgerDefine.USER_ID))
        tvName.setText(arguments!!.getString(LedgerDefine.NAME))
        tvAmount.setText(LedgerUtils.getRupeesFormatted(arguments!!.getLong(LedgerDefine.AMOUNT)))
        tvAddress.setText(arguments!!.getString(LedgerDefine.ADDRESS))
        tvUserCreatedDate.setText(arguments!!.getString(LedgerDefine.TIME_STAMP))
        tvPhone.setText(arguments!!.getString(LedgerDefine.PHONE_NUMBER))
        tvEmail.setText(arguments!!.getString(LedgerDefine.EMAIL))
        tvAccessibleProjects.setText(arguments!!.getString(LedgerDefine.ACCESSIBLE_PROJECTS))
        designation.setText("" + arguments!!.getLong(LedgerDefine.DESIGNATION))
        tvRemarks.setText(arguments!!.getString(LedgerDefine.REMARK))
    }

}
