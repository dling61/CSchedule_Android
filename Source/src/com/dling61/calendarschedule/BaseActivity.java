package com.dling61.calendarschedule;

import com.dling61.calendarschedule.utils.Utils;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		overridePendingTransition(R.anim.animation_enter,
//			      R.anim.animation_leave);
//		Utils.pushRightToLeft(BaseActivity.this);
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Utils.postLeftToRight(BaseActivity.this);
	}

}