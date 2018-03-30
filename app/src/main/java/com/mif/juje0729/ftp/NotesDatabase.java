package com.mif.juje0729.ftp;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotesDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "notesDatabase";

    private static String TABLE_NOTES;

    private static final String KEY_ID = "id";
    private static final String KEY_NOTE = "note";

    public void setTableNotes(Context context) {
        if (context instanceof FilesActivity == false && context instanceof EditFileActivity ==
                false) {
            TABLE_NOTES = "servers";
        } else {
            TABLE_NOTES = "files";
        }
    }

    public NotesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setTableNotes(context);
        onCreate(this.getWritableDatabase());

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NOTES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NOTES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NOTE + " TEXT" + ")";
        db.execSQL(CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);

        onCreate(db);
    }


    void addNote(Context context, Note note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NOTE, note.getNote());

        setTableNotes(context);

        db.insert(TABLE_NOTES, null, values);
        db.close();
    }

    Note getNote(Context context, int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        setTableNotes(context);

        Cursor cursor = db.query(TABLE_NOTES, new String[]{KEY_ID,
                        KEY_NOTE}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Note note = new Note(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1));
        return note;
    }

    public List<Note> getAllNotes(Context context) {
        List<Note> noteList = new ArrayList<Note>();

        setTableNotes(context);

        String selectQuery = "SELECT  * FROM " + TABLE_NOTES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setID(Integer.parseInt(cursor.getString(0)));
                note.setNote(cursor.getString(1));

                noteList.add(note);
            } while (cursor.moveToNext());
        }

        return noteList;
    }

    public List<Note> getServers() {
        List<Note> serverList = new ArrayList<Note>();


        String selectQuery = "SELECT  * FROM servers";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Note server = new Note();
                server.setID(Integer.parseInt(cursor.getString(0)));
                server.setNote(cursor.getString(1));

                serverList.add(server);
            } while (cursor.moveToNext());
        }

        return serverList;
    }

    public List<Note> getFiles() {
        List<Note> fileList = new ArrayList<Note>();


        String selectQuery = "SELECT  * FROM files";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Note file = new Note();
                file.setID(Integer.parseInt(cursor.getString(0)));
                file.setNote(cursor.getString(1));

                fileList.add(file);
            } while (cursor.moveToNext());
        }

        return fileList;
    }

    public int updateNote(Context context, Note note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NOTE, note.getNote());

        setTableNotes(context);

        return db.update(TABLE_NOTES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(note.getID())});
    }

    public void deleteNote(Context context, Note note) {
        SQLiteDatabase db = this.getWritableDatabase();

        setTableNotes(context);

        db.delete(TABLE_NOTES, KEY_ID + " = ?",
                new String[]{String.valueOf(note.getID())});
        db.close();
    }

    public int getNotesCount(Context context) {
        setTableNotes(context);
        String countQuery = "SELECT  * FROM " + TABLE_NOTES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        return cursor.getCount();
    }

    public void searchAndDelete(Context context, String note) {
        setTableNotes(context);
        String selectQuery = "SELECT  " + KEY_ID + " FROM " + TABLE_NOTES + " WHERE " + KEY_NOTE
                + " = " + "'" + note + "';";
        Note _note = new Note();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        _note.setID(Integer.parseInt(cursor.getString(0)));

        deleteNote(context, _note);
    }
}
