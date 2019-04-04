package com.datamangement.devast007.bahikhata.utils

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri

class SqlDBFile : ContentProvider() {

    companion object {
        val DATABASE = "BAHIHAKTA_DATABASE"
        val TABLE_BAHIKHATA = "TABLE_BAHIKHATA"
        val ID = "_id"
        val PROJECT_ID = "PROJECT_ID"
        val RECEIVER_ID = "RECEIVER_ID"
        val CREDIT_ACCOUNT = "CREDIT_ACCOUNT"
        val REMARKS = "REMARKS"
        val TEXT = " text, "
        val AUTHORITY = "com.datamangement.devast007.bahikhata"
        val CONTENT_URI: Uri = Uri.parse(
            "content://" + AUTHORITY + "/" +
                    TABLE_BAHIKHATA
        )


    }

    val CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS "
            + TABLE_BAHIKHATA + "(" + ID + " integer primary key autoincrement,"
            + PROJECT_ID + TEXT + RECEIVER_ID + TEXT + CREDIT_ACCOUNT + TEXT + REMARKS + " text " + ");")


    private var myDB: MyDBHandler? = null

    override fun insert(uri: Uri?, values: ContentValues?): Uri {

        val sqlDB = myDB!!.writableDatabase

        val id = sqlDB.insert(TABLE_BAHIKHATA, null, values)
        context.contentResolver.notifyChange(uri, null)
        return Uri.parse(TABLE_BAHIKHATA + "/" + id)
    }

    override fun query(
        uri: Uri?,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val queryBuilder = SQLiteQueryBuilder()
        queryBuilder.tables = TABLE_BAHIKHATA


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
        myDB = MyDBHandler(context, DATABASE, null, 1)
        return false
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val sqlDB: SQLiteDatabase = myDB!!.writableDatabase
        val rowsUpdated: Int = sqlDB.update(TABLE_BAHIKHATA, values, selection, selectionArgs)
        context.contentResolver.notifyChange(uri, null)
        return rowsUpdated
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        val sqlDB = myDB!!.writableDatabase
        val rowsDeleted: Int = sqlDB.delete(TABLE_BAHIKHATA, selection, selectionArgs)
        context.contentResolver.notifyChange(uri, null)
        return rowsDeleted
    }

    override fun getType(p0: Uri?): String? {
        return null
    }

    inner class MyDBHandler(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) :
        SQLiteOpenHelper(context, name, factory, version) {
        override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {

            sqLiteDatabase!!.execSQL(CREATE_TABLE)
        }


        override fun onUpgrade(sqLiteDatabase: SQLiteDatabase?, p1: Int, p2: Int) {
            sqLiteDatabase!!.execSQL("DROP TABLE IF EXISTS " + TABLE_BAHIKHATA)
            onCreate(sqLiteDatabase)
        }

    }
}



