package com.mif.juje0729.ftp;

public class Note {

    int _id;
    String _note;

    public Note(){

    }
    public Note(int id, String note){
        this._id = id;
        this._note = note;
    }

    public Note(String note){
        this._note = note;
    }
    public int getID(){
        return this._id;
    }

    public void setID(int id){
        this._id = id;
    }

    public String getNote(){
        return this._note;
    }

    public void setNote(String note){
        this._note = note;
    }

}
