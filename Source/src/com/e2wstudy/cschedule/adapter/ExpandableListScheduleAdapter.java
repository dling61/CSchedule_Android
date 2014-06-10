package com.e2wstudy.cschedule.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.devsmart.android.ui.HorizontalListView;
import com.e2wstudy.cschedule.CreateNewScheduleActivity;
import com.e2wstudy.cschedule.R;
import com.e2wstudy.cschedule.db.DatabaseHelper;
import com.e2wstudy.cschedule.models.MyActivity;
import com.e2wstudy.cschedule.models.Schedule;
import com.e2wstudy.cschedule.models.Sharedmember;
import com.e2wstudy.cschedule.utils.CommConstant;
import com.e2wstudy.cschedule.utils.MyDate;
import com.e2wstudy.cschedule.utils.Utils;
import com.e2wstudy.cschedule.views.DutyScheduleView;
import com.e2wstudy.cschedule.views.ParticipantInforDialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ExpandableListScheduleAdapter extends BaseExpandableListAdapter {

	private Context context;
	public Map<String, ArrayList<Schedule>> scheduleCollection;
	public ArrayList<String> listSchedulesByDay;
	private LayoutInflater mInflater;
	DatabaseHelper dbHelper;
	boolean isToday;
	Date nearestDate;// date nearest current date
	public int group_position_scrolled = 0;// scroll to nearest date

	public ExpandableListScheduleAdapter() {

	}

	public ExpandableListScheduleAdapter(Context context,
			ArrayList<String> listSchedulesByDay,
			Map<String, ArrayList<Schedule>> scheduleCollection) {
		this.context = context;
		this.scheduleCollection = scheduleCollection;
		this.listSchedulesByDay = listSchedulesByDay;

		mInflater = LayoutInflater.from(context);
		dbHelper = DatabaseHelper.getSharedDatabaseHelper(context);

	}

	public void setNearestDate(Date nearestDate) {
		this.nearestDate = nearestDate;
	}

	public Object getChild(int groupPosition, int childPosition) {
		return scheduleCollection.get(listSchedulesByDay.get(groupPosition))
				.get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ScheduleViewHolder viewHolder;
		if ((convertView == null)) {
			viewHolder = new ScheduleViewHolder();
			convertView = mInflater.inflate(R.layout.schedulecell, null);
			viewHolder.service_TV = (TextView) convertView
					.findViewById(R.id.schedule_servicename_tv);
			viewHolder.time_TV = (TextView) convertView
					.findViewById(R.id.schedule_date_tv);
			viewHolder.participants_TV = (TextView) convertView
					.findViewById(R.id.schedule_participants_tv);
			viewHolder.listview = (HorizontalListView) convertView
					.findViewById(R.id.listview);
			// viewHolder.listview=(ListView)convertView.findViewById(R.id.listview);
			viewHolder.service_TV.setTypeface(Utils.getTypeFace(context));
			viewHolder.time_TV.setTypeface(Utils.getTypeFace(context));
			viewHolder.participants_TV.setTypeface(Utils.getTypeFace(context));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ScheduleViewHolder) convertView.getTag();
		}

		final Schedule schedule = (Schedule) getChild(groupPosition,
				childPosition);
		if (schedule != null) {
			MyActivity activity = dbHelper
					.getActivity(schedule.getService_ID());
			String activity_name = activity != null ? activity
					.getActivity_name() : "";
			String date = MyDate.getTimeWithAPMFromUTCTime(schedule
					.getStarttime())
					+ " to "
					+ MyDate.getTimeWithAPMFromUTCTime(schedule.getEndtime());

			viewHolder.service_TV.setText(activity_name);
			viewHolder.time_TV.setText(date.toLowerCase());
			// viewHolder.participants_TV.setText(members);
			List<Integer> memberids = dbHelper
					.getParticipantsForSchedule(schedule.getSchedule_ID());
			if (memberids != null && memberids.size() > 0) {
				OnDutyMemberAdapter adapter = new OnDutyMemberAdapter(
						memberids, activity.getActivity_ID());
				viewHolder.listview.setAdapter(adapter);
			}
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					Intent inforActivityIntent = new Intent(context,
							CreateNewScheduleActivity.class);
					inforActivityIntent.putExtra(CommConstant.TYPE,
							DatabaseHelper.EXISTED);
					inforActivityIntent.putExtra(CommConstant.SCHEDULE_ID,
							schedule.getSchedule_ID());
					inforActivityIntent.putExtra(CommConstant.ACTIVITY_ID,
							schedule.getService_ID());
					inforActivityIntent.putExtra(CommConstant.CREATOR,
							schedule.getOwner_ID());
					context.startActivity(inforActivityIntent);
					Utils.pushRightToLeft(context);
				}
			});
		}
		return convertView;
	}

	public int getChildrenCount(int groupPosition) {
		if (scheduleCollection != null && listSchedulesByDay != null) {
			return scheduleCollection
					.get(listSchedulesByDay.get(groupPosition)).size();
		}
		return 0;
	}

	public Object getGroup(int groupPosition) {
		if (listSchedulesByDay == null) {
			return 0;
		}
		return listSchedulesByDay.get(groupPosition);
	}

	public int getGroupCount() {
		if (listSchedulesByDay == null) {
			return 0;
		}
		return listSchedulesByDay.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		HeaderViewHolder viewHolder;
		if ((convertView == null)
				|| (convertView.getTag().getClass().getName()
						.contains("ScheduleViewHolder"))) {
			viewHolder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.headercell, null);
			viewHolder.weekday_TV = (TextView) convertView
					.findViewById(R.id.schedule_weekday_tv);
			viewHolder.date_TV = (TextView) convertView
					.findViewById(R.id.schedule_date_tv);

			viewHolder.weekday_TV.setTypeface(Utils.getTypeFace(context));
			viewHolder.date_TV.setTypeface(Utils.getTypeFace(context));
			convertView.setTag(viewHolder);
		} else {
			// Log.i("class name", convertView.getTag().getClass().getName());
			viewHolder = (HeaderViewHolder) convertView.getTag();
		}
		// Schedule schedule=(Schedule) getGroup(groupPosition);
		// String weekday =
		// MyDate.getWeekdayFromUTCTime(listSchedulesByDay.get(groupPosition));
		// String date =
		// MyDate.transformUTCTimeToCustomStyle(this.getHeader(position));
		String date_time_str = listSchedulesByDay.get(groupPosition);

		if (date_time_str != null) {
			String[] date_time = date_time_str.split(";");

			if (date_time != null) {

				viewHolder.weekday_TV
						.setText(date_time[0] != null ? date_time[0] : "");
				viewHolder.date_TV.setText(date_time[1] != null ? date_time[1]
						: "");
				viewHolder.weekday_TV.setVisibility(View.VISIBLE);
				viewHolder.date_TV.setVisibility(View.VISIBLE);
			}
		}
		return convertView;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	static class ScheduleViewHolder {
		TextView service_TV;
		TextView time_TV;
		TextView participants_TV;
		HorizontalListView listview;// = (HorizontalListView)
									// findViewById(R.id.listview);
		// ListView listview;
	}

	static class HeaderViewHolder {
		TextView weekday_TV;
		TextView date_TV;
	}

	private class OnDutyMemberAdapter extends BaseAdapter {
		List<Integer> listParticipantId;
		String activity_id = "";

		public OnDutyMemberAdapter(List<Integer> listParticipantId,
				String activity_id) {
			this.listParticipantId = listParticipantId;
			this.activity_id = activity_id;

		}

		@Override
		public int getCount() {
			return listParticipantId.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			DutyScheduleView holder;
			//
			if (convertView == null) {
				holder = new DutyScheduleView(context);
				convertView = holder;
				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (DutyScheduleView) convertView.getTag();

			}
			int mem_id = listParticipantId.get(position);
			final Sharedmember sm = dbHelper.getSharedmember(mem_id,
					activity_id);
			if (sm != null) {
				holder.title.setText(sm.getName());
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						participantInforDialog(sm);
					}
				});
			}
			else
			{
				holder.setVisibility(View.GONE);
				convertView.setVisibility(View.GONE);
			}
			return holder;
		}

	};

	private void participantInforDialog(final Sharedmember participant) {
		if (participant != null) {
			String[] array = context.getResources().getStringArray(
					R.array.onduty_member_infor_array);
			int length = array.length;
			for (int i = 0; i < length; i++) {
				array[i] += " " + participant.getName();
			}
			TextViewBaseAdapter adapter = new TextViewBaseAdapter(context,
					array);

			final ParticipantInforDialog dialog = new ParticipantInforDialog(
					context);
			dialog.show();
			dialog.list_item.setAdapter(adapter);
			dialog.list_item.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					switch (position) {
					case 0:
						Utils.makeAPhoneCall(context, participant.getMobile());
						break;
					case 1:
						Utils.sendAMessage(context, participant.getMobile());
						break;
					case 2:
						Utils.sendAnEmail(context, participant.getEmail());
						break;
					default:
						break;
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
	}

	public void clearAdapter() {
		try {
			if (listSchedulesByDay != null) {
				listSchedulesByDay.clear();
			}
			if (scheduleCollection != null) {
				scheduleCollection.clear();
			}
			notifyDataSetChanged();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}