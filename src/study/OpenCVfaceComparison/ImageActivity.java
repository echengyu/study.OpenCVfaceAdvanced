package study.OpenCVfaceComparison;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.security.auth.Subject;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;

import com.opencv_application.Opencv_Application;

import android.R.bool;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImageActivity extends Activity implements CvCameraViewListener2 {
	private static final String TAG  = "OCVSample::Activity";
	private ScanTool mOpenCvCameraView0;
	private ScanTool mOpenCvCameraView1;

	private boolean onCameraViewStarted0 = true;
	private boolean onCameraViewStarted1 = true;
	
	private List<android.hardware.Camera.Size> mResolutionList0,mResolutionList1;
	private android.hardware.Camera.Size resolution0 = null;
	private android.hardware.Camera.Size resolution1 = null;
	
	private File mCascadeFileFace;
	private File mCascadeFileEye;
	private File mCascadeFileNose;
	private File mCascadeFileMouth;
	
	private CascadeClassifier mJavaDetectorFace;
	private CascadeClassifier mJavaDetectorEye;  
	private CascadeClassifier mJavaDetectorNose; 
	private CascadeClassifier mJavaDetectorMouth;
		
	private ImageView mImage_Identification, mImage_Capture;
	
	private Point pointCenterEyeLeft = new Point();
	private Point pointCenterEyeRight = new Point();
	private Point pointCenterNose = new Point();
	private Point pointCenterMouth = new Point();
	
	private Bitmap mBitmap2;  
    private Button mBtn_compare,mBtn_capture,mBtn_openimage,mBtn_Frontlens_adjustment,mBtn_btn_opensave;
    
    private ImageButton mImageBtn_lens;
    
    private SQLite sqlite; //SQLite資料庫
    private double target_select = 0.0; //相似度數值
    private String target_str = "0"; //相似度數值
    private Bitmap mBitmap11 = null;
    private String str_ImageName = "";
    private String str_EditName = "";
    
    private EditText edt_ImageName; //填寫名字
    private TextView tv_ImageName; //顯示名字
    private TextView tv_ImageSimilarity; //顯示相似度
    private TextView tv_matTmp; //tv_matTmp;
    
	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				
				try {
					// load cascade file from application resources
					InputStream isFace 			= getResources().openRawResource(R.raw.lbpcascade_frontalface);
					InputStream isEye 			= getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
					InputStream isNose 			= getResources().openRawResource(R.raw.haarcascade_mcs_nose);
					InputStream isMouth 		= getResources().openRawResource(R.raw.haarcascade_mcs_mouth);

					File cascadeDirFace 		= getDir("cascade", Context.MODE_PRIVATE);
					File cascadeDirEye 			= getDir("cascade", Context.MODE_PRIVATE);
					File cascadeDirNose 		= getDir("cascade", Context.MODE_PRIVATE);
					File cascadeDirMouth 		= getDir("cascade", Context.MODE_PRIVATE);

					mCascadeFileFace 			= new File(cascadeDirFace, "lbpcascade_frontalface.xml");
					mCascadeFileEye 			= new File(cascadeDirEye, "haarcascade_eye_tree_eyeglasses.xml");
					mCascadeFileNose 			= new File(cascadeDirNose, "haarcascade_mcs_nose.xml");
					mCascadeFileMouth			= new File(cascadeDirMouth, "haarcascade_mcs_mouth.xml");

					FileOutputStream osFace  	= new FileOutputStream(mCascadeFileFace);
					FileOutputStream osEye		= new FileOutputStream(mCascadeFileEye);
					FileOutputStream osNose  	= new FileOutputStream(mCascadeFileNose);
					FileOutputStream osMouth 	= new FileOutputStream(mCascadeFileMouth);
					
					byte[] buffer;
					int bytesRead;
					
					buffer = new byte[4096];
					bytesRead = 0;
					while ((bytesRead = isFace.read(buffer)) != -1) {
						osFace.write(buffer, 0, bytesRead);
					}
					isFace.close();
					osFace.close();
					
					buffer = new byte[4096];
					bytesRead = 0;
					while ((bytesRead = isEye.read(buffer)) != -1) {
						osEye.write(buffer, 0, bytesRead);
					}
					isEye.close();
					osEye.close();
					
					buffer = new byte[4096];
					bytesRead = 0;
					while ((bytesRead = isNose.read(buffer)) != -1) {
						osNose.write(buffer, 0, bytesRead);
					}
					isNose.close();
					osNose.close();
					
					buffer = new byte[4096];
					bytesRead = 0;
					while ((bytesRead = isMouth.read(buffer)) != -1) {
						osMouth.write(buffer, 0, bytesRead);
					}
					isMouth.close();
					osMouth.close();
					
					mJavaDetectorFace  	= new CascadeClassifier(mCascadeFileFace.getAbsolutePath());
					mJavaDetectorEye  	= new CascadeClassifier(mCascadeFileEye.getAbsolutePath());
					mJavaDetectorNose  	= new CascadeClassifier(mCascadeFileNose.getAbsolutePath());
					mJavaDetectorMouth 	= new CascadeClassifier(mCascadeFileMouth.getAbsolutePath());
					
					if (mJavaDetectorFace.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetectorFace = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from " + mCascadeFileFace.getAbsolutePath());
					
					if (mJavaDetectorEye.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetectorEye = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from " + mCascadeFileEye.getAbsolutePath());
					
					if (mJavaDetectorNose.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetectorNose = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from " + mCascadeFileNose.getAbsolutePath());
					
					if (mJavaDetectorMouth.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetectorMouth = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from " + mCascadeFileMouth.getAbsolutePath());
				
					cascadeDirFace.delete();
					cascadeDirEye.delete(); 	
					cascadeDirNose.delete(); 
					cascadeDirMouth.delete();				

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}
				
				mOpenCvCameraView0.enableView();
				mOpenCvCameraView1.enableView();
			}
			break;
			default: {
				super.onManagerConnected(status);
			}
			break;
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.picture_view);
		
		mOpenCvCameraView0 = (ScanTool) findViewById(R.id.picture_view0);
		mOpenCvCameraView0.setCameraIndex(ScanTool.CAMERA_ID_BACK);		
		
		mOpenCvCameraView1 = (ScanTool) findViewById(R.id.picture_view1);
		mOpenCvCameraView1.setCameraIndex(ScanTool.CAMERA_ID_FRONT);
		
		//判斷前後鏡頭
		if (!((Opencv_Application)getApplication()).Bool_lens) {
			
			mOpenCvCameraView0.setVisibility(SurfaceView.VISIBLE);
			
		} else {
			
			mOpenCvCameraView1.setVisibility(SurfaceView.VISIBLE); 						 
		}
		
		mOpenCvCameraView0.setCvCameraViewListener(this);
		mOpenCvCameraView1.setCvCameraViewListener(this);
		
		//不自動跳虛擬鍵盤的程式
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		sqlite(); //資料庫				
		initialize(); //初始設定
	}
	
	public void sqlite() {

		sqlite = new SQLite(this, new SQLite.OnMessageRecive() {

			@Override
			public void onReceive(String id, String Data, String Name) {
				// TODO Auto-generated method stub

				File file = new File(Environment.getExternalStorageDirectory(), "sample_picture" + "_faceTmpNew");
				Bitmap mBitmap1 = null;
				
				String[] Data_Inquire = Data.split(",/");
				String[] Name_ImageName = Name.split(",/");
				
				for (int i=0; i<Data_Inquire.length; i++) {
					
					String filepath = "/sdcard/sample_picture" + "_faceTmpNew/" + Data_Inquire[i] + ".jpg";
					
					if (file.exists()) {
						
						mBitmap1 = BitmapFactory.decodeFile(filepath);
					}
					
					Mat mat1 = new Mat();
					Mat mat2 = new Mat();
					Mat mat10 = new Mat();
					Mat mat20 = new Mat();
					Mat mat11 = new Mat();
					Mat mat22 = new Mat();
					
					// 获得图片的宽高  
				    int width1 = 0, height1 = 0;
				    int width2 = 0, height2 = 0;
				    // 设置想要的大小  
				    int newWidth = 0, newHeight = 0;
				    // 计算缩放比例  
				    float scaleWidth1 = 0, scaleHeight1 = 0;
				    float scaleWidth2 = 0, scaleHeight2 = 0;
				    // 取得想要缩放的matrix参数  
				    Matrix matrix1 = null;
				    Matrix matrix2 = null;
				    // 得到新的图片 
				    Bitmap newbitmap1 = null;
				    Bitmap newbitmap2 = null;

					if (mBitmap1 != null && mBitmap2 != null) {
						
						// 获得图片的宽高  
					    width1 = mBitmap1.getWidth();  
					    height1 = mBitmap1.getHeight();
					    width2 = mBitmap2.getWidth();  
					    height2 = mBitmap2.getHeight();
					    // 设置想要的大小  
					    newWidth = 300;  
					    newHeight = 300;
					    // 计算缩放比例  
					    scaleWidth1 = ((float) newWidth) / width1;  
					    scaleHeight1 = ((float) newHeight) / height1; 
					    scaleWidth2 = ((float) newWidth) / width2;  
					    scaleHeight2 = ((float) newHeight) / height2;
					    // 取得想要缩放的matrix参数  
					    matrix1 = new Matrix();
					    matrix2 = new Matrix();
					    matrix1.postScale(scaleWidth1, scaleHeight1);
					    matrix2.postScale(scaleWidth2, scaleHeight2);
					    // 得到新的图片  
					    newbitmap1 = Bitmap.createBitmap(mBitmap1, 0, 0, width1, height1, matrix1, true);
					    newbitmap2 = Bitmap.createBitmap(mBitmap2, 0, 0, width2, height2, matrix2, true);

						Utils.bitmapToMat(newbitmap1, mat1);
						Utils.bitmapToMat(newbitmap2, mat2);

						Imgproc.resize(mat1, mat10, new Size(300, 300));
						Imgproc.resize(mat2, mat20, new Size(300, 300));

						Imgproc.cvtColor(mat10, mat11, Imgproc.COLOR_BGR2GRAY);
						Imgproc.cvtColor(mat20, mat22, Imgproc.COLOR_BGR2GRAY);
						
						Imgproc.equalizeHist(mat11, mat11);
						Imgproc.equalizeHist(mat22, mat22);
						
						mat11.convertTo(mat11, CvType.CV_32F);
						mat22.convertTo(mat22, CvType.CV_32F);
					    double target = Imgproc.compareHist(mat11, mat22, Imgproc.CV_COMP_CORREL); // 用直方圖比較圖片相似度

					    if(target > target_select) {
					    	
					    	target_select = target;
					    	mBitmap11 = mBitmap1;
					    	str_ImageName = Name_ImageName[i];
					    }
					    
					    if (Data_Inquire.length-1 == i) {
				    		
				    		mImage_Identification.setImageBitmap(mBitmap11);
				    		tv_ImageName.setText(str_ImageName);
					    	Log.e(TAG, "相似度 ：   == " + target_select);  					    	
//					    	Toast.makeText(ImageActivity.this, "相似度 ：   == " + target_select, 1000).show();
					    	
					    	DecimalFormat df=new DecimalFormat("#%"); //new一個十進位的數字格式化，取到小數第二位
					    	target_str = df.format(target_select);					    	
					    	tv_ImageSimilarity.setText(target_str);					    	
					    	target_select = 0.0;
				    	}
					} else if (mBitmap1 == null) {
						
						sqlite.delete(Data_Inquire[i]); // 刪除資料
						
					}
				}
			}
		});
		
		sqlite.Start_value(); //開啟資料庫

	}
	
	public void initialize() {
		
		edt_ImageName = (EditText)findViewById(R.id.edt_image_name);
		tv_ImageName = (TextView)findViewById(R.id.tv_image_name);
		tv_ImageSimilarity = (TextView)findViewById(R.id.tv_image_similarity);
		tv_matTmp = (TextView)findViewById(R.id.tv_matTmp);
		
		mImage_Identification = (ImageView)findViewById(R.id.image_Identification);
		
		mImage_Capture = (ImageView)findViewById(R.id.image_capture);
        mImage_Capture.setImageBitmap(mBitmap2);
		
        mBtn_compare = (Button)findViewById(R.id.btn_compare);  
        mBtn_capture = (Button)findViewById(R.id.btn_capture);
        mBtn_openimage = (Button)findViewById(R.id.btn_openimage);
        mImageBtn_lens = (ImageButton)findViewById(R.id.imagebtn_lens);
        mBtn_Frontlens_adjustment = (Button)findViewById(R.id.btn_frontlens_adjustment);
        mBtn_btn_opensave = (Button)findViewById(R.id.btn_opensave);
        
		// 判斷辨識按鈕
		if (!((Opencv_Application) getApplication()).Bool_Identification) {

			mBtn_compare.getBackground().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);

		} else {

			mBtn_compare.getBackground().setColorFilter(0xFFFFFF00, android.graphics.PorterDuff.Mode.MULTIPLY);
		}
		
		// 前置鏡頭調整Button顯示
		if (!((Opencv_Application) getApplication()).Bool_lens) {

			mBtn_Frontlens_adjustment.setVisibility(SurfaceView.GONE);

		} else {

			mBtn_Frontlens_adjustment.setVisibility(SurfaceView.VISIBLE);
		}
		
		// 開啟截取Button顯示
		if (!((Opencv_Application) getApplication()).Bool_opensave) {
			
			mImage_Capture.setImageResource(R.drawable.image_capture);
			edt_ImageName.setVisibility(SurfaceView.GONE);
			mBtn_capture.setVisibility(SurfaceView.GONE);
			mBtn_btn_opensave.setText("開啟截取");
			mBtn_btn_opensave.setTextSize(16);

		} else {

			mImage_Capture.setImageResource(R.drawable.image_capture);
			edt_ImageName.setVisibility(SurfaceView.VISIBLE);
			mBtn_capture.setVisibility(SurfaceView.VISIBLE);
			mBtn_btn_opensave.setText("關閉截取");
			mBtn_btn_opensave.setTextSize(16);
			
		}

		// 辨識Button
        mBtn_compare.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				if (!((Opencv_Application)getApplication()).Bool_Identification) {
					
					((Opencv_Application)getApplication()).Bool_Identification = true;
					mImage_Identification.setImageResource(R.drawable.question_mark);
					tv_ImageName.setText("name");
					tv_ImageName.setTextSize(16);
					tv_ImageSimilarity.setText("相似度");
					tv_ImageSimilarity.setTextSize(16);
					mBtn_compare.getBackground().setColorFilter(0xFFFFFF00,android.graphics.PorterDuff.Mode.MULTIPLY);
					
				} else {
					
					((Opencv_Application)getApplication()).Bool_Identification = false;
					mImage_Identification.setImageResource(R.drawable.question_mark);
					tv_ImageName.setText("name");
					tv_ImageName.setTextSize(16);
					tv_ImageSimilarity.setText("相似度");
					tv_ImageSimilarity.setTextSize(16);
					mBtn_compare.getBackground().setColorFilter(0xFFFFFFFF,android.graphics.PorterDuff.Mode.MULTIPLY);
				}
			}
		});
        
        // 储存Button
        mBtn_capture.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				str_EditName = edt_ImageName.getText().toString();
				
				if (mBitmap2 != null && !str_EditName.equals("")) {
					
					Toast.makeText(getApplicationContext(), "存取完成", Toast.LENGTH_SHORT).show();
					SaveImage(mBitmap2, str_EditName); //存取圖片
					
				} else {
					
					Toast.makeText(getApplicationContext(), "名稱未填寫或是圖片位擷取，無法存取", Toast.LENGTH_SHORT).show();
					
				}
			}
		});
        
        // 開啟圖片Button
        mBtn_openimage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent intent = new Intent(ImageActivity.this,OpenImage.class);
				startActivity(intent);
				
			}
		});
        
        // 前後鏡頭Button
        mImageBtn_lens.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (!((Opencv_Application)getApplication()).Bool_lens) {
					
					((Opencv_Application)getApplication()).Bool_lens = true;
					mOpenCvCameraView0.setVisibility(SurfaceView.GONE);
					mOpenCvCameraView1.setVisibility(SurfaceView.VISIBLE);
					mBtn_Frontlens_adjustment.setVisibility(SurfaceView.VISIBLE);
					mOpenCvCameraView1.setCameraIndex(ScanTool.CAMERA_ID_FRONT);
					mOpenCvCameraView1.setCvCameraViewListener(ImageActivity.this);
					mOpenCvCameraView1.enableView();
					
				} else {
					
					((Opencv_Application)getApplication()).Bool_lens = false;
					mOpenCvCameraView1.setVisibility(SurfaceView.GONE);
					mOpenCvCameraView0.setVisibility(SurfaceView.VISIBLE);
					mBtn_Frontlens_adjustment.setVisibility(SurfaceView.GONE);
					mOpenCvCameraView0.setCameraIndex(ScanTool.CAMERA_ID_BACK);
					mOpenCvCameraView0.setCvCameraViewListener(ImageActivity.this);
					mOpenCvCameraView0.enableView();
				}
			}
		});
        
        // 前置鏡頭調整Button
        mBtn_Frontlens_adjustment.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				if (((Opencv_Application)getApplicationContext()).Int_frontlens_adjustment == 0) {	
					
					((Opencv_Application)getApplicationContext()).Int_frontlens_adjustment = 1;
				
				} else if (((Opencv_Application)getApplicationContext()).Int_frontlens_adjustment == 1) {
					
					((Opencv_Application)getApplicationContext()).Int_frontlens_adjustment = 2;
				
				} else {
					
					((Opencv_Application)getApplicationContext()).Int_frontlens_adjustment = 0;
				}
				
			}
		});
        
        // 開啟截取Button
        mBtn_btn_opensave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				if (!((Opencv_Application)getApplication()).Bool_opensave) {
					
					((Opencv_Application)getApplication()).Bool_opensave = true;
					mImage_Capture.setImageResource(R.drawable.image_capture);
					edt_ImageName.setVisibility(SurfaceView.VISIBLE);
					mBtn_capture.setVisibility(SurfaceView.VISIBLE);
					mBtn_btn_opensave.setText("關閉截取");
					mBtn_btn_opensave.setTextSize(16);
					
				} else {
					
					((Opencv_Application)getApplication()).Bool_opensave = false;
					mImage_Capture.setImageResource(R.drawable.image_capture);
					edt_ImageName.setVisibility(SurfaceView.GONE);
					mBtn_capture.setVisibility(SurfaceView.GONE);
					mBtn_btn_opensave.setText("開啟截取");
					mBtn_btn_opensave.setTextSize(16);
				}
				
			}
		});
        
    }	
	
	public void SaveImage(Bitmap Bitmap, String edt_ImageName) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String currentDateandTime = sdf.format(new Date());
		
		// 首先保存图片
	    File appDir = new File(Environment.getExternalStorageDirectory(), "sample_picture"+"_faceTmpNew");
	    if (!appDir.exists()) {
	        appDir.mkdir();
	    }
	    String fileName = currentDateandTime + ".jpg";
	    File file = new File(appDir, fileName);
	    try {
	        FileOutputStream fos = new FileOutputStream(file); 
	        Bitmap.compress(CompressFormat.JPEG, 100, fos);
	        fos.flush();
	        fos.close();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
		}

