package com.datamangement.devast007.bahikhata.ui.adapter

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.ui.GstViewActivity
import com.datamangement.devast007.bahikhata.utils.GstDetails
import com.datamangement.devast007.bahikhata.utils.LedgerUtils

class GstViewAdapter(
    gstViewActivity: GstViewActivity,
    gstlists: ArrayList<GstDetails>
) : BaseExpandableListAdapter() {
    private val mGstViewActivity: GstViewActivity = gstViewActivity
    val mGstList = gstlists
    var mInflater: LayoutInflater =
        mGstViewActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getChildrenCount(p0: Int): Int {
        return 1;
    }

    override fun getGroup(groupPos: Int): Any {
        return mGstList.get(groupPos)
    }

    override fun onGroupCollapsed(p0: Int) {
    }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun registerDataSetObserver(p0: DataSetObserver?) {
    }

    override fun getChild(group: Int, child: Int): Any {
        return mGstList.get(group)
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
        pos: Int,
        p1: Int,
        p2: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        var view: View? = convertView
        var childHolder: ChildHolder? = null
        if (view == null) {
            view = mInflater.inflate(R.layout.row_gst_child_view, parent, false);
            childHolder = ChildHolder(view)
            view.tag = childHolder
        } else {
            childHolder = view.tag as ChildHolder?
        }


        val gstDetails: GstDetails = mGstList[pos]

        childHolder!!.tvsupplierID.text = getSpannableString(
            gstDetails.supplierID,
            mGstViewActivity.mUsersMap[getUserId(gstDetails.supplierID)]
        )
        childHolder!!.tvRecevierID.text = getSpannableString(
            gstDetails.receiverId,
            mGstViewActivity.mUsersMap[getUserId(gstDetails.receiverId)]
        )

        childHolder!!.tvDate.text = LedgerUtils.getConvertDate(gstDetails.date)
        childHolder!!.tvBillAmount.text =
            LedgerUtils.getRupeesFormatted(gstDetails.billAmount.toLong())
        childHolder!!.tvProjectID.text = mGstViewActivity.mProjectsMap[gstDetails.projectId]
        childHolder!!.tvMaterial.text = gstDetails.material


        childHolder!!.tvRemarks.text = gstDetails.remarks
        childHolder!!.tvLoggedInId.text = gstDetails.loggedInID
        childHolder!!.tvTimestamp.text = gstDetails.timeStamp.toString()
        childHolder!!.tvgstId.text = gstDetails.gstId

        childHolder!!.tvGsttax.text = gstDetails.gstTax
        childHolder!!.tvGstPercentage.text = gstDetails.gstTaxPercent

        childHolder.editTransaction.setTag(R.string.tag_gst_id, gstDetails.gstId)
        childHolder.tvsupplierID.setTag(R.string.tag_user_id, getUserId(gstDetails.supplierID))
        childHolder.tvRecevierID.setTag(R.string.tag_user_id, getUserId(gstDetails.receiverId))
        childHolder.tvLoggedInId.setTag(R.string.tag_user_id, gstDetails.loggedInID)
        childHolder.tvProjectID.setTag(R.string.tag_project_id, gstDetails.projectId)

        childHolder.tvsupplierID.setOnClickListener(mGstViewActivity)
        childHolder.tvRecevierID.setOnClickListener(mGstViewActivity)
        childHolder.tvLoggedInId.setOnClickListener(mGstViewActivity)
        childHolder.tvProjectID.setOnClickListener(mGstViewActivity)

        childHolder.editTransaction.setOnClickListener(mGstViewActivity)

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
            view = mInflater.inflate(R.layout.row_gst_group_view, parent, false);
            groupHolder = GroupHolder(view)
            view.tag = groupHolder
        } else {
            groupHolder = view.tag as GroupHolder?
        }

        val gstDetails: GstDetails = mGstList[pos]

        groupHolder!!.tvSupplierID.text = getSpannableString(
            gstDetails.supplierID,
            mGstViewActivity.mUsersMap[getUserId(gstDetails.supplierID)]
        )

        if (isExpanded) {
            view!!.setBackgroundResource(R.drawable.selected_group)
        } else {
            view!!.setBackgroundColor(Color.parseColor("#00000000"))
        }


        groupHolder!!.tvMaterial.text = gstDetails.material
        groupHolder!!.tvDate.text = LedgerUtils.getConvertDate(gstDetails.date)
        groupHolder!!.tvBillAmount.text =
            LedgerUtils.getRupeesFormatted(gstDetails.billAmount.toLong())

        return view
    }

    override fun unregisterDataSetObserver(p0: DataSetObserver?) {
    }

    override fun getGroupCount(): Int {
        return mGstList.size
    }

    class GroupHolder(view: View) {
        var tvMaterial = view.findViewById<TextView>(R.id.tv_supplier)
        val tvDate = view.findViewById<TextView>(R.id.tv_date)
        val tvSupplierID = view.findViewById<TextView>(R.id.tv_sender_id)
        var tvBillAmount = view.findViewById<TextView>(R.id.tv_bill_amount)
    }

    class ChildHolder(view: View) {

        val tvMaterial = view.findViewById<TextView>(R.id.tv_material)
        val tvGsttax = view.findViewById<TextView>(R.id.tv_gst_tax)
        val tvGstPercentage = view.findViewById<TextView>(R.id.tv_gst_percentage)

        var tvBillAmount = view.findViewById<TextView>(R.id.tv_bill_amount)
        val tvDate = view.findViewById<TextView>(R.id.tv_date)

        var tvsupplierID = view.findViewById<TextView>(R.id.tv_sender_id)
        val tvRecevierID = view.findViewById<TextView>(R.id.tv_receiver_id)

        var tvProjectID = view.findViewById<TextView>(R.id.tv_project_id)

        var tvTimestamp = view.findViewById<TextView>(R.id.tv_time_stamp)
        var tvLoggedInId = view.findViewById<TextView>(R.id.tv_log_in_id)
        val tvRemarks = view.findViewById<TextView>(R.id.tv_remarks)
        val tvgstId = view.findViewById<TextView>(R.id.tv_gst_id)

        var editTransaction = view.findViewById<TextView>(R.id.tv_edit_gst)
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