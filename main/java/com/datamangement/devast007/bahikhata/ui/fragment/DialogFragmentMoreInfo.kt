package com.datamangement.devast007.bahikhata.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerUtils


class DialogFragmentMoreInfo : DialogFragment() {


    private val TAG = "DialogFragmentToSelectUserOrProject"
    private lateinit var mBottomSheetDialog: Dialog
    private var mType: Int = -1;

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
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

    private fun setProjectView(view: Dialog) {
        var title = view.findViewById<TextView>(R.id.tv_dialog_title)
        var tvId = view.findViewById<TextView>(R.id.tv_project_id)
        val tvName = view.findViewById<TextView>(R.id.tv_project_name)
        val tvNickname = view.findViewById<TextView>(R.id.tv_project_nickname)
        val tvAmount = view.findViewById<TextView>(R.id.tv_amount)
        var tvAddress = view.findViewById<TextView>(R.id.tv_project_address)
        var tvDivision = view.findViewById<TextView>(R.id.tv_project_div)
        val tvStartDate = view.findViewById<TextView>(R.id.tv_project_start_date)
        val tvEndDate = view.findViewById<TextView>(R.id.tv_project_end_date)
        val tvRemarks = view.findViewById<TextView>(R.id.tv_project_remarks)

        val tvMbNo = view.findViewById<TextView>(R.id.tv_project_mb_no)
        val tvHead = view.findViewById<TextView>(R.id.tv_project_head)
        val tvMainAmount = view.findViewById<TextView>(R.id.tv_project_main_amount)
        val tvM1stYear = view.findViewById<TextView>(R.id.tv_project_maintenance_amount_1st_year)
        val tvM2ndYear = view.findViewById<TextView>(R.id.tv_project_maintenance_amount_2nd_year)
        val tvM3rdYear = view.findViewById<TextView>(R.id.tv_project_maintenance_amount_3rd_year)
        val tvM4thYear = view.findViewById<TextView>(R.id.tv_project_maintenance_amount_4th_year)
        val tvM5thYear = view.findViewById<TextView>(R.id.tv_project_maintenance_amount_5th_year)
        val tvTimestamp = view.findViewById<TextView>(R.id.tv_time_stamp)


        title.text = arguments!!.getString(LedgerDefine.PROJECT_ID)
        tvId.text = arguments!!.getString(LedgerDefine.PROJECT_ID)
        tvName.text = arguments!!.getString(LedgerDefine.NAME)
        tvNickname.text = arguments!!.getString(LedgerDefine.NICKNAME)
        tvAmount.text = LedgerUtils.getRupeesFormatted(arguments!!.getLong(LedgerDefine.AMOUNT))
        tvAddress.text = arguments!!.getString(LedgerDefine.ADDRESS)
        tvDivision.text = arguments!!.getString(LedgerDefine.DIVISION)
        tvStartDate.text = arguments!!.getString(LedgerDefine.START_DATE)
        tvEndDate.text = arguments!!.getString(LedgerDefine.END_DATE)
        tvRemarks.text = arguments!!.getString(LedgerDefine.REMARK)

        tvMbNo.text = arguments!!.getString(LedgerDefine.MB_NO)
        tvHead.text = arguments!!.getString(LedgerDefine.HEAD)
        tvMainAmount.text = arguments!!.getString(LedgerDefine.MAIN_AMOUNT)
        tvM1stYear.text = arguments!!.getString(LedgerDefine.MAINTENANCE_1ST_YEAR_AMOUNT)
        tvM2ndYear.text = arguments!!.getString(LedgerDefine.MAINTENANCE_2ND_YEAR_AMOUNT)
        tvM3rdYear.text = arguments!!.getString(LedgerDefine.MAINTENANCE_3RD_YEAR_AMOUNT)
        tvM4thYear.text = arguments!!.getString(LedgerDefine.MAINTENANCE_4TH_YEAR_AMOUNT)
        tvM5thYear.text = arguments!!.getString(LedgerDefine.MAINTENANCE_5TH_YEAR_AMOUNT)
        tvTimestamp.text = arguments!!.getString(LedgerDefine.TIME_STAMP)

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

    private fun setUserView(view: Dialog) {
        var title = view.findViewById<TextView>(R.id.tv_dialog_title)
        var tvId = view.findViewById<TextView>(R.id.tv_user_id)
        val tvName = view.findViewById<TextView>(R.id.tv_user_name)
        val tvAmount = view.findViewById<TextView>(R.id.tv_amount)
        var tvAddress = view.findViewById<TextView>(R.id.tv_user_address)
        var tvUserCreatedDate = view.findViewById<TextView>(R.id.tv_user_created_date)
        val tvPhone = view.findViewById<TextView>(R.id.tv_user_phone)
        val tvEmail = view.findViewById<TextView>(R.id.tv_user_email)
        val tvAccessibleProjects = view.findViewById<TextView>(R.id.tv_user_project_access)
        val tvRemarks = view.findViewById<TextView>(R.id.tv_user_remarks)
        val designation = view.findViewById<TextView>(R.id.tv_user_designation)
        val tvPersonalMaterialCost = view.findViewById<TextView>(R.id.tv_personal_material_cost)
        val tvPersonalPayment = view.findViewById<TextView>(R.id.tv_personal_payment)
        val tvPersonalGst = view.findViewById<TextView>(R.id.tv_personal_gst)
        val tvMasterAmount = view.findViewById<TextView>(R.id.tv_master_amount)

        title.text = arguments!!.getString(LedgerDefine.USER_ID)

        tvId.text = arguments!!.getString(LedgerDefine.USER_ID)
        tvName.text = arguments!!.getString(LedgerDefine.NAME)
        tvAmount.text = LedgerUtils.getRupeesFormatted(arguments!!.getLong(LedgerDefine.AMOUNT))
        tvAddress.text = arguments!!.getString(LedgerDefine.ADDRESS)
        tvUserCreatedDate.text = arguments!!.getString(LedgerDefine.TIME_STAMP)
        tvPhone.text = arguments!!.getString(LedgerDefine.PHONE_NUMBER)
        tvEmail.text = arguments!!.getString(LedgerDefine.EMAIL)
        tvAccessibleProjects.text = arguments!!.getString(LedgerDefine.ACCESSIBLE_PROJECTS)
        designation.text = "" + arguments!!.getLong(LedgerDefine.DESIGNATION)
        tvRemarks.text = arguments!!.getString(LedgerDefine.REMARK)

        var materialCost = arguments!!.getLong(LedgerDefine.P_MATERIAL_COST, 0)

        tvPersonalMaterialCost.text = String.format(
            activity!!.getString(R.string.material_cost_formattor),
            LedgerUtils.getRupeesFormatted(materialCost)
        )

        var payment = arguments!!.getLong(LedgerDefine.P_PAYMENT, 0)

        tvPersonalPayment.text = String.format(
            activity!!.getString(R.string.payment_formattor),
            LedgerUtils.getRupeesFormatted(payment)
        )

        var gstBill = arguments!!.getLong(LedgerDefine.P_GST_BILL, 0)

        tvPersonalGst.text = String.format(
            activity!!.getString(R.string.gst_bill_formattor),
            LedgerUtils.getRupeesFormatted(gstBill)
        )

        var count = arguments!!.getInt(LedgerDefine.USER_ACCOUNT_COUNT, 1)
        if (count > 1) {
            var masterAmount = arguments!!.getLong(LedgerDefine.M_AMOUNT, 0)

            tvMasterAmount.text = String.format(
                activity!!.getString(R.string.left_balance_master_formattor),
                LedgerUtils.getRupeesFormatted(masterAmount)
            )
        } else {
            tvMasterAmount.isEnabled = false
            tvMasterAmount.setText(R.string.no_master_account)
        }

    }

}
