package com.datamangement.devast007.bahikhata.ui.adapter

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.MaterialViewActivity
import com.datamangement.devast007.bahikhata.utils.LedgerUtils
import com.datamangement.devast007.bahikhata.utils.MaterialDetails

class MaterialViewAdapter(
    materialViewActivity: MaterialViewActivity,
    MaterialsList: ArrayList<MaterialDetails>
) : BaseExpandableListAdapter() {
    private val mMaterialViewActivity: MaterialViewActivity = materialViewActivity
    val mMaterialsList = MaterialsList
    var mInflater: LayoutInflater =
        mMaterialViewActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getChildrenCount(p0: Int): Int {
        return 1;
    }

    override fun getGroup(groupPos: Int): Any {
        return mMaterialsList.get(groupPos)
    }

    override fun onGroupCollapsed(p0: Int) {
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun registerDataSetObserver(p0: DataSetObserver?) {
    }

    override fun getChild(group: Int, child: Int): Any {
        return mMaterialsList.get(group)
    }

    override fun onGroupExpanded(p0: Int) {
    }

    override fun getCombinedChildId(p0: Long, groudID: Long): Long {
        return groudID
    }

    override fun getGroupId(groudID: Int): Long {
        return groudID.toLong()
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return false
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getChildView(
        groupPos: Int,
        p1: Int,
        p2: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        var view: View? = convertView
        var childHolder: ChildHolder? = null
        if (view == null) {
            view = mInflater.inflate(R.layout.row_material_child_view, parent, false);
            childHolder = ChildHolder(view)
            view.tag = childHolder
        } else {
            childHolder = view.tag as ChildHolder?
        }
        val materilasDetails: MaterialDetails = mMaterialsList[groupPos]

        childHolder!!.tvSupplier.text = getSpannableString(
            materilasDetails.supplierId,
            mMaterialViewActivity.mUsersMap[getUserId(materilasDetails.supplierId)]
        )
        childHolder!!.tvReporter.text = getSpannableString(
            materilasDetails.reporterId,
            mMaterialViewActivity.mUsersMap[getUserId(materilasDetails.reporterId)]
        )
        childHolder!!.tvDate.text = LedgerUtils.getConvertDate(materilasDetails.date)
        childHolder!!.tvRateQuantityAmount.text =
            "${materilasDetails.quantity} ${mMaterialViewActivity.mUnits[materilasDetails.unit]} X ${materilasDetails.rate} = " + LedgerUtils.getRupeesFormatted(
                materilasDetails.amount.toLong()
            )
        Log.d("testing ", "testing " + materilasDetails.projectId)
        childHolder!!.tvProjectID.text =
            mMaterialViewActivity.mProjectsMap[materilasDetails.projectId]

        childHolder!!.tvVehicleAndChallanNo.text =
            "${materilasDetails.vehicleNo}/${materilasDetails.challanNo} "

        childHolder!!.tvRemarks.text = materilasDetails.remarks
        childHolder!!.tvLoggedInId.text = materilasDetails.loggedInID
        childHolder!!.tvTimestamp.text = materilasDetails.timeStamp.toString()
        childHolder!!.tvMaterialID.text = materilasDetails.materialID
        childHolder!!.tvSubCategory.text = materilasDetails.subCategory


        var materialService = materilasDetails.materialOrService
        var serviceType = ""

        if (!TextUtils.isEmpty(materilasDetails.serviceType)) {
            Log.d("TAG", "materilasDetails.serviceType =" + materilasDetails.serviceType)
            serviceType =
                mMaterialViewActivity.mServiceType[materialService]!![materilasDetails.serviceType]!!
        }

        var medium = ""

        if (!TextUtils.isEmpty(materilasDetails.medium)) {
            medium = mMaterialViewActivity.mMedium[materilasDetails.medium]!!
        }


        childHolder!!.tvMaterialService.text =
            "${mMaterialViewActivity.mMaterialOrService[materialService]} $serviceType $medium "

        childHolder.editTransaction.setTag(R.string.tag_material_id, materilasDetails.materialID)
        childHolder.tvSupplier.setTag(R.string.tag_user_id, getUserId(materilasDetails.supplierId))
        childHolder.tvReporter.setTag(
            R.string.tag_user_id,
            getUserId(materilasDetails.reporterId)
        )

        val mIsAdmin: Boolean = LedgerUtils.signInProfile!!.isAdmin

        childHolder.btnLayout.visibility = View.GONE
        if (!materilasDetails.verified && mIsAdmin) {
            childHolder.btnLayout.visibility = View.VISIBLE
        }


        childHolder.btnVerify.setTag(
            R.string.tag_material_id,
            materilasDetails.materialID
        )
        childHolder.btnVerify.setTag(
            R.string.tag_group_pos, groupPos
        )

        childHolder.btnDel.setTag(R.string.tag_material_id, materilasDetails.materialID)
        childHolder.tvLoggedInId.setTag(R.string.tag_user_id, materilasDetails.loggedInID)
        childHolder.tvProjectID.setTag(R.string.tag_project_id, materilasDetails.projectId)


        childHolder.btnDel.setOnClickListener(mMaterialViewActivity)
        childHolder.btnVerify.setOnClickListener(mMaterialViewActivity)

        childHolder.tvSupplier.setOnClickListener(mMaterialViewActivity)
        childHolder.tvReporter.setOnClickListener(mMaterialViewActivity)
        childHolder.tvLoggedInId.setOnClickListener(mMaterialViewActivity)
        childHolder.tvProjectID.setOnClickListener(mMaterialViewActivity)

        childHolder.editTransaction.setOnClickListener(mMaterialViewActivity)

        if (materilasDetails.verified) {
            view!!.setBackgroundColor(Color.parseColor("#FFF3F3F3"))
        } else {
            view!!.setBackgroundColor(Color.parseColor("#FBC7D9"))
        }

        return view
    }

    private fun getUserId(userAccount: String): String? {
        var userId: String = ""
        userId = userAccount.substring(2)
        return userId

    }

    override fun areAllItemsEnabled(): Boolean {
        return true
    }

    override fun getChildId(p0: Int, childId: Int): Long {
        return childId.toLong()
    }

    override fun getCombinedGroupId(groupID: Long): Long {
        return groupID
    }

    override fun getGroupView(
        pos: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        var view: View? = convertView
        var groupHolder: GroupHolder? = null
        if (view == null) {
            view = mInflater.inflate(R.layout.row_material_group_view, parent, false);
            groupHolder = GroupHolder(view)
            view.tag = groupHolder
        } else {
            groupHolder = view.tag as GroupHolder?
        }

        val materialsDetails: MaterialDetails = mMaterialsList[pos]


        if (isExpanded) {
            view!!.setBackgroundResource(R.drawable.selected_group)
        } else {
            view!!.setBackgroundColor(Color.parseColor("#00000000"))
        }

        var project = mMaterialViewActivity.mProjectsMap[materialsDetails.projectId]
        if (!TextUtils.isEmpty(materialsDetails.subCategory)) project += "[${materialsDetails.subCategory}]"
        groupHolder!!.cbProject.text = project

        groupHolder!!.tvSupplier.text = getSpannableString(
            materialsDetails.supplierId,
            mMaterialViewActivity.mUsersMap[getUserId(materialsDetails.supplierId)]
        )
        groupHolder!!.tvDate.text = LedgerUtils.getConvertDate(materialsDetails.date)

        var quantityAndRate =
            mMaterialViewActivity.mMaterialOrService[materialsDetails.materialOrService] +
                    " [" + materialsDetails.quantity + " " + mMaterialViewActivity.mUnits[materialsDetails.unit] + " X ${materialsDetails.rate}]"

        var amount = LedgerUtils.getRupeesFormatted(materialsDetails.amount.toLong())

        groupHolder!!.tvMaterialQuantity.text = quantityAndRate
        groupHolder!!.tvAmount.text = amount

        groupHolder!!.tvVehicleAndChallanNo.text =
            "${materialsDetails.vehicleNo}/${materialsDetails.challanNo} "

        groupHolder!!.cbProject.setTag(
            R.string.tag_material_id,
            materialsDetails.materialID
        )
        groupHolder!!.tvMaterialQuantity.setTag(
            R.string.tag_material_id,
            materialsDetails.materialID
        )

        if (!TextUtils.isEmpty(materialsDetails.imageLink)) {
            groupHolder!!.tvMaterialQuantity.setTextColor(
                mMaterialViewActivity.resources
                    .getColorStateList(R.color.selector_text_color_state, null)
            )
        } else {
            groupHolder!!.tvMaterialQuantity.setTextColor(
                mMaterialViewActivity.resources
                    .getColorStateList(R.color.selector_textview_pressed, null)
            )
        }
        groupHolder!!.tvMaterialQuantity.setOnLongClickListener(mMaterialViewActivity)
        groupHolder!!.cbProject.setOnClickListener(mMaterialViewActivity)

        groupHolder!!.cbProject.isChecked = materialsDetails.isMaterialChecked

        if (materialsDetails.verified) {
            view!!.setBackgroundColor(Color.parseColor("#FFF3F3F3"))
        } else {
            view!!.setBackgroundColor(Color.parseColor("#FBC7D9"))
        }

        if (materialsDetails.isMaterialChecked) {
            view!!.setBackgroundColor(Color.parseColor("#A0A1A3"))
        }

        if (materialsDetails.isMaterialSelected) {
            view!!.setBackgroundColor(Color.parseColor("#CDDC39"))
        }

        return view
    }

    override fun unregisterDataSetObserver(p0: DataSetObserver?) {
    }

    override fun getGroupCount(): Int {
        return mMaterialsList.size
    }

    class GroupHolder(view: View) {
        var cbProject = view.findViewById<CheckBox>(R.id.cb_project)
        var tvSupplier = view.findViewById<TextView>(R.id.tv_supplier)
        val tvDate = view.findViewById<TextView>(R.id.tv_date)
        val tvMaterialQuantity = view.findViewById<TextView>(R.id.tv_material_and_quantity)
        var tvAmount = view.findViewById<TextView>(R.id.tv_material_amount)
        val tvVehicleAndChallanNo = view.findViewById<TextView>(R.id.tv_vehicle_and_challan_no)
    }

    class ChildHolder(view: View) {

        val tvMaterialService = view.findViewById<TextView>(R.id.tv_material_or_service)

        var tvRateQuantityAmount = view.findViewById<TextView>(R.id.tv_quantity_rate_amount)
        val tvDate = view.findViewById<TextView>(R.id.tv_date)

        var tvSupplier = view.findViewById<TextView>(R.id.tv_supplier)
        val tvReporter = view.findViewById<TextView>(R.id.tv_reporter_id)
        val tvVehicleAndChallanNo = view.findViewById<TextView>(R.id.tv_vehicle_and_challan_no)

        var tvProjectID = view.findViewById<TextView>(R.id.tv_project_id)

        var tvTimestamp = view.findViewById<TextView>(R.id.tv_time_stamp)
        var tvLoggedInId = view.findViewById<TextView>(R.id.tv_log_in_id)
        val tvRemarks = view.findViewById<TextView>(R.id.tv_remarks)
        val tvMaterialID = view.findViewById<TextView>(R.id.tv_material_id)
        var tvSubCategory = view.findViewById<TextView>(R.id.tv_subcategory)

        var editTransaction = view.findViewById<TextView>(R.id.tv_edit_material)

        var btnLayout = view.findViewById<LinearLayout>(R.id.ll_btn_layout)
        var btnDel = view.findViewById<Button>(R.id.btn_del)
        var btnVerify = view.findViewById<Button>(R.id.btn_verify)
    }

    private fun getSpannableString(key: String?, value: String?): SpannableString {
        if (key == null || value == null) return SpannableString(key)

        val bold = StyleSpan(Typeface.BOLD)
        val normal = StyleSpan(Typeface.NORMAL)

        val content = SpannableString("$key [$value]")

        content.setSpan(bold, 0, key.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        content.setSpan(normal, key.length + 1, content.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return content
    }


}