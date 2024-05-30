
package com.example.farmtech;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.github.mikephil.charting.data.BarEntry;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HarvestProgress.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "harvest_progress";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CROP_TYPE = "crop_type";
    private static final String COLUMN_PROGRESS_VALUE = "progress_value";
    private static final String COLUMN_DATE = "date";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CROP_TYPE + " TEXT, " +
                COLUMN_PROGRESS_VALUE + " REAL, " +
                COLUMN_DATE + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addProgress(String cropType, float progressValue, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CROP_TYPE, cropType);
        values.put(COLUMN_PROGRESS_VALUE, progressValue);
        values.put(COLUMN_DATE, date);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void deleteLastProgress(String cropType) {
        SQLiteDatabase db = this.getWritableDatabase();
        String deleteQuery = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_CROP_TYPE + " = '" + cropType + "' ORDER BY " + COLUMN_ID + " DESC LIMIT 1";
        db.execSQL(deleteQuery);
        db.close();
    }

    public void deleteAllProgress(String cropType) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_CROP_TYPE + " = ?", new String[]{cropType});
        db.close();
    }

    public ArrayList<BarEntry> getProgressEntries(String cropType) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_ID, COLUMN_PROGRESS_VALUE},
                COLUMN_CROP_TYPE + "=?",
                new String[]{cropType},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                float progressValue = cursor.getFloat(cursor.getColumnIndex(COLUMN_PROGRESS_VALUE));
                entries.add(new BarEntry(id, progressValue));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return entries;
    }
}
