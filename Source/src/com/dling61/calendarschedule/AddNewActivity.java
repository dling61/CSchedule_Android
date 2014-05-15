package com.dling61.calendarschedule;

import java.util.ArrayList;
import java.util.List;

import com.dling61.calendarschedule.adapter.SharedMemberAdapter;
import com.dling61.calendarschedule.adapter.TextViewBaseAdapter;
import com.dling61.calendarschedule.db.DatabaseHelper;
import com.dling61.calendarschedule.models.ActivityTable;
import com.dling61.calendarschedule.models.MyActivity;
import com.dling61.calendarschedule.models.OndutyTable;
import com.dling61.calendarschedule.models.ParticipantTable;
import com.dling61.calendarschedule.models.Schedule;
import com.dling61.calendarschedule.models.ScheduleTable;
import com.dling61.calendarschedule.models.SharedMemberTable;
import com.dling61.calendarschedule.models.Sharedmember;
import com.dling61.calendarschedule.net.WebservicesHelper;
import com.dling61.calendarschedule.utils.CommConstant;
import com.dling61.calendarschedule.utils.MyDate;
import com.dling61.calendarschedule.utils.SharedReference;
import com.dling61.calendarschedule.utils.Utils;
import com.dling61.calendarschedule.views.AddActivityView;
import com.dling61.calendarschedule.views.ParticipantInforDialog;
import com.dling61.calendarschedule.views.PopupDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

@SuppressLint("NewApi")
public class AddNewActivity extends Activity implements OnClickListener {
	private MyActivity thisActivity;
	private int composeType;
	private DatabaseHelper dbHelper;
	Context mContext;
	AddActivityView view;
	String[] alert_array = null;
	String[] timezone_array = null;
	String[] timezone_value_array = null;
	String[] repeat_array = null;

	int time_zone = 0;// timezone position
	int alert_type = 0;
	int repeat_type = 0;
	int type = -1;// type=repeat, alert, timezone
	static final int REPEAT = 0;
	static final int ALERT = 1;
	static final int TIMEZONE = 2;
	String activity_id = "";

	// shared role for privacy
	int shared_role = CommConstant.OWNER;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this.setContentView(R.layout.composeactivity);
		mContext = this;
		view = new AddActivityView(mContext);
		setContentView(view.layout);

		dbHelper = DatabaseHelper.getSharedDatabaseHelper(mContext);

		thisActivity = new MyActivity(dbHelper.getNextActivityID() + "",
				new SharedReference().getCurrentOwnerId(mContext), 0, 0, "",
				MyDate.transformLocalDateTimeToUTCFormat(MyDate
						.getCurrentDateTime()),
				MyDate.transformLocalDateTimeToUTCFormat(MyDate
						.getCurrentDateTime()), "", 0, 0);

		Intent myIntent = getIntent();
		composeType = myIntent.getIntExtra(CommConstant.TYPE, 3);
		view.et_new_activity_description.setFocusable(false);
		if (composeType == DatabaseHelper.NEW) {
			Log.i("next service id", "is " + dbHelper.getNextActivityID());
			thisActivity = new MyActivity(dbHelper.getNextActivityID() + "",
					new SharedReference().getCurrentOwnerId(mContext), 0, 0,
					"", MyDate.transformLocalDateTimeToUTCFormat(MyDate
							.getCurrentDateTime()),
					MyDate.transformLocalDateTimeToUTCFormat(MyDate
							.getCurrentDateTime()), "", 0, 0);
			view.layout_save.setVisibility(View.GONE);
			view.layout_next.setVisibility(View.VISIBLE);
		} else if (composeType == DatabaseHelper.EXISTED) {
			activity_id = myIntent.getStringExtra(CommConstant.ACTIVITY_ID);
			shared_role = myIntent.getIntExtra(CommConstant.ROLE,
					CommConstant.OWNER);

			thisActivity = dbHelper.getActivity(activity_id);
			// set visible when edit
			view.btn_add_paticipant.setVisibility(View.VISIBLE);
			view.btn_remove_activity.setVisibility(View.VISIBLE);
			view.layout_next.setVisibility(View.GONE);

			view.layout_save.setVisibility(View.VISIBLE);
			view.layout_next.setVisibility(View.GONE);
			hideKeyboard();
		}
		this.initViewValues();
		onClickListener();

