package eu.fisoft.fit.edux.storage;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import eu.fisoft.fit.edux.Course;

public class AttendedSQLHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "attended";
	private static final String COURSE = "code";
	private static final String COURSE_FULLNAME = "name";

	private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE "
			+ TABLE_NAME + " (" + COURSE + " TEXT," + COURSE_FULLNAME + " TEXT );";

	public AttendedSQLHelper(Context context) {
		super(context, TABLE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DICTIONARY_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	public void clear(){
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("DELETE FROM "+TABLE_NAME);
		db.close();
	}
	
	public void fill(List<Course> courses){
		clear();
		SQLiteDatabase db = getWritableDatabase();
		for (Course course : courses){
			db.execSQL("INSERT INTO "+TABLE_NAME+
					" VALUES ('"+course.getCode()+"', '' );");
		}
		db.close();
	}
	
	public List<Course> all(){
		SQLiteDatabase db = getReadableDatabase();
		Cursor result = db.query(TABLE_NAME, new String[]{COURSE, COURSE_FULLNAME}, "1=1", null, "", "", "");
		List<Course> courses = new ArrayList<Course>();
		if (!result.isAfterLast())
			result.moveToNext();
		while (!result.isAfterLast()){
			String code = result.getString(0);
			String name = result.getString(1);
			result.moveToNext();
			courses.add(new Course(code, name, code));
		}
		result.close();
		db.close();
		return courses;
			
	}

	public void updateName(String code, String name) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("UPDATE "+TABLE_NAME+" SET "+COURSE_FULLNAME+"='"+name+"' WHERE "+COURSE+" LIKE '"+code+"%' ;");
		db.close();
	}

}
