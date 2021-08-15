package com.datamangement.devast007.bahikhata.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.datamangement.devast007.bahikhata.R
import com.datamangement.devast007.bahikhata.firestore.FirestoreDataBase
import com.datamangement.devast007.bahikhata.ui.MaterialAndServiceViewActivity
import com.datamangement.devast007.bahikhata.utils.LedgerDefine
import com.datamangement.devast007.bahikhata.utils.LedgerSharePrefManger
import com.datamangement.devast007.bahikhata.utils.IdAndNameObject
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.FieldValue
import java.util.HashMap


class DialogFragmentForUnitAndMedium : DialogFragment() {

    private var mType: Int = -1
    private var mAdapter: Adapter? = null
    private val TAG = "DialogFragmentForUnit"
    private var materialAndServiceActivity: MaterialAndServiceViewActivity? = null
    private lateinit var mBottomSheetDialog: Dialog
    private var mArrayList: ArrayList<IdAndNameObject>? = null
    private var mBaseFireStoreDoc: String? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        materialAndServiceActivity = activity as MaterialAndServiceViewActivity
        mArrayList = materialAndServiceActivity!!.mArrayList
        mBottomSheetDialog = Dialog(
            activity!!,
            R.style.MaterialDialogSheetAmount
        )
        mBottomSheetDialog.setContentView(R.layout.dialog_common_for_material)
        mBottomSheetDialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mBottomSheetDialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.horizontalMargin = 10.10f
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        lp.verticalMargin = 5.0f
        lp.gravity = Gravity.TOP
        lp.windowAnimations = R.style.MaterialDialogSheetAnimation
        mBottomSheetDialog.window!!.attributes = lp

        val companyID = LedgerSharePrefManger(materialAndServiceActivity).getCompanyID()
        mType = arguments!!.getInt(LedgerDefine.KEY_SELECTION_TYPE)
        if (mType == LedgerDefine.TYPE_UNIT) {
            mBaseFireStoreDoc = LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_UNITS
            mBottomSheetDialog.findViewById<TextView>(R.id.title).setText(R.string.manage_units)
        } else {
            mBaseFireStoreDoc = LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MEDIUM
            mBottomSheetDialog.findViewById<TextView>(R.id.title).setText(R.string.manage_madium)
        }

        var listView = mBottomSheetDialog.findViewById<ListView>(android.R.id.list)
        addNewItems(mBottomSheetDialog)
        mAdapter = Adapter()!!
        listView.adapter = mAdapter

        return mBottomSheetDialog

    }

    private fun addNewItems(view: Dialog) {
        val etAddNew = view.findViewById<EditText>(R.id.et_add_new_service_type)
        if (mType == LedgerDefine.TYPE_UNIT) {
            etAddNew.setHint(R.string.add_new_units)
        } else {
            etAddNew.setHint(R.string.add_new_medium)
        }

        view.findViewById<ImageView>(R.id.iv_add_new_item).setOnClickListener {
            val etAddNewTxt = "" + etAddNew.text
            if (!TextUtils.isEmpty(etAddNewTxt)) {
                var size = mArrayList!!.size
                var id = "" + (size + 1)
                if (id.toInt() < 10) {
                    id = "0$id"
                }
                if (mType == LedgerDefine.TYPE_UNIT) {
                    id += "_UNIT"
                } else {
                    id += "_MEDIUM"
                }
                val db = FirestoreDataBase().db
                var masterRef =
                    db.collection(mBaseFireStoreDoc!!)
                        .document(id!!)
                val map = HashMap<String, Any>()

                map[LedgerDefine.NAME] = "" + etAddNewTxt
                map[LedgerDefine.ID] = id
                map[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()

                masterRef.set(map)
                    .addOnSuccessListener(OnSuccessListener<Void> {
                        etAddNew.setText("")
                        var details = IdAndNameObject()
                        details.id = id
                        details.name = etAddNewTxt
                        mArrayList!!.add(details)
                        mAdapter!!.notifyDataSetChanged()
                    })
                    .addOnFailureListener(OnFailureListener { e ->

                    })
            } else {
                Toast.makeText(materialAndServiceActivity, "No text ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class Adapter : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view: View? = convertView
            if (view == null) {
                var inflater: LayoutInflater =
                    materialAndServiceActivity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                view = inflater.inflate(R.layout.row_service_type_view, parent, false)
            }

            val item = getItem(position) as IdAndNameObject
            var et = view!!.findViewById<EditText>(R.id.et_service_type)
            var iv = view!!.findViewById<ImageView>(R.id.iv_edit_service_type)
            iv.setOnClickListener {
                if (!TextUtils.isEmpty(et.text)) {
                    val db = FirestoreDataBase().db
                    val companyID = LedgerSharePrefManger(materialAndServiceActivity).getCompanyID()
                    var masterRef =
                        db.collection(mBaseFireStoreDoc!!)
                            .document(item.id)

                    val serviceTypeMap = HashMap<String, Any>()

                    serviceTypeMap[LedgerDefine.NAME] = "" + et.text
                    serviceTypeMap[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()

                    masterRef.update(serviceTypeMap)
                        .addOnSuccessListener(OnSuccessListener<Void> {
                            for (details in mArrayList!!) {
                                if (details.id == item.id) {
                                    details.name = "" + et.text
                                    break
                                }
                            }
                            mAdapter!!.notifyDataSetChanged()
                        })
                        .addOnFailureListener(OnFailureListener { e ->

                        })
                } else {
                    Toast.makeText(materialAndServiceActivity, "No text ", Toast.LENGTH_SHORT)
                        .show()
                }

            }
            et.setText(item.name)
            et.setTag(R.string.tag_id, item.id)
            iv.setTag(R.string.tag_id, item.id)
            return view!!
        }

        override fun getItem(position: Int): Any {
            return mArrayList!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return mArrayList!!.size
        }
    }
}