//	    // 其次把文件插入到系统图库
//	    try {
//	        MediaStore.Images.Media.insertImage(ImageActivity.this.getContentResolver(),
//					file.getAbsolutePath(), fileName, null);
//	    } catch (FileNotFoundException e) {
//	        e.printStackTrace();
//	    }
	    // 最后通知图库更新
	    ImageActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + "/sdcard+/sample_picture"+"_faceTmpNew/"+currentDateandTime+".jpg")));
		
		sqlite.insert("1", currentDateandTime, edt_ImageName);
	}
 
    /** 
     * 比较来个矩阵的相似度 
     * @param srcMat 
     * @param desMat 
     */  
    
	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView0 != null)
			mOpenCvCameraView0.disableView();
		
		if (mOpenCvCameraView1 != null)
			mOpenCvCameraView1.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView0 != null)
			mOpenCvCameraView0.disableView();
		
		if (mOpenCvCameraView1 != null)
			mOpenCvCameraView1.disableView();
		
	}

	public void onCameraViewStarted(int width, int height) {
		
		
		if (!((Opencv_Application)getApplication()).Bool_lens) {
			
			if(onCameraViewStarted0 == true) {
				onCameraViewStarted0 = false;
				mResolutionList0 = mOpenCvCameraView0.getResolutionList();
				for(int i=0; i<mResolutionList0.size(); i++) {
					if(mResolutionList0.get(i).width == 640) {
						resolution0 = mResolutionList0.get(i);
						mOpenCvCameraView0.setResolution(resolution0);
						resolution0 = mOpenCvCameraView0.getResolution();
						String caption = Integer.valueOf(resolution0.width).toString() + "x" + Integer.valueOf(resolution0.height).toString();
						Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
					}
				}
			}
						
		} else {
			
			if(onCameraViewStarted1 == true) {
				onCameraViewStarted1 = false;
				mResolutionList1 = mOpenCvCameraView1.getResolutionList();
				for(int i=0; i<mResolutionList1.size(); i++) {
					if(mResolutionList1.get(i).width == 640) {
						resolution1 = mResolutionList1.get(i);
						mOpenCvCameraView1.setResolution(resolution1);
						resolution1 = mOpenCvCameraView1.getResolution();
						String caption = Integer.valueOf(resolution1.width).toString() + "x" + Integer.valueOf(resolution1.height).toString();
						Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
					}
				}
			}		
			
		}
	}

	public void onCameraViewStopped() {

	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Mat mRgba = inputFrame.rgba();
		Mat dRgba = inputFrame.rgba();
								
		MatOfRect matOfRectTmp;
		Rect[] rectArrayFace;
		int width;
		int height;
		int x;
		int y;
		
		if (((Opencv_Application)getApplication()).Bool_lens) {
			
//			Core.flip(dRgba, mRgba, 1);//翻轉(flipCode:(0:水平軸翻轉(垂直翻轉)、1:垂直軸的翻轉(水平翻轉)、-1:兩軸的翻轉))
			
			if (((Opencv_Application)getApplicationContext()).Int_frontlens_adjustment == 1) {	
				
				Core.flip(dRgba, mRgba, 0);//翻轉(flipCode:(0:水平軸翻轉(垂直翻轉))
			
			} else if (((Opencv_Application)getApplicationContext()).Int_frontlens_adjustment == 2) {
				
				Core.flip(dRgba, mRgba, 1);//翻轉(flipCode:(1:垂直軸的翻轉(水平翻轉))
			
			} else {
				
				Core.flip(dRgba, mRgba, -1);//翻轉(flipCode:(-1:兩軸的翻轉))
			}
									
		}
		
		matOfRectTmp = new MatOfRect();
		mJavaDetectorFace.detectMultiScale(dRgba, matOfRectTmp, 1.1, 6, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                new Size((dRgba.rows() * 0.1), (dRgba.rows() * 0.1)),
                new Size());
		rectArrayFace = matOfRectTmp.toArray();		
		
		
		if(rectArrayFace.length != 0){
//			for (int i = 0; i < rectArrayFace.length; i++){
			for (int i = 0; i < 1; i++){
				
				x = rectArrayFace[i].x;
				y = rectArrayFace[i].y;
				width = rectArrayFace[i].width;
				height = rectArrayFace[i].height;
//				Log.e("rectArrayTmp["+i+"]", String.valueOf(x+", "+y+", "+width+", "+height));
				
				boolean findEye = true;
				boolean findNose = true;
				boolean findMouth = true;				
				
				final Mat matDraw = new Mat();
				Mat matRoiEye = new Mat();
				Mat matRoiNose = new Mat();
				Mat matRoiMouth = new Mat();
				
				Rect[] rectArrayTmp;
				
				final Rect rectRoiMaster = new Rect(x, y, width, height);
				
				// Rect Roi Setting
				Rect rectRoiEye = new Rect(
						(int) (x + (width * 0.0))	, (int) (y + (height * 0.25)),
						(int) (width * 1.0)			, (int) (height * 0.35));
				
				Rect rectRoiNose = new Rect(
						(int) (x + (width * 0.25))	, (int) (y + (height * 0.3)),
						(int) (width * 0.5)			, (int) (height * 0.65));				
				
				Rect rectRoiMouth = new Rect(
						(int) (x + (width * 0.2))	, (int) (y + (height * 0.7)),
						(int) (width * 0.6)			, (int) (height * 0.3));
				
				
				// Mat Roi Cut
				dRgba.submat(rectRoiMaster).copyTo(matDraw);
				dRgba.submat(rectRoiEye).copyTo(matRoiEye);
				dRgba.submat(rectRoiNose).copyTo(matRoiNose);
				dRgba.submat(rectRoiMouth).copyTo(matRoiMouth);
				
				/*
				// Test Roi
				Imgproc.cvtColor(matRoiEye, matRoiEye, Imgproc.COLOR_RGBA2GRAY);
				Imgproc.Canny(matRoiEye, matRoiEye, 128, 255, 3, false);
				Imgproc.cvtColor(matRoiEye, matRoiEye, Imgproc.COLOR_GRAY2RGBA);
				*/

				// 眼睛
				matOfRectTmp = new MatOfRect();
				mJavaDetectorEye.detectMultiScale(matRoiEye, matOfRectTmp, 1.1, 3, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
		                new Size(height * 0.09, height * 0.09), new Size(width, height));
				rectArrayTmp = matOfRectTmp.toArray();		
				if(rectArrayTmp.length == 2){
					for (int j = 0; j < 2; j++){
						if(rectArrayTmp[j].x > (width / 2)){							
							pointCenterEyeRight = new Point(
									(rectArrayTmp[j].x + (width * 0.0) + ((rectArrayTmp[j].br().x - rectArrayTmp[j].tl().x) / 2)),
									(rectArrayTmp[j].y + (height * 0.25) + ((rectArrayTmp[j].br().y - rectArrayTmp[j].tl().y) / 2)));
							Core.circle(matDraw, pointCenterEyeRight, 3, new Scalar(255, 255, 0, 255), -1);
//							Core.rectangle(matDraw, 
//									new Point((rectArrayTmp[j].tl().x + (width * 0.0)), (rectArrayTmp[j].tl().y + (height * 0.25))),
//									new Point((rectArrayTmp[j].br().x + (width * 0.0)), (rectArrayTmp[j].br().y + (height * 0.25))),
//									new Scalar(0, 255, 255, 255), 3);
						}else{
							pointCenterEyeLeft = new Point(
									(rectArrayTmp[j].x + (width * 0.0) + ((rectArrayTmp[j].br().x - rectArrayTmp[j].tl().x) / 2)),
									(rectArrayTmp[j].y + (height * 0.25) + ((rectArrayTmp[j].br().y - rectArrayTmp[j].tl().y) / 2)));
							Core.circle(matDraw, pointCenterEyeLeft, 3, new Scalar(255, 255, 0, 255), -1);
//							Core.rectangle(matDraw, 
//									new Point((rectArrayTmp[j].tl().x + (width * 0.0)), (rectArrayTmp[j].tl().y + (height * 0.25))),
//									new Point((rectArrayTmp[j].br().x + (width * 0.0)), (rectArrayTmp[j].br().y + (height * 0.25))),
//									new Scalar(0, 0, 255, 255), 3);
							
						}
//						Core.rectangle(matDraw, 
//								new Point((rectArrayTmp[j].tl().x + (width * 0.0)), (rectArrayTmp[j].tl().y + (height * 0.25))),
//								new Point((rectArrayTmp[j].br().x + (width * 0.0)), (rectArrayTmp[j].br().y + (height * 0.25))),
//								new Scalar(0, 0, 255, 255), 3);
					}
				}else{
					findEye = false;
					Log.e("DetectorEye", "Not find eye");
				}
				
				
				// 鼻子
				matOfRectTmp = new MatOfRect();
				mJavaDetectorNose.detectMultiScale(matRoiNose, matOfRectTmp, 1.1, 6, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
		                new Size(height * 0.2, height * 0.2), new Size(width, height));
				rectArrayTmp = matOfRectTmp.toArray();		
				if(rectArrayTmp.length != 0){
					for (int j = 0; j < 1; j++){
						pointCenterNose = new Point(
								(rectArrayTmp[j].x + (width * 0.25) + ((rectArrayTmp[j].br().x - rectArrayTmp[j].tl().x) / 2)),
								(rectArrayTmp[j].y + (height * 0.25) + ((rectArrayTmp[j].br().y - rectArrayTmp[j].tl().y) / 2)));
						Core.circle(matDraw, pointCenterNose, 3, new Scalar(255, 255, 0, 255), -1);
//						Core.rectangle(matDraw, 
//								new Point((rectArrayTmp[j].tl().x + (width * 0.25)), (rectArrayTmp[j].tl().y + (height * 0.25))),
//								new Point((rectArrayTmp[j].br().x + (width * 0.25)), (rectArrayTmp[j].br().y + (height * 0.25))),
//								new Scalar(0, 255, 0, 255), 3);	
					}
				}else{
					findNose = false;
					Log.e("DetectorNose", "Not find nose");
				}
				
				
				// 嘴巴
				matOfRectTmp = new MatOfRect();
				mJavaDetectorMouth.detectMultiScale(matRoiMouth, matOfRectTmp, 1.1, 6, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
		                new Size(height * 0.05, height * 0.05), new Size(height * 0.7, height * 0.7));
				rectArrayTmp = matOfRectTmp.toArray();		
				if(rectArrayTmp.length != 0){
					for (int j = 0; j < 1; j++){
						pointCenterMouth = new Point(
								(rectArrayTmp[j].x + (width * 0.2) + ((rectArrayTmp[j].br().x - rectArrayTmp[j].tl().x) / 2)),
								(rectArrayTmp[j].y + (height * 0.7) + ((rectArrayTmp[j].br().y - rectArrayTmp[j].tl().y) / 2)));
						Core.circle(matDraw, pointCenterMouth, 3, new Scalar(255, 255, 0, 255), -1);
//						Core.rectangle(matDraw, 
//								new Point((rectArrayTmp[j].tl().x + (width * 0.2)), (rectArrayTmp[j].tl().y + (height * 0.7))),
//								new Point((rectArrayTmp[j].br().x + (width * 0.2)), (rectArrayTmp[j].br().y + (height * 0.7))),
//								new Scalar(255, 0, 0, 255), 3);
					}
				}else{
					findMouth = false;
					Log.e("DetectorMouth", "Not find mouth");
				}
							
				/*
				matRoiEye.copyTo(mRgba.submat(rectRoiEye));
				matRoiNose.copyTo(mRgba.submat(rectRoiNose));
				matRoiMouth.copyTo(mRgba.submat(rectRoiMouth));
				matDraw.copyTo(mRgba.submat(rectRoiMaster));
				*/
				
				if(findEye && findNose && findMouth){
					
					Core.rectangle(mRgba, rectArrayFace[i].tl(), rectArrayFace[i].br(), new Scalar(255, 0, 255, 255), 3);
					
//					Point pointCenterface = new Point(
//							(rectArrayFace[i].x  + ((rectArrayFace[i].br().x - rectArrayFace[i].tl().x) / 2)),
//							(rectArrayFace[i].y  + ((rectArrayFace[i].br().y - rectArrayFace[i].tl().y) / 2)));
//					Core.circle(mRgba, pointCenterface, (height)/2, new Scalar(255, 0, 255, 255),3);
					
					/*
					Core.line(matDraw, pointCenterEyeLeft, pointCenterEyeRight, new Scalar(255, 255, 153, 255), 3);
					Core.line(matDraw, pointCenterEyeLeft, pointCenterMouth, new Scalar(255, 255, 153, 255), 3);
					Core.line(matDraw, pointCenterEyeLeft, pointCenterNose, new Scalar(255, 255, 153, 255), 3);
					Core.line(matDraw, pointCenterEyeRight, pointCenterNose, new Scalar(255, 255, 153, 255), 3);
					Core.line(matDraw, pointCenterEyeRight, pointCenterMouth, new Scalar(255, 255, 153, 255), 3);
					Core.line(matDraw, pointCenterNose, pointCenterMouth, new Scalar(255, 255, 153, 255), 3);
					*/
					
					matDraw.copyTo(mRgba.submat(rectRoiMaster));
					
					/*
					final double m0 = (pointCenterEyeRight.y - pointCenterEyeLeft.y) / (pointCenterEyeRight.x - pointCenterEyeLeft.x);
					final double m1 = (pointCenterMouth.y - pointCenterEyeLeft.y) / (pointCenterMouth.x - pointCenterEyeLeft.x);
					final double m2 = (pointCenterNose.y - pointCenterEyeLeft.y) / (pointCenterNose.x - pointCenterEyeLeft.x);
					final double m3 = (pointCenterEyeRight.y - pointCenterNose.y) / (pointCenterEyeRight.x - pointCenterNose.x);
					final double m4 = (pointCenterMouth.y - pointCenterEyeRight.y) / (pointCenterMouth.x - pointCenterEyeRight.x);
					final double m5 = (pointCenterMouth.y - pointCenterNose.y) / (pointCenterMouth.x - pointCenterNose.x);
					*/			
					
					Thread thread = new Thread() {
					    public void run() {
					        runOnUiThread(new Runnable() {
					            @Override
					            public void run() {
					            	
					            	/*
					            	textView0.setText(String.valueOf(m0));
					            	textView1.setText(String.valueOf(m1));
					            	textView2.setText(String.valueOf(m2));
					            	textView3.setText(String.valueOf(m3));
					            	textView4.setText(String.valueOf(m4));
					            	textView5.setText(String.valueOf(m5));
					            	*/
					            	
					            	
//					            	imageView1.setImageResource(R.drawable.face01);
//					            	imageView2.setImageResource(R.drawable.why);
					            	
					            	
					            	// 人臉辨識展示
					            	Mat matTmp0 = new Mat();
//					            	Imgproc.cvtColor(matDraw, matTmp0, Imgproc.COLOR_RGBA2GRAY);
//					            	Imgproc.threshold(matTmp0, matTmp0, 100, 255, Imgproc.CV_WARP_INVERSE_MAP);
					            	Imgproc.resize(matDraw, matTmp0, new Size(300, 300));
					            	Bitmap bitmapTmp0 = Bitmap.createBitmap(300, 300, Config.RGB_565);
					        		Utils.matToBitmap(matTmp0, bitmapTmp0);
					        		mBitmap2 = bitmapTmp0;
//					        		mImage_Capture.setImageBitmap(mBitmap2);
					        		tv_matTmp.setText("matTmp: " +  String.valueOf(matTmp0.cols() + " ' " + matTmp0.rows()));
//					        		SaveImage(matTmp0);
					            	
					            	// 開啟截取Button顯示
					        		if (((Opencv_Application) getApplication()).Bool_opensave) {					        			

					        			mImage_Capture.setImageBitmap(mBitmap2);
						        		
					        		}        	
					            	
					        		// 開啟辨識Button顯示
					        		if (((Opencv_Application)getApplication()).Bool_Identification) {
					        			
					        			sqlite.Inquire("1"); // 辨識
					        		}
					        		
					        		// 影像角度轉換
//					        		Mat src = new Mat();
//					        		matDraw.copyTo(src);
//					        		Mat dst2 = new Mat();
//					        	    double angle = 180 - angleOf(pointCenterEyeRight, pointCenterEyeLeft);
//					        	    double scale = 1;						                
//					        	    Mat rot_mat = Imgproc.getRotationMatrix2D(pointCenterEyeRight, angle, scale);
//					        	    Imgproc.warpAffine(src, dst2, rot_mat, dst2.size());
//					        	    mBitmap2 = Bitmap.createBitmap(dst2.cols(), dst2.rows(), Config.RGB_565);
//					        		Utils.matToBitmap(dst2, mBitmap2);
//					        		mImage_Capture.setImageBitmap(mBitmap2);
					        		
					        		/*
					            	Mat matTmp1 = new Mat();
					            	Bitmap bitmapTmp1 = BitmapFactory.decodeResource(getResources(), R.drawable.my);
					            	Utils.bitmapToMat(bitmapTmp1, matTmp1);
					            	
					            	Imgproc.cvtColor(matTmp0, matTmp0, Imgproc.COLOR_BGR2GRAY);
					        		Imgproc.cvtColor(matTmp1, matTmp1, Imgproc.COLOR_BGR2GRAY);
					        		
					            	matTmp0.convertTo(matTmp0, CvType.CV_32F);
					            	matTmp1.convertTo(matTmp1, CvType.CV_32F);
					        		target = Imgproc.compareHist(matTmp0, matTmp1, Imgproc.CV_COMP_CORREL);
					        		
					        		textView0.setText("target: " +  String.valueOf(target));
					        		*/
					            }
					        });
					    }
					};
					thread.start();
				}
			}
		}
		return mRgba;
	}
	
	
	
	public static double angleOf(Point p1, Point p2) {
	    // NOTE: Remember that most math has the Y axis as positive above the X.
	    // However, for screens we have Y as positive below. For this reason, 
	    // the Y values are inverted to get the expected results.
	    final double deltaY = (p1.y - p2.y);
	    final double deltaX = (p2.x - p1.x);
	    final double result = Math.toDegrees(Math.atan2(deltaY, deltaX)); 
	    return (result < 0) ? (360d + result) : result;
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
 
		Log.v("onKeyDown()", "onKeyDown()~~");		
		((Opencv_Application)getApplication()).Bool_lens = false;
		((Opencv_Application)getApplication()).Bool_Identification = false;   
		((Opencv_Application)getApplicationContext()).Int_frontlens_adjustment = 0;
		((Opencv_Application)getApplication()).Bool_opensave = false;
        
        return super.onKeyDown(keyCode, event);
        
    }
}
