package eu.fisoft.fit.edux.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CoursesSQLHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "courses";
	private static final String COURSE = "course";

	private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE "
			+ TABLE_NAME + " (" + COURSE + " TEXT);";

	private CoursesSQLHelper(Context context) {
		super(context, TABLE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DICTIONARY_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
