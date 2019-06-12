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
        private const val TEXT = " text, "
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
            CREATE_TABLE + TABLE_SUGGESTION + "(" + ID + INTEGER_AUTO_INCREAMENT + LedgerDefine.PROJECT_ID + TEXT + LedgerDefine.RECEIVER_ID + TEXT + LedgerDefine.CREDIT_ACCOUNT_ID + TEXT + LedgerDefine.REMARK + " text );"
// SUGGESTION --> END

        // PROJECTS --> START

        const val TABLE_PROJECTS = "TABLE_PROJECTS "
        private const val PROJECTS = 2
        val CONTENT_URI_TABLE_PROJECTS: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    TABLE_PROJECTS
        )

        //TODO ADD TIMESTAMP
        const val CREATE_TABLE_PROJECTS =
            CREATE_TABLE + TABLE_PROJECTS + "(" + ID + INTEGER_AUTO_INCREAMENT + LedgerDefine.PROJECT_ID + TEXT + LedgerDefine.NAME + TEXT + LedgerDefine.ADDRESS + TEXT + LedgerDefine.DIVISION + TEXT + LedgerDefine.AMOUNT + TEXT + LedgerDefine.START_DATE + TEXT + LedgerDefine.END_DATE + TEXT + LedgerDefine.REMARK + TEXT + LedgerDefine.MAIN_AMOUNT + TEXT + LedgerDefine.MB_NO + TEXT + LedgerDefine.HEAD + TEXT + LedgerDefine.MAINTENANCE_1ST_YEAR_AMOUNT + TEXT + LedgerDefine.MAINTENANCE_2ND_YEAR_AMOUNT + TEXT + LedgerDefine.MAINTENANCE_3RD_YEAR_AMOUNT + TEXT + LedgerDefine.MAINTENANCE_4TH_YEAR_AMOUNT + TEXT + LedgerDefine.MAINTENANCE_5TH_YEAR_AMOUNT + " text );"

        // PROJECTS --> END

        // USERS --> START
        private const val TABLE_USERS = "TABLE_USERS "
        private const val USERS = 3
        val CONTENT_URI_TABLE_USERS: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    TABLE_USERS
        )
        const val CREATE_TABLE_USERS =
            CREATE_TABLE + TABLE_USERS + "(" + ID + INTEGER_AUTO_INCREAMENT + LedgerDefine.USER_ID + TEXT + LedgerDefine.NAME + TEXT + LedgerDefine.ADDRESS + TEXT + LedgerDefine.PHONE_NUMBER + TEXT + LedgerDefine.DESIGNATION + TEXT + LedgerDefine.TIME_STAMP + TEXT + LedgerDefine.EMAIL + TEXT + LedgerDefine.ACCESSIBLE_PROJECTS + TEXT + LedgerDefine.AMOUNT + TEXT + LedgerDefine.REMARK + " text );"
        // USERS --> END

        // BANK_ACCOUNTS --> START
        private const val TABLE_BANK_ACCOUNTS = "TABLE_BANK_ACCOUNTS "
        private const val BANK_ACCOUNTS = 4
        val CONTENT_URI_TABLE_BANK_ACCOUNTS: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    TABLE_BANK_ACCOUNTS
        )
        const val CREATE_TABLE_BANK_ACCOUNTS =
            CREATE_TABLE + TABLE_BANK_ACCOUNTS + "(" + ID + INTEGER_AUTO_INCREAMENT + LedgerDefine.BANK_ACCOUNT_ID + TEXT + LedgerDefine.PAYEE_NAME + TEXT + LedgerDefine.BANK_ACCOUNT_NUMBER + TEXT + LedgerDefine.IFSC_CODE + TEXT + LedgerDefine.BANK_ACCOUNT_BRANCH_NAME + TEXT + LedgerDefine.TIME_STAMP + TEXT + LedgerDefine.AMOUNT + TEXT + LedgerDefine.REMARK + " text " + ");"
        // BANK_ACCOUNTS --> END


        // TRANSACTIONS --> START
        private const val TABLE_TRANSACTIONS = "TABLE_TRANSACTIONS "
        private const val TRANSACTIONS = 5
        val CONTENT_URI_TABLE_TRANSACTIONS: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    TABLE_TRANSACTIONS
        )
        const val CREATE_TABLE_TRANSACTIONS =
            CREATE_TABLE + TABLE_TRANSACTIONS + "(" + ID + INTEGER_AUTO_INCREAMENT + LedgerDefine.TRANSACTION_ID + TEXT + LedgerDefine.SENDER_ID + TEXT + LedgerDefine.RECEIVER_ID + TEXT + LedgerDefine.AMOUNT + TEXT + LedgerDefine.PROJECT_ID + TEXT + LedgerDefine.TRANSACTION_DATE + TEXT + LedgerDefine.TIME_STAMP + TEXT + LedgerDefine.TRANSACTION_TYPE + TEXT + LedgerDefine.LOGGED_IN_ID + TEXT + LedgerDefine.VERIFIED + TEXT + LedgerDefine.REMARK + TEXT + LedgerDefine.DEBIT_ACCOUNT_ID + TEXT + LedgerDefine.CREDIT_ACCOUNT_ID + TEXT + LedgerDefine.PAYMENT_MODE + " text );"
        // TRANSACTIONS --> END


        // MATERIALS --> START
        private const val TABLE_MATERIALS = "TABLE_MATERIALS "
        private const val MATERIALS = 6
        val CONTENT_URI_TABLE_MATERIALS: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    TABLE_MATERIALS
        )
        const val CREATE_TABLE_MATERIALS =
            CREATE_TABLE + TABLE_MATERIALS + "(" + ID + INTEGER_AUTO_INCREAMENT + LedgerDefine.MATERIAL_ID + TEXT + LedgerDefine.MATERIAL + TEXT + LedgerDefine.RATE + TEXT + LedgerDefine.QUANTITY + TEXT + LedgerDefine.AMOUNT + TEXT + LedgerDefine.SENDER_ID + TEXT + LedgerDefine.RECEIVER_ID + TEXT + LedgerDefine.PROJECT_ID + TEXT + LedgerDefine.DATE + TEXT + LedgerDefine.TIME_STAMP + TEXT + LedgerDefine.LOGGED_IN_ID + TEXT + LedgerDefine.REMARK + " text );"
        // MATERIALS --> END


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


        }
    }

    private var myDB: MyDBHandler? = null

    override fun insert(uri: Uri?, values: ContentValues?): Uri {

        val sqlDB = myDB!!.writableDatabase

        val tables = getTable(uri)

        val id = sqlDB.insert(tables, null, values)
        context.contentResolver.notifyChange(uri, null)
        return Uri.parse(tables + SLASH + id)
    }

    override fun query(
        uri: Uri?,
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
            context.contentResolver,
            uri
        )
        return cursor
    }

    override fun onCreate(): Boolean {
        Log.d(DATABASE, "MyDB onCreate ")
        myDB = MyDBHandler(context, DATABASE, null, 2)
        return true
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val tables = getTable(uri)
        val sqlDB: SQLiteDatabase = myDB!!.writableDatabase
        val rowsUpdated: Int = sqlDB.update(tables, values, selection, selectionArgs)
        context.contentResolver.notifyChange(uri, null)
        return rowsUpdated
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        val tables = getTable(uri)
        val sqlDB = myDB!!.writableDatabase
        val rowsDeleted: Int = sqlDB.delete(tables, selection, selectionArgs)
        context.contentResolver.notifyChange(uri, null)
        return rowsDeleted
    }

    override fun getType(p0: Uri?): String? {
        return null
    }

    private fun getTable(uri: Uri?): String? {
        when (mMatcher.match(uri)) {
            SUGGESTION -> return TABLE_SUGGESTION
            PROJECTS -> return TABLE_PROJECTS
            USERS -> return TABLE_USERS
            BANK_ACCOUNTS -> return TABLE_BANK_ACCOUNTS
            TRANSACTIONS -> return TABLE_TRANSACTIONS
            MATERIALS -> return TABLE_MATERIALS
        }
        return null

    }

    inner class MyDBHandler(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) :
        SQLiteOpenHelper(context, name, factory, version) {
        override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {
            Log.d(DATABASE, "MyDB handler ")
            sqLiteDatabase!!.execSQL(CREATE_TABLE_SUGGESTION)
            sqLiteDatabase!!.execSQL(CREATE_TABLE_PROJECTS)
            sqLiteDatabase!!.execSQL(CREATE_TABLE_USERS)
            sqLiteDatabase!!.execSQL(CREATE_TABLE_BANK_ACCOUNTS)
            sqLiteDatabase!!.execSQL(CREATE_TABLE_TRANSACTIONS)
            sqLiteDatabase!!.execSQL(CREATE_TABLE_MATERIALS)
        }


        override fun onUpgrade(sqLiteDatabase: SQLiteDatabase?, p1: Int, p2: Int) {
            sqLiteDatabase!!.execSQL(DROP_TABLE + TABLE_SUGGESTION)
            sqLiteDatabase!!.execSQL(DROP_TABLE + TABLE_PROJECTS)
            sqLiteDatabase!!.execSQL(DROP_TABLE + TABLE_USERS)
            sqLiteDatabase!!.execSQL(DROP_TABLE + TABLE_BANK_ACCOUNTS)
            sqLiteDatabase!!.execSQL(DROP_TABLE + TABLE_TRANSACTIONS)
            sqLiteDatabase!!.execSQL(DROP_TABLE + TABLE_MATERIALS)

            onCreate(sqLiteDatabase)
        }

    }
}





