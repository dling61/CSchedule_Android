package com.e2wstudy.cschedule;

import java.util.ArrayList;
import java.util.List;

import com.e2wstude.schedule.interfaces.ActvityInterface;
import com.e2wstude.schedule.interfaces.ContactInterface;
import com.e2wstude.schedule.interfaces.ScheduleInterface;
import com.e2wstudy.cschedule.adapter.MyPagerAdapter;
import com.e2wstudy.cschedule.adapter.MyTabFactory;
import com.e2wstudy.cschedule.fragments.AccountFragment;
import com.e2wstudy.cschedule.fragments.ActivityFragment;
import com.e2wstudy.cschedule.fragments.ContactFragment;
import com.e2wstudy.cschedule.fragments.ScheduleFragment;
import com.e2wstudy.cschedule.net.WebservicesHelper;
import com.e2wstudy.cschedule.views.ConfirmDialog;
import com.e2wstudy.cschedule.views.CustomViewPager;
import com.e2wstudy.cschedule.views.LoadingPopupViewHolder;
import com.e2wstudy.cschedule.views.MenuAppView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

/**
 * @author Huyen
 * 
 */
public class CategoryTabActivity extends FragmentActivity implements
		OnTabChangeListener, OnPageChangeListener {
	MyPagerAdapter pageAdapter;
	private static CustomViewPager mViewPager;
	private static TabHost mTabHost;
	Context mContext;
	// HorizontalScrollView horizontalView;
	public MenuAppView menuApp;
	public static CategoryTabActivity sharedTab;
	public static int currentPage = 0;
	boolean isActivityDownloadDone = false;
	boolean isScheduleDownloadDone = false;
	public static int TAB_SCHEDULE = 0;
	public static int TAB_ACTIVITY = 2;
	public static LoadingPopupViewHolder loadingPopup;
	public static final int DIALOG_LOADING_THEME = android.R.style.Theme_Translucent_NoTitleBar;
	public static boolean flag_schedule = false;
	public static boolean flag_activity = false;
	public static boolean flag_contact = false;
	ScheduleFragment schedule;
	ContactFragment contact;
	ActivityFragment activity;
	AccountFragment account;

	static CategoryTabActivity getTab(Context context) {
		if (sharedTab == null) {
			sharedTab = new CategoryTabActivity();
		}
		return sharedTab;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.category_tab_view);
		mContext = this;
		schedule = new ScheduleFragment();
		contact = new ContactFragment();
		contact.setInSideTab(true);
		activity = new ActivityFragment();
		account = new AccountFragment();
		loadingPopup = new LoadingPopupViewHolder(mContext,
				DIALOG_LOADING_THEME);
		loadingPopup.setCancelable(false);

		mViewPager = (CustomViewPager) findViewById(R.id.viewpager);
		menuApp = (MenuAppView) findViewById(R.id.menuTop);
		// Tab Initialization
		initialiseTabHost();
		initData();

		// Fragments and ViewPager Initialization
		List<Fragment> fragments = getFragments();
		pageAdapter = new MyPagerAdapter(getSupportFragmentManager(), fragments);
		mViewPager.setAdapter(pageAdapter);
		mViewPager.setOnPageChangeListener(CategoryTabActivity.this);
		mViewPager.setOffscreenPageLimit(4);
		try {
			IntentFilter filterRefreshUpdate = new IntentFilter();
			filterRefreshUpdate.addAction("goToActivity");
			registerReceiver(goToActivity, filterRefreshUpdate);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void onDestroy() {
		try {
			unregisterReceiver(goToActivity);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	};

	/**
	 * receiver when have no activity
	 * */
	BroadcastReceiver goToActivity = new BroadcastReceiver() {
		public void onReceive(Context arg0, Intent arg1) {
			moveToPage(TAB_ACTIVITY);
		}
	};

	private void initData() {
		// get all data after that, go to tab
		showLoading(mContext);
		WebservicesHelper ws = new WebservicesHelper(mContext);
		ws.getAllActivitys(activityInterface,scheduleInterface);
		ws.getParticipantsFromWeb(contactInterface);
	}

	ScheduleInterface scheduleInterface=new ScheduleInterface() {
		
		@Override
		public void onError() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onComplete() {
			schedule.setInterface(this);
			
		}
	};
	
	ContactInterface contactInterface=new ContactInterface() {
		
		@Override
		public void onError() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onComplete() {
			// TODO Auto-generated method stub
			contact.setInterface(this);
		}
	};
	
	ActvityInterface activityInterface = new ActvityInterface() {

		@Override
		public void onError(String error) {
			// TODO Auto-generated method stub
			try {
				dimissDialog();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void onComplete() {
			try {
				activity.setInterface(this);
				dimissDialog();				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	// show loading
	public void showLoading(Context mContext) {
		if (loadingPopup == null) {
			loadingPopup = new LoadingPopupViewHolder(mContext,
					DIALOG_LOADING_THEME);
		}
		loadingPopup.setCancelable(false);
		loadingPopup.show();
	}

	public void dimissDialog() {
		if (loadingPopup != null && loadingPopup.isShowing()) {
			loadingPopup.dismiss();
		}
	}

	// Method to add a TabHost
	private static void addTabRight(CategoryTabActivity activity,
			TabHost tabHost, TabHost.TabSpec tabSpec, String title, int imageId) {
		MyTabFactory myTab = new MyTabFactory(activity, imageId);
		tabSpec.setContent(myTab);
		View v = myTab.createTabContent(title);
		tabSpec.setIndicator(v);
		tabHost.addTab(tabSpec);
	}

	// Manages the Tab changes, synchronizing it with Pages
	public void onTabChanged(String tag) {
		int pos = this.mTabHost.getCurrentTab();
		mViewPager.setCurrentItem(pos);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	// Manages the Page changes, synchronizing it with Tabs
	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		int pos = mViewPager.getCurrentItem();
		currentPage = pos;
		this.mTabHost.setCurrentTab(pos);
		View tabView = mTabHost.getTabWidget().getChildAt(pos);
		// if (tabView != null) {
		// final int width = horizontalView.getWidth();
		// final int scrollPos = tabView.getLeft()
		// - (width - tabView.getWidth()) / 2;
		// horizontalView.scrollTo(scrollPos, 0);
		// } else {
		// horizontalView.scrollBy(positionOffsetPixels, 0);
		// }

	}

	public static void moveToPage(int page) {

		mViewPager.setCurrentItem(page);
	}

	public static void moveToTab(int page) {
		int pos = mTabHost.getCurrentTab();
		mViewPager.setCurrentItem(pos);
	}

	@Override
	public void onPageSelected(int arg0) {

	}

	private List<Fragment> getFragments() {
		List<Fragment> fList = new ArrayList<Fragment>();

		// TODO Put here your Fragments

		fList.add(schedule);
		fList.add(contact);
		fList.add(activity);
		fList.add(account);

		return fList;
	}

	// Tabs Creation
	private void initialiseTabHost() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		CategoryTabActivity.addTabRight(CategoryTabActivity.this,
				this.mTabHost,
				this.mTabHost.newTabSpec("Tab3").setIndicator("Tab3"),
				"Schedule", R.drawable.btn_schedule_selector);
		CategoryTabActivity.addTabRight(CategoryTabActivity.this,
				this.mTabHost,
				this.mTabHost.newTabSpec("Tab2").setIndicator("Tab2"), mContext
						.getResources().getString(R.string.contact),
				R.drawable.btn_contact_selector);
		// TODO Put here your Tabs
		CategoryTabActivity.addTabRight(CategoryTabActivity.this,
				this.mTabHost,
				this.mTabHost.newTabSpec("Tab1").setIndicator("Tab1"),
				"Activity", R.drawable.btn_activity_selector);

		CategoryTabActivity.addTabRight(CategoryTabActivity.this,
				this.mTabHost,
				this.mTabHost.newTabSpec("Tab3").setIndicator("Tab3"),
				"Account", R.drawable.btn_account_selector);
		mTabHost.getTabWidget().setDividerDrawable(R.drawable.ic_menu_line);
		mTabHost.setOnTabChangedListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		final ConfirmDialog dialog = new ConfirmDialog(
				CategoryTabActivity.this, mContext.getResources().getString(
						R.string.sure_to_exit));
		dialog.show();
		dialog.btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		dialog.btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				// deleteDatabase(DatabaseHelper.DB_NAME);
				System.exit(0);
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		moveToPage(currentPage);

	}

}
