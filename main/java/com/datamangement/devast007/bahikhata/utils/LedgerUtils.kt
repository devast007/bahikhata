package com.datamangement.devast007.bahikhata.utils

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils.isEmpty
import android.text.style.ForegroundColorSpan
import android.util.Log
import kotlinx.android.synthetic.main.activity_add_transaction.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.activity_add_transaction.*

class LedgerUtils {

    companion object {
        var signInProfile: SignInProfile? = null
            get() = field
            set(value) {
                field = value
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
                    strAmount = numberFormat.format(amount).toString()

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
            var account: String? = null
            if (!isEmpty(str)) {
                account = str.substringBefore("\n")
            }
            return account
        }


    }
}