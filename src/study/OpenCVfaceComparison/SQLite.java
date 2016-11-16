package study.OpenCVfaceComparison;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ArrayAdapter;

public class SQLite {

	// ListView push_data_list;
	ArrayAdapter<String> listAdapter;

	private SQLiteDatabase mydb = null; // 声明数据库
	private final static String DATABASE_NAME = "faceTmpNew_sqlite.db"; // 数据库名称
	String TABLE_NAME = "firstTable"; // 表名称
	String CREATE_TABLE = "create table firstTable" + "(id TEXT," + "Data TEXT," + "Name TEXT)"; // 创建表的SQL语句

	private final static String ID = "id"; // ID
	private final static String Data = "Data"; // 数据项
	private final static String Name = "Name"; // 名稱
	private static final int MODE_PRIVATE = 0;

	Cursor cursor;
	int count;
	ContentValues cv;

	OnMessageRecive onMessageRecive;

	public static Context context;
	
	public SQLite(Context context) {

		SQLite.context = context;
	}

	public SQLite(Context context, OnMessageRecive onMessageRecive) {

		SQLite.context = context;
		this.onMessageRecive = onMessageRecive;

	}

	public void Start_value() {

		mydb = context.openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null); // 創建數據庫

		try {

			mydb.execSQL(CREATE_TABLE); // 创建表

		} catch (Exception e) {

		}
	}

	// 開啟資料庫
	public void database() {

		mydb = context.openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);// 打开数据库
		// showData();
		mydb.close();
	}

	// 插入數據
	public void insert(String id, String data, String name) {

		mydb = context.openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);// 打开数据库

		cv = new ContentValues();
		cv.put(ID, id); // 名称
		cv.put(Data, data); // Data
		cv.put(Name, name); // name
		mydb.insert(TABLE_NAME, null, cv); // 插入数据

		// showData();
		mydb.close();

	}

	// 刪除數據
	public void delete(String data) {

		if (data.equals("1")) {
			
			mydb = context.openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);// 打开数据库

			String whereClause = "ID=?";
			String[] whereArgs = { data };
			mydb.delete(TABLE_NAME, whereClause, whereArgs); // 删除数据
			
		} else {
			
			mydb = context.openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);// 打开数据库

			String whereClause = "Data=?";
			String[] whereArgs = { data };
			mydb.delete(TABLE_NAME, whereClause, whereArgs); // 删除数据

		}
		
		// showData();
		mydb.close();
	}

	// 修改數據
	public void updata(String id, String data, String name) {

		mydb = context.openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);// 打开数据库

		cv = new ContentValues();  
		cv.put("ID", id);
		cv.put("Data", data);
		cv.put("Name", name);
		String whereClause = "id=?";
		String[] whereArgs = { id };
		mydb.update(TABLE_NAME,cv, whereClause, whereArgs); // 删除数据

		// showData();
		mydb.close();

	}

	// 查詢數據
	public void Inquire(String id) {
		
		String id_Inquire = "";
		String data_Inquire = "";
		String name_Inquire = "";

		mydb = context.openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);// 打开数据库

		String selection = "id=?"; // 查询条件
		String[] selectionArgs = { id };
		cursor = mydb.query(TABLE_NAME, new String[] { ID, Data, Name }, selection, selectionArgs, null, null, null); // 查询数据
		if (cursor != null && count >= 0) {
			if (cursor.moveToFirst()) // 移动到第一个
			{
				do {
					String Id = cursor.getString(0); // 获取ID
					String Data = cursor.getString(1); // 获取Data
					String Name = cursor.getString(2); // 获取Data

					// this.listAdapter.add(Data);
					id_Inquire = Id + ",/" + id_Inquire;
					data_Inquire = Data + ",/" + data_Inquire;
					name_Inquire = Name + ",/" + name_Inquire;

				} while (cursor.moveToNext()); // 移动到下一个
			}
		}

		if (!id_Inquire.equals("") && !data_Inquire.equals("") && !name_Inquire.equals("")) {
			
			this.onMessageRecive.onReceive(id_Inquire, data_Inquire, name_Inquire);
		}
		
		// showData();
		mydb.close();
	}

	public void showData() // 显示数据
	{

		cursor = mydb.query(TABLE_NAME, new String[] { ID, Data, Name }, null, null, null, null, null);
		count = cursor.getCount(); // 获取个数
		if (cursor != null && count >= 0) {
			if (cursor.moveToFirst()) // 移动到第一个
			{
				do {
					String id = cursor.getString(0); // 获取ID
					String Data = cursor.getString(1); // 获取Data
					String Name = cursor.getString(2); // 获取Data

					// this.listAdapter.add(Data);
					this.onMessageRecive.onReceive(id, Data, Name);

				} while (cursor.moveToNext()); // 移动到下一个
			}
		}
	}

	public interface OnMessageRecive {

		void onReceive(String id, String Data, String Name);

	}

}
