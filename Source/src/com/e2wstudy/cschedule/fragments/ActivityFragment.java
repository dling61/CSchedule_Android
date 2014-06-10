package com.e2wstudy.cschedule.fragments;

import java.util.ArrayList;

import com.e2wstudy.cschedule.AddNewActivity;
import com.e2wstudy.cschedule.adapter.ActivityAdapter;
import com.e2wstudy.cschedule.db.DatabaseHelper;
import com.e2wstudy.cschedule.models.MyActivity;
import com.e2wstudy.cschedule.utils.CommConstant;
import com.e2wstudy.cschedule.utils.Utils;
import com.e2wstudy.cschedule.views.ActivityView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * @author Huyen
 * @category show all activity of login account
 * 
 */
public class ActivityFragment extends Fragment implements OnClickListener {
	ActivityView view;
	Context mContext;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();
		onClickListener();
	}

	public static ActivityFragment getInstance() {
		return ActivityFragment.getInstance();
	}

	/**
	 * On click listener all views
	 * */
	private void onClickListener() {
		// registerForContextMenu(view.listview);
		view.btn_add_activity.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == view.btn_add_activity) {
			
			Intent intent = new Intent(mContext, AddNewActivity.class);
			intent.putExtra("type", DatabaseHelper.NEW);
			mContext.startActivity(intent);
			Utils.slideUpDown(mContext);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = new ActivityView(getActivity());
		this.view = (ActivityView) view;
		return view;
	}

	BroadcastReceiver changeAcitivityList = new BroadcastReceiver() {
		public void onReceive(Context arg0, Intent arg1) {
			DatabaseHelper dbHelper = DatabaseHelper
					.getSharedDatabaseHelper(mContext);
			ArrayList<MyActivity> activities = dbHelper.getActivities();
			if (activities != null && activities.size() > 0) {
				ActivityAdapter activityAdapter = new ActivityAdapter(mContext,
						activities);
				view.listview.setAdapter(activityAdapter);
				view.listview.setVisibility(View.VISIBLE);
				view.layout_no_activity.setVisibility(View.GONE);
			} else {
				view.listview.setVisibility(View.GONE);
				view.layout_no_activity.setVisibility(View.VISIBLE);
			}
		}
	};

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		IntentFilter filterRefreshUpdate = new IntentFilter();
		filterRefreshUpdate.addAction(CommConstant.DELETE_ACTIVITY_COMPLETE);
		filterRefreshUpdate.addAction(CommConstant.ACTIVITY_DOWNLOAD_SUCCESS);
		filterRefreshUpdate.addAction(CommConstant.UPDATE_SCHEDULE);
		filterRefreshUpdate
				.addAction(CommConstant.GET_SHARED_MEMBER_ACTIVITY_COMPLETE);
		filterRefreshUpdate
				.addAction(CommConstant.ADD_SHARED_MEMBER_FROM_ACTIVITY);
		filterRefreshUpdate.addAction(CommConstant.ADD_CONTACT_SUCCESS);
		getActivity()
				.registerReceiver(changeAcitivityList, filterRefreshUpdate);
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		getActivity().unregisterReceiver(changeAcitivityList);
	}
}