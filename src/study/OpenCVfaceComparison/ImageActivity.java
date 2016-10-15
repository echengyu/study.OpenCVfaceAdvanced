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
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.WindowManager;
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
	private CascadeClassifier mJavaDetector;
	
	private double scaleFactor = 1.1;
	private int minNeighbors = 6;
	private int flags = 2;
	private int minSize = 0;
	private SeekBar seekBar3;
	private TextView seekBarValue3;
	private boolean onProgressChanged = true;
	
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
    
	private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				
				try {
					// load cascade file from application resources
					InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
					if (mJavaDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

					cascadeDir.delete();

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
		setContentView(R.layout.image_manipulations_surface_view_4);
		mOpenCvCameraView = (ScanTool) findViewById(R.id.image_activity_view);
		mOpenCvCameraView.setCvCameraViewListener(this);
		
		TextView textViewName0 = (TextView)findViewById(R.id.textViewName0);
		textViewName0.setText("ScaleFactor");
		SeekBar seekBar0 = (SeekBar)findViewById(R.id.seekBar0);
		seekBar0.setMax(30);
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
				if(mResolutionList.get(i).width == 320) {
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
		Mat rgba = inputFrame.rgba();

		if(onProgressChanged){
			minSize = inputFrame.rgba().rows();      	
			mHandler.post(mRunnable);
		}
		
		MatOfRect faces = new MatOfRect();
		
		mJavaDetector.detectMultiScale(rgba, faces, scaleFactor, minNeighbors, flags, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                new Size(minSize, minSize), new Size());

		Rect[] facesArray = faces.toArray();
		
		for (int i = 0; i < facesArray.length; i++)
			Core.rectangle(rgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(255, 0, 255, 255), 3);
		
		return rgba;
	}
}
