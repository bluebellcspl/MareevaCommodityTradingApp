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
        db?.execSQL("CREATE table APMCMaster (APMCId TEXT,APMCName TEXT,SrNo TEXT,Location TEXT,MarketCess TEXT,LabourCharges TEXT,TranportationCharges TEXT,NoOfShop TEXT,StateId TEXT,StateName TEXT,DistrictId TEXT,DistrictName TEXT,CompanyCode TEXT,IsActive TEXT,CreateUser TEXT,CreateDate TEXT,UpdateDate TEXT,UpdateUser TEXT)")
        db?.execSQL("CREATE table CommodityMaster (CommodityId TEXT primary key,CommodityName TEXT,Bharti TEXT,CompanyCode TEXT,IsActive TEXT,CreateUser TEXT,CreateDate TEXT,UpdateUser TEXT,UpdateDate TEXT)")

        db?.execSQL("CREATE table ShopMaster (APMCId TEXT,APMCName TEXT,StateId TEXT,StateName TEXT,DistrictId TEXT,DistrictName TEXT,ShopId TEXT primary key ,ShopNo TEXT,ShopName TEXT,ShopAddress TEXT,CompanyCode TEXT,IsActive TEXT,CreateUser TEXT,CreateDate TEXT,UpdateUser TEXT,UpdateDate TEXT)")
        db?.execSQL("CREATE table PCAMaster (PCAId TEXT,StateId TEXT,StateName TEXT,DistrictId TEXT,DistrictName TEXT,APMCId TEXT,APMCName TEXT,CityId TEXT,CityName TEXT,CommodityId TEXT,CommodityName TEXT,PCAName TEXT,PCARegId TEXT,PCAPhoneNumber TEXT,Mobile2 TEXT,Address TEXT,EmailId TEXT,BuyerId TEXT,RoleId TEXT,RoleName TEXT,AdharNo TEXT,PanCardNo TEXT,GSTNo TEXT,AdharPhoto TEXT,PanCardPhoto TEXT,GSTCertiPhoto TEXT,LicenseCopyPhoto TEXT,ProfilePic TEXT,ApprStatus TEXT,GCACommission TEXT,PCACommission TEXT,MarketCess TEXT,LabourCharges TEXT,IsActive TEXT,CompanyCode TEXT,CreateUser TEXT,CreateDate TEXT,UpdateUser TEXT,UpdateDate TEXT)")
        db?.execSQL("CREATE table CityMaster (CityId TEXT,CityName TEXT,StateId TEXT,StateName TEXT,DistrictId TEXT,DistrictName TEXT,CompanyCode TEXT,IsActive TEXT,CreateUser TEXT,CreateDate TEXT,UpdateUser TEXT,UpdateDate TEXT)")
        db?.execSQL("CREATE table TransportationMaster (TransportId TEXT,City1 TEXT,CityName TEXT,City2 TEXT,CityName2 TEXT,PerBoriRate TEXT,CompanyCode TEXT,IsActive TEXT,CreateUser TEXT,Cdate TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        this.onCreate(db)
    }


}