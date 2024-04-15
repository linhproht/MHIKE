package com.example.mhike;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "M_HIKE_DB";
    public static final int DATABASE_VERSION = 2;
    public static final String TABLE_NAME1 = "hike_plan";
    public static final String COLUMN_HP_ID = "_hp_id";
    public static final String COLUMN_HP_NAME = "hp_name";
    public static final String COLUMN_HP_LOCATION = "hp_location";
    public static final String COLUMN_HP_DOTH = "hp_DOTH";
    public static final String COLUMN_HP_Packing_Available = "hp_Packing_Available";
    public static final String COLUMN_HP_Hike_Length = "hp_Hike_Length";
    public static final String COLUMN_HP_LOD = "hp_LOD";
    public static final String COLUMN_HP_Des = "hp_Des";
    public static final String COLUMN_HP_Intend_Time = "hp_Intend_Time";
    public static final String COLUMN_HP_License = "hp_License";

    public static final String TABLE_NAME2 = "observation";
    public static final String COLUMN_OB_ID = "_ob_id";

    public static final String COLUMN_OB_HP_ID = "ob_hp_id";
    public static final String COLUMN_OB_ANIMAL_SIGN = "ob_animal_sign";
    public static final String COLUMN_OB_TYPE_OF_VEGETATIONS = "ob_type_of_vegetations";
    public static final String COLUMN_OB_TRAILS_CONDITIONS = "ob_trails_conditions";
    public static final String COLUMN_OB_WEATHER_CONDITIONS = "ob_weather_conditions";
    public static final String COLUMN_OB_LAKE_LOCATION = "ob_lake_location";
    public static final String COLUMN_OB_TIME_OF_OBSERVATIONS = "ob_time_of_observations";
    public static final String COLUMN_OB_COMMENT = "ob_comment";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME1 + " (" +
                COLUMN_HP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HP_NAME + " TEXT, " +
                COLUMN_HP_LOCATION + " TEXT, " +
                COLUMN_HP_DOTH + " TEXT, " +
                COLUMN_HP_Packing_Available + " TEXT, " +
                COLUMN_HP_Hike_Length + " TEXT, " +
                COLUMN_HP_LOD + " TEXT, " +
                COLUMN_HP_Des + " TEXT, " +
                COLUMN_HP_Intend_Time + " TEXT, " +
                COLUMN_HP_License + " TEXT)";
        db.execSQL(createTableQuery);

        String createTableQuery2 = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME2 + " (" +
                COLUMN_OB_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_OB_ANIMAL_SIGN + " TEXT, " +
                COLUMN_OB_TYPE_OF_VEGETATIONS + " TEXT, " +
                COLUMN_OB_TRAILS_CONDITIONS + " TEXT, " +
                COLUMN_OB_WEATHER_CONDITIONS + " TEXT, " +
                COLUMN_OB_LAKE_LOCATION + " TEXT, " +
                COLUMN_OB_TIME_OF_OBSERVATIONS + " TEXT, " +
                COLUMN_OB_COMMENT + " TEXT, " +
                COLUMN_OB_HP_ID + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_OB_HP_ID + ") REFERENCES " + TABLE_NAME1 + "(" + COLUMN_HP_ID + ")" + ")";
        db.execSQL(createTableQuery2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertDataObservation(long hikePlanId, String animalSign, String typeOfVegetations, String timeOfObservation, String trailsConditions, String weatherCondtions, String lakeLocation, String addtionalComments) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_OB_HP_ID, hikePlanId);
        values.put(COLUMN_OB_ANIMAL_SIGN, animalSign);
        values.put(COLUMN_OB_TYPE_OF_VEGETATIONS, typeOfVegetations);
        values.put(COLUMN_OB_TIME_OF_OBSERVATIONS, timeOfObservation);
        values.put(COLUMN_OB_TRAILS_CONDITIONS, trailsConditions);
        values.put(COLUMN_OB_WEATHER_CONDITIONS, weatherCondtions);
        values.put(COLUMN_OB_LAKE_LOCATION, lakeLocation);
        values.put(COLUMN_OB_COMMENT, addtionalComments);

        try {
            long newRowId = db.insert(TABLE_NAME2, null, values);
            db.close();
            return newRowId;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    public long insertData(String name, String location, String doth, String packingAvailable, String hikeLength, String lod, String description, String intendTime, String license) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HP_NAME, name);
        values.put(COLUMN_HP_LOCATION, location);
        values.put(COLUMN_HP_DOTH, doth);
        values.put(COLUMN_HP_Packing_Available, packingAvailable);
        values.put(COLUMN_HP_Hike_Length, hikeLength);
        values.put(COLUMN_HP_LOD, lod);
        values.put(COLUMN_HP_Des, description);
        values.put(COLUMN_HP_Intend_Time, intendTime);
        values.put(COLUMN_HP_License, license);

        try {
            long newRowId = db.insert(TABLE_NAME1, null, values);
            db.close();
            return newRowId;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Cursor getCursor() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                COLUMN_HP_ID,
                COLUMN_HP_NAME
        };

        return db.query(TABLE_NAME1, projection, null, null, null, null, null);
    }



    public boolean isTableHKEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME1, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count == 0;
        } else {
            return true;
        }
    }

    public boolean isTableOBEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME2, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count == 0;
        } else {
            return true;
        }
    }

    public Cursor getHikePlanById(long hikePlanId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                COLUMN_HP_ID,
                COLUMN_HP_NAME,
                COLUMN_HP_LOCATION,
                COLUMN_HP_DOTH,
                COLUMN_HP_Packing_Available,
                COLUMN_HP_Hike_Length,
                COLUMN_HP_LOD,
                COLUMN_HP_Des,
                COLUMN_HP_Intend_Time,
                COLUMN_HP_License
        };
        String selection = COLUMN_HP_ID + " = ?";
        String[] selectionArgs = {String.valueOf(hikePlanId)};

        return db.query(TABLE_NAME1, projection, selection, selectionArgs, null, null, null);
    }

    public long updateHikePlanInDatabase(long hikePlanId, String hikePlanName, String location, String date, boolean packingAvailable, String hikeLength, String difficulty, String description, String intendTime, boolean license) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_HP_NAME, hikePlanName);
        values.put(COLUMN_HP_LOCATION, location);
        values.put(COLUMN_HP_DOTH, date);
        values.put(COLUMN_HP_Packing_Available, String.valueOf(packingAvailable));
        values.put(COLUMN_HP_Hike_Length, hikeLength);
        values.put(COLUMN_HP_LOD, difficulty);
        values.put(COLUMN_HP_Des, description);
        values.put(COLUMN_HP_Intend_Time, intendTime);
        values.put(COLUMN_HP_License, String.valueOf(license));

        String whereClause = COLUMN_HP_ID + " = ?";
        String[] whereArgs = {String.valueOf(hikePlanId)};

        try {
            int numRowsUpdated = db.update(TABLE_NAME1, values, whereClause, whereArgs);
            db.close();
            return numRowsUpdated;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int deleteHikePlan(long hikePlanId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_HP_ID + " = ?";
        String[] selectionArgs = {String.valueOf(hikePlanId)};

        try {
            int deletedRows = db.delete(TABLE_NAME1, selection, selectionArgs);
            db.close();
            return deletedRows;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void clearAllHikePlans() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME1, null, null);
        db.close();
    }

    public Cursor findObservationsByHikePlanId(long hikePlanId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                COLUMN_OB_ID,
                COLUMN_OB_ANIMAL_SIGN,
                COLUMN_OB_TYPE_OF_VEGETATIONS,
                COLUMN_OB_TRAILS_CONDITIONS,
                COLUMN_OB_WEATHER_CONDITIONS,
                COLUMN_OB_LAKE_LOCATION,
                COLUMN_OB_TIME_OF_OBSERVATIONS,
                COLUMN_OB_COMMENT
        };
        String selection = COLUMN_OB_HP_ID + " = ?";
        String[] selectionArgs = {String.valueOf(hikePlanId)};

        return db.query(TABLE_NAME2, projection, selection, selectionArgs, null, null, null);
    }

    public Cursor getFilteredCursor(String searchName) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COLUMN_HP_ID,
                COLUMN_HP_NAME
        };

        String selection = COLUMN_HP_NAME + " LIKE ?";
        String[] selectionArgs = {"%" + searchName + "%"}; // This will match any record containing the searchName

        return db.query(TABLE_NAME1, projection, selection, selectionArgs, null, null, null);
    }

    // Check if the database is empty or the table has no rows

    public Cursor getObservationsById(long hikePlanId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                COLUMN_OB_ID,
                COLUMN_OB_HP_ID,
                COLUMN_OB_ANIMAL_SIGN,
                COLUMN_OB_TYPE_OF_VEGETATIONS,
                COLUMN_OB_TIME_OF_OBSERVATIONS,
                COLUMN_OB_TRAILS_CONDITIONS,
                COLUMN_OB_WEATHER_CONDITIONS,
                COLUMN_OB_LAKE_LOCATION,
                COLUMN_OB_COMMENT
        };
        String selection = COLUMN_OB_HP_ID + " = ?";
        String[] selectionArgs = {String.valueOf(hikePlanId)};

        return db.query(TABLE_NAME2, projection, selection, selectionArgs, null, null, null);
    }

    public long updateObservationData(long hikePlanId, String animalSign, String
            typeOfVegetations, String timeOfObservation, String trailsConditions, String
                                              weatherConditions, String lakeLocation, String additionalComments) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_OB_HP_ID, hikePlanId);
        values.put(COLUMN_OB_ANIMAL_SIGN, animalSign);
        values.put(COLUMN_OB_TYPE_OF_VEGETATIONS, typeOfVegetations);
        values.put(COLUMN_OB_TIME_OF_OBSERVATIONS, timeOfObservation);
        values.put(COLUMN_OB_TRAILS_CONDITIONS, trailsConditions);
        values.put(COLUMN_OB_WEATHER_CONDITIONS, weatherConditions);
        values.put(COLUMN_OB_LAKE_LOCATION, lakeLocation);
        values.put(COLUMN_OB_COMMENT, additionalComments);

        String whereClause = COLUMN_OB_ID + " = ?";
        String[] whereArgs = {String.valueOf(hikePlanId)};

        try {
            int numRowsUpdated = db.update(TABLE_NAME2, values, whereClause, whereArgs);
            db.close();
            return numRowsUpdated;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

}

