package com.datamangement.devast007.bahikhata.utils

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log

class SqlDBFile : ContentProvider() {

    companion object {
        private const val SLASH = "/"
        private const val SLASH_HASH = "/#"
        const val DATABASE = "BAHIHAKTA_DATABASE"

        const val ID = "_id"
        private const val TEXT_COMMA_SPACE = " text, "
        private const val TEXT_UNIQUE_COMMA_SPACE = " text unique, "

        private const val INTEGER_COMMA_SPACE = " integer, "
        private const val INTEGER_AUTO_INCREAMENT = " integer primary key autoincrement, "
        private const val AUTHORITY = "com.datamangement.devast007.bahikhata"


        private const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
        private const val DROP_TABLE = "DROP TABLE IF EXISTS "

        // SUGGESTION --> START
        const val TABLE_SUGGESTION: String = "TABLE_SUGGESTION "
        private const val SUGGESTION = 1
        val CONTENT_URI_TABLE_SUGGESTION: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    TABLE_SUGGESTION
        )
        const val CREATE_TABLE_SUGGESTION =
            CREATE_TABLE + TABLE_SUGGESTION + "(" + ID + INTEGER_AUTO_INCREAMENT + LedgerDefine.PROJECT_ID + TEXT_COMMA_SPACE + LedgerDefine.RECEIVER_ID + TEXT_COMMA_SPACE + LedgerDefine.CREDIT_ACCOUNT_ID + TEXT_COMMA_SPACE + LedgerDefine.REMARK + " text );"
// SUGGESTION --> END

        // PROJECTS --> START

