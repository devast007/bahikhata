package com.datamangement.devast007.bahikhata.utils

import android.content.ContentValues
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.*
import kotlin.collections.HashMap

class DataModel {

    companion object{
        public fun getBankAccountDetails(document: QueryDocumentSnapshot?): BankAccountDetail {
            var accountDetails: BankAccountDetail = BankAccountDetail()
            if (document != null) {
                accountDetails.id = document.get(LedgerDefine.BANK_ACCOUNT_ID) as String
                accountDetails.payee = document.get(LedgerDefine.PAYEE_NAME) as String
                accountDetails.accountNo = document.get(LedgerDefine.BANK_ACCOUNT_NUMBER) as String
                accountDetails.ifscCode = document.get(LedgerDefine.IFSC_CODE) as String
                accountDetails.branch = document.get(LedgerDefine.BANK_ACCOUNT_BRANCH_NAME) as String

                var timestamp = document.get(LedgerDefine.TIME_STAMP)
                if (timestamp != null) {
                    try {
                        accountDetails.timestamp = timestamp as String
                    } catch (e: java.lang.ClassCastException) {
                        accountDetails.timestamp = (timestamp as Date).toString()
                    }
                }

                val amount = document.get(LedgerDefine.AMOUNT)
                if (amount != null) accountDetails.amount = amount as Long
                accountDetails.remarks = document.get(LedgerDefine.REMARK) as String

                val systemMilli = document.get(LedgerDefine.SYSTEM_MILLI)
                if (systemMilli != null) accountDetails.systemMill = systemMilli as Long

            }
            return accountDetails
        }

        public fun getBankAccountDetails(document: Map<String, Any>?): BankAccountDetail {
            var accountDetails: BankAccountDetail = BankAccountDetail()
            if (document != null) {
                accountDetails.id = document[LedgerDefine.BANK_ACCOUNT_ID] as String
                accountDetails.payee = document[LedgerDefine.PAYEE_NAME] as String
                accountDetails.accountNo = document[LedgerDefine.BANK_ACCOUNT_NUMBER] as String
                accountDetails.ifscCode = document[LedgerDefine.IFSC_CODE] as String
                accountDetails.branch = document[LedgerDefine.BANK_ACCOUNT_BRANCH_NAME] as String

                var timestamp = document[LedgerDefine.TIME_STAMP]
                if (timestamp != null) {
                    try {
                        accountDetails.timestamp = timestamp as String
                    } catch (e: java.lang.ClassCastException) {
                        accountDetails.timestamp = (timestamp as Date).toString()
                    }
                }

                val amount = document[LedgerDefine.AMOUNT]
                if (amount != null) accountDetails.amount = amount as Long
                accountDetails.remarks = document[LedgerDefine.REMARK] as String

                val systemMilli = document[LedgerDefine.SYSTEM_MILLI]
                if (systemMilli != null) accountDetails.systemMill = systemMilli as Long

            }
            return accountDetails
        }


        private fun getContentValues(accounts: BankAccountDetail): ContentValues {
            val cv = ContentValues()
            cv.put(LedgerDefine.BANK_ACCOUNT_ID, accounts.id!!)
            cv.put(LedgerDefine.PAYEE_NAME, accounts.payee!!)
            cv.put(LedgerDefine.BANK_ACCOUNT_NUMBER, accounts.accountNo!!)
            cv.put(LedgerDefine.IFSC_CODE, accounts.ifscCode!!)
            cv.put(LedgerDefine.BANK_ACCOUNT_BRANCH_NAME, accounts.branch!!)
            cv.put(LedgerDefine.TIME_STAMP, accounts.timestamp!!)
            cv.put(LedgerDefine.AMOUNT, accounts.amount!!)
            cv.put(LedgerDefine.SYSTEM_MILLI, accounts.systemMill!!)
            cv.put(LedgerDefine.REMARK, accounts.remarks!!)

            return cv

        }
    }
}