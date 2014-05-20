package com.example.testdavilk;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {
	static {
		try {
			System.loadLibrary("hackdavilk");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		findViewById(R.id.text).setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				new Thread(new Runnable() {
//
//					@Override
//					public void run() {
//						String dexpath = "/sdcard/CtripSplashActivity.apk";
//						String dexoutputpath = "/sdcard/";
//						LoadAPK(null, dexpath, dexoutputpath);
//					}
//				}).start();
//			}
//		});
	}
//
//	public void LoadAPK(Bundle paramBundle, String dexpath, String dexoutputpath) {
//		ClassLoader localClassLoader = ClassLoader.getSystemClassLoader();
//		DexClassLoader localDexClassLoader = new DexClassLoader(dexpath, dexoutputpath, null, localClassLoader);
//		try {
//			PackageInfo plocalObject = getPackageManager().getPackageArchiveInfo(dexpath, PackageManager.GET_ACTIVITIES);
//
//			if ((plocalObject.activities != null) && (plocalObject.activities.length > 0)) {
//				int size = plocalObject.activities.length;
//				for (int i = 0; i < size; i++) {
//					String activityname = plocalObject.activities[i].name;
//					Log.e("sys", "activityname = " + activityname);
//					try {
//						Class localClass = localDexClassLoader.loadClass(activityname);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//			return;
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
