package com.datamangement.devast007.bahikhata.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils.isEmpty
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import com.datamangement.devast007.bahikhata.R
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class LedgerUtils {

    companion object {
        private var statusListner: StatusListner? = null
        private val TAG: String? = "LedgerUtils"
        var signInProfile: SignInProfile? = null


        internal fun getTransactionDetails(document: QueryDocumentSnapshot): TransactionDetails {
            val transactionID = document.get(LedgerDefine.TRANSACTION_ID).toString()

            var transactionDetail = TransactionDetails()
            transactionDetail.senderId = document.get(LedgerDefine.SENDER_ID) as String
            transactionDetail.receiverId = document.get(LedgerDefine.RECEIVER_ID) as String
            transactionDetail.amount = document.get(LedgerDefine.AMOUNT) as Long
            var roundOff = document!!.get(LedgerDefine.ROUND_OFF)
            if (roundOff != null) {
                transactionDetail.roundOff = roundOff as Long
            }
            var projectID = document.get(LedgerDefine.PROJECT_ID)
            if (projectID != null) transactionDetail.projectId = projectID as String
            transactionDetail.transactionDate =
                document.get(LedgerDefine.TRANSACTION_DATE) as String

            try {
                transactionDetail.timeStamp =
                    document.getTimestamp(LedgerDefine.TIME_STAMP)!!.toDate().toString()
            } catch (e: RuntimeException) {
                transactionDetail.timeStamp = document.get(LedgerDefine.TIME_STAMP).toString()
            } catch (e: ClassCastException) {
                transactionDetail.timeStamp =
                    (document.get(LedgerDefine.TIME_STAMP) as Date).toString()
            }

            transactionDetail.transactionID = transactionID
            transactionDetail.transactionType =
                document.get(LedgerDefine.TRANSACTION_TYPE) as Long
            transactionDetail.loggedInID = document.get(LedgerDefine.LOGGED_IN_ID) as String
            transactionDetail.verified = document.get(LedgerDefine.VERIFIED) as Boolean


            var subCategory = document.get(LedgerDefine.SUBCATEGORY)
            if (subCategory != null) transactionDetail.subCategory = subCategory as String


            var remark = document.get(LedgerDefine.REMARK)
            if (remark != null) transactionDetail.remarks = remark as String

            var debitAccount = document.get(LedgerDefine.DEBIT_ACCOUNT_ID)
            if (debitAccount != null) transactionDetail.debitedTo = debitAccount.toString()

            var creditAccount = document.get(LedgerDefine.CREDIT_ACCOUNT_ID)
            if (creditAccount != null) transactionDetail.creditedTo = creditAccount.toString()

            var paymentMode = document.get(LedgerDefine.PAYMENT_MODE)
            if (paymentMode != null) transactionDetail.paymentMode = paymentMode.toString()

            var isTrackingOn = document.get(LedgerDefine.IS_TRACKING_ON)
            if (isTrackingOn != null) transactionDetail.isTrackingOn = isTrackingOn as Boolean

            var relatedTransactionIds = document.get(LedgerDefine.RELATED_TRANSACTIONS_IDS)
            if (relatedTransactionIds != null) transactionDetail.relatedTransactionsIds =
                relatedTransactionIds as ArrayList<String>

            var imageLink = document.get(LedgerDefine.IMAGE_LINK)
            if (imageLink != null) transactionDetail.imageLink = imageLink as String

            return transactionDetail
        }


        fun getRupeesFormatted(amount: Long): SpannableString {
            var strAmount: String? = null
            try {
                val indian = Locale("en", "IN")
                val numberFormat = NumberFormat.getCurrencyInstance(indian)
                numberFormat.maximumFractionDigits = 0
                strAmount = numberFormat.format(amount).toString()

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val content = SpannableString(strAmount)
            var color: Int = Color.BLACK
            if (amount < 0) {
                color = Color.RED
            }
            content.setSpan(
                ForegroundColorSpan(color),
                0,
                strAmount!!.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            return content
        }

        fun getRupeesFormatted(amount: String): String {
            var strAmount: String? = ""
            if (!isEmpty(amount)) {
                try {
                    val indian = Locale("en", "IN")
                    val numberFormat = NumberFormat.getCurrencyInstance(indian)
                    numberFormat.maximumFractionDigits = 0
                    strAmount = numberFormat.format(amount.toLong()).toString()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return strAmount!!
        }


        fun getConvertDate(original: String): String? {
            val originalFormat = SimpleDateFormat("yyyyMMddHHmmss")
            val targetFormat = SimpleDateFormat("dd-MM-yyyy")
            val date = originalFormat.parse(original)
            val formattedDate = targetFormat.format(date)

            return formattedDate.toString()

        }


        fun getUserAccount(str: String): String? {
            var account: String? = ""
            if (!isEmpty(str)) {
                account = str.substringBefore("\n")
            }
            return account
        }

        fun setDataToFirestore(
            context: Context,
            id: Any,
            uri: Uri,
            type: Int,
            masterRef: DocumentReference,
            data: HashMap<String, Any>
        ) {

            Log.d(
                TAG,
                "map data $data"
            )


            if (!isTimeAutomatic(context)) {
                Toast.makeText(context, "Please set automatic time!!", Toast.LENGTH_LONG).show()
                return
            }

            data[LedgerDefine.SYSTEM_MILLI] = System.currentTimeMillis()
            when (type) {
                LedgerDefine.SET_DATA -> {

                    masterRef.set(data) // 3rd(1), set the actual data with SYSTEM_MILLI
                        .addOnSuccessListener(OnSuccessListener<Void> {

                            masterRef.addSnapshotListener { snapshot, e ->
                                Log.d(TAG, "New city: ${snapshot}")
                                //  updateDataIntoSqlDb(context,type, id, uri, snapshot!!.data)
                            }
                            statusListner!!.onComplete(true)
                            Log.w(
                                TAG,
                                "set the actual data with SYSTEM_MILLI"
                            )
                        })
                        .addOnFailureListener(OnFailureListener { e ->
                            statusListner!!.onComplete(false)
                            Log.w(
                                TAG,
                                "Error writing document",
                                e
                            )
                        })

                }
                LedgerDefine.UPDATE_DATA -> {


                    masterRef.update(data)
                        .addOnSuccessListener(OnSuccessListener {
                            statusListner!!.onComplete(true)
                            masterRef.addSnapshotListener { snapshot, e ->
                                Log.d(TAG, "update city: ${snapshot}")
                            }
                            Log.w(
                                TAG,
                                "update the actual data with SYSTEM_MILLI"
                            )
                        }).addOnFailureListener(OnFailureListener { e ->
                            statusListner!!.onComplete(false)
                            Log.w(
                                TAG,
                                "Error writing document",
                                e
                            )
                        })

                }
            }
        }

        private fun updateDataIntoSqlDb(
            context: Context,
            type: Int,
            id: Any,
            uri: Uri,
            data: Map<String, Any>?
        ) {

            // DataModel.getBankAccountDetails(data)


        }


        fun isTimeAutomatic(c: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Settings.Global.getInt(
                    c.contentResolver,
                    Settings.Global.AUTO_TIME,
                    0
                ) == 1
            } else {
                Settings.System.getInt(
                    c.getContentResolver(),
                    Settings.System.AUTO_TIME,
                    0
                ) == 1
            }
        }


        fun setstatusListner(listner: StatusListner) {

            statusListner = listner
        }

        fun isHasEditPermission(ctx: Context?): Boolean {

            if (!signInProfile!!.isHasEditPermission) {
                Toast.makeText(ctx, R.string.no_edit_permission, Toast.LENGTH_LONG).show()
                return false
            }

            return true
        }


        interface StatusListner {
            fun onComplete(status: Boolean)
        }

    }
}