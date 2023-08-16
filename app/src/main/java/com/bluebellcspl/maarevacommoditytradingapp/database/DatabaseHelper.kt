package com.bluebellcspl.maarevacommoditytradingapp.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "MAAREVA.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        //Masters
        db?.execSQL("CREATE table RoleMaster (RoleId TEXT primary key,RoleName TEXT,IsActive TEXT,CompanyCode TEXT,IsUser TEXT,CreateUser TEXT,UpdateUser TEXT,CreateDate TEXT,UpdateDate TEXT,activeStatus1 TEXT)")
        db?.execSQL("CREATE table StateMaster (StateId TEXT primary key,StateName TEXT,IsActive TEXT,CompanyCode TEXT,IsUser TEXT,CreateUser TEXT,CreateDate TEXT,UpdateUser TEXT,UpdateDate TEXT)")
        db?.execSQL("CREATE table DistrictMaster (DistrictId TEXT  primary key,DistrictName TEXT,StateId TEXT,StateName TEXT,IsActive TEXT,CompanyCode TEXT,IsUser TEXT,CreateUser TEXT,CreateDate TEXT,UpdateUser TEXT,UpdateDate TEXT)")
        db?.execSQL("CREATE table APMCMaster (APMCId TEXT  primary key,APMCName TEXT,SrNo TEXT,Location TEXT,EDate TEXT,EstablishmentDate TEXT,EstabishmentDetails TEXT,InfrastructureFacility TEXT,NoOfShop TEXT,NoOfTraders TEXT,NoOfBoardMember TEXT,ChairmanName TEXT,WiseChairman TEXT,SecretaryName TEXT,NoOfEmployee TEXT,LaboratoryFacility TEXT,BankFacility TEXT,WarehouseFacility TEXT,WeightbridgeFacility TEXT,StateId TEXT,StateName TEXT,DistrictId TEXT,DistrictName TEXT,IsActive TEXT,CompanyCode TEXT,CreateUser TEXT,CreateDate TEXT,UpdateUser TEXT,UpdateDate TEXT)")
        db?.execSQL("CREATE table CommodityMaster (CommodityId TEXT primary key,CommodityName TEXT,StateId TEXT,StateName TEXT,DistrictId TEXT,DistrictName TEXT,APMCId TEXT,APMCName TEXT,CompanyCode TEXT,IsActive TEXT,CreateUser TEXT,CreateDate TEXT,UpdateUser TEXT,UpdateDate TEXT)")

        db?.execSQL("CREATE table ShopMaster (APMCId TEXT,APMCName TEXT,StateId TEXT,StateName TEXT,DistrictId TEXT,DistrictName TEXT,ShopId TEXT primary key ,ShopNo TEXT,ShopName TEXT,ShopAddress TEXT,CompanyCode TEXT,IsActive TEXT,CreateUser TEXT,CreateDate TEXT,UpdateUser TEXT,UpdateDate TEXT)")

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        this.onCreate(db)
    }
}