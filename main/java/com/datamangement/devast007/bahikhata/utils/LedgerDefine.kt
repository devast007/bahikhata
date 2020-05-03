package com.datamangement.devast007.bahikhata.utils

class LedgerDefine {
    companion object {

        val ADMIN_ID: String = "3010000"
        val ADMIN_ID_WITH_PREFIX: String = "A_3010000"
        val PREFIX_ADMIN: String = "A_"
        val USER_ACCOUNT_COUNT: String? = "USER_ACCOUNT_COUNT"
        val P_MATERIAL_COST: String = "p_materialCost"
        val P_PAYMENT: String = "p_payment"
        val P_GST_BILL: String = "p_gstBill"
        val M_AMOUNT: String = "m_amount"


        val PREFIX_PERSONAL: String = "P_"
        val PREFIX_MASTER: String = "M_"
        const val KEY_COMPANY_ID = "KEY_COMPANY_ID"
        const val KEY_USER_NAME = "KEY_USER_NAME"
        const val KEY_USER_ID = "KEY_USER_ID"
        const val KEY_SENDER_NAME = "KEY_SENDER_NAME"
        const val KEY_FETCHING_START_FROM = "KEY_FETCHING_START_FROM"

        const val KEY_SIGN_IN_INFO_PROFILE: String = "KEY_SIGN_IN_INFO_PROFILE"
        const val USER_ID = "userID"
        const val IS_HAS_LIMITED_ACCESS = "isHasLimitedAccess"
        const val NAME = "name"
        const val NICKNAME = "nickname"
        const val IS_ADMIN = "isAdmin"
        const val DESIGNATION = "designation"
        const val AMOUNT = "amount"
        const val PROJECT_ID = "projectID"
        const val ADDRESS = "address"
        const val START_DATE = "startDate"
        const val END_DATE = "endDate"
        const val DIVISION = "division"
        const val WORK_AMOUNT = "workAmount"
        const val PHONE_NUMBER = "phoneNumber"
        const val EMAIL = "email"
        const val ACCESSIBLE_PROJECTS = "accessibleProjects"
        const val ACCOUNTS: String = "accounts"

        // Material
        const val MATERIAL_ID = "materialID"
        const val RATE = "rate"
        const val QUANTITY = "quantity"
        const val MATERIAL = "material"
        const val DATE: String = "date"

        // GST
        const val GST_ID = "gstID"
        const val GST_BILL_AMOUNT = "billAmount"
        const val GST_TAX_AMOUNT = "gstAmount"
        const val GST_TAX_PERCENTAGE = "gstPercentage"

        const val REMARK = "remark"
        const val DESIGNATION_ADMIN: Long = 0
        const val DESIGNATION_SUPERVISOR: Long = 1
        const val DESIGNATION_NORMAL: Long = 2
        const val TIME_STAMP: String = "timeStamp"
        const val ALL_PROJECTS: String = "ALL_PROJECTS"
        const val KEY_SELECTION_TYPE: String = "KEY_SELECTION_TYPE"
        const val SELECTION_TYPE_SENDER: Int = 0
        const val SELECTION_TYPE_RECEIVER: Int = 1
        const val SELECTION_TYPE_PROJECT: Int = 2
        const val SELECTION_TYPE_DEBIT_ACCOUNT = 3
        const val SELECTION_TYPE_CREDIT_ACCOUNT = 4
        const val LOGGED_IN_ID: String = "loggedInID"
        const val SENDER_ID: String = "senderID"
        const val RECEIVER_ID: String = "receiverID"
        const val TRANSACTION_DATE: String = "transactionDate"
        const val TRANSACTION_TYPE: String = "transactionType"

        const val TRANSACTION_CHECKED: String = "TRANSACTION_CHECKED"


        const val TRANSACTION_TYPE_ADMIN = 1
        const val TRANSACTION_TYPE_SUPERVISOR = 2
        const val TRANSACTION_TYPE_NORMAL = 3
        const val VERIFIED: String = "verified"
        const val TRANSACTION_ID: String = "transactionID"
        const val TRANSACTION_VIEW_TYPE: String = "TRANSACTION_VIEW_TYPE"
        const val TRANSACTION_VIEW_TYPE_USER: Int = 1
        const val TRANSACTION_VIEW_TYPE_PROJECT: Int = 2
        const val TRANSACTION_VIEW_TYPE_BANK_ACCOUNT: Int = 3
        const val TRANSACTION_VIEW_TYPE_ALL: Int = 4

        const val ID: String = "ID"
        const val MORE_INFO_TYPE: String = "MORE_INFO_TYPE"
        const val MORE_INFO_TYPE_USER: Int = 1
        const val MORE_INFO_TYPE_PROJECT: Int = 2
        const val MORE_INFO_TYPE_ACCOUNT: Int = 3

        // Bank Account
        const val BANK_ACCOUNT_ADD_TYPE: String = "BANK_ACCOUNT_ADD_TYPE"
        const val BANK_ACCOUNT_ADD: Int = 1
        const val BANK_ACCOUNT_MODIFY: Int = 2
        const val BANK_ACCOUNT_ID: String = "bankAccountID"
        const val BANK_ACCOUNT_NUMBER: String = "bankAccountNumber"
        const val PAYEE_NAME: String = "payeeName"
        const val IFSC_CODE: String = "ifscCode"
        const val BANK_ACCOUNT_BRANCH_NAME: String = "bankAccountBranchName"

        // path
        const val SLASH_TRANSACTIONS = "/transactions"
        const val COMPANIES_SLASH = "companies/"
        const val SLASH_BANK_ACCOUNTS = "/bankAccounts"
        const val SLASH_MATERIALS = "/materials"
        const val SLASH_USERS = "/users"
        const val SLASH_PROJECTS = "/projects"
        const val SLASH_GST = "/gst"

        const val DEBIT_ACCOUNT_ID: String = "DebitAccountID"
        const val CREDIT_ACCOUNT_ID: String = "CreditAccountID"
        const val PAYMENT_MODE: String = "paymentMode"
        const val USER_ADD_TYPE: String = "USER_ADD_TYPE"
        const val USER_ADD_TYPE_NEW: Int = 1
        const val USER_ADD_TYPE_MODIFY: Int = 2
        const val IS_SKIP: String = "isSkip"
        const val MB_NO: String = "MB_NO"
        const val HEAD: String = "HEAD"
        const val MAIN_AMOUNT: String = "MAIN_AMOUNT"
        const val MAINTENANCE_1ST_YEAR_AMOUNT: String = "MAINTENANCE_1ST_YEAR_AMOUNT"
        const val MAINTENANCE_2ND_YEAR_AMOUNT: String = "MAINTENANCE_2ND_YEAR_AMOUNT"
        const val MAINTENANCE_3RD_YEAR_AMOUNT: String = "MAINTENANCE_3RD_YEAR_AMOUNT"
        const val MAINTENANCE_4TH_YEAR_AMOUNT: String = "MAINTENANCE_4TH_YEAR_AMOUNT"
        const val MAINTENANCE_5TH_YEAR_AMOUNT: String = "MAINTENANCE_5TH_YEAR_AMOUNT"
        const val TRANSACTION_EDIT_TYPE: String = "TRANSACTION_EDIT_TYPE"
        const val TRANSACTION_EDIT_TYPE_MODIFY: Int = 1

        const val RTGS: String = "RTGS"
        const val NEFT: String = "NEFT"
        const val IMPS: String = "IMPS"
        const val UPI: String = "UPI"
        const val CHEQUE: String = "CHEQUE"
        const val OTHER: String = "OTHER"
        val LOCAL_BROADCAST_INTENT: String? = "LOCAL_BROADCAST_INTENT"
        val INTENT_EXTRA_FILE_PATH: String? = "INTENT_EXTRA_FILE_PATH"

        // only for excelsheet
        const val SENDER_NAME: String = "SENDER_NAME"
        const val RECEIVER_NAME: String = "RECEIVER_NAME"
        const val DEBIT_PAYEE: String = "DEBIT_PAYEE"
        const val CREDIT_PAYEE: String = "CREDIT_PAYEE"

        const val SEARCH_FOR: String = "SEARCH_FOR"
        const val SEARCH_FOR_TRANSACTIONS: Int = 1
        const val SEARCH_FOR_MATERIALS: Int = 2
        const val SEARCH_FOR_GST: Int = 3

    }

}