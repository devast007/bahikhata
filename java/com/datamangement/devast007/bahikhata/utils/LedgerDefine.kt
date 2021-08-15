package com.datamangement.devast007.bahikhata.utils

class LedgerDefine {
    companion object {

        val MODIFIER_LOGIN_ID: String ="MODIFIER_LOGIN_ID" // this is for history tracking
        val MODIFIED_DATE: String ="MODIFIED_DATE" // this is for history tracking
        val HISTORY: String="HISTORY"
        val LAUNCHED_FROM: String? ="LAUNCHED_FROM"
        val LAUNCHED_FROM_VIEW_LIST_ACTIVITY: Int= 1
        val TRANSACTION_SENT_OR_RECEIVED ="TRANSACTION_SENT_OR_RECEIVED"
        val SENT_TRANSACTION: Int = 1
        val RECEIVED_TRANSACTIONS: Int = 2
        val RELATED_TRANSACTIONS_IDS: String="relatedTransactionsIDs"
        val IS_FOR_PAYMENT: String?="IS_FOR_PAYMENT"
        val BANK_ACCOUNT: String="BANK ACCOUNT"
        val PAID_TO: String="PAID TO"
        val PAID_BY: String="PAID BY"
        val BALANCE: String?="BALANCE"
        val PAYMENT: String="PAYMENT"
        val REPORTER: String?="REPORTER"
        val PROJECT: String?="PROJECT"
        val SUPPLIER_NAME: String?="SUPPLIER"
        val VEHICLE_NO: String= "vehicleNo"
        val CHALLAN_NO: String= "challanNo"
        val TYPE_UNIT: Int=1
        val TYPE_MEDIUM: Int=2
        val MATERIAL_OR_SERVICE_ID: String = "materialOrServiceId"
        val MATERIAL_OR_SERVICE_NAME: String = "materialOrServiceName"
        val SERVICE_TYPE: String = "serviceType"
        val SERVICE_MEDIUM: String = "serviceMedium"
        const val REQUEST_CODE_BANK_ACCOUNT_ADDED: Int = 11
        val SET_DATA: Int = 0
        val UPDATE_DATA: Int = 1
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
        const val IS_USER_DISABLE: String = "isUserDisable"
        const val IS_HAS_LIMITED_ACCESS = "isHasLimitedAccess"
        const val IS_HAS_EDIT_PERMISSION = "isHasEditPermission"
        const val NAME = "name"
        const val NICKNAME = "nickname"
        const val IS_ADMIN = "isAdmin"
        const val DESIGNATION = "designation"
        const val AMOUNT = "amount"
        const val ROUND_OFF = "roundOff"
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
        const val UNIT = "unit"
        const val QUANTITY = "quantity"
        const val MATERIAL = "material"
        const val DATE: String = "date"

        // GST
        const val GST_ID = "gstID"
        const val GST_BILL_AMOUNT = "billAmount"
        const val GST_TAX_AMOUNT = "gstAmount"
        const val GST_TAX_PERCENTAGE = "gstPercentage"

        const val IMAGE_LINK = "imageLink"
        const val REMARK = "remark"
        const val SUBCATEGORY = "subCategory"

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
        const val SELECTION_TYPE_SUBCATEGORY = 5
        const val SELECTION_TYPE_REMARKS = 6
        val SELECTION_TYPE_VEHICLE_NO: Int= 7
        const val LOGGED_IN_ID: String = "loggedInID"
        const val SENDER_ID: String = "senderID"
        const val RECEIVER_ID: String = "receiverID"
        const val TRANSACTION_DATE: String = "transactionDate"
        const val TRANSACTION_TYPE: String = "transactionType"
        const val SYSTEM_MILLI: String = "systemMilli"
        const val IS_TRACKING_ON: String = "isTrackingOn"



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
        const val TRANSACTION_VIEW_TYPE_TRACKING: Int = 5
        const val TRANSACTION_VIEW_TYPE_PENDING: Int = 6

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
        const val SLASH_MATERIAL_AND_SERVICES_SETTINGS = "/materialAndServicesSettings"
        const val SLASH_UNITS = "/units"
        const val SLASH_MEDIUM = "/medium"
        const val SLASH_SERVICE_TYPES = "/serviceTypes"
        const val SLASH_MATERIALS_OR_SERVICES = "/materialsOrServices"
        const val SLASH_USERS = "/users"
        const val SLASH_PROJECTS = "/projects"
        const val SLASH_GST = "/gst"
        const val SLASH_SERVERTIME = "/serverTime"

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
        val GOOGLE_DRIVE_LINK: String = "GOOGLE_DRIVE_LINK"

        const val RTGS: String = "RTGS"
        const val NEFT: String = "NEFT"
        const val IMPS: String = "IMPS"
        const val UPI: String = "UPI"
        const val CHEQUE: String = "CHEQUE"
        const val OTHER: String = "OTHER"
        const val ONLINE: String = "ONLINE"
        const val CASH: String = "CASH"

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