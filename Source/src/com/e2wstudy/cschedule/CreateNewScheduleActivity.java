package com.e2wstudy.cschedule;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.e2wstudy.cschedule.adapter.ActivityNameAdapter;
import com.e2wstudy.cschedule.adapter.SharedMemberAdapter;
import com.e2wstudy.cschedule.db.DatabaseHelper;
import com.e2wstudy.cschedule.models.MyActivity;
import com.e2wstudy.cschedule.models.OndutyTable;
import com.e2wstudy.cschedule.models.Schedule;
import com.e2wstudy.cschedule.models.ScheduleTable;
import com.e2wstudy.cschedule.models.Sharedmember;
import com.e2wstudy.cschedule.net.WebservicesHelper;
import com.e2wstudy.cschedule.utils.CommConstant;
import com.e2wstudy.cschedule.utils.MyDate;
import com.e2wstudy.cschedule.utils.SharedReference;
import com.e2wstudy.cschedule.utils.Utils;
import com.e2wstudy.cschedule.views.AddScheduleView;
import com.e2wstudy.cschedule.views.ConfirmDialog;
import com.e2wstudy.cschedule.views.PopupDialog;
import com.e2wstudy.cschedule.views.ToastDialog;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author khoahuyen
 * @category add new schedule
 * */
