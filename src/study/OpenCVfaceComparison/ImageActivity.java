package study.OpenCVfaceComparison;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ListIterator;

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
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class ImageActivity extends Activity implements CvCameraViewListener2 {
	private static final String TAG  = "OCVSample::Activity";
	private ScanTool mOpenCvCameraView;

	private boolean onCameraViewStarted = true;
	private List<android.hardware.Camera.Size> mResolutionList;
	private android.hardware.Camera.Size resolution = null;
	private SubMenu mResolutionMenu;
	private MenuItem[] mResolutionMenuItems;
	
	private File mCascadeFile;
	
	private File mCascadeFileFace;
	private File mCascadeFileEye;
	private File mCascadeFileNose;
	private File mCascadeFileMouth;
	
	private CascadeClassifier mJavaDetectorFace;
	private CascadeClassifier mJavaDetectorEye;  
	private CascadeClassifier mJavaDetectorNose; 
	private CascadeClassifier mJavaDetectorMouth;
	
	
	
	
	private CascadeClassifier mJavaDetector;
	
	private double scaleFactor = 2.0;
	private int minNeighbors = 6;
	private int flags = 2;
	private int minSize = 0;
	private SeekBar seekBar3;
	private TextView seekBarValue3;
	private boolean onProgressChanged = true;
	
	private ImageView imageView0;
	private Bitmap bt1;
	private Bitmap bt3;
	
	/*
	private Handler mHandler = new Handler();
	private Runnable mRunnable= new Runnable(){
        @Override
        public void run(){	
        	seekBar3.setMax(minSize);
        	minSize = minSize / 6;
        	seekBar3.setProgress(minSize);
        	seekBarValue3.setText(String.valueOf(minSize));
        	onProgressChanged = false;
        	mHandler.removeCallbacks(mRunnable);
        }
    };
    */
    
	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				
				try {
					// load cascade file from application resources
					InputStream isFace 	= getResources().openRawResource(R.raw.lbpcascade_frontalface);
					InputStream isEye 	= getResources().openRawResource(R.raw.haarcascade_eye);
					InputStream isNose 	= getResources().openRawResource(R.raw.haarcascade_mcs_nose);
					InputStream isMouth = getResources().openRawResource(R.raw.haarcascade_mcs_mouth);

					File cascadeDirFace 	= getDir("cascade", Context.MODE_PRIVATE);
					File cascadeDirEye 		= getDir("cascade", Context.MODE_PRIVATE);
					File cascadeDirNose 	= getDir("cascade", Context.MODE_PRIVATE);
					File cascadeDirMouth 	= getDir("cascade", Context.MODE_PRIVATE);

					mCascadeFileFace 	= new File(cascadeDirFace, "lbpcascade_frontalface.xml");
					mCascadeFileEye 	= new File(cascadeDirEye, "haarcascade_eye.xml");
					mCascadeFileNose 	= new File(cascadeDirNose, "haarcascade_mcs_nose.xml");
					mCascadeFileMouth	= new File(cascadeDirMouth, "haarcascade_mcs_mouth.xml");

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
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from " + mCascadeFileFace.getAbsolutePath());
					
					if (mJavaDetectorEye.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from " + mCascadeFileEye.getAbsolutePath());
					
					if (mJavaDetectorNose.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from " + mCascadeFileNose.getAbsolutePath());
					
					if (mJavaDetectorMouth.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
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
		mOpenCvCameraView.setCvCameraViewListener(this);
		
		
		/*
		TextView textViewName0 = (TextView)findViewById(R.id.textViewName0);
		textViewName0.setText("ScaleFactor");
		SeekBar seekBar0 = (SeekBar)findViewById(R.id.seekBar0);
		seekBar0.setMax(100);
		seekBar0.setProgress((int) (scaleFactor * 10));
		final TextView seekBarValue0 = (TextView)findViewById(R.id.textViewStatus0);
		seekBarValue0.setText(String.valueOf(scaleFactor));
		seekBar0.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
				// TODO Auto-generated method stub
				if(progress <= 10){
					seekBarValue0.setText(String.valueOf(1.1));
					scaleFactor = 1.1f;
					seekBar.setProgress(11);
				}else{
					seekBarValue0.setText(String.valueOf(progress / 10.0f));
					scaleFactor = progress / 10.0f;
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
		});
		
		TextView textViewName1 = (TextView)findViewById(R.id.textViewName1);
		textViewName1.setText("MinNeighbors");
		SeekBar seekBar1 = (SeekBar)findViewById(R.id.seekBar1);
		seekBar1.setMax(10);
		seekBar1.setProgress(minNeighbors);
		final TextView seekBarValue1 = (TextView)findViewById(R.id.textViewStatus1);
		seekBarValue1.setText(String.valueOf(minNeighbors));
		seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
				// TODO Auto-generated method stub
				seekBarValue1.setText(String.valueOf(progress));
				minNeighbors = progress;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
		});		

		TextView textViewName2 = (TextView)findViewById(R.id.textViewName2);
		textViewName2.setText("Flags");
		SeekBar seekBar2 = (SeekBar)findViewById(R.id.seekBar2);
		seekBar2.setMax(10);
		seekBar2.setProgress(flags);
		final TextView seekBarValue2 = (TextView)findViewById(R.id.textViewStatus2);
		seekBarValue2.setText(String.valueOf(flags));
		seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
				// TODO Auto-generated method stub
				seekBarValue2.setText(String.valueOf(progress));
				flags = progress;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
		});
			
		TextView textViewName3 = (TextView)findViewById(R.id.textViewName3);
		textViewName3.setText("MinSize");
		seekBar3 = (SeekBar)findViewById(R.id.seekBar3);
		seekBarValue3 = (TextView)findViewById(R.id.textViewStatus3);
		seekBarValue3.setText(String.valueOf(minSize));
		seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
				// TODO Auto-generated method stub
				if(!onProgressChanged){
					if(progress < 1){
						seekBar3.setProgress(1);
						seekBarValue3.setText(String.valueOf(1));
						minSize = 1;
					}else{
						seekBarValue3.setText(String.valueOf(progress));
						minSize = progress;
					}
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
		});
		*/
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
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "called onCreateOptionsMenu");

		mResolutionMenu = menu.addSubMenu("Resolution");
		mResolutionList = mOpenCvCameraView.getResolutionList();
		mResolutionMenuItems = new MenuItem[mResolutionList.size()];
		ListIterator<android.hardware.Camera.Size> resolutionItr = mResolutionList.listIterator();
		int idx = 0;
		while(resolutionItr.hasNext()) {
			android.hardware.Camera.Size element = resolutionItr.next();
			mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
			                            Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
			idx++;
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

		if (item.getGroupId() == 2) {
			int id = item.getItemId();
			android.hardware.Camera.Size resolution = mResolutionList.get(id);
			mOpenCvCameraView.setResolution(resolution);
			resolution = mOpenCvCameraView.getResolution();
			String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
			Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
			onProgressChanged = true;
		}
		return true;
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Mat mRgba = inputFrame.rgba();
		Mat dRgba = inputFrame.rgba();
		
		/*
		if(onProgressChanged){
			minSize = inputFrame.rgba().rows();      	
			mHandler.post(mRunnable);
		}
		*/
		
		
//		Mat matPicture = new Mat();
//		bt1 = BitmapFactory.decodeResource(getResources(), R.drawable.face);
//		Utils.bitmapToMat(bt1, matPicture);
		
//		Log.e("matPicture", String.valueOf(matPicture.cols()+", "+matPicture.rows()));		
//		
//		MatOfRect faces = new MatOfRect();
//		
//		mJavaDetector.detectMultiScale(matPicture, faces, scaleFactor, minNeighbors, flags, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
//                new Size(minSize, minSize), new Size());
//
//		Rect[] facesArray = faces.toArray();
//		
//		for (int i = 0; i < facesArray.length; i++)
//			Core.rectangle(matPicture, facesArray[i].tl(), facesArray[i].br(), new Scalar(255, 0, 255, 255), 3);
		
//		bt3 = Bitmap.createBitmap(matPicture.cols(), matPicture.rows(), Config.RGB_565);
//		Utils.matToBitmap(matPicture, bt3);
//		imageView0.setImageBitmap(bt3);
		
		MatOfRect matOfRectTmp;
		Rect[] rectArrayFace;
		int width;
		int height;
		int x;
		int y;
			
		// scaleFactor, minNeighbors, flags
		matOfRectTmp = new MatOfRect();
		mJavaDetectorFace.detectMultiScale(dRgba, matOfRectTmp, 2.0, 3, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                new Size(100, 100), new Size(450, 450));
		rectArrayFace = matOfRectTmp.toArray();		
//		for (int i = 0; i < rectArrayTmp.length; i++){
		
		if(rectArrayFace.length != 0){
		
			for (int i = 0; i < 1; i++){
				
				x = rectArrayFace[i].x;
				y = rectArrayFace[i].y;
				width = rectArrayFace[i].width;
				height = rectArrayFace[i].height;
				Log.e("rectArrayTmp["+i+"]", String.valueOf(x+", "+y+", "+width+", "+height));
				
				boolean findEye = true;
				boolean findNose = true;
				boolean findMouth = true;				
				
				Mat matDraw = new Mat();
				Mat matRoiEye = new Mat();
				Mat matRoiNose = new Mat();
				Mat matRoiMouth = new Mat();
				
				Rect[] rectArrayTmp;
				
				Rect rectRoiMaster = new Rect(x, y, width, height);
				
				Rect rectRoiEye = new Rect(
						(int) (x + (width * 0.0))	, (int) (y + (height * 0.25)),
						(int) (width * 1.0)			, (int) (height * 0.35));
				
				Rect rectRoiNose = new Rect(
						(int) (x + (width * 0.0))	, (int) (y + (height * 0.2)),
						(int) (width * 1.0)			, (int) (height * 0.5));				
				
				Rect rectRoiMouth = new Rect(
						(int) (x + (width * 0.2))	, (int) (y + (height * 0.7)),
						(int) (width * 0.6)			, (int) (height * 0.3));
				
				
				dRgba.submat(rectRoiMaster).copyTo(matDraw);
				dRgba.submat(rectRoiEye).copyTo(matRoiEye);
				dRgba.submat(rectRoiNose).copyTo(matRoiNose);
				dRgba.submat(rectRoiMouth).copyTo(matRoiMouth);
				
				
//				Imgproc.cvtColor(matRoiEye, matRoiEye, Imgproc.COLOR_RGBA2GRAY);
//				Imgproc.Canny(matRoiEye, matRoiEye, 128, 255, 3, false);
//				Imgproc.cvtColor(matRoiEye, matRoiEye, Imgproc.COLOR_GRAY2RGBA);
				
			
				matOfRectTmp = new MatOfRect();
				mJavaDetectorEye.detectMultiScale(matRoiEye, matOfRectTmp, 3.0, 6, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
		                new Size(30, 30), new Size(width, height));
				rectArrayTmp = matOfRectTmp.toArray();		
				if(rectArrayTmp.length == 2){
					for (int j = 0; j < 2; j++)
						Core.rectangle(matDraw, 
								new Point((rectArrayTmp[j].tl().x + (width * 0.0)), (rectArrayTmp[j].tl().y + (height * 0.25))),
								new Point((rectArrayTmp[j].br().x + (width * 0.0)), (rectArrayTmp[j].br().y + (height * 0.25))),
								new Scalar(0, 0, 255, 255), 3);
				}else{
					findEye = false;
					Log.e("DetectorEye", "Not find eye");
				}
				
				
				matOfRectTmp = new MatOfRect();
				mJavaDetectorNose.detectMultiScale(matRoiNose, matOfRectTmp, 5.0, 6, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
		                new Size(30, 30), new Size(width, height));
				rectArrayTmp = matOfRectTmp.toArray();		
				if(rectArrayTmp.length != 0){
					for (int j = 0; j < 1; j++)
						Core.rectangle(matDraw, 
								new Point((rectArrayTmp[j].tl().x + (width * 0.0)), (rectArrayTmp[j].tl().y + (height * 0.2))),
								new Point((rectArrayTmp[j].br().x + (width * 0.0)), (rectArrayTmp[j].br().y + (height * 0.2))),
								new Scalar(0, 255, 0, 255), 3);	
				}else{
					findNose = false;
					Log.e("DetectorNose", "Not find nose");
				}
				
				
				matOfRectTmp = new MatOfRect();
				mJavaDetectorMouth.detectMultiScale(matRoiMouth, matOfRectTmp, scaleFactor, minNeighbors, flags, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
		                new Size(height*0.1, height*0.1), new Size(height*0.7, height*0.7));
				rectArrayTmp = matOfRectTmp.toArray();		
				if(rectArrayTmp.length != 0){
					for (int j = 0; j < 1; j++)
						Core.rectangle(matDraw, 
								new Point((rectArrayTmp[j].tl().x + (width * 0.2)), (rectArrayTmp[j].tl().y + (height * 0.7))),
								new Point((rectArrayTmp[j].br().x + (width * 0.2)), (rectArrayTmp[j].br().y + (height * 0.7))),
								new Scalar(255, 0, 0, 255), 3);
				}else{
					findMouth = false;
					Log.e("DetectorMouth", "Not find mouth");
				}
				
				
//				matRoiEye.copyTo(mRgba.submat(rectRoiEye));
//				matRoiNose.copyTo(mRgba.submat(rectRoiNose));
//				matRoiMouth.copyTo(mRgba.submat(rectRoiMouth));
//				matDraw.copyTo(mRgba.submat(rectRoiMaster));
				
				
				if(findEye && findNose && findMouth){
					matDraw.copyTo(mRgba.submat(rectRoiMaster));
					Core.rectangle(mRgba, rectArrayFace[i].tl(), rectArrayFace[i].br(), new Scalar(255, 0, 255, 255), 3);
				}
			}
		}
		
		return mRgba;
	}
}
