package com.opencv_application;

import android.app.Application;

public class Opencv_Application extends Application{
	
	/**--ImageActivity--**/
	public boolean Bool_lens = false;  // 切換鏡頭
	public boolean Bool_Identification = false; // 辨視開關
	public int Int_frontlens_adjustment = 0; // 前置鏡頭調整開關
	public boolean Bool_opensave = false; // 開啟擷取開關

}
