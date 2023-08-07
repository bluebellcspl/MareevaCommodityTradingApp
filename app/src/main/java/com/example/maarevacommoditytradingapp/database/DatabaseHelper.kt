package com.example.maarevacommoditytradingapp.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "MAAREVA.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        //Masters

//        db?.execSQL("CREATE table LeaveMaster (LeaveId TEXT primary key,LeavePriority TEXT,LeaveName TEXT)")
//        db?.execSQL("CREATE TABLE UserMaster (CreateUser TEXT,EmployeeId TEXT,EmployeeCode TEXT,EmployeeName TEXT,Password TEXT,CompanyCode TEXT,CenterId TEXT,DesignationType TEXT,DesignationId TEXT,DepartmentId TEXT,BloodGroup TEXT,Age TEXT,MaritalStatus TEXT,IsActive TEXT,UpdateDate TEXT,UpdateUser TEXT,SAPNo TEXT,EmployeeTypeId TEXT,Email TEXT,AppVersion TEXT,BirthCtryCode TEXT,CostCtr TEXT,JoiningTime,TokenId TEXT,Salutation TEXT,FirstName TEXT,MiddleName TEXT,ShortName TEXT,LastName TEXT,BirthPlace TEXT,Language TEXT,BirthCtry TEXT,Nationality TEXT,NoOfChild TEXT,ReligionId TEXT,  Gender TEXT,CastId TEXT,SubCast TEXT,EducationId TEXT,IsLoggedIn TEXT,EmPhone TEXT,DepartmentCode TEXT,DesignationCode TEXT,CenterName TEXT,DepartmentName TEXT,DesignationName TEXT,EmployeeTypeName TEXT,BirthDate TEXT,JoiningDate TEXT,LeavingDate TEXT,chngD TEXT,cdate TEXT,EmpShift TEXT)")
//        db?.execSQL("CREATE TABLE EmployeeMaster (CreateUser TEXT,EmployeeId TEXT,EmployeeCode TEXT,EmployeeName TEXT,Password TEXT,CompanyCode TEXT,CenterId TEXT,DesignationType TEXT,DesignationId TEXT,DepartmentId TEXT,BloodGroup TEXT,Age TEXT,MaritalStatus TEXT,IsActive TEXT,UpdateDate TEXT,UpdateUser TEXT,SAPNo TEXT,EmployeeTypeId TEXT,Email TEXT,AppVersion TEXT,BirthCtryCode TEXT,CostCtr TEXT,JoiningTime,TokenId TEXT,Salutation TEXT,FirstName TEXT,MiddleName TEXT,ShortName TEXT,LastName TEXT,BirthPlace TEXT,Language TEXT,BirthCtry TEXT,Nationality TEXT,NoOfChild TEXT,ReligionId TEXT,  Gender TEXT,CastId TEXT,SubCast TEXT,EducationId TEXT,IsLoggedIn TEXT,EmPhone TEXT,DepartmentCode TEXT,DesignationCode TEXT,CenterName TEXT,DepartmentName TEXT,DesignationName TEXT,EmployeeTypeName TEXT,BirthDate TEXT,JoiningDate TEXT,LeavingDate TEXT,chngD TEXT,cdate TEXT,EmpShift TEXT)")

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        this.onCreate(db)
    }
}