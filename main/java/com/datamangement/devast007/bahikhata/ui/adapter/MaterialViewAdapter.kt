package com.datamangement.devast007.bahikhata.ui.adapter

import android.content.Context
import android.database.DataSetObserver
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
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

    override fun getChildView(pos: Int, p1: Int, p2: Boolean, convertView: View?, parent: ViewGroup?): View? {
        var view: View? = convertView
        var childHolder: ChildHolder? = null
        if (view == null) {
            view = mInflater.inflate(R.layout.row_material_child_view, parent, false);
            childHolder = ChildHolder(view)
            view.tag = childHolder
        } else {
            childHolder = view.tag as ChildHolder?
        }


        val materilasDetails: MaterialDetails = mMaterialsList[pos]
        childHolder!!.tvSenderID.text = materilasDetails.senderId
        childHolder!!.tvRecevierID.text = materilasDetails.receiverId
        childHolder!!.tvDate.text = LedgerUtils.getConvertDate(materilasDetails.date)
        childHolder!!.tvAmount.text = LedgerUtils.getRupeesFormatted(materilasDetails.amount.toLong())
        childHolder!!.tvProjectID.text = materilasDetails.projectId
        childHolder!!.tvMaterial.text = materilasDetails.material


        childHolder!!.tvRemarks.text = materilasDetails.remarks
        childHolder!!.tvLoggedInId.text = materilasDetails.loggedInID
        childHolder!!.tvTimestamp.text = materilasDetails.timeStamp.toString()
        childHolder!!.tvMaterialID.text = materilasDetails.materialID

        childHolder!!.tvRate.text = materilasDetails.rate
        childHolder!!.tvQuantity.text = materilasDetails.quantity

        childHolder.editTransaction.setTag(R.string.tag_material_id, materilasDetails.materialID)
        childHolder.tvSenderID.setTag(R.string.tag_user_id, materilasDetails.senderId)
        childHolder.tvRecevierID.setTag(R.string.tag_user_id, materilasDetails.receiverId)
        childHolder.tvLoggedInId.setTag(R.string.tag_user_id, materilasDetails.loggedInID)
        childHolder.tvProjectID.setTag(R.string.tag_project_id, materilasDetails.projectId)

        childHolder.tvSenderID.setOnClickListener(mMaterialViewActivity)
        childHolder.tvRecevierID.setOnClickListener(mMaterialViewActivity)
        childHolder.tvLoggedInId.setOnClickListener(mMaterialViewActivity)
        childHolder.tvProjectID.setOnClickListener(mMaterialViewActivity)

        childHolder.editTransaction.setOnClickListener(mMaterialViewActivity)

        return view
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

    override fun getGroupView(pos: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View? {
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
        groupHolder!!.tvMaterial.text = materialsDetails.material
        groupHolder!!.tvDate.text = LedgerUtils.getConvertDate(materialsDetails.date)
        groupHolder!!.tvRateIntoQuantity.text = materialsDetails.rate + " * " + materialsDetails.quantity
        groupHolder!!.tvAmount.text = LedgerUtils.getRupeesFormatted(materialsDetails.amount.toLong())

        return view
    }

    override fun unregisterDataSetObserver(p0: DataSetObserver?) {
    }

    override fun getGroupCount(): Int {
        return mMaterialsList.size
    }

    class GroupHolder(view: View) {
        var tvMaterial = view.findViewById<TextView>(R.id.tv_material)
        val tvDate = view.findViewById<TextView>(R.id.tv_date)
        val tvRateIntoQuantity = view.findViewById<TextView>(R.id.tv_rate_into_quantity)
        var tvAmount = view.findViewById<TextView>(R.id.tv_amount)
    }

    class ChildHolder(view: View) {

        val tvMaterial = view.findViewById<TextView>(R.id.tv_material)
        val tvRate = view.findViewById<TextView>(R.id.tv_rate)
        val tvQuantity = view.findViewById<TextView>(R.id.tv_quantity)

        var tvAmount = view.findViewById<TextView>(R.id.tv_amount)
        val tvDate = view.findViewById<TextView>(R.id.tv_date)

        var tvSenderID = view.findViewById<TextView>(R.id.tv_sender_id)
        val tvRecevierID = view.findViewById<TextView>(R.id.tv_receiver_id)

        var tvProjectID = view.findViewById<TextView>(R.id.tv_project_id)

        var tvTimestamp = view.findViewById<TextView>(R.id.tv_time_stamp)
        var tvLoggedInId = view.findViewById<TextView>(R.id.tv_log_in_id)
        val tvRemarks = view.findViewById<TextView>(R.id.tv_remarks)
        val tvMaterialID = view.findViewById<TextView>(R.id.tv_material_id)

        var editTransaction = view.findViewById<TextView>(R.id.tv_edit_material)
    }


}