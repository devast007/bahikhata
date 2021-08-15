package com.datamangement.devast007.bahikhata.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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


class DialogFragmentForServiceType : DialogFragment() {

    private var mAdapter: Adapter? = null
    private val TAG = "FragmentForServiceType"
    private var materialAndServiceActivity: MaterialAndServiceViewActivity? = null
    private lateinit var mBottomSheetDialog: Dialog
    private var mServiceType: ArrayList<IdAndNameObject>? = null
    private var materialAndServiceId: String? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        materialAndServiceActivity = activity as MaterialAndServiceViewActivity
        mServiceType = materialAndServiceActivity!!.mArrayList
        materialAndServiceId = materialAndServiceActivity!!.mSelectedMaterialServiceId
        Log.d(TAG, " onItemClick materialAndServiceId  " + materialAndServiceId)
        mBottomSheetDialog = Dialog(
            activity!!,
            R.style.MaterialDialogSheetAmount
        )
        mBottomSheetDialog.setContentView(R.layout.dialog_common_for_material)
        mBottomSheetDialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(mBottomSheetDialog.window!!.attributes)
        lp.width = 700
        lp.horizontalMargin = 10.10f
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        lp.verticalMargin = 5.0f
        lp.gravity = Gravity.TOP
        lp.windowAnimations = R.style.MaterialDialogSheetAnimationAmount
        mBottomSheetDialog.window!!.attributes = lp
        mBottomSheetDialog.findViewById<TextView>(R.id.title).text = materialAndServiceId
        var listView = mBottomSheetDialog.findViewById<ListView>(android.R.id.list)
        addNewItems(mBottomSheetDialog)
        mAdapter = Adapter()!!
        listView.adapter = mAdapter

        return mBottomSheetDialog

    }

    private fun addNewItems(view: Dialog) {
        val etAddNew = view.findViewById<EditText>(R.id.et_add_new_service_type)

        view.findViewById<ImageView>(R.id.iv_add_new_item).setOnClickListener {
            val etAddNewTxt = "" + etAddNew.text
            if (!TextUtils.isEmpty(etAddNewTxt)) {
                var size = mServiceType!!.size
                var id = "" + (size + 1)
                if (id.toInt() < 10) {
                    id = "0$id"
                }
                id += "_ST"

                val db = FirestoreDataBase().db
                val companyID = LedgerSharePrefManger(materialAndServiceActivity).getCompanyID()
                var masterRef =
                    db.collection(
                        LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIAL_AND_SERVICES_SETTINGS
                                + "/" + materialAndServiceId + LedgerDefine.SLASH_SERVICE_TYPES
                    )
                        .document(id!!)

                val serviceTypeMap = HashMap<String, Any>()

                serviceTypeMap[LedgerDefine.NAME] = "" + etAddNewTxt
                serviceTypeMap[LedgerDefine.ID] = id
                serviceTypeMap[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()

                masterRef.set(serviceTypeMap)
                    .addOnSuccessListener(OnSuccessListener<Void> {
                        etAddNew.setText("")
                        var details = IdAndNameObject()
                        details.id = id
                        details.name = etAddNewTxt
                        mServiceType!!.add(details)
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
                        db.collection(
                            LedgerDefine.COMPANIES_SLASH + companyID + LedgerDefine.SLASH_MATERIAL_AND_SERVICES_SETTINGS
                                    + "/" + materialAndServiceId + LedgerDefine.SLASH_SERVICE_TYPES
                        )
                            .document(item.id)

                    val serviceTypeMap = HashMap<String, Any>()

                    serviceTypeMap[LedgerDefine.NAME] = "" + et.text
                    serviceTypeMap[LedgerDefine.TIME_STAMP] = FieldValue.serverTimestamp()

                    masterRef.update(serviceTypeMap)
                        .addOnSuccessListener(OnSuccessListener<Void> {
                            for (details in mServiceType!!) {
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
            return mServiceType!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return mServiceType!!.size
        }
    }
}
