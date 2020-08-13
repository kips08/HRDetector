package com.kips.hrdetector.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kips.hrdetector.model.AvgHR;
import com.kips.hrdetector.model.DetailHR;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class DatabaseHelper extends SQLiteOpenHelper {


    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "HeartDetector";

    // Table Names
    private static final String TABLE_AVGHR = "avghr";
    private static final String TABLE_DETAILHR = "detailhr";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // NOTES Table - column nmaes
    private static final String KEY_AVGBPM = "avgbpm";
    private static final String KEY_STRESSLV = "stresslv";

    // TAGS Table - column names
    private static final String KEY_BPM = "bpm";
    private static final String KEY_ID_AVGHR = "id_avghr";


    // Table Create Statements
    // Todo table create statement
    private static final String CREATE_TABLE_AVGHR = "CREATE TABLE "
            + TABLE_AVGHR + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_AVGBPM
            + " INTEGER," + KEY_STRESSLV + " TEXT," + KEY_CREATED_AT
            + " DATETIME" + ")";

    // Tag table create statement
    private static final String CREATE_TABLE_DETAILHR = "CREATE TABLE " + TABLE_DETAILHR
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_BPM + " INTEGER,"
            + KEY_ID_AVGHR + " INTEGER" + ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_AVGHR);
        db.execSQL(CREATE_TABLE_DETAILHR);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETAILHR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AVGHR);

        // create new tables
        onCreate(db);
    }

    //Simpan Data
    public void saveAvg(AvgHR avghr){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(KEY_AVGBPM, avghr.getAvgbpm());
        values.put(KEY_STRESSLV, avghr.getStresslv());
        values.put(KEY_CREATED_AT, currentDateandTime);
        db.insert(TABLE_AVGHR, null, values);
        db.close();
    }

    //Simpan detail BPM
    public void saveDetail(DetailHR detailhr){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(KEY_BPM, detailhr.getBpm());
        values.put(KEY_ID_AVGHR, detailhr.getId_AvgHR());
        db.insert(TABLE_DETAILHR, null, values);
        db.close();
    }

    //Cari semua data
    public List<AvgHR> findAll(){
        List<AvgHR> listAvg=new ArrayList<AvgHR>();
        String query="SELECT * FROM "+TABLE_AVGHR;

        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            do{
                AvgHR Avghr=new AvgHR();
                Avghr.setId(Integer.valueOf(cursor.getString(0)));
                Avghr.setAvgbpm(cursor.getInt(1));
                Avghr.setStresslv(cursor.getString(2));
                Avghr.setCreated_at(cursor.getString(3));
                listAvg.add(Avghr);
            }while(cursor.moveToNext());
        }

        return listAvg;
    }

    //Data satuan
    public AvgHR findOne(int id){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.query(TABLE_AVGHR, new String[]{KEY_ID,KEY_AVGBPM,KEY_STRESSLV, KEY_CREATED_AT},
                KEY_ID+"=?", new String[]{String.valueOf(id)}, null, null, null);

        if(cursor!=null){
            cursor.moveToFirst();
        }


        return new AvgHR(Integer.parseInt(cursor.getString(0)),cursor.getInt(1),cursor.getString(2), cursor.getString(2));
    }

}