        const val TABLE_PROJECTS = "TABLE_PROJECTS "
        private const val PROJECTS = 2
        val CONTENT_URI_TABLE_PROJECTS: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    TABLE_PROJECTS
        )

        const val CREATE_TABLE_PROJECTS =
            CREATE_TABLE + TABLE_PROJECTS + "(" + ID + INTEGER_AUTO_INCREAMENT + LedgerDefine.PROJECT_ID + TEXT_UNIQUE_COMMA_SPACE +
                    LedgerDefine.NAME + TEXT_COMMA_SPACE + LedgerDefine.ADDRESS + TEXT_COMMA_SPACE + LedgerDefine.DIVISION + TEXT_COMMA_SPACE +
                    LedgerDefine.AMOUNT + TEXT_COMMA_SPACE + LedgerDefine.START_DATE + TEXT_COMMA_SPACE + LedgerDefine.END_DATE + TEXT_COMMA_SPACE +
                    LedgerDefine.REMARK + TEXT_COMMA_SPACE + LedgerDefine.MAIN_AMOUNT + TEXT_COMMA_SPACE + LedgerDefine.MB_NO + TEXT_COMMA_SPACE +
                    LedgerDefine.HEAD + TEXT_COMMA_SPACE + LedgerDefine.MAINTENANCE_1ST_YEAR_AMOUNT + TEXT_COMMA_SPACE +
                    LedgerDefine.MAINTENANCE_2ND_YEAR_AMOUNT + TEXT_COMMA_SPACE + LedgerDefine.MAINTENANCE_3RD_YEAR_AMOUNT + TEXT_COMMA_SPACE +
                    LedgerDefine.MAINTENANCE_4TH_YEAR_AMOUNT + TEXT_COMMA_SPACE +
                    LedgerDefine.SYSTEM_MILLI + INTEGER_COMMA_SPACE + LedgerDefine.MAINTENANCE_5TH_YEAR_AMOUNT + " text " + ");"

        // PROJECTS --> END


        // BANK_ACCOUNTS --> START
        private const val TABLE_BANK_ACCOUNTS = "TABLE_BANK_ACCOUNTS "
        private const val BANK_ACCOUNTS = 4
        val CONTENT_URI_TABLE_BANK_ACCOUNTS: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    TABLE_BANK_ACCOUNTS
        )
        const val CREATE_TABLE_BANK_ACCOUNTS =
            CREATE_TABLE + TABLE_BANK_ACCOUNTS + "(" + ID + INTEGER_AUTO_INCREAMENT + LedgerDefine.BANK_ACCOUNT_ID + TEXT_UNIQUE_COMMA_SPACE +
                    LedgerDefine.PAYEE_NAME + TEXT_COMMA_SPACE + LedgerDefine.BANK_ACCOUNT_NUMBER + TEXT_COMMA_SPACE +
                    LedgerDefine.IFSC_CODE + TEXT_COMMA_SPACE + LedgerDefine.BANK_ACCOUNT_BRANCH_NAME + TEXT_COMMA_SPACE +
                    LedgerDefine.TIME_STAMP + TEXT_COMMA_SPACE + LedgerDefine.AMOUNT + TEXT_COMMA_SPACE +
                    LedgerDefine.SYSTEM_MILLI + INTEGER_COMMA_SPACE + LedgerDefine.REMARK + " text " + ");"
        // BANK_ACCOUNTS --> END


        // USERS --> START
        private const val TABLE_USERS = "TABLE_USERS "
        private const val USERS = 3
        val CONTENT_URI_TABLE_USERS: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    TABLE_USERS
        )
        const val CREATE_TABLE_USERS =
            CREATE_TABLE + TABLE_USERS + "(" + ID + INTEGER_AUTO_INCREAMENT + LedgerDefine.USER_ID + TEXT_UNIQUE_COMMA_SPACE +
                    LedgerDefine.NAME + TEXT_COMMA_SPACE + LedgerDefine.ADDRESS + TEXT_COMMA_SPACE +
                    LedgerDefine.PHONE_NUMBER + TEXT_COMMA_SPACE + LedgerDefine.DESIGNATION + TEXT_COMMA_SPACE +
                    LedgerDefine.TIME_STAMP + TEXT_COMMA_SPACE + LedgerDefine.EMAIL + TEXT_COMMA_SPACE +
                    LedgerDefine.ACCESSIBLE_PROJECTS + TEXT_COMMA_SPACE + LedgerDefine.AMOUNT + TEXT_COMMA_SPACE +
                    LedgerDefine.SYSTEM_MILLI + INTEGER_COMMA_SPACE + LedgerDefine.REMARK + " text );"
        // USERS --> END

        // TRANSACTIONS --> START
        private const val TABLE_TRANSACTIONS = "TABLE_TRANSACTIONS "
        private const val TRANSACTIONS = 5
        val CONTENT_URI_TABLE_TRANSACTIONS: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    TABLE_TRANSACTIONS
        )
        const val CREATE_TABLE_TRANSACTIONS =
            CREATE_TABLE + TABLE_TRANSACTIONS + "(" + ID + INTEGER_AUTO_INCREAMENT + LedgerDefine.TRANSACTION_ID + TEXT_UNIQUE_COMMA_SPACE +
                    LedgerDefine.SENDER_ID + TEXT_COMMA_SPACE + LedgerDefine.SUBCATEGORY + TEXT_COMMA_SPACE + LedgerDefine.RECEIVER_ID + TEXT_COMMA_SPACE +
                    LedgerDefine.AMOUNT + TEXT_COMMA_SPACE + LedgerDefine.PROJECT_ID + TEXT_COMMA_SPACE +
                    LedgerDefine.TRANSACTION_DATE + TEXT_COMMA_SPACE + LedgerDefine.TIME_STAMP + TEXT_COMMA_SPACE +
                    LedgerDefine.TRANSACTION_TYPE + TEXT_COMMA_SPACE + LedgerDefine.LOGGED_IN_ID + TEXT_COMMA_SPACE +
                    LedgerDefine.VERIFIED + TEXT_COMMA_SPACE + LedgerDefine.REMARK + TEXT_COMMA_SPACE + LedgerDefine.IS_TRACKING_ON + INTEGER_COMMA_SPACE +
                    LedgerDefine.DEBIT_ACCOUNT_ID + TEXT_COMMA_SPACE + LedgerDefine.CREDIT_ACCOUNT_ID + TEXT_COMMA_SPACE +
                    LedgerDefine.SYSTEM_MILLI + INTEGER_COMMA_SPACE + LedgerDefine.PAYMENT_MODE + " text );"
        // TRANSACTIONS --> END


        // MATERIALS --> START
        private const val TABLE_MATERIALS = "TABLE_MATERIALS "
        private const val MATERIALS = 6
        val CONTENT_URI_TABLE_MATERIALS: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    TABLE_MATERIALS
        )
        const val CREATE_TABLE_MATERIALS =
            CREATE_TABLE + TABLE_MATERIALS + "(" + ID + INTEGER_AUTO_INCREAMENT + LedgerDefine.MATERIAL_ID + TEXT_UNIQUE_COMMA_SPACE +
                    LedgerDefine.MATERIAL + TEXT_COMMA_SPACE + LedgerDefine.RATE + TEXT_COMMA_SPACE +
                    LedgerDefine.QUANTITY + TEXT_COMMA_SPACE + LedgerDefine.AMOUNT + TEXT_COMMA_SPACE +
                    LedgerDefine.SENDER_ID + TEXT_COMMA_SPACE + LedgerDefine.RECEIVER_ID + TEXT_COMMA_SPACE +
                    LedgerDefine.PROJECT_ID + TEXT_COMMA_SPACE + LedgerDefine.DATE + TEXT_COMMA_SPACE +
                    LedgerDefine.TIME_STAMP + TEXT_COMMA_SPACE + LedgerDefine.LOGGED_IN_ID + TEXT_COMMA_SPACE +
                    LedgerDefine.SYSTEM_MILLI + INTEGER_COMMA_SPACE + LedgerDefine.REMARK + " text );"
        // MATERIALS --> END

        // GST --> START
        private const val TABLE_GST = "TABLE_GST "
        private const val GST = 7
        val CONTENT_URI_TABLE_GST: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    TABLE_GST
        )
        const val CREATE_TABLE_GST =
            CREATE_TABLE + TABLE_GST + "(" + ID + INTEGER_AUTO_INCREAMENT + LedgerDefine.GST_ID + TEXT_UNIQUE_COMMA_SPACE +
                    LedgerDefine.GST_BILL_AMOUNT + TEXT_COMMA_SPACE + LedgerDefine.DATE + TEXT_COMMA_SPACE +
                    LedgerDefine.GST_TAX_AMOUNT + TEXT_COMMA_SPACE + LedgerDefine.GST_TAX_PERCENTAGE + TEXT_COMMA_SPACE +
                    LedgerDefine.LOGGED_IN_ID + TEXT_COMMA_SPACE + LedgerDefine.MATERIAL + TEXT_COMMA_SPACE +
                    LedgerDefine.PROJECT_ID + TEXT_COMMA_SPACE + LedgerDefine.RECEIVER_ID + TEXT_COMMA_SPACE +
                    LedgerDefine.REMARK + TEXT_COMMA_SPACE + LedgerDefine.SENDER_ID + TEXT_COMMA_SPACE +
                    LedgerDefine.SYSTEM_MILLI + INTEGER_COMMA_SPACE + LedgerDefine.TIME_STAMP + " text );"
        // GST --> END


        var mMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            mMatcher.addURI(AUTHORITY, SLASH + TABLE_SUGGESTION, SUGGESTION)
            mMatcher.addURI(AUTHORITY, SLASH + TABLE_SUGGESTION + SLASH_HASH, SUGGESTION)

            mMatcher.addURI(AUTHORITY, SLASH + TABLE_PROJECTS, PROJECTS)
            mMatcher.addURI(AUTHORITY, SLASH + TABLE_PROJECTS + SLASH_HASH, PROJECTS)

            mMatcher.addURI(AUTHORITY, SLASH + TABLE_USERS, USERS)
            mMatcher.addURI(AUTHORITY, SLASH + TABLE_USERS + SLASH_HASH, USERS)

            mMatcher.addURI(AUTHORITY, SLASH + TABLE_BANK_ACCOUNTS, BANK_ACCOUNTS)
            mMatcher.addURI(AUTHORITY, SLASH + TABLE_BANK_ACCOUNTS + SLASH_HASH, BANK_ACCOUNTS)

            mMatcher.addURI(AUTHORITY, SLASH + TABLE_TRANSACTIONS, TRANSACTIONS)
            mMatcher.addURI(AUTHORITY, SLASH + TABLE_TRANSACTIONS + SLASH_HASH, TRANSACTIONS)

            mMatcher.addURI(AUTHORITY, SLASH + TABLE_MATERIALS, MATERIALS)
            mMatcher.addURI(AUTHORITY, SLASH + TABLE_MATERIALS + SLASH_HASH, MATERIALS)

            mMatcher.addURI(AUTHORITY, SLASH + TABLE_GST, GST)
            mMatcher.addURI(AUTHORITY, SLASH + TABLE_GST + SLASH_HASH, GST)


        }
    }

    private var myDB: MyDBHandler? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri {

        val sqlDB = myDB!!.writableDatabase

        val tables = getTable(uri)

        val id = sqlDB.insert(tables, null, values)
        context!!.contentResolver.notifyChange(uri, null)
        return Uri.parse(tables + SLASH + id)
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val queryBuilder = SQLiteQueryBuilder()
        queryBuilder.tables = getTable(uri)


        val cursor = queryBuilder.query(
            myDB?.readableDatabase,
            projection, selection, selectionArgs, null, null,
            sortOrder
        )
        cursor.setNotificationUri(
            context!!.contentResolver,
            uri
        )
        return cursor
    }

    override fun onCreate(): Boolean {
        Log.d(DATABASE, "MyDB onCreate ")
        myDB = MyDBHandler(context, DATABASE, null, 2)
        return true
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val tables = getTable(uri)
        val sqlDB: SQLiteDatabase = myDB!!.writableDatabase
        val rowsUpdated: Int = sqlDB.update(tables, values, selection, selectionArgs)
        context!!.contentResolver.notifyChange(uri, null)
        return rowsUpdated
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val tables = getTable(uri)
        val sqlDB = myDB!!.writableDatabase
        val rowsDeleted: Int = sqlDB.delete(tables, selection, selectionArgs)
        context!!.contentResolver.notifyChange(uri, null)
        return rowsDeleted
    }


    override fun getType(p0: Uri): String? {
        return null
    }

    private fun getTable(uri: Uri): String? {
        when (mMatcher.match(uri)) {
            SUGGESTION -> return TABLE_SUGGESTION
            PROJECTS -> return TABLE_PROJECTS
            USERS -> return TABLE_USERS
            BANK_ACCOUNTS -> return TABLE_BANK_ACCOUNTS
            TRANSACTIONS -> return TABLE_TRANSACTIONS
            MATERIALS -> return TABLE_MATERIALS
            GST -> return TABLE_GST
        }
        return null

    }

    inner class MyDBHandler(
        context: Context?,
        name: String?,
        factory: SQLiteDatabase.CursorFactory?,
        version: Int
    ) :
        SQLiteOpenHelper(context, name, factory, version) {
        override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {
            Log.d(DATABASE, "MyDB handler ")
            sqLiteDatabase!!.execSQL(CREATE_TABLE_SUGGESTION)
            sqLiteDatabase!!.execSQL(CREATE_TABLE_PROJECTS)
            sqLiteDatabase!!.execSQL(CREATE_TABLE_USERS)
            sqLiteDatabase!!.execSQL(CREATE_TABLE_BANK_ACCOUNTS)
            sqLiteDatabase!!.execSQL(CREATE_TABLE_TRANSACTIONS)
            sqLiteDatabase!!.execSQL(CREATE_TABLE_MATERIALS)
            sqLiteDatabase!!.execSQL(CREATE_TABLE_GST)
        }


        override fun onUpgrade(sqLiteDatabase: SQLiteDatabase?, p1: Int, p2: Int) {
            sqLiteDatabase!!.execSQL(DROP_TABLE + TABLE_SUGGESTION)
            sqLiteDatabase!!.execSQL(DROP_TABLE + TABLE_PROJECTS)
            sqLiteDatabase!!.execSQL(DROP_TABLE + TABLE_USERS)
            sqLiteDatabase!!.execSQL(DROP_TABLE + TABLE_BANK_ACCOUNTS)
            sqLiteDatabase!!.execSQL(DROP_TABLE + TABLE_TRANSACTIONS)
            sqLiteDatabase!!.execSQL(DROP_TABLE + TABLE_MATERIALS)
            sqLiteDatabase!!.execSQL(DROP_TABLE + TABLE_GST)

            onCreate(sqLiteDatabase)
        }

    }
}