		try {
			registerReceiver(activityGetSharedMemberComplete, new IntentFilter(
					CommConstant.GET_SHARED_MEMBER_ACTIVITY_COMPLETE));
			registerReceiver(deleteActivityComplete, new IntentFilter(
					CommConstant.DELETE_ACTIVITY_COMPLETE));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	//hide keyboard
	private void hideKeyboard()
	{
		 try
	        {
	            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	         }
	        catch (Exception e)
	        {
	            // Ignore exceptions if any
	                Log.e("KeyBoardUtil", e.toString(), e);
	        }
	}
	
	BroadcastReceiver deleteActivityComplete = new BroadcastReceiver() {
		public void onReceive(Context arg0, Intent arg1) {
			finish();
		}
	};

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		try {
			unregisterReceiver(deleteActivityComplete);
			unregisterReceiver(activityGetSharedMemberComplete);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		super.onPause();
	};

	/**
	 * get shared member of activity
	 * */
	private void setParticipantOfActivity() {
		if (activity_id != null && (!activity_id.equals(""))) {
			DatabaseHelper dbHelper = DatabaseHelper
					.getSharedDatabaseHelper(mContext);
			ArrayList<Sharedmember> list_participant = dbHelper
					.getParticipantsOfActivity(activity_id);
			final SharedMemberAdapter adapter = new SharedMemberAdapter(
					mContext, list_participant, false, false, true);
			view.list_participant.setAdapter(adapter);
			Utils.setListViewHeightBasedOnChildren(view.list_participant,
					adapter);
			view.list_participant.setVisibility(View.VISIBLE);

			view.tv_participant.setVisibility(View.VISIBLE);
			view.list_participant
					.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, final int position, long id) {
							final Sharedmember participantSelected = adapter.sharedMembers
									.get(position);
							participantInforDialog(participantSelected);

						}
					});
		}

	}

	BroadcastReceiver activityGetSharedMemberComplete = new BroadcastReceiver() {
		public void onReceive(Context arg0, Intent arg1) {
			setParticipantOfActivity();
		}
	};

	private void participantInforDialog(final Sharedmember participant) {
		String[] array = getResources().getStringArray(
				R.array.owner_infor_array);

		if (shared_role != CommConstant.OWNER) {
			array = getResources().getStringArray(
					R.array.participant_infor_array);
		}

		int length = array.length;
		for (int i = 0; i < length; i++) {
			array[i] += " " + participant.getName();
		}
		TextViewBaseAdapter adapter = new TextViewBaseAdapter(mContext, array);

		final ParticipantInforDialog dialog = new ParticipantInforDialog(
				mContext);
		dialog.show();
		dialog.list_item.setAdapter(adapter);
		dialog.list_item.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				if (shared_role == CommConstant.OWNER) {
					switch (position) {
					case 0:
						Utils.makeAPhoneCall(mContext, participant.getMobile());
						break;
					case 1:
						Utils.sendAMessage(mContext, participant.getMobile());
						break;
					case 2:
						if (shared_role == CommConstant.OWNER) {
							// remove participant from activity
							deleteParticipantFromActivity(participant);
						}
						break;
					case 3:
						break;
					default:
						break;
					}
				} else {
					switch (position) {
					case 0:
						Utils.makeAPhoneCall(mContext, participant.getMobile());
						break;
					case 1:
						Utils.sendAMessage(mContext, participant.getMobile());
						break;
					case 2:

						break;
					default:
						break;
					}
				}
				dialog.dismiss();
			}
		});
		dialog.btn_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
	}

	public void deleteParticipantFromActivity(final Sharedmember participant) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		alertDialog.setTitle(mContext.getResources()
				.getString(R.string.caution));
		alertDialog.setMessage(mContext.getResources().getString(
				R.string.delete_activity));
		alertDialog.setPositiveButton(
				mContext.getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						removeParticipant(participant);
					}
				});
		alertDialog.setNegativeButton(
				mContext.getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Toast.makeText(mContext, "You clicked on NO",
						// Toast.LENGTH_SHORT).show();
						dialog.cancel();
					}
				});
		alertDialog.show();
	}

	public void removeParticipant(Sharedmember participant) {
		WebservicesHelper ws = new WebservicesHelper(mContext);
		ws.deleteSharedmemberOfActivity(participant.getID(), activity_id);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/**
	 * Return popup time zone
	 * */
	public void popUp(final String[] array, final int type) {
		TextViewBaseAdapter adapter = new TextViewBaseAdapter(mContext, array);

		String title = "";
		switch (type) {
		case TIMEZONE:
			title = getResources().getString(R.string.time_zone);
			break;
		case ALERT:
			title = getResources().getString(R.string.alert);
			break;
		case REPEAT:
			title = getResources().getString(R.string.repeat);
			break;
		default:
			break;
		}

		final PopupDialog dialog = new PopupDialog(mContext, title);
		dialog.show();
		dialog.list_item.setAdapter(adapter);
		dialog.list_item.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				switch (type) {
				case TIMEZONE:
					SharedReference ref = new SharedReference();
					ref.setTimeZone(mContext, position);
					time_zone = position;
					view.et_new_activity_time_zone.setText(array[position]);
					// thisActivity.setOtc_offset(Float
					// .parseFloat(timezone_value_array[position]));
					break;
				case ALERT:
					alert_type = position;
					view.et_new_activity_alert.setText(array[position]);
					// thisActivity.setAlert(position);
					break;
				case REPEAT:
					repeat_type = position;
					view.et_new_activity_repeat.setText(array[position]);
					// thisActivity.setRepeat(position);
					break;
				default:
					break;
				}
				dialog.dismiss();

			}
		});

	}

	/**
	 * On Click listener
	 * */
	public void onClickListener() {
		// view.btn_new_activity_next.setOnClickListener(this);
		view.et_new_activity_time_zone.setOnClickListener(this);
		view.et_new_activity_alert.setOnClickListener(this);
		view.et_new_activity_repeat.setOnClickListener(this);
		view.btn_add_paticipant.setOnClickListener(this);
		view.btn_remove_activity.setOnClickListener(this);
		view.layout_next.setOnClickListener(this);
		view.layout_back.setOnClickListener(this);
		view.et_new_activity_description.setOnClickListener(this);
		view.layout_save.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Init values
	 * */
	public void initViewValues() {
		alert_array = getResources().getStringArray(R.array.alert_array);
		timezone_array = getResources().getStringArray(R.array.timezone_array);
		timezone_value_array = getResources().getStringArray(
				R.array.timezone_value_array);

		repeat_array = getResources().getStringArray(R.array.repeat_array);

		if (composeType == DatabaseHelper.NEW) {
			view.title_tv.setText(mContext.getResources().getString(
					R.string.add_activity));
			// timezone saved is position in array timezone
			// SharedReference ref = new SharedReference();
			// time_zone = ref.getTimeZone(mContext);
			// set time zone if used to select
			int timezone = new SharedReference().getTimeZone(mContext);
			// : 0;
			Log.d("timeze", timezone + "");
			if (timezone <= 0) {
				view.et_new_activity_time_zone.setText(timezone_array[0]);
			} else {
				view.et_new_activity_time_zone
						.setText(timezone_array[timezone]);
			}
		} else if (composeType == DatabaseHelper.EXISTED) {
			view.title_tv.setText(mContext.getResources().getString(
					R.string.edit_activity));

			float timezone = thisActivity.getOtc_offset();
			view.et_new_activity_time_zone.setText(getTimezone(timezone));
			setParticipantOfActivity();

			if (shared_role != CommConstant.OWNER) {

				view.et_new_activity_alert.setEnabled(false);
				view.et_new_activity_time_zone.setEnabled(false);
				view.et_new_activity_description.setEnabled(false);
				view.et_new_activity_name.setEnabled(false);
				view.et_new_activity_repeat.setEnabled(false);
				view.btn_add_paticipant.setVisibility(View.GONE);
				view.btn_remove_activity.setVisibility(View.GONE);
				view.layout_save.setEnabled(false);
			} else {

				view.et_new_activity_alert.setEnabled(true);
				view.et_new_activity_time_zone.setEnabled(true);
				view.et_new_activity_description.setEnabled(true);
				view.et_new_activity_name.setEnabled(true);
				view.et_new_activity_repeat.setEnabled(true);
				view.btn_add_paticipant.setVisibility(View.VISIBLE);
				view.btn_remove_activity.setVisibility(View.VISIBLE);
				view.layout_save.setEnabled(true);

			}

		}

		String activity_name = thisActivity != null ? thisActivity
				.getActivity_name() : "";
		view.et_new_activity_name.setText(activity_name);

		String desp = thisActivity != null ? thisActivity.getDesp() : "";
		view.et_new_activity_description.setText(desp);

		alert_type = thisActivity != null ? thisActivity.getAlert() : 0;
		view.et_new_activity_alert.setText(getAlert(alert_type, alert_array));

		repeat_type = thisActivity != null ? thisActivity.getRepeat() : 0;
		view.et_new_activity_repeat
				.setText(getAlert(repeat_type, repeat_array));

	}

	/**
	 * Return string timezone string
	 * */
	public String getTimezone(float timezone) {
		try {
			for (int i = 0; i < timezone_value_array.length; i++) {
				String s = timezone_value_array[i];
				if (s.equals(timezone + "")) {
					return timezone_array[i];
				}
			}
		} catch (ArrayIndexOutOfBoundsException ex) {
			ex.printStackTrace();

		}
		return timezone_array[0];
	}

	/**
	 * Return string alert
	 * */
	public String getAlert(int alt, String[] array) {
		try {
			return array[alt];
		} catch (ArrayIndexOutOfBoundsException ex) {
			ex.printStackTrace();

		}
		return "None";
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == view.layout_next) {
			createNewActivity();
		} else if (v == view.et_new_activity_time_zone) {
			// if owner, can modify/delete else if is participant, only view
			// if (shared_role == CommConstant.OWNER) {
			SharedReference ref = new SharedReference();
			time_zone = ref.getTimeZone(mContext);
			if (time_zone <= 0) {
				popUp(timezone_array, TIMEZONE);
			}
			// }

		} else if (v == view.et_new_activity_repeat) {
			popUp(repeat_array, REPEAT);
		} else if (v == view.et_new_activity_alert) {
			popUp(alert_array, ALERT);
		} else if (v == view.btn_add_paticipant) {
			if (composeType == DatabaseHelper.EXISTED) {
				finish();
				Intent intent = new Intent(mContext, ParticipantActivity.class);
				intent.putExtra(CommConstant.TYPE,
						CommConstant.ADD_PARTICIPANT_FOR_ACTIVITY);
				intent.putExtra(CommConstant.ACTIVITY_ID, activity_id);
				intent.putExtra(CommConstant.TYPE, CommConstant.TYPE_CONTACT);

				mContext.startActivity(intent);
			}
		} else if (v == view.btn_remove_activity) {
			dialogDeleteActivity();
		} else if (v == view.layout_back) {
			((Activity) mContext).finish();
		} else if (v == view.et_new_activity_description) {
			// show an activity to edit description

			Intent intent = new Intent(mContext, EditDescriptionActivity.class);
			intent.putExtra(CommConstant.ACTIVITY_DESCRIPTION,
					thisActivity.getDesp());
			startActivityForResult(intent, 0);

		} else if (v == view.layout_save) {
			editActivity();
		}
	}

	/**
	 * delete activity
	 * */
	private void deleteActivity() {

		ArrayList<Sharedmember> listSharedMemberOfActivity = dbHelper
				.getSharedMemberForActivity(activity_id);
		if (listSharedMemberOfActivity != null
				&& listSharedMemberOfActivity.size() > 0) {
			for (Sharedmember sharedMember : listSharedMemberOfActivity) {
				ContentValues cv = new ContentValues();
				cv.put(SharedMemberTable.is_Deleted, 1);
				cv.put(SharedMemberTable.is_Synced, 0);
				dbHelper.updateSharedmember(sharedMember.getID(), activity_id,
						cv);
				WebservicesHelper ws = new WebservicesHelper(mContext);
				ws.deleteSharedmemberOfActivity(sharedMember.getID(),
						activity_id);
			}
		}

		List<Schedule> sbelongtoa = dbHelper
				.getSchedulesBelongtoActivity(activity_id);
		for (int i = 0; i < sbelongtoa.size(); i++) {
			ContentValues scv = new ContentValues();
			scv.put(ScheduleTable.is_Deleted, 1);
			scv.put(ScheduleTable.is_Synchronized, 0);
			int schedule_id = sbelongtoa.get(i).getSchedule_ID();
			// dbHelper.updateSchedule(schedule_id, scv);
			List<Integer> onduties = dbHelper
					.getOndutyRecordsForSchedule(schedule_id);
			for (int j = 0; j < onduties.size(); j++) {
				ContentValues ocv = new ContentValues();
				ocv.put(OndutyTable.is_Deleted, 1);
				ocv.put(OndutyTable.is_Synchronized, 0);
				// int onduty_id = onduties.get(j);
				dbHelper.updateOnduty(schedule_id, ocv);
			}
			dbHelper.updateSchedule(schedule_id, scv);
			WebservicesHelper ws = new WebservicesHelper(mContext);
			ws.deleteSchedule(sbelongtoa.get(i));
		}

		ContentValues cv = new ContentValues();
		cv.put(ActivityTable.is_Deleted, 1);
		cv.put(ActivityTable.is_Synchronized, 0);
		dbHelper.updateActivity(activity_id, cv);

		WebservicesHelper ws = new WebservicesHelper(mContext);
		ws.deleteActivity(thisActivity);
	}

	/**
	 * Delete activity
	 * */
	private void dialogDeleteActivity() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		alertDialog.setTitle(mContext.getResources()
				.getString(R.string.caution));
		alertDialog.setMessage(mContext.getResources().getString(
				R.string.delete_activity));
		alertDialog.setPositiveButton(
				mContext.getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						deleteActivity();
					}
				});
		alertDialog.setNegativeButton(
				mContext.getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Toast.makeText(mContext, "You clicked on NO",
						// Toast.LENGTH_SHORT).show();
						dialog.cancel();
					}
				});
		alertDialog.show();
	}

	/**
	 * Edit activity
	 * */
	private void editActivity() {
		if (setAndCheckDataForActivity()) {
			if (composeType == DatabaseHelper.EXISTED) {
				ContentValues cv = new ContentValues();
				cv.put(ActivityTable.service_Name,
						thisActivity.getActivity_name());
				cv.put(ActivityTable.alert, thisActivity.getAlert());
				cv.put(ActivityTable.repeat, thisActivity.getRepeat());
				cv.put(ActivityTable.start_time, thisActivity.getStarttime());
				cv.put(ActivityTable.end_time, thisActivity.getEndtime());
				cv.put(ActivityTable.service_description,
						thisActivity.getDesp());
				cv.put(ActivityTable.otc_Offset, thisActivity.getOtc_offset());
				cv.put(ActivityTable.sharedrole, thisActivity.getRole());
				cv.put(ActivityTable.is_Deleted, 0);
				cv.put(ActivityTable.is_Synchronized, 0);
				dbHelper.updateActivity(thisActivity.getActivity_ID(), cv);

				WebservicesHelper ws = new WebservicesHelper(mContext);
				ws.updateActivity(thisActivity);

			}
		}
	}

	/**
	 * Set and check data invalid for activity to add or edit
	 * */
	private boolean setAndCheckDataForActivity() {
		if (view.et_new_activity_name.getText().toString().equalsIgnoreCase("")) {
			Toast.makeText(this, "The activity name should not be empty",
					Toast.LENGTH_LONG).show();
			return false;
		}

		String activity_name = view.et_new_activity_name.getText().toString()
				.trim();
		String activity_description = view.et_new_activity_description
				.getText().toString().trim();

		if (activity_name == null || activity_name.equals("")) {
			Toast.makeText(
					mContext,
					mContext.getResources().getString(
							R.string.input_activity_name)
							+ "\n", Toast.LENGTH_LONG).show();
			return false;
		}
		if (activity_description == null || activity_description.equals("")) {
			Toast.makeText(
					mContext,
					mContext.getResources().getString(
							R.string.input_activity_description)
							+ "\n", Toast.LENGTH_LONG).show();
			return false;
		}

		thisActivity.setActivity_name(activity_name);
		thisActivity.setDesp(activity_description);
		thisActivity.setAlert(alert_type);
		Log.d("alert_type", alert_type + "");
		Log.d("repeat_type", repeat_type + "");
		thisActivity.setRepeat(repeat_type);

		int timezone = new SharedReference().getTimeZone(mContext);
		thisActivity.setOtc_offset((int) (Float
				.parseFloat(timezone_value_array[timezone]) * 3600));
		return true;
	}

	/**
	 * Create new activity
	 * */
	private void createNewActivity() {

		if (setAndCheckDataForActivity()) {
			if (composeType == DatabaseHelper.NEW) {
				ContentValues newActivity = new ContentValues();
				newActivity.put(ActivityTable.service_ID,
						thisActivity.getActivity_ID());
				newActivity.put(ActivityTable.own_ID,
						thisActivity.getOwner_ID());
				newActivity.put(ActivityTable.service_Name,
						thisActivity.getActivity_name());
				newActivity.put(ActivityTable.alert, thisActivity.getAlert());
				newActivity.put(ActivityTable.repeat, thisActivity.getRepeat());
				newActivity.put(ActivityTable.sharedrole,
						thisActivity.getRole());
				newActivity.put(ActivityTable.start_time,
						thisActivity.getStarttime());
				newActivity.put(ActivityTable.end_time,
						thisActivity.getEndtime());
				newActivity.put(ActivityTable.service_description,
						thisActivity.getDesp());
				newActivity.put(ActivityTable.otc_Offset,
						thisActivity.getOtc_offset());
				Log.d("timezone add activity", thisActivity.getOtc_offset()
						+ "");
				newActivity.put(ActivityTable.is_Deleted, 0);
				newActivity.put(ActivityTable.is_Synchronized, 0);
				newActivity.put(ActivityTable.last_ModifiedTime, "nouploaded");
				newActivity.put(ParticipantTable.user_login,
						new SharedReference().getCurrentOwnerId(mContext));
				if (dbHelper.insertActivity(newActivity)) {

				}
				WebservicesHelper ws = new WebservicesHelper(mContext);
				ws.addActivity(thisActivity);
			}
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 222) {
			String activity_decription = data
					.getStringExtra(CommConstant.ACTIVITY_DESCRIPTION);
			view.et_new_activity_description
					.setText(activity_decription != null ? activity_decription
							: view.et_new_activity_description.getText()
									.toString());
		}
		switch (requestCode) {
		case (11): {
			Intent resultIntent = new Intent();
			resultIntent.putExtra("id", thisActivity.getActivity_ID());
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
			break;
		}

		}
	}

}