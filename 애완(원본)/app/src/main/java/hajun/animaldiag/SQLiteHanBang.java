package hajun.animaldiag;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class SQLiteHanBang extends SQLiteOpenHelper {

	//The Android's default system path of your application database.
	private static String DB_PATH = "/data/data/hajun.animaldiag/databases/";
	private static final String DB_NAME = "AnimalRefined.db";
	private static final int DB_VER = 1;

	private SQLiteDatabase myDataBase;

	private final Context myContext;

	/**
	 * Constructor
	 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	 * @param context
	 */
	public SQLiteHanBang(Context context){
		super(context, DB_NAME, null, DB_VER);
		this.myContext = context;
	}	

	/**
	 * Creates a empty database on the system and rewrites it with your own database.
	 * */
	public boolean createDataBase() throws IOException{

		boolean dbExist = checkDataBase();

		if(dbExist){
			return false;
		}else{
			//By calling this method and empty database will be created into the default system path
			//of your application so we are gonna be able to overwrite that database with our database.
			this.getReadableDatabase();

			try{
				copyDataBase();
				return true;
			}catch(IOException e){
				throw new Error("Error copying database");
			}
		}
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase(){

		SQLiteDatabase checkDB = null;

		try{
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
		}catch(SQLiteException e){
			//database does't exist yet.
		}

		if(checkDB != null){
			checkDB.close();
		}

		return checkDB != null ? true : false;
	}

	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 * This is done by transfering bytestream.
	 * */
	private void copyDataBase() throws IOException{

		//Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		//Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while((length = myInput.read(buffer)) > 0){
			myOutput.write(buffer, 0, length);
		}

		//Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}
	
	public void openDataBase() throws SQLException{
		//Open the database
		String myPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
	}

	@Override
	public synchronized void close(){
		if(myDataBase != null)
			myDataBase.close();

		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db){
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		if(newVersion > oldVersion)
		{
			try{copyDataBase();}
			catch(IOException e) {}
		}
	}
	
	public BeanDisease[] LoadDisease()
	{
		String strQuery = "select * from Disease";
		Cursor cursor = myDataBase.rawQuery(strQuery,  null);

		BeanDisease[] beans = new BeanDisease[cursor.getCount()];

		int nIdx = 0;
		if (cursor.moveToFirst()) {
			do {
				BeanDisease bean = new BeanDisease();

				bean.nNum = cursor.getInt(0);				// _id 
				bean.strName = cursor.getString(1);		// name
				bean.strFactor = cursor.getString(2);		// factor
				bean.strSymptom = cursor.getString(3);		// symptom
				bean.strExplanation = cursor.getString(4); // explanation
				
				beans[nIdx++] = bean;

			} while (cursor.moveToNext());
		}

		return beans;		
	}
	
	public BeanSymptom[] LoadSymptom()
	{
		String strQuery = "select * from Symptom";
		Cursor cursor = myDataBase.rawQuery(strQuery,  null);

		BeanSymptom[] beans = new BeanSymptom[cursor.getCount()];
		
		int nIdx = 0;
		if (cursor.moveToFirst()) {
			do {
				BeanSymptom bean = new BeanSymptom();

				bean.nNum = cursor.getInt(0);			// _id 
				bean.strName = cursor.getString(1);		// name
				bean.nBodyPart = cursor.getInt(2);		// bodypart
				
				beans[nIdx++] = bean;

			} while (cursor.moveToNext());
		}
		
		return beans;		
	}
	
	public BeanBodyPart[] LoadBodyPart()
	{
		String strQuery = "select * from Bodypart";
		Cursor cursor = myDataBase.rawQuery(strQuery,  null);

		BeanBodyPart[] beans = new BeanBodyPart[cursor.getCount()];
		
		int nIdx = 0;
		if (cursor.moveToFirst()) {
			do {
				BeanBodyPart bean = new BeanBodyPart();

				bean.nNum = cursor.getInt(0);				// _id 
				bean.strName = cursor.getString(1);			// name
				
				beans[nIdx++] = bean;

			} while (cursor.moveToNext());
		}
		
		return beans;		
	}
}
