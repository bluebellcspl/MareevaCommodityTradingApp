package com.example.maarevacommoditytradingapp.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.maarevacommoditytradingapp.constants.Constants

class DatabaseManager() {

    companion object{
        var mOpenCounter = 0
        var instance: DatabaseManager? = null
        lateinit var mDatabaseHelper : DatabaseHelper
        var mDatabase: SQLiteDatabase? = null

        @Synchronized
        fun initializeInstance(context: Context?) {
            if (instance == null) {
                 mDatabaseHelper = DatabaseHelper(context!!)
                instance = DatabaseManager()
            }
        }

        @JvmName("getInstance1")
        fun getInstance(): DatabaseManager {
            if (instance != null) {
                throw IllegalStateException(DatabaseManager::class.java.simpleName + "is not initialized, Call initializeInstance(..) method first")
            }
            return instance!!
        }

        fun openDatabase(): SQLiteDatabase {
            mOpenCounter++
            if (mOpenCounter == 1) {
                mDatabase = mDatabaseHelper.writableDatabase
            }

            return mDatabaseHelper.writableDatabase
        }

        fun closeDatabase() {
            mOpenCounter--
            if (mOpenCounter == 0) {
                mDatabase?.close()
            }
        }

        fun ExecuteScalar(qry: String?): String? {
            Log.d("ExecuteScalar", qry!!)
            mDatabase = openDatabase()
            var str = "invalid"
            val cursor: Cursor = mDatabase!!.rawQuery(qry, null)
            try {
                str = if (cursor.count > 0) {
                    cursor.moveToFirst()
                    cursor.getString(Constants.index)
                } else {
                    "invalid"
                }
            } catch (ex: java.lang.Exception) {
                str = "invalid"
                ex.printStackTrace()
            }
            cursor.close()
            return str
        }

        fun CommonExecute(qry: String) {
            Log.d("CommonExecute", qry)
            mDatabase = openDatabase()
            try {
                mDatabase?.execSQL(qry)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CommonExecute", "" + e.message)
            }
        }

        fun ExecuteQuery(qry: String): Int {
            Log.d("ExecuteQuery", qry)
            mDatabase = openDatabase()
            var data = 0
            try {
                val cursor = mDatabase?.rawQuery(qry, null)
                if (cursor != null && cursor.count > 0) {
                    data = 1
                } else {
                    data = 0
                }
            } catch (e: Exception) {
                data = 0
                e.printStackTrace()
                Log.e("ExecuteQuery", "" + e.message)
            }

            return data
        }

        fun ExecuteRawSql(qry: String): Cursor? {
            Log.d("ExecuteRawSql", qry)
            mDatabase = openDatabase()
            return try {
                val cursor: Cursor? = mDatabase?.rawQuery(qry, null)
                if (cursor != null && cursor.count > 0) {
                    cursor
                } else {
                    null
                }
            } catch (ex: java.lang.Exception) {
                null
            }
        }

        fun commonInsert(list: ContentValues, tableName: String) {
            mDatabase = openDatabase()
            try {
                mDatabase?.insert(tableName, null, list)
                closeDatabase()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("commonInsert", "" + e.message)
            }
        }

        fun deleteData(tableName: String){
            Log.d("deleteData: ",tableName)
            mDatabase = openDatabase()
            try {
                mDatabase?.execSQL("DELETE FROM $tableName")
            }catch (e:Exception)
            {
                e.printStackTrace()
                Log.e("deleteData",""+e.message)
            }
        }
    }
}