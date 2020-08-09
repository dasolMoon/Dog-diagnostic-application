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
	private static String DB_PATH = "/data/data/hajun.animaldiag/databases/"; //DB 주소
	private static final String DB_NAME = "AnimalRefined.db"; //DB이름
	private static final int DB_VER = 1; //이건 뭘까 VERSON?

	private SQLiteDatabase myDataBase;

	private final Context myContext;

	/**
	 * Constructor
	 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	 * @param context
	 */
	public SQLiteHanBang(Context context){ // 생성자
		super(context, DB_NAME, null, DB_VER);
		this.myContext = context;
	}	

	/**
	 * Creates a empty database on the system and rewrites it with your own database.
	 * 시스템에 비어있는 데이터베이스를 하나 만들고 당신의 데이터베이스에 다시 씀
	 * */
	public boolean createDataBase() throws IOException{

		//DB가 존재하는지 검사하는 변수
		boolean dbExist = checkDataBase();

		//만약 데이터베이스가 존재한다면
		if(dbExist){
			return false; //false값을 return
		}else{
			// 데이터베이스가 존재하지 않는다면
			//By calling this method and empty database will be created into the default system path
			//이 메소드가 호출될것이며 당신의 애플리케이션 시스템 기본 위치에 빈 데이터베이스가 만들어질것이다.
			//of your application so we are gonna be able to overwrite that database with our database.
			//그래서 우리는 당신의 데이터베이스를가지고 오버라이트 할수있다
			this.getReadableDatabase(); //메소드 호출

			try{ // DB 복사작업 실시
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
	 * 체크한다 만약 데이터베이스가 이미 전조하는지 애플리케이션을 열었을 때
	 * 있으면 true 없으면 false를 return
	 */
	private boolean checkDataBase(){ // DB가 존재하는지 검사하는 함수

		SQLiteDatabase checkDB = null;

		try{
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
		}catch(SQLiteException e){
			System.out.println(e.toString());
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
	 *
	 * 복사한다 당신의 데이터베이스를 당신의 로컬 폴더로부터 시스템 폴더 안에 새로운 빈 데이터베이스를 만들기 위해서
	 * 핸들러에 액세스 할 수 있을 때 부터
	 * 이건은 transfering bytestream에 의해 실행되었다
	 * */
	private void copyDataBase() throws IOException{

		//Open your local db as the input stream
		//당신의 로컬 디비를 연다 input stream으로
		InputStream myInput = myContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		//길정함 새로운 비어있는 디비를 만들기위해
		String outFileName = DB_PATH + DB_NAME;

		//Open the empty db as the output stream
		//연다 비어있는 디비 output stream으로
		OutputStream myOutput = new FileOutputStream(outFileName);

		//transfer bytes from the inputfile to the outputfile
		//트랜스퍼 바이트는 인풋 아웃풋으로부터 옴
		byte[] buffer = new byte[1024];
		int length;
		while((length = myInput.read(buffer)) > 0){
			myOutput.write(buffer, 0, length);
		}

		//Close the streams
		//스트림 끝내준다
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
