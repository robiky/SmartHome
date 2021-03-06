package io.particle.android.sdk.cloudDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.particle.android.sdk.Device;


public class DBhelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "deviceManager1";
    private static final String TABLE_DEVICE = "devices";

    /**
     * Columns of Database
     * - String deviceName, deviceState
     * - int type, roomNum
     */
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";
    private static final String KEY_ROOMNUM = "room";
    private static final String KEY_STATE = "state";
    private static final String KEY_DETAIL_STATE= "detailState";
    private static final String KEY_CUSTOM_ROOM1 = "customRoom1";
    private static final String KEY_CUSTOM_ROOM2 = "customRoom2";

    public DBhelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DEVICES_TABLE = "CREATE TABLE " + TABLE_DEVICE + "("
                + KEY_ID + " INTEGER, " + KEY_NAME + " TEXT PRIMARY KEY,"
                + KEY_ROOMNUM + " INTEGER, " + KEY_TYPE + " INTEGER,"
                + KEY_STATE + " TEXT, " + KEY_DETAIL_STATE + " TEXT,"
                + KEY_CUSTOM_ROOM1 + " INTEGER, " + KEY_CUSTOM_ROOM2 + " INTEGER" + ")";
        db.execSQL(CREATE_DEVICES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICE);
        onCreate(db);
    }

    /**
     * CURD Functions
     */
    public void addDevice(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, device.getDeviceId());
        values.put(KEY_NAME, device.getDeviceName());
        values.put(KEY_ROOMNUM, device.getDeviceRoomNum());
        values.put(KEY_TYPE, device.getDeviceType());
        values.put(KEY_STATE, device.getDeviceState());
        values.put(KEY_DETAIL_STATE, device.getDeviceDetailState());
        values.put(KEY_CUSTOM_ROOM1, device.getDeviceCustomRoomNum1());
        values.put(KEY_CUSTOM_ROOM2, device.getDeviceCustomRoomNum2());

        db.insert(TABLE_DEVICE, null, values);
        db.close();
    }

    public Device getDevice(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DEVICE, new String[] {
                        KEY_ID, KEY_NAME, KEY_ROOMNUM, KEY_TYPE, KEY_DETAIL_STATE, KEY_CUSTOM_ROOM1, KEY_CUSTOM_ROOM2}, KEY_NAME + "=?",
                new String[] {name},null,null,null,null);
        if(cursor != null)
            cursor.moveToFirst();

        Device device = new Device();
        device.setDeviceId(Integer.parseInt(cursor.getString(0)));
        device.setDeviceName(cursor.getString(1));
        device.setDeviceRoomNum(Integer.parseInt(cursor.getString(2)));
        device.setDeviceType(Integer.parseInt(cursor.getString(3)));
        device.setDeviceState(cursor.getString(4));
        device.setDeviceState(cursor.getString(5));
        device.setDeviceCustomRoomNum1(Integer.parseInt(cursor.getString(6)));
        device.setDeviceCustomRoomNum2(Integer.parseInt(cursor.getString(7)));

        return device;
    }

    public ArrayList<Device>[] getAllDevices() {
        ArrayList<Device>[] deviceList = new ArrayList[6];
        for(int i=0; i<6; i++){
            deviceList[i] = new ArrayList<Device>();
        }

        String selectQuery = "SELECT * FROM "+TABLE_DEVICE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()) {
            do{
                Device device = new Device();
                device.setDeviceId(Integer.parseInt(cursor.getString(0)));
                device.setDeviceName(cursor.getString(1));
                device.setDeviceRoomNum(Integer.parseInt(cursor.getString(2)));
                device.setDeviceType(Integer.parseInt(cursor.getString(3)));
                device.setDeviceState(cursor.getString(4));
                device.setDeviceDetailState(cursor.getString(5));
                device.setDeviceCustomRoomNum1(Integer.parseInt(cursor.getString(6)));
                device.setDeviceCustomRoomNum2(Integer.parseInt(cursor.getString(7)));

                // roomNum에 맞는 device를 deviceList에 넣어줌
                deviceList[Integer.parseInt(cursor.getString(2))].add(device);

                // For customRoom
                if(Integer.parseInt(cursor.getString(6)) == 4) {
                    deviceList[4].add(device);
                }
                if(Integer.parseInt(cursor.getString(7)) == 5) {
                    deviceList[5].add(device);
                }
            } while(cursor.moveToNext());
        }
        return deviceList;
    }

    public int updateDevice(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, device.getDeviceName());
        values.put(KEY_ROOMNUM, device.getDeviceRoomNum());
        values.put(KEY_TYPE, device.getDeviceType());
        values.put(KEY_STATE, device.getDeviceState());
        values.put(KEY_DETAIL_STATE, device.getDeviceDetailState());
        values.put(KEY_CUSTOM_ROOM1, device.getDeviceCustomRoomNum1());
        values.put(KEY_CUSTOM_ROOM2, device.getDeviceCustomRoomNum2());

        return db.update(TABLE_DEVICE, values, KEY_NAME + " = ?",
                new String[] {String.valueOf(device.getDeviceName())});
    }

    public void deleteDevice(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DEVICE, KEY_NAME + " = ? ",
                new String[] {String.valueOf(device.getDeviceName())});
        db.close();
    }

    public int getDevicesCount() {
        String countQuery = "SELECT * FROM " + TABLE_DEVICE;
        int cnt=0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cnt = cursor.getCount();
        cursor.close();

        return cnt;
    }

    public void printAllDevices() {
        String selectQuery = "SELECT * FROM "+TABLE_DEVICE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()) {
            do{
                Log.e("smart db", cursor.getString(0)+", "+
                        cursor.getString(1)+", "+cursor.getString(2)+", "+
                        cursor.getString(3)+", "+cursor.getString(4)+", "+
                        cursor.getString(5) + ", " + cursor.getString(6)+", " +
                        cursor.getString(7));
            } while(cursor.moveToNext());
        }
        return;
    }
}