package com.dling61.calendarschedule.adapter;

import java.util.ArrayList;
import com.dling61.calendarschedule.R;
import com.dling61.calendarschedule.models.Participant;
import com.dling61.calendarschedule.utils.Utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ParticipantAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	public ArrayList<Participant> participants;

	Context mContext;
	boolean isCheck = false;
	boolean isShowFull = false;// show full information or breif

	public ParticipantAdapter(Context context,
			ArrayList<Participant> participants, boolean isCheck,
			boolean isShowFull) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		this.participants = participants;
		this.isCheck = isCheck;
		this.isShowFull = isShowFull;


	}

	public void setParticipants(ArrayList<Participant> participants) {
		this.participants = participants;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return participants.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub

		 ParticipantViewHolder viewHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.participantcell, null);
			viewHolder = new ParticipantViewHolder();
			viewHolder.name_tv = (TextView) convertView
					.findViewById(R.id.participant_name_tv);
			viewHolder.email_tv = (TextView) convertView
					.findViewById(R.id.participant_email_tv);
			viewHolder.mobile_tv = (TextView) convertView
					.findViewById(R.id.participant_mobile_tv);
			viewHolder.cb_check = (ImageView) convertView
					.findViewById(R.id.cb_check);
			viewHolder.name_tv.setTypeface(Utils.getTypeFace(mContext));
			viewHolder.email_tv.setTypeface(Utils.getTypeFace(mContext));
			viewHolder.mobile_tv.setTypeface(Utils.getTypeFace(mContext));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ParticipantViewHolder) convertView.getTag();
		}

		final Participant participant = participants.get(position);
		if (isCheck) {
			viewHolder.cb_check.setVisibility(View.VISIBLE);
			
			if(participant.isChecked)
			{
				
			
			viewHolder.cb_check.setImageResource(R.drawable.check_box_selected);;
			}
			else
			{
				viewHolder.cb_check.setImageResource(R.drawable.check_box_unselected);
			}
			
		} else {
			viewHolder.cb_check.setVisibility(View.GONE);
		}

		viewHolder.name_tv.setText(participant.getName());

		viewHolder.email_tv.setVisibility(View.GONE);
		viewHolder.mobile_tv.setVisibility(View.GONE);
//		if (isShowFull) {
//			viewHolder.email_tv.setText(participant.getEmail());
//
//			viewHolder.mobile_tv.setText(participant.getMobile());
//
//			viewHolder.email_tv.setVisibility(View.VISIBLE);
//			viewHolder.mobile_tv.setVisibility(View.VISIBLE);
//		} else {
//			viewHolder.email_tv.setVisibility(View.GONE);
//			viewHolder.mobile_tv.setVisibility(View.GONE);
//		}
		final ParticipantViewHolder view=viewHolder;
//		convertView.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				if (isCheck) {
//					participant.isChecked = !participant.isChecked;
//					participants.add(position, participant);
//					view.cb_check.setChecked(participant.isChecked);
//				} 
//				if(isShowFull){
//					Intent inforActivityIntent = new Intent(mContext,
//							AddNewContactActivity.class);
//					inforActivityIntent.putExtra(CommConstant.TYPE,
//							DatabaseHelper.EXISTED);
//					inforActivityIntent.putExtra(CommConstant.CONTACT_ID,
//							participant.getID());
//					mContext.startActivity(inforActivityIntent);
//				}
//			}
//		});
		return convertView;
	}

	static class ParticipantViewHolder {
		TextView name_tv;
		TextView email_tv;
		TextView mobile_tv;
		ImageView cb_check;
	}

}