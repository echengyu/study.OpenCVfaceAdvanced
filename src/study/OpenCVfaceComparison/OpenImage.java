package study.OpenCVfaceComparison;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

public class OpenImage extends Activity{
	
	ListView image_data_list;
	List<HashMap<String, Object>> listData;
	SimpleAdapter adapter;
	HashMap<String, Object> mapValue;
	
	SQLite sqlite;
	
	String data_str[];
	String data_delete = "";
	
	String name_str[];
	
	Boolean image_allclear_bool = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.openimage_view);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
//		// ActionBar按鈕
//		getActionBar().setDisplayHomeAsUpEnabled(true);
//		getActionBar().setHomeButtonEnabled(true);
		
		image_data_list = (ListView) findViewById(R.id.name_data_list);
		listData = new ArrayList<HashMap<String, Object>>();
		
		sqlite = new SQLite(this, new SQLite.OnMessageRecive() {

			@Override
			public void onReceive(String id, String Data, String Name) {
				// TODO Auto-generated method stub
				
				File file = new File(Environment.getExternalStorageDirectory(), "sample_picture" + "_faceTmpNew");
				Bitmap mBitmap1 = null;				
				// 获得图片的宽高  
			    int width = 0, height = 0;  
			    // 设置想要的大小  
			    int newWidth = 0, newHeight = 0;  
			    // 计算缩放比例  
			    float scaleWidth = 0, scaleHeight = 0;
			    // 取得想要缩放的matrix参数  
			    Matrix matrix = null;
			    // 得到新的图片 
			    Bitmap newbitmap = null;

				data_str = Data.split(",/");
				name_str = Name.split(",/");
				
				
				for (int i = 0; i < data_str.length; i++) {
					
					String filepath = "/sdcard/sample_picture" + "_faceTmpNew/" + data_str[i] + ".jpg";
					
					if (file.exists()) {
						
						mBitmap1 = BitmapFactory.decodeFile(filepath);						
						// 获得图片的宽高  
					    width = mBitmap1.getWidth();  
					    height = mBitmap1.getHeight();  
					    // 设置想要的大小  
					    newWidth = 100;  
					    newHeight = 100;  
					    // 计算缩放比例  
					    scaleWidth = ((float) newWidth) / width;  
					    scaleHeight = ((float) newHeight) / height;  
					    // 取得想要缩放的matrix参数  
					    matrix = new Matrix();  
					    matrix.postScale(scaleWidth, scaleHeight);  
					    // 得到新的图片  
					    newbitmap = Bitmap.createBitmap(mBitmap1, 0, 0, width, height, matrix, true);  
					}
					
					if (mBitmap1 == null) {
						
						sqlite.delete(data_str[i]);
						
					} else {
						
						mapValue = new HashMap<String, Object>();
						mapValue.put("Image", newbitmap);
						mapValue.put("Data", data_str[i]);
						mapValue.put("Name", name_str[i]);
						listData.add(mapValue);
						
					}

					// listAdapter.add(data_str[i]);
					
					if (image_allclear_bool) {
						
						File clear_file = new File("/sdcard/sample_picture" + "_faceTmpNew/" + data_str[i] + ".jpg");

						if (clear_file.isFile()) { // 判断是否是文件

							clear_file.delete(); // delete()方法 你应该知道 是删除的意思;

						}
						
						sqlite.delete("1"); // 刪除資料

						if (data_str.length-1 == i) {
							
							image_allclear_bool = false;
							
							listData.clear();
							sqlite.Inquire("1");
							adapter.notifyDataSetChanged(); // 更改的數據							
						}
												
					}
				}				
				
			}
		});
		
		adapter = new SimpleAdapter(OpenImage.this, listData, R.layout.openimage_item, new String[] { "Image", "Data", "Name" },
				new int[] { R.id.imageData, R.id.tvData, R.id.tvName });
		
		
		/* 实现ViewBinder()这个接口 */
		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				// TODO Auto-generated method stub
				if (view instanceof ImageView && data instanceof Bitmap) {
					ImageView i = (ImageView) view;
					i.setImageBitmap((Bitmap) data);
					return true;
				}
				return false;
			}
		});
		
		image_data_list.setAdapter(adapter);
		
		image_data_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	
				Toast.makeText(getApplicationContext(), "你選擇的是" + data_str[position], Toast.LENGTH_SHORT).show();

				data_delete = data_str[position];

				AlertDialog.Builder dialog = new AlertDialog.Builder(OpenImage.this);
				dialog.setTitle("圖片訊息");
				dialog.setMessage("是否開啟圖片或是刪除圖片");
				dialog.setPositiveButton("開啟", new DialogInterface.OnClickListener() {
			        @Override
			        public void onClick(DialogInterface arg0, int arg1) {
			        	
			        	Intent intent = new Intent( Intent.ACTION_VIEW );
			        	File file = new File( "/sdcard/sample_picture" + "_faceTmpNew/" + data_delete + ".jpg");
			        	// 檔名小寫, 容易判斷副檔名
			        	intent.setDataAndType( Uri.fromFile(file), "image/*" );
			        	// 切換到開啟的檔案
			        	startActivity(intent);
			        }
			    });
				dialog.setNeutralButton("刪除", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						Toast.makeText(OpenImage.this, "已刪除了" + data_delete, Toast.LENGTH_SHORT).show();

						File file = new File("/sdcard/sample_picture" + "_faceTmpNew/" + data_delete + ".jpg");

						if (file.isFile()) { // 判断是否是文件

							file.delete(); // delete()方法 你应该知道 是删除的意思;

						}

						sqlite.delete(data_delete); // 刪除資料

						listData.clear();
						sqlite.Inquire("1");
						adapter.notifyDataSetChanged(); // 更改的數據
					}

				});
				dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
					}

				});

				dialog.show();
				
			}
		});
		
		sqlite.Start_value();
		sqlite.Inquire("1");
		
		adapter.notifyDataSetChanged(); //更改的數據
		
	}
	
	protected void onDestroy() {
		super.onDestroy();
		listData.clear();
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu1) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.open_image, menu1);
						
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.image_all_clear:
			
			AlertDialog.Builder dialog = new AlertDialog.Builder(OpenImage.this);
			dialog.setTitle("圖片訊息");
			dialog.setMessage("是否刪除全部的圖片");
			dialog.setPositiveButton("刪除", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					
						
					Toast.makeText(OpenImage.this, "已刪除全部的圖片", Toast.LENGTH_SHORT).show();
					
					image_allclear_bool = true;
					sqlite.Inquire("1");
										
				}

			});
			dialog.setNeutralButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
				}

			});

			dialog.show();
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