// @SuppressLint("NewApi")
public class CreateNewScheduleActivity extends Activity implements
		OnDateSetListener, OnTimeSetListener, OnMenuItemClickListener,
		OnClickListener {

	static private final int START = 0;
	static private final int END = 1;

	private Schedule thisSchedule;
	private int composeType;
	private int StartOrEnd;
	private DatabaseHelper dbHelper;
	private List<Integer> pins;

	AddScheduleView view;
	String activity_id = "";
	Context mContext;
	MyActivity myActivity;
	int startMonth, startYear, startDate, startHour, startMin;
	int endMonth, endYear, endDate, endHour, endMin;

	int REQUEST_CODE = 15;
	int schedule_id = -1;
	ProgressDialog progress = null;
	// creator of this schedule
	int creator;

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mContext = this;
		view = new AddScheduleView(mContext);
		this.setContentView(view.layout);
		view.et_new_activity_description.requestFocus();
		Intent myIntent = this.getIntent();
		composeType = myIntent.getIntExtra(CommConstant.TYPE, -1);
		activity_id = myIntent.getStringExtra(CommConstant.ACTIVITY_ID);

		dbHelper = DatabaseHelper.getSharedDatabaseHelper(this);

		creator = new SharedReference().getCurrentOwnerId(mContext);
//		view.et_new_activity_description.setFocusable(false);
		if (activity_id != null && (!activity_id.equals(""))) {
			myActivity = dbHelper.getActivity(activity_id);
		} else {
			// TODO:make activityname is suggestion activity in system
		}
		if (composeType == DatabaseHelper.EXISTED) {
			schedule_id = myIntent.getIntExtra(CommConstant.SCHEDULE_ID, -1);
			creator = myIntent.getIntExtra(CommConstant.CREATOR,
					new SharedReference().getCurrentOwnerId(mContext));
		}
		initViewValues();

		onClickListener();
		// try {
		//
		// registerReceiver(onDutyComplete, new IntentFilter(
		// CommConstant.ON_DUTY_ITEM_SELECTED));
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// }
	}

	Handler createNewScheduleHandle = new Handler() {

	    @Override
	    public void handleMessage(Message msg) {

	        if (msg.what != 1) { // code if not connected
	        	final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
	        } else { // code if connected
	        	createNewSchedule();
	        }

	    }
	};
	
	
	Handler editScheduleHandle = new Handler() {

	    @Override
	    public void handleMessage(Message msg) {

	        if (msg.what != 1) { // code if not connected
	        	final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
	        } else { // code if connected
	        	editSchedule();
	        }

	    }
	};
	
	
	Handler deleteScheduleHandle = new Handler() {

	    @Override
	    public void handleMessage(Message msg) {

	        if (msg.what != 1) { // code if not connected
	        	final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
	        } else { // code if connected
	        	deleteSchedule();
	        }

	    }
	};
	
	
	/**
	 * Return popup activityname have shared role owner or organizer
	 * */
	public void popUp() {
		DatabaseHelper dbHelper = DatabaseHelper
				.getSharedDatabaseHelper(mContext);
		final ArrayList<MyActivity> listActivity = dbHelper
				.getActivitiesOwnerOrOrganizer(new SharedReference()
						.getCurrentOwnerId(mContext) + "");
		ActivityNameAdapter adapter = new ActivityNameAdapter(mContext,
				listActivity);
		final PopupDialog dialog = new PopupDialog(mContext, mContext
				.getResources().getString(R.string.activity));
		dialog.show();
		dialog.list_item.setAdapter(adapter);
		dialog.list_item.setVisibility(View.VISIBLE);
		dialog.list_item.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				myActivity = listActivity.get(position);
				activity_id = myActivity.getActivity_ID();
				view.et_new_activity_name.setText(myActivity.getActivity_name());
				dialog.dismiss();

			}
		});

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		try {
			// unregisterReceiver(deleteScheduleComplete);
			// unregisterReceiver(onDutyComplete);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * get shared member of activity
	 * */
	private void setParticipantOfActivity(List<Integer> pins) {
		if (activity_id != null && (!activity_id.equals(""))) {
			DatabaseHelper dbHelper = DatabaseHelper
					.getSharedDatabaseHelper(mContext);
			ArrayList<Sharedmember> list_participant = new ArrayList<Sharedmember>();

			if (pins != null && pins.size() > 0) {
				for (int i = 0; i < pins.size(); i++) {
					Sharedmember p = dbHelper.getSharedmember(pins.get(i),
							activity_id);
					if (p != null) {
						p.isChecked = true;
						list_participant.add(p);
					}
				}
			}

			if (list_participant != null && list_participant.size() > 0) {
				final SharedMemberAdapter adapter = new SharedMemberAdapter(
						mContext, list_participant, false, false, true);
				view.list_participant.setAdapter(adapter);
				Utils.setListViewHeightBasedOnChildren(view.list_participant,
						adapter);
				view.list_participant.setVisibility(View.VISIBLE);
				view.layout_list_on_duty.setVisibility(View.VISIBLE);
				view.layout_on_duty.setVisibility(View.VISIBLE);
				view.tv_participant.setVisibility(View.VISIBLE);
				view.list_participant
						.setOnItemClickListener(new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, final int position, long id) {
								final Sharedmember participantSelected = adapter.sharedMembers
										.get(position);
								// participantInforDialog(participantSelected);

							}
						});
			}

			else {
				view.list_participant.setVisibility(View.GONE);
				view.layout_list_on_duty.setVisibility(View.GONE);
				view.layout_on_duty.setVisibility(View.GONE);
				view.tv_participant.setVisibility(View.GONE);
			}

		}

	}

	private void onClickListener() {
		view.et_endDate.setOnClickListener(this);
		view.et_startDate.setOnClickListener(this);
		view.et_endTime.setOnClickListener(this);
		view.et_startTime.setOnClickListener(this);
		view.et_on_duty.setOnClickListener(this);
		view.titleBar.layout_next.setOnClickListener(this);
		view.btn_remove_schedule.setOnClickListener(this);
		view.titleBar.layout_back.setOnClickListener(this);
		view.titleBar.layout_save.setOnClickListener(this);
		view.et_new_activity_name.setOnClickListener(this);
		view.et_new_activity_description.setOnClickListener(this);
		// view.tv_participant.setOnClickListener(this);
		view.btn_change_on_duty.setOnClickListener(this);
		view.et_new_activity_description.setOnTouchListener(new View.OnTouchListener() {

	        @Override
	        public boolean onTouch(final View v, final MotionEvent motionEvent) {	   
	             v.getParent().requestDisallowInterceptTouchEvent(true);
	                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
	                    case MotionEvent.ACTION_UP:
	                    	
	         	           
	                        v.getParent().requestDisallowInterceptTouchEvent(
	                                false);
	                        break;
	                }
//	            }
	            return false;
	        }
	    });
	}

	@Override
	public void onClick(View v) {
		Utils.hideKeyboard(CreateNewScheduleActivity.this,
				view.et_new_activity_description);
		if (v == view.et_startDate) {
			// setStartDate();
			if (!view.et_new_activity_name.getText().toString().trim()
					.equals("")) {
				setStartDate();
			} else {
				final ToastDialog dialog = new ToastDialog(mContext,
						mContext.getResources().getString(R.string.select_activity_schedule));
				dialog.show();

				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} else if (v == view.et_endDate) {
			if (!view.et_new_activity_name.getText().toString().trim()
					.equals("")) {
				setEndDate();
			} else {
				final ToastDialog dialog = new ToastDialog(mContext,
						mContext.getResources().getString(R.string.select_activity_schedule));
				dialog.show();

				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} else if (v == view.et_endTime) {
			if (!view.et_new_activity_name.getText().toString().trim()
					.equals("")) {
				setEndTime();
			} else {
				final ToastDialog dialog = new ToastDialog(mContext,
						mContext.getResources().getString(R.string.select_activity_schedule));
				dialog.show();

				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} else if (v == view.et_startTime) {
			if (!view.et_new_activity_name.getText().toString().trim()
					.equals("")) {
				setStartTime();
			} else {
				final ToastDialog dialog = new ToastDialog(mContext,
						mContext.getResources().getString(R.string.select_activity_schedule));
				dialog.show();

				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} else if (v == view.et_on_duty) {
			if (!view.et_new_activity_name.getText().toString().trim()
					.equals("")) {

				Intent intent = new Intent(mContext, ParticipantActivity.class);
				intent.putExtra(CommConstant.ACTIVITY_ID, activity_id);
				intent.putExtra(CommConstant.TYPE,
						CommConstant.TYPE_PARTICIPANT);
				intent.putExtra(CommConstant.SCHEDULE_ID,
						thisSchedule != null ? thisSchedule.getSchedule_ID()
								: -1);
				
				intent.putIntegerArrayListExtra("pins",
						(ArrayList<Integer>) pins);
				startActivityForResult(intent, REQUEST_CODE);
				Utils.pushRightToLeft(mContext);
			} else {
				final ToastDialog dialog = new ToastDialog(mContext,
						mContext.getResources().getString(R.string.select_activity_schedule));
				dialog.show();

				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}

		} else if (v == view.titleBar.layout_next) {
//			createNewSchedule();
			Utils.isNetworkAvailable(createNewScheduleHandle);
		} else if (v == view.btn_remove_schedule) {
//			deleteSchedule();
			Utils.isNetworkAvailable(deleteScheduleHandle);
		} else if (v == view.titleBar.layout_back) {
			((Activity) mContext).finish();
//			Utils.postLeftToRight(mContext);
		} else if (v == view.titleBar.layout_save) {
			if (composeType == DatabaseHelper.EXISTED) {
//				editSchedule();
				Utils.isNetworkAvailable(editScheduleHandle);
			} else if (composeType == DatabaseHelper.NEW) {
//				createNewSchedule();
				Utils.isNetworkAvailable(createNewScheduleHandle);
			}
		} else if (v == view.et_new_activity_name) {
			if (composeType == DatabaseHelper.NEW) {
				popUp();
			}
		} else if (v == view.et_new_activity_description) {
			SharedReference ref = new SharedReference();
			int owner_id = ref.getCurrentOwnerId(mContext);
			if (creator == owner_id)
			{
			Utils.openKeyboard(mContext,view.et_new_activity_description);
			}
			if (!view.et_new_activity_name.getText().toString().trim()
					.equals("")) {

				// show an activity to edit description
//				Intent intent = new Intent(mContext,
//						EditDescriptionActivity.class);
//				intent.putExtra(CommConstant.ACTIVITY_DESCRIPTION,
//						thisSchedule.getDesp());
//				startActivityForResult(intent, 0);
//				Utils.slideUpDown(mContext);
			} else {
				final ToastDialog dialog = new ToastDialog(mContext,
						mContext.getResources().getString(R.string.select_activity_schedule));
				dialog.show();

				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		}

		else if (v == view.btn_change_on_duty) {
			if (!view.et_new_activity_name.getText().toString().trim()
					.equals("")) {
				Utils.pushRightToLeft(mContext);
				Intent intent = new Intent(mContext, ParticipantActivity.class);
				intent.putExtra(CommConstant.ACTIVITY_ID, activity_id);
				intent.putExtra(CommConstant.TYPE,
						CommConstant.TYPE_PARTICIPANT);
				intent.putExtra(CommConstant.SCHEDULE_ID,
						thisSchedule != null ? thisSchedule.getSchedule_ID()
								: -1);
				// List<Integer> pins = new ArrayList<Integer>();
				// if (composeType == DatabaseHelper.EXISTED) {
				// pins = dbHelper.getParticipantsForSchedule(thisSchedule
				// .getSchedule_ID());
				// }
				intent.putIntegerArrayListExtra("pins",
						(ArrayList<Integer>) pins);
				startActivityForResult(intent, REQUEST_CODE);
			} else {
				final ToastDialog dialog = new ToastDialog(mContext,
						mContext.getResources().getString(R.string.select_activity_schedule));
				dialog.show();

				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}

		}
	}

	public void initViewValues() {
		try {
			if (progress == null) {
				// display progress dialog like this
				progress = new ProgressDialog(mContext);
				progress.setCancelable(false);
				progress.setMessage(mContext.getResources().getString(
						R.string.processing));
			}
			if (composeType == DatabaseHelper.NEW) {
				Log.i("next service id", "is " + dbHelper.getNextActivityID());
				thisSchedule = new Schedule(
						new SharedReference().getCurrentOwnerId(mContext),
						dbHelper.getNextScheduleID(), activity_id == null ? "0"
								: activity_id,
						MyDate.transformLocalDateTimeToUTCFormat(MyDate
								.getCurrentDateTime()),
						MyDate.transformLocalDateTimeToUTCFormat(MyDate
								.getCurrentDateTime()), "");
				view.btn_remove_schedule.setVisibility(View.GONE);
				view.titleBar.layout_next.setVisibility(View.GONE);
				view.titleBar.layout_save.setVisibility(View.VISIBLE);
				view.titleBar.tv_name.setText(mContext.getResources()
						.getString(R.string.add_schedule));
				schedule_id = thisSchedule.getSchedule_ID();

				DatabaseHelper dbHelper = DatabaseHelper
						.getSharedDatabaseHelper(mContext);
				final ArrayList<MyActivity> listActivity = dbHelper
						.getActivitiesOwnerOrOrganizer(new SharedReference()
								.getCurrentOwnerId(mContext) + "");

				// have no activity before and show popup
				if (listActivity == null || listActivity.size() == 0) {
					final ConfirmDialog dialog = new ConfirmDialog(mContext,
							mContext.getResources().getString(
									R.string.no_activity_for_create_schedule));
					dialog.show();
					dialog.btnOk.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
							finish();
							Utils.postLeftToRight(mContext);
							CategoryTabActivity.currentPage = CategoryTabActivity.TAB_ACTIVITY;
							// Intent intent=new Intent("goToActivity");
							// sendBroadcast(intent);

						}
					});
					dialog.btnCancel.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							dialog.dismiss();
							finish();
						}
					});
				}

			} else if (composeType == DatabaseHelper.EXISTED) {
				if (schedule_id > 0) {
					thisSchedule = dbHelper.getScheduleSortedByID(schedule_id);
				}
				view.btn_remove_schedule.setVisibility(View.VISIBLE);
				view.titleBar.layout_next.setVisibility(View.GONE);
				view.titleBar.layout_save.setVisibility(View.VISIBLE);
				view.titleBar.tv_name.setText(mContext.getResources()
						.getString(R.string.edit_schedule));
				if (thisSchedule != null) {
					pins = dbHelper.getParticipantsForSchedule(thisSchedule
							.getSchedule_ID());

					if (pins != null && pins.size() > 0) {
						view.layout_on_duty.setVisibility(View.VISIBLE);
					} else {
						view.layout_on_duty.setVisibility(View.GONE);
					}

					//
					// view.et_on_duty.setText(members);
					// if(members.equals(""))
					// {
					// view.et_on_duty.setText("Choose Participant");
					// }
					setParticipantOfActivity(pins);
				}
			}

			view.et_new_activity_name.setText(myActivity != null ? myActivity
					.getActivity_name() : "");

			String startdate = thisSchedule != null ? thisSchedule
					.getStarttime() : "";
			String startfulldate = MyDate.getWeekdayFromUTCTime(startdate)
					+ ", " + MyDate.transformUTCTimeToCustomStyle(startdate);
			String starttime = MyDate.getTimeWithAPMFromUTCTime(startdate);
			view.et_startDate.setText(startfulldate);
			view.et_startTime.setText(starttime);

			String enddate = thisSchedule.getEndtime();
			String endfulldate = MyDate.getWeekdayFromUTCTime(enddate) + ", "
					+ MyDate.transformUTCTimeToCustomStyle(enddate);
			String endtime = MyDate.getTimeWithAPMFromUTCTime(enddate);
			view.et_endDate.setText(endfulldate);
			view.et_endTime.setText(endtime);

			view.et_new_activity_description
					.setText(thisSchedule != null ? thisSchedule.getDesp() : "");

			SharedReference ref = new SharedReference();
			int owner_id = ref.getCurrentOwnerId(mContext);

			// can create/modify/delete
			if (creator == owner_id) {
				view.et_endDate.setEnabled(true);
				view.et_endTime.setEnabled(true);
				view.et_startDate.setEnabled(true);
				view.et_startTime.setEnabled(true);
				view.et_on_duty.setEnabled(true);
				view.et_new_activity_description.setEnabled(true);
				view.et_new_activity_description.setFocusableInTouchMode(true);
//				view.et_new_activity_name.setEnabled(true);
//				view.et_new_activity_name.setEnabled(false);
				// view.btn_remove_schedule.setVisibility(View.VISIBLE);
				view.titleBar.layout_save.setEnabled(true);
				view.titleBar.layout_next.setEnabled(true);
				view.btn_change_on_duty.setEnabled(true);
				view.btn_change_on_duty.setVisibility(View.VISIBLE);
			} else {
				// only view
				view.et_endDate.setEnabled(false);
				view.et_endTime.setEnabled(false);
				view.et_startDate.setEnabled(false);
				view.et_startTime.setEnabled(false);
				view.et_on_duty.setEnabled(false);
				view.et_new_activity_description.setEnabled(false);
				view.et_new_activity_name.setEnabled(false);
				view.btn_remove_schedule.setVisibility(View.GONE);
				view.titleBar.layout_save.setEnabled(false);
				view.titleBar.layout_next.setEnabled(false);
				view.btn_change_on_duty.setEnabled(false);
				view.btn_change_on_duty.setVisibility(View.GONE);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setStartDate() {
		String[] startdatetime = MyDate.transformUTCDateToLocalDate(
				MyDate.STANDARD, thisSchedule.getStarttime()).split(" ");
		String[] datecomponents = startdatetime[0].split("-");
		int year = Integer.valueOf(datecomponents[0]);
		int month = Integer.valueOf(datecomponents[1]) - 1;
		int day = Integer.valueOf(datecomponents[2]);
		DatePickerDialog dialog = new DatePickerDialog(this, this, year, month,
				day);
		dialog.setTitle("Set Start Date");
		// dialog.getDatePicker().setTag("startdate");
		dialog.show();
		StartOrEnd = START;
	}

	public void setEndDate() {
		String[] enddatetime = MyDate.transformUTCDateToLocalDate(
				MyDate.STANDARD, thisSchedule.getEndtime()).split(" ");
		String[] datecomponents = enddatetime[0].split("-");
		int year = Integer.valueOf(datecomponents[0]);
		int month = Integer.valueOf(datecomponents[1]) - 1;
		int day = Integer.valueOf(datecomponents[2]);
		DatePickerDialog dialog = new DatePickerDialog(this, this, year, month,
				day);
		dialog.setTitle("Set End Date");
		// dialog.getDatePicker().setTag("enddate");
		dialog.show();
		StartOrEnd = END;
	}

	public void setStartTime() {
		String[] startdatetime = MyDate.transformUTCDateToLocalDate(
				MyDate.STANDARD, thisSchedule.getStarttime()).split(" ");
		String[] timecomponents = startdatetime[1].split(":");
		int hour = Integer.valueOf(timecomponents[0]);
		int minute = Integer.valueOf(timecomponents[1]);
		TimePickerDialog dialog = new TimePickerDialog(this, this, hour,
				minute, true);
		dialog.setTitle("Set Start Time");
		dialog.show();
		StartOrEnd = START;
	}

	public void setEndTime() {
		String[] enddatetime = MyDate.transformUTCDateToLocalDate(
				MyDate.STANDARD, thisSchedule.getEndtime()).split(" ");
		String[] timecomponents = enddatetime[1].split(":");
		int hour = Integer.valueOf(timecomponents[0]);
		int minute = Integer.valueOf(timecomponents[1]);
		TimePickerDialog dialog = new TimePickerDialog(this, this, hour,
				minute, true);
		dialog.setTitle("Set End Time");
		dialog.show();
		StartOrEnd = END;
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		// TODO Auto-generated method stub
		int month = monthOfYear + 1;
		String monthstr = null;
		String daystr = null;
		if (month < 10)
			monthstr = "0" + month;
		else
			monthstr = String.valueOf(month);

		if (dayOfMonth < 10)
			daystr = "0" + dayOfMonth;
		else
			daystr = String.valueOf(dayOfMonth);
		String hourMinute = "";
		if (StartOrEnd == START) {
			hourMinute = this.view.et_startTime.getText().toString().trim();

		} else {
			hourMinute = this.view.et_endTime.getText().toString().trim();
		}
		hourMinute = hourMinute.replace("AM", "").replace("PM", "");
		hourMinute = hourMinute.trim() + ":00";
		Log.d("hourMinute", hourMinute);
		String weekday = MyDate.getWeekdayFromUTCTime(MyDate
				.transformLocalDateTimeToUTCFormat(year + "-" + monthstr + "-"
						+ daystr + " "
						+ (hourMinute.equals("") ? "00:00:00" : hourMinute)));
		String customDate = MyDate.transformUTCTimeToCustomStyle(MyDate
				.transformLocalDateTimeToUTCFormat(year + "-" + monthstr + "-"
						+ daystr + " "
						+ (hourMinute.equals("") ? "00:00:00" : hourMinute)));
		String fulldate = weekday + ", " + customDate;
		if (StartOrEnd == START) {
			this.view.et_startDate.setText(fulldate);
			thisSchedule
					.setStarttime(MyDate.transformLocalDateTimeToUTCFormat(year
							+ "-"
							+ monthstr
							+ "-"
							+ daystr
							+ " "
							+ MyDate.transformUTCDateToLocalDate(
									MyDate.STANDARD,
									thisSchedule.getStarttime()).split(" ")[1]));

		} else {

			this.view.et_endDate.setText(fulldate);
			thisSchedule
					.setEndtime(MyDate.transformLocalDateTimeToUTCFormat(year
							+ "-"
							+ monthstr
							+ "-"
							+ daystr
							+ " "
							+ MyDate.transformUTCDateToLocalDate(
									MyDate.STANDARD,
									thisSchedule.getStarttime()).split(" ")[1]));
		}
		if (MyDate.IsFirstDateLaterThanSecondDate(
				this.thisSchedule.getStarttime(),
				this.thisSchedule.getEndtime())) {
			this.thisSchedule.setEndtime(this.thisSchedule.getStarttime());
			this.view.et_endDate.setText(this.view.et_startDate.getText());
			this.view.et_endTime.setText(this.view.et_startTime.getText());
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onTimeSet(TimePicker arg0, int hour, int minute) {
		// TODO Auto-generated method stub
		String hourstr = String.valueOf(hour);
		if (hour < 10)
			hourstr = "0" + hourstr;
		String minutestr = String.valueOf(minute);
		if (minute < 10)
			minutestr = "0" + minutestr;
		String time = MyDate.getTimeWithAPMFromUTCTime(MyDate
				.transformLocalDateTimeToUTCFormat("0000-00-00 " + hourstr
						+ ":" + minutestr + ":" + "00"));
		if (StartOrEnd == START) {
			this.view.et_startTime.setText(time);
			thisSchedule.setStarttime(MyDate
					.transformLocalDateTimeToUTCFormat(MyDate
							.transformUTCDateToLocalDate(MyDate.STANDARD,
									thisSchedule.getStarttime()).split(" ")[0]
							+ " " + hourstr + ":" + minutestr + ":" + "00"));

		} else {
			this.view.et_endTime.setText(time);
			thisSchedule.setEndtime(MyDate
					.transformLocalDateTimeToUTCFormat(MyDate
							.transformUTCDateToLocalDate(MyDate.STANDARD,
									thisSchedule.getEndtime()).split(" ")[0]
							+ " " + hourstr + ":" + minutestr + ":" + "00"));
			if (MyDate.IsFirstDateLaterThanSecondDate(
					this.thisSchedule.getStarttime(),
					this.thisSchedule.getEndtime())) {
				this.thisSchedule.setEndtime(this.thisSchedule.getEndtime());
				this.view.et_endDate.setText(this.view.et_startDate.getText());
				this.view.et_startTime.setText(this.view.et_endTime.getText());

			} else {

			}
		}
		if (MyDate.IsFirstDateLaterThanSecondDate(
				this.thisSchedule.getStarttime(),
				this.thisSchedule.getEndtime())) {
			this.thisSchedule.setEndtime(this.thisSchedule.getEndtime());
			this.view.et_endDate.setText(this.view.et_startDate.getText());
			this.view.et_endTime.setText(this.view.et_startTime.getText());

		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
		thisSchedule.setService_ID(id + "");
		String name = item.getTitle().toString();
		view.et_new_activity_name.setText(name);
		return false;
	}

	/**
	 * Check&set data, return true if valid else return false
	 * */
	private boolean checkAndSetData() {
		if (myActivity == null) {
			final ToastDialog dialog = new ToastDialog(mContext,
					mContext.getResources().getString(R.string.select_activity_schedule));
			dialog.show();

			dialog.btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			return false;
		}
		thisSchedule.setDesp(view.et_new_activity_description.getText()
				.toString());
		thisSchedule.setService_ID(myActivity.getActivity_ID());
		return true;
	}

	/**
	 * Create new schedule
	 * */
	private void createNewSchedule() {
		if (checkAndSetData() && (composeType == DatabaseHelper.NEW)) {

			ContentValues cv = new ContentValues();
			cv.put(ScheduleTable.own_ID, thisSchedule.getOwner_ID());
			cv.put(ScheduleTable.schedule_ID, thisSchedule.getSchedule_ID());
			cv.put(ScheduleTable.last_Modified, "upload");
			cv.put(ScheduleTable.start_Time, thisSchedule.getStarttime());
			cv.put(ScheduleTable.schedule_Description, thisSchedule.getDesp());
			cv.put(ScheduleTable.end_Time, thisSchedule.getEndtime());
			cv.put(ScheduleTable.service_ID, thisSchedule.getService_ID());
			cv.put(ScheduleTable.is_Deleted, 0);
			cv.put(ScheduleTable.is_Synchronized, 0);
			cv.put(ScheduleTable.user_login,
					new SharedReference().getCurrentOwnerId(mContext));

			dbHelper.insertSchedule(cv);
			if (pins != null) {
				int size = pins.size();
				if (size > 0) {
					for (int i = 0; i < size; i++) {
						ContentValues onduty = new ContentValues();
						onduty.put(OndutyTable.schedule_ID,
								thisSchedule.getSchedule_ID());
						onduty.put(OndutyTable.service_ID,
								thisSchedule.getService_ID());
						onduty.put(OndutyTable.participant_ID, pins.get(i));
						onduty.put(OndutyTable.last_Modified, "");
						onduty.put(OndutyTable.is_Deleted, 0);
						onduty.put(OndutyTable.is_Synchronized, 0);
						dbHelper.insertOnduty(onduty);
					}
				}
			}
			WebservicesHelper ws = new WebservicesHelper(mContext);
			ws.addSchedule(thisSchedule, pins);

		}

	}

	/**
	 * Edit schedule
	 * */
	private void editSchedule() {
		if (checkAndSetData() && (composeType == DatabaseHelper.EXISTED)) {
			ContentValues cv = new ContentValues();
			cv.put(ScheduleTable.start_Time, thisSchedule.getStarttime());
			cv.put(ScheduleTable.schedule_Description, thisSchedule.getDesp());
			cv.put(ScheduleTable.end_Time, thisSchedule.getEndtime());
			cv.put(ScheduleTable.service_ID, thisSchedule.getService_ID());
			cv.put(ScheduleTable.is_Deleted, 0);
			cv.put(ScheduleTable.is_Synchronized, 0);
			dbHelper.updateSchedule(thisSchedule.getSchedule_ID(), cv);
			dbHelper.deleteRelatedOnduty(thisSchedule.getSchedule_ID());
			if (pins != null) {
				int size = pins.size();
				for (int i = 0; i < size; i++) {
					ContentValues onduty = new ContentValues();
					onduty.put(OndutyTable.schedule_ID,
							thisSchedule.getSchedule_ID());
					onduty.put(OndutyTable.service_ID,
							thisSchedule.getSchedule_ID());
					onduty.put(OndutyTable.participant_ID, pins.get(i));
					onduty.put(OndutyTable.last_Modified, "");
					onduty.put(OndutyTable.is_Deleted, 0);
					onduty.put(OndutyTable.is_Synchronized, 0);
					dbHelper.insertOnduty(onduty);
				}
			}

			WebservicesHelper ws = new WebservicesHelper(mContext);
			ws.updateSchedule(thisSchedule, pins);

		}
	}

	JsonHttpResponseHandler deleteScheduleHandler = new JsonHttpResponseHandler() {
		public void onSuccess(JSONObject response) {
			try {
				if (response.getString("lastmodified") != null) {

					dbHelper.deleteRelatedOnduty(schedule_id);
					dbHelper.deleteSchedule(schedule_id);
					Log.i("delete schedule", "successfully");
					finish();
					Utils.postLeftToRight(mContext);
					Intent intent = new Intent(
							CommConstant.DELETE_SCHEDULE_COMPLETE);
					mContext.sendBroadcast(intent);

				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onStart() {
			// TODO Auto-generated method stub
			super.onStart();
			progress.show();
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			super.onFinish();
			try {
				if (progress.isShowing()) {
					progress.dismiss();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public void onFailure(Throwable e, String response) {
			final ToastDialog dialog=new ToastDialog(mContext, mContext.getResources().getString(
					R.string.delete_schedule_error)
					+ "\n" + response.toString());
			dialog.show();
			dialog.btnOk.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
	};

	/**
	 * delete schedule update to database
	 * */
	public void deleteSchedule() {

		final ConfirmDialog dialog = new ConfirmDialog(mContext, mContext
				.getResources().getString(R.string.delete_schedule));
		dialog.show();
		dialog.btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					ContentValues scv = new ContentValues();
					scv.put(ScheduleTable.is_Deleted, 1);
					scv.put(ScheduleTable.is_Synchronized, 0);

					int schedule_id = thisSchedule.getSchedule_ID();
					dbHelper.updateSchedule(schedule_id, scv);
					List<Integer> onduties = dbHelper
							.getOndutyRecordsForSchedule(schedule_id);
					for (int j = 0; j < onduties.size(); j++) {
						ContentValues ocv = new ContentValues();
						ocv.put(OndutyTable.is_Deleted, 1);
						ocv.put(OndutyTable.is_Synchronized, 0);
						int onduty_id = onduties.get(j);
						dbHelper.updateSchedule(onduty_id, ocv);
					}

					WebservicesHelper ws = new WebservicesHelper(mContext);
					ws.deleteSchedule(thisSchedule, deleteScheduleHandler);

					dialog.dismiss();

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		dialog.btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 333) {
			String activity_id = data.getStringExtra(CommConstant.ACTIVITY_ID);
			if (activity_id.equalsIgnoreCase(this.activity_id)) {
				pins = data
						.getIntegerArrayListExtra(CommConstant.ON_DUTY_ITEM_SELECTED);
				// String members = "";
				// if (pins != null && pins.size() > 0) {
				// for (int i = 0; i < pins.size(); i++) {
				// Sharedmember p = dbHelper.getSharedmember(pins.get(i),
				// activity_id);
				// if (p != null) {
				// if (i == 0)
				// members = members + p.getName();
				// else
				// members = members + "," + p.getName();
				// }
				// }
				// }
				// view.et_on_duty.setText(members);

				setParticipantOfActivity(pins);
			}
		} else if (resultCode == 222) {
			String activity_decription = data
					.getStringExtra(CommConstant.ACTIVITY_DESCRIPTION);
			view.et_new_activity_description
					.setText(activity_decription != null ? activity_decription
							: view.et_new_activity_description.getText()
									.toString());
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Utils.hideKeyboard(CreateNewScheduleActivity.this,
				view.et_new_activity_description);
	}
}