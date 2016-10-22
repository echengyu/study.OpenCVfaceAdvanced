package study.OpenCVfaceComparison;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImageActivity extends Activity implements CvCameraViewListener2 {
	private static final String TAG  = "OCVSample::Activity";
	private ScanTool mOpenCvCameraView;

	private boolean onCameraViewStarted = true;
	private List<android.hardware.Camera.Size> mResolutionList;
	private android.hardware.Camera.Size resolution = null;
	
	private File mCascadeFileFace;
	private File mCascadeFileEye;
	private File mCascadeFileNose;
	private File mCascadeFileMouth;
	
	private CascadeClassifier mJavaDetectorFace;
	private CascadeClassifier mJavaDetectorEye;  
	private CascadeClassifier mJavaDetectorNose; 
	private CascadeClassifier mJavaDetectorMouth;
		
	private ImageView imageView0, imageView1, imageView2;
	private TextView textView0, textView1, textView2, textView3, textView4, textView5;
	
	private Point pointCenterEyeLeft = new Point();
	private Point pointCenterEyeRight = new Point();
	private Point pointCenterNose = new Point();
	private Point pointCenterMouth = new Point();
    
	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				
				try {
					// load cascade file from application resources
					InputStream isFace 			= getResources().openRawResource(R.raw.lbpcascade_frontalface);
					InputStream isEye 			= getResources().openRawResource(R.raw.haarcascade_eye);
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
				mOpenCvCameraView.enableView();
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
		mOpenCvCameraView = (ScanTool) findViewById(R.id.picture_view0);
		mOpenCvCameraView.setCameraIndex(1);
		mOpenCvCameraView.setCvCameraViewListener(this);
		
		imageView0 = (ImageView) findViewById(R.id.imageView0);
		imageView1 = (ImageView) findViewById(R.id.imageView1);
		imageView2 = (ImageView) findViewById(R.id.imageView2);
		textView0 = (TextView) findViewById(R.id.textView0);
		textView1 = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		if(onCameraViewStarted == true) {
			onCameraViewStarted = false;
			mResolutionList = mOpenCvCameraView.getResolutionList();
			for(int i=0; i<mResolutionList.size(); i++) {
				if(mResolutionList.get(i).width == 640) {
					resolution = mResolutionList.get(i);
					mOpenCvCameraView.setResolution(resolution);
					resolution = mOpenCvCameraView.getResolution();
					String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
					Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
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
					
					Thread t = new Thread() {
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
					            	
					            	
					            	imageView1.setImageResource(R.drawable.my);
//					            	imageView2.setImageResource(R.drawable.why);
					            	
								            	
					            	// 人臉辨識展示
					            	Mat matTmp0 = new Mat();
					            	Imgproc.resize(matDraw, matTmp0, new Size(100, 100));
					            	Bitmap bitmapTmp0 = Bitmap.createBitmap(100, 100, Config.RGB_565);
					        		Utils.matToBitmap(matTmp0, bitmapTmp0);
					        		imageView0.setImageBitmap(bitmapTmp0);					         				            	
					            	textView5.setText("matTmp: " +  String.valueOf(matTmp0.cols() + "' " + matTmp0.rows()));
//					        		SaveImage(matTmp);
					            						        		
					        		// 影像角度轉換
					        		Mat src = new Mat();
					        		matDraw.copyTo(src);
					        		Mat dst2 = new Mat();
					        	    double angle = 180 - angleOf(pointCenterEyeRight, pointCenterEyeLeft);
					        	    double scale = 1;						                
					        	    Mat rot_mat = Imgproc.getRotationMatrix2D(pointCenterEyeRight, angle, scale);
					        	    Imgproc.warpAffine(src, dst2, rot_mat, dst2.size());
					            	Bitmap bitmapTmp4 = Bitmap.createBitmap(dst2.cols(), dst2.rows(), Config.RGB_565);
					        		Utils.matToBitmap(dst2, bitmapTmp4);
					        		imageView2.setImageBitmap(bitmapTmp4);
					        		
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
					t.start();
				}
			}
		}
		return mRgba;
	}
	
	public void SaveImage (Mat mat) {

		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2BGR, 3);

		Log.i(TAG,"onTouch event");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String currentDateandTime = sdf.format(new Date());
		String fileName = Environment.getExternalStorageDirectory().getPath() +
		                  "/sample_picture_" + "faceTmpNew" + ".jpg";

		Boolean bool = Highgui.imwrite(fileName, mat);

		if (bool)
			Log.i(TAG, "SUCCESS writing image to external storage");
		else
			Log.i(TAG, "Fail writing image to external storage");
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
}
