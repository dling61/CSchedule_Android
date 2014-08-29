/**
 * Develop by Antking
 * */
package com.e2wstudy.cschedule.net;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.e2wstude.schedule.interfaces.ActvityInterface;
import com.e2wstude.schedule.interfaces.ConfirmInterface;
import com.e2wstude.schedule.interfaces.ContactInterface;
import com.e2wstude.schedule.interfaces.LoginInterface;
import com.e2wstude.schedule.interfaces.ScheduleAbstract;
import com.e2wstude.schedule.interfaces.ScheduleInterface;
import com.e2wstudy.cschedule.CategoryTabActivity;
import com.e2wstudy.cschedule.LoginActivity;
import com.e2wstudy.cschedule.R;
import com.e2wstudy.cschedule.db.DatabaseHelper;
import com.e2wstudy.cschedule.models.ActivityTable;
import com.e2wstudy.cschedule.models.Confirm;
import com.e2wstudy.cschedule.models.MyActivity;
import com.e2wstudy.cschedule.models.OndutyTable;
import com.e2wstudy.cschedule.models.Participant;
import com.e2wstudy.cschedule.models.ParticipantTable;
import com.e2wstudy.cschedule.models.Schedule;
import com.e2wstudy.cschedule.models.ScheduleTable;
import com.e2wstudy.cschedule.models.SharedMemberTable;
import com.e2wstudy.cschedule.models.Sharedmember;
import com.e2wstudy.cschedule.models.TimeZoneTable;
import com.e2wstudy.cschedule.utils.CommConstant;
import com.e2wstudy.cschedule.utils.DownloadInterface;
import com.e2wstudy.cschedule.utils.MyDate;
import com.e2wstudy.cschedule.utils.SharedReference;
import com.e2wstudy.cschedule.utils.Utils;
import com.e2wstudy.cschedule.views.LoadingPopupViewHolder;
import com.e2wstudy.cschedule.views.ToastDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.e2wstudy.cschedule.models.AppVersionTable;
import com.google.android.gms.drive.internal.e;

//import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * @class WebservicesHelper
 * @author Huyen Nguyen
 * @version 1.0
 * @Date: April 8th,2014 @ This class helps all process call webservices}
 * */
public class WebservicesHelper {
	private AsyncHttpClient client;
	Context mContext;

	// progress dialog
	ProgressDialog progress = null;
	DatabaseHelper dbHelper;
	public static LoadingPopupViewHolder loadingPopup;
	public static final int DIALOG_LOADING_THEME = android.R.style.Theme_Translucent_NoTitleBar;

	String gcmId;
	// GoogleCloudMessaging gcm;
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	String TAG = "WebservicesHelper";

	String userAgentString = "Mozilla/5.0 (Linux; <Android Version>; <Build Tag etc.>) AppleWebKit/<WebKit Rev> (KHTML, like Gecko) Chrome/<Chrome Rev> Mobile Safari/<WebKit Rev>";

	// show loading
	public void showLoading(Context mContext) {
		try {
			if (loadingPopup == null) {
				loadingPopup = new LoadingPopupViewHolder(mContext,
						CategoryTabActivity.DIALOG_LOADING_THEME);
			}
			loadingPopup.setCancelable(true);

			loadingPopup.show();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void dimissDialog() {
		try {
			// if (loadingPopup != null && loadingPopup.isShowing()) {
			loadingPopup.dismiss();
			loadingPopup.cancel();
			// }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Constructor initial progress dialog
	 * */
	public WebservicesHelper(Context context) {

		client = new AsyncHttpClient();
		// client.setTimeout(100);
		// PersistentCookieStore myCookieStore = new
		// PersistentCookieStore(mContext);
		client.setTimeout(10000);
		// client.setMaxRetriesAndTimeout(3, 5000);

		// client.setCookieStore(myCookieStore);

		this.mContext = context;
		if (progress == null) {
			// display progress dialog like this
			progress = new ProgressDialog(mContext);
			progress.setCancelable(false);
			progress.setMessage(mContext.getResources().getString(
					R.string.processing));
		}
		dbHelper = DatabaseHelper.getSharedDatabaseHelper(context);

		if (loadingPopup == null) {
			loadingPopup = new LoadingPopupViewHolder(context,
					DIALOG_LOADING_THEME);
		}
		loadingPopup.setCancelable(true);
	}

	/**
	 * Create account If success, go to login else show toast notify create
	 * account failure
	 * */
	public void createAccount(final String email, final String password,
			String username, String mobile) {

		String signUpUrl = BaseUrl.BASEURL + "creator?action=register" + "&"
				+ BaseUrl.URL_POST_FIX;
		try {
			JSONObject jsonParams = new JSONObject();
			jsonParams.put(CommConstant.EMAIL, email);
			jsonParams.put(CommConstant.PASSWORD, password);
			jsonParams.put(CommConstant.USERNAME, username);
			jsonParams.put(CommConstant.MOBILE, mobile);
			client.addHeader("Content-type", "application/json");
			client.addHeader("User-Agent", userAgentString);
			StringEntity entity = new StringEntity(jsonParams.toString());
			if (Utils.isNetworkOnline(mContext)) {
				client.post(null, signUpUrl, entity, "application/json",
						new JsonHttpResponseHandler() {

							@Override
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								Log.i("successful response",
										response.toString());

								if (response != null) {
									Log.d("go there", "success not null");
									// TODO Auto-generated method stub
									try {
										if (!response.get("error message")
												.toString().startsWith("200")) {
											final ToastDialog dialog = new ToastDialog(
													mContext, response.get(
															"error message")
															.toString());
											dialog.show();
											dialog.btnOk
													.setOnClickListener(new OnClickListener() {

														@Override
														public void onClick(
																View v) {
															dialog.dismiss();
														}
													});
										}
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									try {
										if (response.get(CommConstant.OWNER_ID) != null) {

											((Activity) mContext).finish();
											Intent intent = new Intent(
													mContext,
													LoginActivity.class);
											intent.putExtra(CommConstant.EMAIL,
													email);
											intent.putExtra(
													CommConstant.PASSWORD,
													password);
											mContext.startActivity(intent);
											Utils.postLeftToRight(mContext);
										}
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}

							// public void onSuccess(JSONObject response) {
							// Log.i("successful response",
							// response.toString());
							//
							// if (response != null) {
							// Log.d("go there", "success not null");
							// // TODO Auto-generated method stub
							// try {
							// if (!response.get("error message")
							// .toString().startsWith("200")) {
							// final ToastDialog dialog = new ToastDialog(
							// mContext, response.get(
							// "error message")
							// .toString());
							// dialog.show();
							// dialog.btnOk
							// .setOnClickListener(new OnClickListener() {
							//
							// @Override
							// public void onClick(
							// View v) {
							// dialog.dismiss();
							// }
							// });
							// }
							// } catch (JSONException e) {
							// // TODO Auto-generated catch block
							// e.printStackTrace();
							// }
							// try {
							// if (response.get(CommConstant.OWNER_ID) != null)
							// {
							//
							// ((Activity) mContext).finish();
							// Intent intent = new Intent(
							// mContext,
							// LoginActivity.class);
							// intent.putExtra(CommConstant.EMAIL,
							// email);
							// intent.putExtra(
							// CommConstant.PASSWORD,
							// password);
							// mContext.startActivity(intent);
							// Utils.postLeftToRight(mContext);
							// }
							// } catch (JSONException e) {
							// // TODO Auto-generated catch block
							// e.printStackTrace();
							// }
							// }
							// }

							// public void onFailure(Throwable e, String
							// response) {
							@Override
							public void onFailure(int statusCode,
									Header[] headers, String response,
									Throwable throwable) {
								// TODO Auto-generated method stub
								super.onFailure(statusCode, headers, response,
										throwable);
								try {
									dimissDialog();
								} catch (Exception ex) {
									ex.printStackTrace();
								}
								if (response != null) {
									final ToastDialog dialog = new ToastDialog(
											mContext,
											mContext.getResources()
													.getString(
															R.string.create_acc_failure));
									dialog.show();
									dialog.btnOk
											.setOnClickListener(new OnClickListener() {

												@Override
												public void onClick(View v) {
													dialog.dismiss();
												}
											});

									try {
										// if (progress.isShowing()) {
										// progress.dismiss();
										// }
										dimissDialog();
									} catch (Exception ex) {
										ex.printStackTrace();
									}
								}

							}

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								super.onStart();
								showLoading(mContext);
							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub
								super.onFinish();
								try {
									// if (progress.isShowing()) {
									// progress.dismiss();
									// }
									dimissDialog();
								} catch (Exception ex) {
									ex.printStackTrace();
								}

							}
						});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}

		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	// edit information
	class RegisterGcm extends AsyncTask<String, Void, String> {

		// ProgressDialog dialog;
		Context mContext;

		String userId;
		String deviceId;
		String registrationId;

		// show loading
		public void showLoading(Context mContext) {
			try {
				// if (loadingPopup == null) {
				// loadingPopup = new LoadingPopupViewHolder(mContext,
				// CategoryTabActivity.DIALOG_LOADING_THEME);
				// }
				// loadingPopup.setCancelable(false);

				loadingPopup.show();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public void dimissDialog() {
			try {
				// if (loadingPopup != null && loadingPopup.isShowing()) {
				loadingPopup.dismiss();
				loadingPopup.cancel();
				// }
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public RegisterGcm(Context mContext, final String userId,
				final String deviceId, String registrationId) {
			this.mContext = mContext;
			this.userId = userId;
			this.deviceId = deviceId;
			this.registrationId = registrationId;
			if (loadingPopup == null) {
				loadingPopup = new LoadingPopupViewHolder(mContext,
						DIALOG_LOADING_THEME);
			}
			loadingPopup.setCancelable(true);

		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			// dialog.show();
			showLoading(mContext);
		}

		@Override
		protected String doInBackground(String... params) {
			// if register successfully, it's logged in automatically on server
			try {
				String setToken = BaseUrl.BASEURL + "creator?action=settoken"
						+ "&" + BaseUrl.URL_POST_FIX;
				Log.i("set token url is", setToken);

				JSONObject jsonParams = new JSONObject();
				jsonParams.put("userid", userId);
				jsonParams.put("udid", deviceId);
				jsonParams.put("token", registrationId);
				// client.addHeader("Content-type", "application/json");

				Log.d("set gcm param", jsonParams.toString());
				return JSONParser.getJsonFromURLPostNameValuePair(setToken,
						jsonParams.toString());
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();

			}
			return "";

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			try {
				if (result != null) {
					if (result.contains("200")) {
						Log.d("set token", "success");

					} else if (result.contains("201")) {
						final ToastDialog dialog = new ToastDialog(mContext,
								"The token can�t be inserted");
						dialog.show();

						dialog.btnOk.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						});
					} else if (result.equals("202")) {
						final ToastDialog dialog = new ToastDialog(mContext,
								"The token can�t be updated");
						dialog.show();
						dialog.btnOk.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						});

					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();

			}
			dimissDialog();
		}
	}

	private void onLoginSuccess(JSONObject response, String email) {
		Log.i("successful response", response.toString());

		try {

			int ownerid = response.getInt("ownerid");

			String username = response.getString("username");
			SharedReference sharedReference = new SharedReference();
			sharedReference.setUsername(mContext, username);

			sharedReference.setAccount(mContext, response.toString());

			Log.i("SignIn ownerid ", ownerid + "");
			int idCostant = ownerid * 10000;

			sharedReference.setCurrentParticipant(mContext, idCostant + "");

			// Check id
			int nextserviceidOriginal = response.getInt("serviceid");
			Log.i("nextserviceidOriginal", nextserviceidOriginal + "");
			int nextmemberidOriginal = response.getInt("memberid");
			Log.i("nextmemberidOriginal", nextmemberidOriginal + "");
			int nextscheduleidOriginal = response.getInt("scheduleid");
			Log.i("nextscheduleidOriginal", nextscheduleidOriginal + "");
			int nextserviceid = -1;
			// If it is the first time to establish an
			// activity
			if (nextserviceidOriginal == 0) {
				nextserviceid = idCostant + nextserviceidOriginal + 1;

			}
			// If activities has established
			else {
				nextserviceid = nextserviceidOriginal + 1;
				Log.i("SignIn nextserviceid ", nextserviceid + "");
			}
			int nextmemberid = -1;
			// If it is the first time to establish a
			// member
			if (nextmemberidOriginal == 0) {
				nextmemberid = idCostant + nextmemberidOriginal + 1;
				Log.i("SignIn nextserviceid ", nextmemberid + "");
			}
			// If members has established
			else {
				nextmemberid = nextmemberidOriginal + 1;
				Log.i("SignIn nextserviceid ", nextmemberid + "");
			}
			int nextscheduleid = -1;
			// If it is the first time to establish a
			// schedule
			if (nextscheduleidOriginal == 0) {
				nextscheduleid = idCostant + nextscheduleidOriginal + 1;
				Log.i("SignIn nextscheduleid ", nextscheduleid + "");
			} else {
				nextscheduleid = nextscheduleidOriginal + 1;
				Log.i("SignIn nextscheduleid ", nextscheduleid + "");
			}

			int nextSharedMemberId = -1;
			// If it is the first time to establish a
			// schedule
			if (nextscheduleidOriginal == 0) {
				nextscheduleid = idCostant + nextscheduleidOriginal + 1;
				Log.i("SignIn nextscheduleid ", nextscheduleid + "");
			} else {
				nextscheduleid = nextscheduleidOriginal + 1;
				Log.i("SignIn nextscheduleid ", nextscheduleid + "");
			}

			sharedReference
					.setInformationUserLogined(mContext, username, email,
							ownerid, nextserviceid, nextmemberid,
							nextscheduleid);

			new RegisterGcm(mContext, String.valueOf(ownerid),
					Utils.getDeviceId(mContext),
					new SharedReference().getRegistrationId(mContext))
					.execute();

			uploadRecentEditedActivitiesToWeb();
			uploadRecentEditedParticipantsToWeb();
			uploadRecentNewActivitiesToWeb();
			uploadRecentNewParticipantsToWeb();
			uploadRecentNewSchedulesToWeb();

			try {
				dimissDialog();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			((Activity) mContext).finish();
			Utils.postLeftToRight(mContext);

			Intent intent = new Intent(mContext, CategoryTabActivity.class);
			mContext.startActivity(intent);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Login if success go to TabAcivity else show Toast notify login fail
	 * */
	public void login(final String email, final String password)
			throws JSONException {
		String signInUrl = BaseUrl.BASEURL + "creator?action=signin" + "&"
				+ BaseUrl.URL_POST_FIX;
		Log.i("sign in url is", signInUrl);
		try {
			JSONObject jsonParams = new JSONObject();
			jsonParams.put(CommConstant.PASSWORD, password);
			jsonParams.put(CommConstant.EMAIL, email);
			client.addHeader("User-Agent", userAgentString);
			StringEntity entity = new StringEntity(jsonParams.toString());
			Log.d("login param", jsonParams.toString());

			if (Utils.isNetworkOnline(mContext)) {
				client.post(null, signInUrl, entity, "application/json",
						new JsonHttpResponseHandler() {

							@Override
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								onLoginSuccess(response, email);

							}

							@Override
							public void onFailure(int statusCode,
									Header[] headers, String responseString,
									Throwable throwable) {
								// TODO Auto-generated method stub
								super.onFailure(statusCode, headers,
										responseString, throwable);

								Log.d("login", "failure " + responseString);

								try {

									dimissDialog();
								} catch (Exception ex) {
									ex.printStackTrace();
								}
								try {
									final ToastDialog noNetwork = new ToastDialog(
											mContext,
											"Invalid email or password");
									noNetwork.show();
									noNetwork.btnOk
											.setOnClickListener(new OnClickListener() {

												@Override
												public void onClick(View v) {
													noNetwork.dismiss();
												}
											});

								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								super.onStart();
								showLoading(mContext);
							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub
								super.onFinish();
								try {
									// if (progress.isShowing()) {
									// progress.dismiss();
									// }
									dimissDialog();
								} catch (Exception ex) {
									ex.printStackTrace();
								}

							}
						});
			} else {

				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void onCompleteGetAllActivity(ActvityInterface activityInterface,
			JSONObject response,ScheduleInterface scheduleInterface) {
		final SharedReference ref = new SharedReference();
		final DatabaseHelper dbHelper = DatabaseHelper
				.getSharedDatabaseHelper(mContext);

		Log.i("get all activity", response.toString());
		try {
			// deleted services and schedule relationship with
			// this
			// service
			JSONArray deleted_services = response
					.getJSONArray("deletedservices");
			int deleted_services_count = deleted_services.length();
			if (deleted_services_count > 0) {
				for (int i = 0; i < deleted_services_count; i++) {
					String id = deleted_services.getString(i);
					List<Schedule> sbelongtoa = dbHelper
							.getSchedulesBelongtoActivity(id);
					for (int j = 0; j < sbelongtoa.size(); j++) {
						Schedule schedule = sbelongtoa.get(j);
						dbHelper.deleteRelatedOnduty(schedule.getSchedule_ID());
						dbHelper.deleteSchedule(schedule.getSchedule_ID());
					}
					dbHelper.deleteActivity(id);
				}
			}

			// services
			JSONArray services = response.getJSONArray("services");
			int service_count = services.length();

			WebservicesHelper ws = new WebservicesHelper(mContext);
			for (int i = 0; i < service_count; i++) {
				JSONObject service = services.getJSONObject(i);
				ContentValues newActivity = new ContentValues();
				int ownid = service.getInt("creatorid");
				newActivity.put(ActivityTable.own_ID, ownid);
				Log.i("getActivitiesFromWeb own_ID ", ownid + "");
				String activityid = service.getString("serviceid");

				String serviceName = service.getString("servicename");
				newActivity.put(ActivityTable.service_Name, serviceName);
				Log.i("getActivitiesFromWeb service_Name ", serviceName + "");
				int role = service.getInt("sharedrole");
				newActivity.put(ActivityTable.sharedrole, role);
				Log.i("getActivitiesFromWeb sharedrole ", role + "");

				String description = service.getString("desp");
				newActivity.put(ActivityTable.service_description, description);

				Log.i("getActivitiesFromWeb service_description ", description
						+ "");

				int is_deleted = 0;
				newActivity.put(ActivityTable.is_Deleted, is_deleted);
				int is_synchronized = 1;
				newActivity.put(ActivityTable.is_Synchronized, is_synchronized);
				newActivity.put(ActivityTable.user_login,
						new SharedReference().getCurrentOwnerId(mContext));
				String last_modified = service.getString("lastmodified");
				newActivity.put(ActivityTable.last_ModifiedTime, last_modified);
				Log.i("getActivitiesFromWeb lastmodified ", last_modified + "");

				if (dbHelper.isActivityExisted(activityid) == false) {
					newActivity.put(ActivityTable.service_ID, activityid);
					Log.i("getActivitiesFromWeb service_ID ", activityid + "");
					if (dbHelper.insertActivity(newActivity))
						Log.i("database", "insert service " + serviceName
								+ " successfully!");
				} else {
					if (dbHelper.updateActivity(activityid, newActivity))
						Log.i("database", "update service " + serviceName
								+ " successfully!");
				}

				ws.getSharedmembersForActivity(activityid);
				// TODO: will delete if service get all schedule
				// implemented
				ws.getSchedulesForActivity(activityid,scheduleInterface);

			}
			activityInterface.onComplete();

			ref.setLastestServiceLastModifiedTime(mContext, MyDate
					.transformPhoneDateTimeToUTCFormat(MyDate
							.getCurrentDateTime()));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * get all activity of current owner id
	 * */
	public void getAllActivitys(final ActvityInterface activityInterface,final ScheduleInterface scheduleInterface) {
		String activityUrl = BaseUrl.BASEURL + "services" + "?"
				+ BaseUrl.URL_POST_FIX;
		Log.i("get all activity url is :", activityUrl);
		final SharedReference ref = new SharedReference();
		int currentOwnerID = ref.getCurrentOwnerId(mContext);
		RequestParams params = new RequestParams();
		params.put(CommConstant.OWNER_ID, String.valueOf(currentOwnerID));
		params.put(CommConstant.LAST_UPDATE_TIME,
				ref.getLastestServiceLastModifiedTime(mContext));
		Log.d("param activity all", params.toString());
		client.addHeader("Content-type", "application/json");
		try {
			if (Utils.isNetworkOnline(mContext)) {
				client.get(activityUrl, params, new JsonHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						// TODO Auto-generated method stub
						super.onSuccess(statusCode, headers, response);
						onCompleteGetAllActivity(activityInterface, response,scheduleInterface);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String response, Throwable throwable) {
						// TODO Auto-generated method stub
						super.onFailure(statusCode, headers, response,
								throwable);

						try {
							dimissDialog();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						final ToastDialog dialog = new ToastDialog(mContext,
								mContext.getResources().getString(
										R.string.error_load_activity));
						dialog.show();
						dialog.btnOk.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						});
					}
				});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void onCompleteGetAllSchedule(JSONObject response,
			ActvityInterface activityInterface) {
		Log.i("all schedule", response.toString());
		try {
			JSONArray deletedschedules = response
					.getJSONArray("deletedschedules");
			int deleted_services_count = deletedschedules.length();
			if (deleted_services_count > 0) {
				for (int i = 0; i < deleted_services_count; i++) {
					int id = deletedschedules.getInt(i);
					if (dbHelper.isOndutyExisted(id)) {
						// Log.i("database", "schedule " +
						// scheduleID +
						// " already exists");
						if (dbHelper.deleteRelatedOnduty(id)) {
							// Log.i("database",
							// "delete schedule " +
							// scheduleID + " successfully!");
						}
					}
					dbHelper.deleteSchedule(id);
				}
			}

			JSONArray schedules = response.getJSONArray("schedules");
			int scheudule_count = schedules.length();
			for (int i = 0; i < scheudule_count; i++) {
				JSONObject Schedule = schedules.getJSONObject(i);
				ContentValues cv = new ContentValues();
				cv.put(ScheduleTable.own_ID, Schedule.getInt("creatorid"));

				cv.put(ScheduleTable.last_Modified,
						Schedule.getString("lastmodified"));
				cv.put(ScheduleTable.start_Time,
						Schedule.getString("startdatetime"));
				cv.put(ScheduleTable.schedule_Description,
						Schedule.getString("desp"));
				cv.put(ScheduleTable.end_Time,
						Schedule.getString("enddatetime"));
				cv.put(ScheduleTable.service_ID, Schedule.getInt("serviceid"));
				cv.put(ScheduleTable.is_Deleted, 0);
				cv.put(ScheduleTable.is_Synchronized, 1);
				cv.put(ActivityTable.user_login,
						new SharedReference().getCurrentOwnerId(mContext));

				int scheduleID = Schedule.getInt("scheduleid");

				if (dbHelper.isScheduleExisted(scheduleID) == false) {
					cv.put(ScheduleTable.schedule_ID,
							Schedule.getInt("scheduleid"));
					Log.i("scheduleid", "= " + Schedule.getInt("scheduleid"));
					if (dbHelper.insertSchedule(cv)) {
						// Log.i("database", "insert schedule "
						// +
						// scheduleID + " successfully!");
					}
				} else {
					if (dbHelper.updateSchedule(scheduleID, cv))
						;
					{
						// Log.i("database", "update schedule "
						// +
						// scheduleID + " successfully!");
					}
				}

				if (dbHelper.isOndutyExisted(scheduleID)) {
					if (dbHelper.deleteRelatedOnduty(scheduleID)) {
					}
				}

				Log.i("getschedule", "done");

				String[] members = Schedule.getString("members").split(",");
				// Log.i("Sync", "schedule " + scheduleID +
				// " has " +
				// members.length + " members");
				for (int j = 0; j < members.length; j++) {
					if (!members[j].equalsIgnoreCase("")) {
						int memberid = Integer.valueOf(members[j]);
						ContentValues newOnduty = new ContentValues();
						newOnduty.put(OndutyTable.service_ID,
								Schedule.getInt("serviceid"));
						newOnduty.put(OndutyTable.participant_ID, memberid);
						newOnduty.put(OndutyTable.schedule_ID,
								Schedule.getInt("scheduleid"));
						newOnduty.put(OndutyTable.last_Modified,
								Schedule.getString("lastmodified"));
						newOnduty.put(OndutyTable.is_Deleted, 0);
						newOnduty.put(OndutyTable.is_Synchronized, 1);

						if (dbHelper.insertOnduty(newOnduty)) {
						}
					}
				}
				Log.i("getmembers", "done");
			}

			// Intent intent = new Intent(
			// CommConstant.SCHEDULE_READY);
			// mContext.sendBroadcast(intent);
			activityInterface.onComplete();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get all schedule of owner id
	 * */
	public void getAllSchedule(final ActvityInterface activityInterface) {
		String scheduleUrl = BaseUrl.BASEURL + "schedules" + "?"
				+ BaseUrl.URL_POST_FIX;
		final SharedReference ref = new SharedReference();
		int currentOwnerID = ref.getCurrentOwnerId(mContext);
		Log.i("get all schedule url:", scheduleUrl);
		RequestParams params = new RequestParams();
		params.put("ownerid", String.valueOf(currentOwnerID));
		params.put("lastupdatetime",
				ref.getLastestScheduleLastModifiedTime(mContext));
		try {
			if (Utils.isNetworkOnline(mContext)) {
				client.get(scheduleUrl, params, new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						// TODO Auto-generated method stub
						super.onSuccess(statusCode, headers, response);
						onCompleteGetAllSchedule(response, activityInterface);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String response, Throwable throwable) {
						// TODO Auto-generated method stub
						super.onFailure(statusCode, headers, response,
								throwable);
						try {
							dimissDialog();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						// Response failed :(
						// Toast.makeText(mContext,mContext.getResources().getString(R.string.error_load_schedule),
						// Toast.LENGTH_LONG).show();
						final ToastDialog dialog = new ToastDialog(mContext,
								mContext.getResources().getString(
										R.string.error_load_schedule));
						dialog.show();
						dialog.btnOk.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						});
					}

					//
					@Override
					public void onFinish() {
						// TODO Auto-generated method stub
						super.onFinish();
						try {
							CategoryTabActivity.flag_schedule = true;
							if (CategoryTabActivity.flag_activity
									&& CategoryTabActivity.flag_contact
									&& CategoryTabActivity.flag_schedule
									&& CategoryTabActivity.loadingPopup
											.isShowing()) {
								CategoryTabActivity.loadingPopup.dismiss();
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}

					}
				});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void onCompleteGetScheduleForActivity(JSONObject response,
			ScheduleInterface scheduleInterface) {
		Log.i("get schedule item", response.toString());
		try {
			JSONArray deletedschedules = response
					.getJSONArray("deletedschedules");
			int deleted_services_count = deletedschedules.length();
			if (deleted_services_count > 0) {
				for (int i = 0; i < deleted_services_count; i++) {
					int id = deletedschedules.getInt(i);
					if (dbHelper.isOndutyExisted(id)) {
						// Log.i("database", "schedule " +
						// scheduleID +
						// " already exists");
						if (dbHelper.deleteRelatedOnduty(id)) {
							// Log.i("database",
							// "delete schedule " +
							// scheduleID + " successfully!");
						}
					}
					dbHelper.deleteSchedule(id);
				}
			}

			JSONArray schedules = response.getJSONArray("schedules");
			int scheudule_count = schedules.length();
			for (int i = 0; i < scheudule_count; i++) {
				JSONObject schedule = schedules.getJSONObject(i);
				ContentValues cv = new ContentValues();
				cv.put(ScheduleTable.own_ID, schedule.getInt("creatorid"));

				cv.put(ScheduleTable.last_Modified,
						schedule.getString("lastmodified"));
				cv.put(ScheduleTable.start_Time,
						schedule.getString("startdatetime"));
				cv.put(ScheduleTable.schedule_Description,
						schedule.getString("desp"));
				cv.put(ScheduleTable.end_Time,
						schedule.getString("enddatetime"));
				cv.put(ScheduleTable.service_ID, schedule.getInt("serviceid"));
				cv.put(ScheduleTable.is_Deleted, 0);
				cv.put(ScheduleTable.is_Synchronized, 1);
				cv.put(ActivityTable.user_login,
						new SharedReference().getCurrentOwnerId(mContext));
				cv.put(ScheduleTable.timeZone, schedule.getString("tzid"));
				cv.put(ScheduleTable.alert, schedule.getString("alert"));

				int scheduleID = schedule.getInt("scheduleid");
				// Log.i("Webservice","schedule " + scheduleID +
				// " has members " +
				// Schedule.getString("members"));
				if (dbHelper.isScheduleExisted(scheduleID) == false) {
					cv.put(ScheduleTable.schedule_ID,
							schedule.getInt("scheduleid"));
					Log.i("scheduleid", "= " + schedule.getInt("scheduleid"));
					if (dbHelper.insertSchedule(cv)) {
						// Log.i("database", "insert schedule "
						// +
						// scheduleID + " successfully!");
					}
				} else {
					if (dbHelper.updateSchedule(scheduleID, cv))

					{
						// Log.i("database", "update schedule "
						// +
						// scheduleID + " successfully!");
					}
				}

				if (dbHelper.isOndutyExisted(scheduleID)) {
					// Log.i("database", "schedule " +
					// scheduleID +
					// " already exists");
					if (dbHelper.deleteRelatedOnduty(scheduleID)) {
						// Log.i("database", "delete schedule "
						// +
						// scheduleID + " successfully!");
					}
				}

				Log.i("getschedule", "done");

				JSONArray members = schedule.getJSONArray("members");
				if (members != null) {
					int size = members.length();
					for (int j = 0; j < size; j++) {
						JSONObject obj = members.getJSONObject(j);

						int memberId = obj.getInt("memberid");
						int confirmId = obj.getInt("confirm");
						ContentValues newOnduty = new ContentValues();
						newOnduty.put(OndutyTable.service_ID,
								schedule.getInt("serviceid"));
						newOnduty.put(OndutyTable.participant_ID, memberId);
						newOnduty.put(OndutyTable.confirm, confirmId);
						newOnduty.put(OndutyTable.schedule_ID,
								schedule.getInt("scheduleid"));
						newOnduty.put(OndutyTable.last_Modified,
								schedule.getString("lastmodified"));
						newOnduty.put(OndutyTable.is_Deleted, 0);
						newOnduty.put(OndutyTable.is_Synchronized, 1);

						if (dbHelper.insertOnduty(newOnduty)) {

						}

					}
					Log.i("getmembers", "done");
				}

			}
			SharedReference ref = new SharedReference();
			ref.setLastestScheduleLastModifiedTime(mContext, MyDate
					.transformPhoneDateTimeToUTCFormat(MyDate
							.getCurrentDateTime()));
			scheduleInterface.onComplete();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getSchedulesForActivity(String activityid,
			final ScheduleInterface scheduleInterface) {
		String SchedulesUrl = BaseUrl.BASEURL + "services/" + activityid
				+ "/schedules" + "?" + BaseUrl.URL_POST_FIX;
		final SharedReference ref = new SharedReference();
		int currentOwnerID = ref.getCurrentOwnerId(mContext);
		Log.i("schedules url:", SchedulesUrl);
		RequestParams params = new RequestParams();
		params.put("ownerid", String.valueOf(currentOwnerID));
		params.put("lastupdatetime",
				ref.getLastestScheduleLastModifiedTime(mContext));
		Log.d("schedule param", params.toString());
		try {

			if (Utils.isNetworkOnline(mContext)) {
				client.get(SchedulesUrl, params, new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						// TODO Auto-generated method stub
						super.onSuccess(statusCode, headers, response);
						onCompleteGetScheduleForActivity(response,
								scheduleInterface);
					}

					@Override
					public void onStart() {
						// TODO Auto-generated method stub
						super.onStart();
						try {
							showLoading(mContext);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

					@Override
					public void onFinish() {
						// TODO Auto-generated method stub
						super.onFinish();
						try {
							dimissDialog();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

					// public void onFailure(Throwable e, String response) {
					@Override
					public void onFailure(int statusCode, Header[] headers,
							String response, Throwable throwable) {
						// TODO Auto-generated method stub
						super.onFailure(statusCode, headers, response,
								throwable);
						dimissDialog();
						final ToastDialog dialog = new ToastDialog(mContext,
								mContext.getResources().getString(
										R.string.error_load_schedule));
						dialog.show();
						dialog.btnOk.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						});
					}
				});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void onCompleteGetParticipant(JSONObject response,
			ContactInterface contactInterface) {
		Log.i("successful response", response.toString());
		try {

			// deleted member, delete member from
			// activity which that
			// member joined in
			JSONArray deleted_member = response.getJSONArray("deletedmembers");
			int delete_members_count = deleted_member.length();
			if (delete_members_count > 0) {

				for (int i = 0; i < delete_members_count; i++) {
					int id = deleted_member.getInt(i);
					dbHelper.deleteParticipant(id);

				}
			}

			JSONArray participants = response.getJSONArray("members");
			int participant_count = participants.length();
			for (int i = 0; i < participant_count; i++) {
				JSONObject Participant = participants.getJSONObject(i);
				int ownerid = Participant.getInt("creatorid");

				// Owner is included in JSON Response
				// "members"
				// Should not appear in Participants

				ContentValues cv = new ContentValues();
				cv.put(ParticipantTable.last_Modified,
						Participant.getString("lastmodified"));
				cv.put(ParticipantTable.participant_Name,
						Participant.getString("membername"));
				// cv.put(ParticipantTable.own_ID,
				// Participant.getInt("creatorid"));
				cv.put(ParticipantTable.own_ID, ownerid);
				Log.i("getParticipantsFromWeb own_ID ", ownerid + "");

				String participant_Mobile = Participant
						.getString("mobilenumber");
				cv.put(ParticipantTable.participant_Mobile, participant_Mobile);
				Log.i("getParticipantsFromWeb participant_Mobile ",
						participant_Mobile);
				String participant_Email = Participant.getString("memberemail");
				Log.i("getParticipantsFromWeb participant_Email ",
						participant_Email);
				cv.put(ParticipantTable.participant_Email, participant_Email);
				cv.put(ParticipantTable.is_Registered, 1);
				cv.put(ParticipantTable.is_Deleted, 0);
				cv.put(ParticipantTable.is_Sychronized, 1);
				cv.put(ActivityTable.user_login,
						new SharedReference().getCurrentOwnerId(mContext));
				int participantID = Participant.getInt("memberid");

				if (dbHelper.isParticipantExisted(participantID) == false) {
					// int participant_ID = (Participant
					// .getInt("memberid"));
					cv.put(ParticipantTable.participant_ID, participantID);
					Log.i("getParticipantsFromWeb participant_ID ",
							participantID + "");
					if (dbHelper.insertParticipant(cv))
						Log.i("database",
								"insert participant "
										+ Participant.getString("membername")
										+ " successfully!");
				} else {
					if (dbHelper.updateParticipant(participantID, cv))
						Log.i("database",
								"update participant "
										+ Participant.getString("membername")
										+ " successfully!");
				}
			}
			SharedReference ref = new SharedReference();
			ref.setLastestParticipantLastModifiedTime(mContext, MyDate
					.transformPhoneDateTimeToUTCFormat(MyDate
							.getCurrentDateTime()));

			Intent intent = new Intent(CommConstant.PARTICIPANT_READY);
			mContext.sendBroadcast(intent);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getParticipantsFromWeb(final ContactInterface activityInterface) {
		String ParticipantUrl = BaseUrl.BASEURL + "members" + "?"
				+ BaseUrl.URL_POST_FIX;
		Log.i("url is :", ParticipantUrl);
		final SharedReference ref = new SharedReference();
		int currentOwnerID = ref.getCurrentOwnerId(mContext);
		RequestParams params = new RequestParams();
		params.put("ownerid", String.valueOf(currentOwnerID));
		params.put("lastupdatetime",
				ref.getLastestParticipantLastModifiedTime(mContext));

		try {

			if (Utils.isNetworkOnline(mContext)) {

				client.get(ParticipantUrl, params,
						new JsonHttpResponseHandler() {

							@Override
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								onCompleteGetParticipant(response,
										activityInterface);
							}

							@Override
							public void onFailure(int statusCode,
									Header[] headers, String response,
									Throwable throwable) {
								// TODO Auto-generated method stub
								super.onFailure(statusCode, headers, response,
										throwable);
								dimissDialog();
								// Response failed :(
								Log.i("webservice", "Get Activities failed");
								// Toast.makeText(mContext,mContext.getResources().getString(R.string.error_load_contact),
								// Toast.LENGTH_LONG).show();
								final ToastDialog dialog = new ToastDialog(
										mContext,
										mContext.getResources().getString(
												R.string.error_load_contact));
								dialog.show();
								dialog.btnOk
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												dialog.dismiss();
											}
										});
							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub
								super.onFinish();
								CategoryTabActivity.flag_contact = true;
								try {
									if (CategoryTabActivity.flag_activity
											&& CategoryTabActivity.flag_contact
											&& CategoryTabActivity.flag_schedule
											&& CategoryTabActivity.loadingPopup
													.isShowing()) {
										CategoryTabActivity.loadingPopup
												.dismiss();
									}
								} catch (Exception ex) {
									ex.printStackTrace();
								}

							}
						});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void onCompleteGetServerSetting(
			DownloadInterface downloadInterface, JSONObject response) {
		Log.e("timezone list", response.toString());
		try {
			JSONArray timeZoneList = response.getJSONArray("timezones");
			int timeZoneListSize = timeZoneList.length();
			for (int i = 0; i < timeZoneListSize; i++) {
				JSONObject timeZone = timeZoneList.getJSONObject(i);
				int id = timeZone.getInt("id");

				String timeZoneName = timeZone.getString("tzname");
				String displayname = timeZone.getString("displayname");
				String displayorder = timeZone.getString("displayorder");
				String abbrtzname = timeZone.getString("abbrtzname");
				String tzname = timeZone.getString("tzname");
				ContentValues cv = new ContentValues();

				cv.put(TimeZoneTable.abbrtzname, abbrtzname);
				cv.put(TimeZoneTable.displayname, displayname);

				cv.put(TimeZoneTable.displayorder, displayorder);
				cv.put(TimeZoneTable.tzname, tzname);

				if (dbHelper.isTimeZoneExisted(id) == false) {
					cv.put(TimeZoneTable.id, id);
					if (dbHelper.insertTimeZone(cv))
						Log.e("database", "insert timezone " + timeZoneName
								+ " successfully!");
				} else {
					if (dbHelper.updateTimeZone(id, cv))
						Log.e("database", "update timezone " + timeZoneName
								+ " successfully!");
				}
			}

			/**
			 * Alerts
			 * */
			JSONArray alertList = response.getJSONArray("alerts");
			int alertSize = alertList.length();
			for (int i = 0; i < alertSize; i++) {
				JSONObject timeZone = alertList.getJSONObject(i);
				int id = timeZone.getInt("id");

				String alert = timeZone.getString("aname");

				ContentValues cv = new ContentValues();
				cv.put(com.e2wstudy.cschedule.models.AlertTable.aname, alert);
				if (dbHelper.isAlertExisted(id) == false) {
					cv.put(com.e2wstudy.cschedule.models.AlertTable.id, id);
					if (dbHelper.insertAlert(cv))
						Log.e("database", "insert timezone " + alert
								+ " successfully!");
				} else {
					if (dbHelper.updateAlert(id, cv))
						Log.e("database", "update timezone " + alert
								+ " successfully!");
				}
			}

			/**
			 * app version
			 * */
			JSONArray appVersions = response.getJSONArray("appversions");
			int appVersionSize = appVersions.length();
			for (int i = 0; i < appVersionSize; i++) {
				JSONObject version = appVersions.getJSONObject(i);
				int id = version.getInt("id");
				String appversion = version.getString("appversion");
				int enforce = version.getInt("enforce");
				String os = version.getString("os");
				String osversion = version.getString("osversion");
				ContentValues cv = new ContentValues();
				cv.put(AppVersionTable.appversion, appversion);
				cv.put(AppVersionTable.os, os);
				cv.put(AppVersionTable.osversion, osversion);
				cv.put(AppVersionTable.enforce, enforce);

				if (dbHelper.isVersionExisted(id) == false) {
					cv.put(AppVersionTable.id, id);
					if (dbHelper.insertAppVersion(cv))
						Log.e("database", "insert appversion " + osversion
								+ " successfully!");
				} else {
					if (dbHelper.updateAppVersion(id, cv))
						Log.e("database", "update appversion " + osversion
								+ " successfully!");
				}
			}

			CommConstant.DOWNLOAD_SETTING = true;
			downloadInterface.onComplete();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get server setting from server
	 * */
	public void getServerSetting(final DownloadInterface downloadInterface) {
		String serverSetting = BaseUrl.BASEURL + "serversetting" + "?"
				+ BaseUrl.URL_POST_FIX;
		Log.i("url server setting is=", serverSetting);
		final SharedReference ref = new SharedReference();
		int currentOwnerID = ref.getCurrentOwnerId(mContext);
		RequestParams params = new RequestParams();
		params.put("ownerid", String.valueOf(currentOwnerID));
		params.put("lastupdatetime",
				ref.getLastestParticipantLastModifiedTime(mContext));
		params.put("appversion", Utils.getAppVersion(mContext));
		try {
			if (Utils.isNetworkOnline(mContext)) {

				client.get(serverSetting, params,
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								onCompleteGetServerSetting(downloadInterface,
										response);
							}

							@Override
							public void onFailure(int statusCode,
									Header[] headers, String responseString,
									Throwable throwable) {
								// TODO Auto-generated method stub
								super.onFailure(statusCode, headers,
										responseString, throwable);
								try {
									dimissDialog();
									downloadInterface.onError();

								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								super.onStart();
								showLoading(mContext);
							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub
								super.onFinish();
								try {
									dimissDialog();
								} catch (Exception ex) {
									ex.printStackTrace();
								}

							}
						});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Add new activity A request body { �ownerid�: 123434, �services�: {
	 * �serviceid�: �2222222�, �servicename�: �Food Service�, �desp�: �This is a
	 * cleaning service�, } }
	 * */
	public void addActivity(final MyActivity activity,
			final ActvityInterface activityInterface) {
		String ActivityUrl = BaseUrl.BASEURL + "services" + "?"
				+ BaseUrl.URL_POST_FIX;
		try {
			JSONObject activityParams = new JSONObject();
			activityParams.put("servicename", activity.getActivity_name());
			activityParams.put("serviceid", activity.getActivity_ID());
			activityParams.put("desp", activity.getDesp());
			JSONObject params = new JSONObject();
			params.put("ownerid", activity.getOwner_ID());
			params.put("services", activityParams);
			Log.d("add activity", params.toString());
			// client.addHeader("Content-type", "application/json");
			client.addHeader("User-Agent", userAgentString);
			Log.i("add activity", params.toString());
			StringEntity entity = new StringEntity(params.toString());

			if (Utils.isNetworkOnline(mContext)) {
				client.post(null, ActivityUrl, entity, "application/json",
						new JsonHttpResponseHandler() {

							@Override
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								Log.i("Add activity "
										+ activity.getActivity_name(),
										response.toString());
								try {
									ContentValues cv = new ContentValues();
									String last_modified = response
											.getString("lastmodified");
									cv.put(ActivityTable.last_ModifiedTime,
											last_modified);
									cv.put(ActivityTable.is_Synchronized, 1);
									dbHelper.updateActivity(
											activity.getActivity_ID(), cv);
									Log.i("last_modified", last_modified);
									if (dbHelper.updateActivity(
											activity.getActivity_ID(), cv)) {
									}

									SharedReference ref = new SharedReference();
									ref.setLastestServiceLastModifiedTime(
											mContext, last_modified);
									activityInterface.onComplete();
									((Activity) mContext).finish();
									Utils.postLeftToRight(mContext);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}

							@Override
							public void onFailure(int statusCode,
									Header[] headers, String response,
									Throwable throwable) {
								// TODO Auto-generated method stub
								super.onFailure(statusCode, headers, response,
										throwable);
								dimissDialog();
								final ToastDialog dialog = new ToastDialog(
										mContext,
										mContext.getResources().getString(
												R.string.create_activity_error)
												+ " " + response.toString());
								dialog.show();
								dialog.btnOk
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												dialog.dismiss();
											}
										});
							}

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								super.onStart();
								showLoading(mContext);
							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub
								super.onFinish();
								try {
									// if (progress.isShowing()) {
									// progress.dismiss();
									// }
									dimissDialog();
								} catch (Exception ex) {
									ex.printStackTrace();
								}

							}
						});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addSchedule(Schedule schedule, List<Confirm> pins,
			final ActvityInterface activityInterface) {
		// String ScheduleUrl = BASEURL + "schedules";
		String ScheduleUrl = BaseUrl.BASEURL + "schedules" + "?"
				+ BaseUrl.URL_POST_FIX;
		Log.d("create new scheduleUrl", ScheduleUrl);
		final int id = schedule.getSchedule_ID();
		try {
			JSONObject scheduleParams = new JSONObject();
			scheduleParams.put("scheduleid", schedule.getSchedule_ID());
			scheduleParams.put("desp", schedule.getDesp());
			scheduleParams.put("startdatetime", schedule.getStarttime());
			scheduleParams.put("enddatetime", schedule.getEndtime());
			scheduleParams.put("tzid", schedule.getTzid());
			scheduleParams.put("alert", schedule.getAlert());
			JSONObject params = new JSONObject();
			params.put("ownerid", schedule.getOwner_ID());
			params.put("serviceid", schedule.getService_ID());
			JSONArray jpins = new JSONArray();
			if (pins != null && pins.size() > 0) {
				for (Confirm pin : pins) {
					JSONObject obj = new JSONObject();
					obj.put("memberid", pin.getMemberId());
					obj.put("confirm", pin.getConfirm());
					jpins.put(obj);
				}
			}
			if (jpins != null && jpins.length() > 0) {
				scheduleParams.put("members", jpins);
			}
			params.put("schedules", scheduleParams);
			Log.i("add schedule", params.toString());
			StringEntity entity = new StringEntity(params.toString());
			client.addHeader("User-Agent", userAgentString);

			if (Utils.isNetworkOnline(mContext)) {
				client.post(null, ScheduleUrl, entity, "application/json",
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								Log.i("successful response",
										response.toString());
								try {
									ContentValues cv = new ContentValues();
									String last_modified = response
											.getString("lastmodified");
									cv.put(ScheduleTable.last_Modified,
											last_modified);
									cv.put(ScheduleTable.is_Synchronized, 1);
									dbHelper.updateSchedule(id, cv);
									Log.i("last_modified", last_modified);

									// go to schedule
									CategoryTabActivity.currentPage = CategoryTabActivity.TAB_SCHEDULE;

									activityInterface.onComplete();

									((Activity) mContext).finish();
									Utils.postLeftToRight(mContext);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							public void onFailure(int statusCode,
									Header[] headers, String response,
									Throwable throwable) {
								// TODO Auto-generated method stub
								super.onFailure(statusCode, headers, response,
										throwable);
								dimissDialog();

								Log.i("failure response", response);
								// Log.i("fail", e.toString());

								final ToastDialog dialog = new ToastDialog(
										mContext,
										mContext.getResources().getString(
												R.string.create_schedule_error)
												+ " " + response.toString());
								dialog.show();
								dialog.btnOk
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												dialog.dismiss();
											}
										});
							}

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								super.onStart();
								showLoading(mContext);
							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub
								super.onFinish();
								try {
									// if (progress.isShowing()) {
									// progress.dismiss();
									// }
									dimissDialog();
								} catch (Exception ex) {
									ex.printStackTrace();
								}

							}
						});

			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateSchedule(Schedule schedule, List<Confirm> pins,
			final ActvityInterface activityInterface) {
		String ScheduleUrl = BaseUrl.BASEURL + "schedules/"
				+ schedule.getSchedule_ID() + "?" + BaseUrl.URL_POST_FIX;
		final int id = schedule.getSchedule_ID();
		try {
			JSONObject scheduleParams = new JSONObject();
			scheduleParams.put("desp", schedule.getDesp());
			scheduleParams.put("startdatetime", schedule.getStarttime());
			scheduleParams.put("enddatetime", schedule.getEndtime());

			scheduleParams.put("tzid", schedule.getTzid());
			scheduleParams.put("alert", schedule.getAlert());
			JSONArray jpins = new JSONArray();
			if (pins != null && pins.size() > 0) {
				for (Confirm pin : pins) {
					JSONObject obj = new JSONObject();
					obj.put("memberid", pin.getMemberId());
					obj.put("confirm", pin.getConfirm());
					jpins.put(obj);
				}
			}
			if (jpins != null && jpins.length() > 0) {
				scheduleParams.put("members", jpins);
			}
			JSONObject params = new JSONObject();
			params.put("ownerid", schedule.getOwner_ID());
			params.put("serviceid", schedule.getService_ID());
			params.put("schedules", scheduleParams);

			// client.addHeader("Content-type", "application/json");
			Log.i("add activity", params.toString());
			StringEntity entity = new StringEntity(params.toString());
			if (Utils.isNetworkOnline(mContext)) {
				client.put(null, ScheduleUrl, entity, "application/json",
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								Log.i("update schedule success",
										response.toString());
								try {
									ContentValues cv = new ContentValues();
									String last_modified = response
											.getString("lastmodified");
									cv.put(ScheduleTable.last_Modified,
											last_modified);
									cv.put(ScheduleTable.is_Synchronized, 1);
									dbHelper.updateSchedule(id, cv);
									CategoryTabActivity.currentPage = CategoryTabActivity.TAB_SCHEDULE;

									((Activity) mContext).finish();
									Utils.postLeftToRight(mContext);
									activityInterface.onComplete();

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							// public void onFailure(Throwable e, String
							// response) {
							@Override
							public void onFailure(int statusCode,
									Header[] headers, String response,
									Throwable throwable) {
								// TODO Auto-generated method stub
								super.onFailure(statusCode, headers, response,
										throwable);
								// Response failed :(

								Log.i("failure response", response);
								// Log.i("fail", e.toString());

								final ToastDialog dialog = new ToastDialog(
										mContext,
										mContext.getResources().getString(
												R.string.edit_schedule_error)
												+ " " + response.toString());
								dialog.show();
								dialog.btnOk
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												dialog.dismiss();
											}
										});
							}

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								super.onStart();
								showLoading(mContext);
							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub
								super.onFinish();
								try {
									// if (progress.isShowing()) {
									// progress.dismiss();
									// }
									dimissDialog();
								} catch (Exception ex) {
									ex.printStackTrace();
								}

							}
						});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateConfirmStatus(final Schedule schedule, final Confirm pin,
			final ConfirmInterface confirmInterface) {
		// String ScheduleUrl = BASEURL + "schedules/" +
		// schedule.getScheduleID();
		String updateConfirmStatus = BaseUrl.BASEURL + "schedules/"
				+ schedule.getSchedule_ID() + "/onduty/" + pin.getMemberId()
				+ "?" + BaseUrl.URL_POST_FIX;
		Log.d("update confirm status url", updateConfirmStatus);
		final int id = schedule.getSchedule_ID();
		try {

			JSONObject params = new JSONObject();
			params.put("ownerid",
					new SharedReference().getCurrentOwnerId(mContext));
			params.put("confirm", pin.getConfirm());

			// client.addHeader("Content-type", "application/json");
			Log.i("updateConfirmStatus param", params.toString());
			StringEntity entity = new StringEntity(params.toString());
			if (Utils.isNetworkOnline(mContext)) {
				client.put(null, updateConfirmStatus, entity,
						"application/json", new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								Log.i("updateConfirmStatus success",
										response.toString());
								try {
									ContentValues cv = new ContentValues();
									String last_modified = response
											.getString("lastmodified");
									cv.put(OndutyTable.last_Modified,
											last_modified);
									cv.put(OndutyTable.is_Synchronized, 1);
									cv.put(OndutyTable.confirm,
											pin.getConfirm());
									cv.put(OndutyTable.schedule_ID,
											schedule.getSchedule_ID());
									dbHelper.updateOnduty(
											schedule.getSchedule_ID(),
											pin.getMemberId(), cv);

									confirmInterface.onComplete();

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							@Override
							public void onFailure(int statusCode,
									Header[] headers, String responseString,
									Throwable throwable) {
								// TODO Auto-generated method stub
								super.onFailure(statusCode, headers,
										responseString, throwable);
								dimissDialog();
								Log.i("failure response", responseString);

								final ToastDialog dialog = new ToastDialog(
										mContext, mContext.getResources()
												.getString(
														R.string.confirm_error)
												+ " "
												+ responseString.toString());
								dialog.show();
								dialog.btnOk
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												dialog.dismiss();
											}
										});
							}

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								super.onStart();
								// showLoading(mContext);
							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub
								super.onFinish();
								// try {
								// // if (progress.isShowing()) {
								// // progress.dismiss();
								// // }
								// dimissDialog();
								// } catch (Exception ex) {
								// ex.printStackTrace();
								// }

							}
						});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * delete schedule
	 * */
	public void deleteSchedule(Schedule schedule,
			JsonHttpResponseHandler handler) {
		// String ScheduleUrl = BASEURL + "schedules/" +
		// schedule.getScheduleID();
		String ScheduleUrl = BaseUrl.BASEURL + "schedules/"
				+ schedule.getSchedule_ID() + "?" + BaseUrl.URL_POST_FIX;
		if (Utils.isNetworkOnline(mContext)) {
			client.delete(ScheduleUrl, handler);
		} else {
			final ToastDialog dialog = new ToastDialog(mContext, mContext
					.getResources().getString(R.string.no_network));
			dialog.show();
			dialog.btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
	}

	/**
	 * delete schedule
	 * */
	public void deleteSchedule(Schedule schedule) {
		// String ScheduleUrl = BASEURL + "schedules/" +
		// schedule.getScheduleID();
		String ScheduleUrl = BaseUrl.BASEURL + "schedules/"
				+ schedule.getSchedule_ID() + "?" + BaseUrl.URL_POST_FIX;
		final int schedule_id = schedule.getSchedule_ID();
		if (Utils.isNetworkOnline(mContext)) {
			client.delete(ScheduleUrl, new JsonHttpResponseHandler() {

				@Override
				public void onSuccess(int statusCode, Header[] headers,
						JSONObject response) {
					// TODO Auto-generated method stub
					super.onSuccess(statusCode, headers, response);
					try {
						if (response.getString("lastmodified") != null) {

							dbHelper.deleteRelatedOnduty(schedule_id);
							dbHelper.deleteSchedule(schedule_id);
							Log.i("delete schedule", "successfully");

							Intent intent = new Intent(
									CommConstant.DELETE_SCHEDULE_COMPLETE);
							mContext.sendBroadcast(intent);
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				public void onSuccess(JSONObject response) {
					try {
						if (response.getString("lastmodified") != null) {

							dbHelper.deleteRelatedOnduty(schedule_id);
							dbHelper.deleteSchedule(schedule_id);
							Log.i("delete schedule", "successfully");

							Intent intent = new Intent(
									CommConstant.DELETE_SCHEDULE_COMPLETE);
							mContext.sendBroadcast(intent);
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void onStart() {
					// TODO Auto-generated method stub
					super.onStart();
					showLoading(mContext);
				}

				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					super.onFinish();
					try {
						// if (progress.isShowing()) {
						// progress.dismiss();
						// }
						dimissDialog();
					} catch (Exception ex) {
						ex.printStackTrace();
					}

				}

				@Override
				public void onFailure(int statusCode, Header[] headers,
						String responseString, Throwable throwable) {
					// TODO Auto-generated method stub
					super.onFailure(statusCode, headers, responseString,
							throwable);

					dimissDialog();
					final ToastDialog dialog = new ToastDialog(mContext,
							mContext.getResources().getString(
									R.string.delete_schedule_error)
									+ " " + responseString.toString());
					dialog.show();
					dialog.btnOk.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
				}
			});
		} else {
			final ToastDialog dialog = new ToastDialog(mContext, mContext
					.getResources().getString(R.string.no_network));
			dialog.show();
			dialog.btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
	}

	/**
	 * send feedback
	 * */
	public void sendFeedBack(String feedBackStr) {

		String feedback = BaseUrl.BASEURL + "feedback" + "?"
				+ BaseUrl.URL_POST_FIX;
		Log.d("feedback link", feedback);
		try {
			JSONObject sharedmemberParams = new JSONObject();
			sharedmemberParams.put("ownerid",
					new SharedReference().getCurrentOwnerId(mContext));
			sharedmemberParams.put("feedback", feedBackStr);

			// client.addHeader("Content-type", "application/json");
			client.addHeader("User-Agent", userAgentString);

			StringEntity entity = new StringEntity(
					sharedmemberParams.toString());
			Log.d("body feedback", sharedmemberParams.toString());
			if (Utils.isNetworkOnline(mContext)) {
				client.post(null, feedback, entity, "application/json",
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								Log.i("successful feedback",
										response.toString());
								if (statusCode == 200) {

									final ToastDialog dialog = new ToastDialog(
											mContext,
											mContext.getResources()
													.getString(
															R.string.send_feedback_success));
									dialog.show();
									dialog.btnOk
											.setOnClickListener(new OnClickListener() {

												@Override
												public void onClick(View v) {
													dialog.dismiss();
												}
											});

								} else if (statusCode == 201) {

									final ToastDialog dialog = new ToastDialog(
											mContext,
											mContext.getResources()
													.getString(
															R.string.invalid_content_feedback));
									dialog.show();
									dialog.btnOk
											.setOnClickListener(new OnClickListener() {

												@Override
												public void onClick(View v) {
													dialog.dismiss();
												}
											});
								}

							}

							// @Override
							public void onSuccess(String response) {
								Log.i("successful feedback",
										response.toString());
								if (response.startsWith("200")) {

									final ToastDialog dialog = new ToastDialog(
											mContext,
											mContext.getResources()
													.getString(
															R.string.send_feedback_success));
									dialog.show();
									dialog.btnOk
											.setOnClickListener(new OnClickListener() {

												@Override
												public void onClick(View v) {
													dialog.dismiss();
												}
											});

								} else if (response.startsWith("201")) {

									final ToastDialog dialog = new ToastDialog(
											mContext,
											mContext.getResources()
													.getString(
															R.string.invalid_content_feedback));
									dialog.show();
									dialog.btnOk
											.setOnClickListener(new OnClickListener() {

												@Override
												public void onClick(View v) {
													dialog.dismiss();
												}
											});
								}

							}

							@Override
							public void onFailure(int statusCode,
									Header[] headers, String responseString,
									Throwable throwable) {
								// TODO Auto-generated method stub
								super.onFailure(statusCode, headers,
										responseString, throwable);
								final ToastDialog dialog = new ToastDialog(
										mContext,
										mContext.getResources().getString(
												R.string.send_feedback_error));
								dialog.show();
								dialog.btnOk
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												dialog.dismiss();
											}
										});
							}

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								super.onStart();
								showLoading(mContext);
							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub
								super.onFinish();
								try {
									// if (progress.isShowing()) {
									// progress.dismiss();
									// }
									dimissDialog();
								} catch (Exception ex) {
									ex.printStackTrace();
								}

							}
						});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void uploadRecentNewActivitiesToWeb() {
		List<MyActivity> unsyncedActivities = dbHelper
				.getUnsyncedNewActivities();
		for (int i = 0; i < unsyncedActivities.size(); i++) {
			MyActivity ma = unsyncedActivities.get(i);
			this.addActivity(ma, new ActvityInterface() {

				@Override
				public void onError(String error) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onComplete() {
					// TODO Auto-generated method stub

				}
			});
		}
	}

	public void uploadRecentEditedActivitiesToWeb() {
		try {
			List<MyActivity> unsyncedActivities = dbHelper
					.getUnsyncedEditedActivities();
			for (int i = 0; i < unsyncedActivities.size(); i++) {
				MyActivity ma = unsyncedActivities.get(i);
				updateActivity(ma);
			}
		} catch (Exception ex) {

		}
	}

	public void uploadRecentNewParticipantsToWeb() {
		List<Participant> unsyncedParticipants = dbHelper
				.getUnsyncedNewParticipants();
		for (int i = 0; i < unsyncedParticipants.size(); i++) {
			Participant p = unsyncedParticipants.get(i);
			addParticipant(p);
		}
	}

	public void uploadRecentEditedParticipantsToWeb() {
		List<Participant> unsyncedParticipants = dbHelper
				.getUnsyncedEditedParticipants();
		for (int i = 0; i < unsyncedParticipants.size(); i++) {
			Participant p = unsyncedParticipants.get(i);
			this.updateParticipant(p);
		}
	}

	public void uploadRecentNewSchedulesToWeb() {
		List<Schedule> unsyncedSchedules = dbHelper.getUnsyncedNewSchedules();
		for (int i = 0; i < unsyncedSchedules.size(); i++) {
			Schedule s = unsyncedSchedules.get(i);
			List<Confirm> members = dbHelper.getParticipantsForSchedule(s
					.getSchedule_ID());
			this.addSchedule(s, members, new ActvityInterface() {

				@Override
				public void onError(String error) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onComplete() {
					// TODO Auto-generated method stub

				}
			});
		}
	}

	public void uploadRecentEditedSchedulesToWeb() {
		List<Schedule> unsyncedSchedules = dbHelper
				.getUnsyncedEditedSchedules();
		for (int i = 0; i < unsyncedSchedules.size(); i++) {
			Schedule s = unsyncedSchedules.get(i);
			List<Confirm> members = dbHelper.getParticipantsForSchedule(s
					.getSchedule_ID());
			this.updateSchedule(s, members, new ActvityInterface() {

				@Override
				public void onError(String error) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onComplete() {
					// TODO Auto-generated method stub

				}
			});
		}
	}

	/**
	 * Get member shared for actiivty
	 * */
	public void getSharedmembersForActivity(final String activity_id) {
		String sharedmembersUrl = BaseUrl.BASEURL + "services/" + activity_id
				+ "/sharedmembers" + "?" + BaseUrl.URL_POST_FIX;
		Log.i("shared member url :", sharedmembersUrl);
		int currentOwnerID = mContext.getSharedPreferences("MyPreferences", 0)
				.getInt("currentownerid", 0);
		RequestParams params = new RequestParams();
		params.put("ownerid", String.valueOf(currentOwnerID));
		params.put("lastupdatetime", "");
		if (Utils.isNetworkOnline(mContext)) {
			client.get(sharedmembersUrl, params, new JsonHttpResponseHandler() {

				@Override
				public void onSuccess(int statusCode, Header[] headers,
						JSONObject response) {
					// TODO Auto-generated method stub
					super.onSuccess(statusCode, headers, response);
					Log.i("successful response", response.toString());
					try {
						JSONArray deleteMember = response
								.getJSONArray("deletedsmembers");
						int deleted_member_count = deleteMember.length();
						if (deleted_member_count > 0) {
							for (int i = 0; i < deleted_member_count; i++) {
								int id = deleteMember.getInt(i);
								dbHelper.deleteSharedmember(id, activity_id);
							}
						}

						JSONArray JSharedmembers = response
								.getJSONArray("sharedmembers");
						int sm_count = JSharedmembers.length();
						for (int i = 0; i < sm_count; i++) {
							JSONObject JSharedmember = JSharedmembers
									.getJSONObject(i);
							int sm_id = JSharedmember.getInt("memberid");
							String sm_email = JSharedmember
									.getString("memberemail");
							String sm_number = JSharedmember
									.getString("mobilenumber");
							String sm_name = JSharedmember
									.getString("membername");
							int sm_role = JSharedmember.getInt("sharedrole");
							String sm_lastmdf = JSharedmember
									.getString("lastmodified");

							ContentValues cv = new ContentValues();

							cv.put(SharedMemberTable.member_email, sm_email);
							cv.put(SharedMemberTable.member_name, sm_name);
							cv.put(SharedMemberTable.member_mobile, sm_number);
							cv.put(SharedMemberTable.service_id, activity_id);
							cv.put(SharedMemberTable.role, sm_role);

							cv.put(SharedMemberTable.last_modified, sm_lastmdf);
							cv.put(SharedMemberTable.is_Deleted, 0);
							cv.put(SharedMemberTable.is_Synced, 1);
							if (dbHelper.isSharedmemberExisted(sm_id,
									activity_id)) {
								dbHelper.updateSharedmember(sm_id, activity_id,
										cv);
							} else {
								cv.put(SharedMemberTable.member_id, sm_id);
								dbHelper.insertSharedmember(cv);
							}
						}

						SharedReference ref = new SharedReference();
						ref.setLastestParticipantLastModifiedTime(mContext,
								MyDate.transformPhoneDateTimeToUTCFormat(MyDate
										.getCurrentDateTime()));

						Intent intent = new Intent();
						intent.setAction(CommConstant.GET_SHARED_MEMBER_ACTIVITY_COMPLETE);
						mContext.sendBroadcast(intent);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				public void onSuccess(JSONObject response) {
					Log.i("successful response", response.toString());
					try {
						JSONArray deleteMember = response
								.getJSONArray("deletedsmembers");
						int deleted_member_count = deleteMember.length();
						if (deleted_member_count > 0) {
							for (int i = 0; i < deleted_member_count; i++) {
								int id = deleteMember.getInt(i);
								dbHelper.deleteSharedmember(id, activity_id);
							}
						}

						JSONArray JSharedmembers = response
								.getJSONArray("sharedmembers");
						int sm_count = JSharedmembers.length();
						for (int i = 0; i < sm_count; i++) {
							JSONObject JSharedmember = JSharedmembers
									.getJSONObject(i);
							int sm_id = JSharedmember.getInt("memberid");
							String sm_email = JSharedmember
									.getString("memberemail");
							String sm_number = JSharedmember
									.getString("mobilenumber");
							String sm_name = JSharedmember
									.getString("membername");
							int sm_role = JSharedmember.getInt("sharedrole");
							String sm_lastmdf = JSharedmember
									.getString("lastmodified");

							ContentValues cv = new ContentValues();

							cv.put(SharedMemberTable.member_email, sm_email);
							cv.put(SharedMemberTable.member_name, sm_name);
							cv.put(SharedMemberTable.member_mobile, sm_number);
							cv.put(SharedMemberTable.service_id, activity_id);
							cv.put(SharedMemberTable.role, sm_role);

							cv.put(SharedMemberTable.last_modified, sm_lastmdf);
							cv.put(SharedMemberTable.is_Deleted, 0);
							cv.put(SharedMemberTable.is_Synced, 1);
							if (dbHelper.isSharedmemberExisted(sm_id,
									activity_id)) {
								dbHelper.updateSharedmember(sm_id, activity_id,
										cv);
							} else {
								cv.put(SharedMemberTable.member_id, sm_id);
								dbHelper.insertSharedmember(cv);
							}
						}

						SharedReference ref = new SharedReference();
						ref.setLastestParticipantLastModifiedTime(mContext,
								MyDate.transformPhoneDateTimeToUTCFormat(MyDate
										.getCurrentDateTime()));

						Intent intent = new Intent();
						intent.setAction(CommConstant.GET_SHARED_MEMBER_ACTIVITY_COMPLETE);
						mContext.sendBroadcast(intent);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers,
						String responseString, Throwable throwable) {
					// TODO Auto-generated method stub
					super.onFailure(statusCode, headers, responseString,
							throwable);
					final ToastDialog dialog = new ToastDialog(mContext,
							mContext.getResources().getString(
									R.string.get_shared_member_error));
					dialog.show();
					dialog.btnOk.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
				}

				// @Override
				// public void onStart() {
				// // TODO Auto-generated method stub
				// super.onStart();
				// showLoading(mContext);
				// }
				//
				// @Override
				// public void onFinish() {
				// // TODO Auto-generated method stub
				// super.onFinish();
				// try {
				// // if (progress.isShowing()) {
				// // progress.dismiss();
				// // }
				// dimissDialog();
				// } catch (Exception ex) {
				// ex.printStackTrace();
				// }
				//
				// }
			});
		} else {
			final ToastDialog dialog = new ToastDialog(mContext, mContext
					.getResources().getString(R.string.no_network));
			dialog.show();
			dialog.btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
	}

	/**
	 * Share member to activity
	 * */
	public void postSharedmemberToActivity(final int memberid, final int role,
			final String activityid) {
		String sharedmemberUrl = BaseUrl.BASEURL + "services/" + activityid
				+ "/" + "sharedmembers" + "?" + BaseUrl.URL_POST_FIX;
		Log.d("share member url", sharedmemberUrl);
		try {
			JSONObject sharedmemberParams = new JSONObject();
			sharedmemberParams.put("ownerid",
					new SharedReference().getCurrentOwnerId(mContext));
			sharedmemberParams.put("sharedrole", role);
			sharedmemberParams.put("memberid", memberid);

			Log.d("post shared member", sharedmemberParams.toString());

			// client.addHeader("Content-type", "application/json");
			client.addHeader("User-Agent", userAgentString);

			// Log.i("add participant", sharedmemberParams.toString());
			StringEntity entity = new StringEntity(
					sharedmemberParams.toString());

			if (Utils.isNetworkOnline(mContext)) {
				client.post(null, sharedmemberUrl, entity, "application/json",
						new JsonHttpResponseHandler() {

							@Override
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								Log.i("successful sharedmember",
										response.toString());
								int code = 0;

								try {
									String lastmodify = "";

									lastmodify = response
											.getString("lastmodified");
									Participant member = dbHelper
											.getParticipant(memberid);
									ContentValues cv = new ContentValues();
									cv.put(SharedMemberTable.member_name,
											member.getName());
									cv.put(SharedMemberTable.member_id,
											member.getID());
									cv.put(SharedMemberTable.member_email,
											member.getEmail());
									cv.put(SharedMemberTable.member_mobile,
											member.getMobile());
									cv.put(SharedMemberTable.role, role);
									cv.put(SharedMemberTable.is_Deleted, 0);
									cv.put(SharedMemberTable.last_modified,
											lastmodify);
									cv.put(SharedMemberTable.is_Synced, 1);
									dbHelper.updateSharedmember(member.getID(),
											cv);
									Intent intent = new Intent(
											CommConstant.GET_SHARED_MEMBER_ACTIVITY_COMPLETE);
									intent.putExtra(CommConstant.ACTIVITY_ID,
											activityid);
									mContext.sendBroadcast(intent);

								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								try {
									code = response.getInt("code");
									if (code != 200) {
										Toast.makeText(mContext,
												response.getString("message"),
												Toast.LENGTH_LONG).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							public void onSuccess(JSONObject response) {
								Log.i("successful sharedmember",
										response.toString());
								int code = 0;
								try {
									code = response.getInt("code");
									if (code != 200) {
										Toast.makeText(mContext,
												response.getString("message"),
												Toast.LENGTH_LONG).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
								try {
									String lastmodify = "";

									lastmodify = response
											.getString("lastmodified");
									Participant member = dbHelper
											.getParticipant(memberid);
									ContentValues cv = new ContentValues();
									cv.put(SharedMemberTable.member_name,
											member.getName());
									cv.put(SharedMemberTable.member_id,
											member.getID());
									cv.put(SharedMemberTable.member_email,
											member.getEmail());
									cv.put(SharedMemberTable.member_mobile,
											member.getMobile());
									cv.put(SharedMemberTable.role, role);
									cv.put(SharedMemberTable.is_Deleted, 0);
									cv.put(SharedMemberTable.last_modified,
											lastmodify);
									cv.put(SharedMemberTable.is_Synced, 1);
									dbHelper.updateSharedmember(member.getID(),
											cv);
									Intent intent = new Intent(
											CommConstant.GET_SHARED_MEMBER_ACTIVITY_COMPLETE);
									intent.putExtra(CommConstant.ACTIVITY_ID,
											activityid);
									mContext.sendBroadcast(intent);

								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

							}

							@Override
							public void onFailure(int statusCode,
									Header[] headers, String responseString,
									Throwable throwable) {
								// TODO Auto-generated method stub
								super.onFailure(statusCode, headers,
										responseString, throwable);
								dimissDialog();
								final ToastDialog dialog = new ToastDialog(
										mContext,
										mContext.getResources()
												.getString(
														R.string.post_shared_member_error));
								dialog.show();
								dialog.btnOk
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												dialog.dismiss();
											}
										});

							}

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								super.onStart();
								// showLoading(mContext);
							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub
								super.onFinish();
								// try {
								// // if (progress.isShowing()) {
								// // progress.dismiss();
								// // }
								// dimissDialog();
								// } catch (Exception ex) {
								// ex.printStackTrace();
								// }

							}
						});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Delete shared member activity
	 * */
	public void deleteSharedmemberOfActivity(final int memberid,
			final String activityid) {
		String sharedmemberUrl = BaseUrl.BASEURL + "services/" + activityid
				+ "/" + "sharedmembers/" + memberid + "?"
				+ BaseUrl.URL_POST_FIX;
		if (Utils.isNetworkOnline(mContext)) {
			client.delete(sharedmemberUrl, new JsonHttpResponseHandler() {

				@Override
				public void onSuccess(int statusCode, Header[] headers,
						JSONObject response) {
					// TODO Auto-generated method stub
					super.onSuccess(statusCode, headers, response);
					Log.i("delete shared member of activity",
							response.toString());
					try {
						String lastmodified = response
								.getString("lastmodified");
						dbHelper.deleteSharedmember(memberid, activityid);
						SharedReference ref = new SharedReference();
						ref.setLastestServiceLastModifiedTime(mContext,
								lastmodified);
						Intent intent = new Intent(
								CommConstant.GET_SHARED_MEMBER_ACTIVITY_COMPLETE);
						mContext.sendBroadcast(intent);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				public void onSuccess(JSONObject response) {
					Log.i("delete shared member of activity",
							response.toString());
					try {
						String lastmodified = response
								.getString("lastmodified");
						dbHelper.deleteSharedmember(memberid, activityid);
						SharedReference ref = new SharedReference();
						ref.setLastestServiceLastModifiedTime(mContext,
								lastmodified);
						Intent intent = new Intent(
								CommConstant.GET_SHARED_MEMBER_ACTIVITY_COMPLETE);
						mContext.sendBroadcast(intent);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers,
						String responseString, Throwable throwable) {
					// TODO Auto-generated method stub
					super.onFailure(statusCode, headers, responseString,
							throwable);
					dimissDialog();
					final ToastDialog dialog = new ToastDialog(mContext,
							mContext.getResources().getString(
									R.string.delete_participant_error));
					dialog.show();
					dialog.btnOk.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
				}

				@Override
				public void onStart() {
					// TODO Auto-generated method stub
					super.onStart();
					showLoading(mContext.getApplicationContext());
				}

				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					super.onFinish();
					try {
						// if (progress.isShowing()) {
						// progress.dismiss();
						// }
						dimissDialog();
					} catch (Exception ex) {
						ex.printStackTrace();
					}

				}
			});
		} else {
			final ToastDialog dialog = new ToastDialog(mContext, mContext
					.getResources().getString(R.string.no_network));
			dialog.show();
			dialog.btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
	}

	// public void alterSharedmemberToActivity(int memberid, int role,
	// int activityid) {
	// String sharedmemberUrl = BaseUrl.BASEURL + "services/" + activityid
	// + "/" + "sharedmembers/" + memberid + "?"
	// + BaseUrl.URL_POST_FIX;
	// try {
	// JSONObject params = new JSONObject();
	// params.put("ownerid",
	// new SharedReference().getCurrentOwnerId(mContext));
	// params.put("sharedrole", role);
	//
	// // client.addHeader("Content-type", "application/json");
	// Log.i("update participant", params.toString());
	// StringEntity entity = new StringEntity(params.toString());
	// if (Utils.isNetworkOnline(mContext)) {
	// client.put(null, sharedmemberUrl, entity, "application/json",
	// new JsonHttpResponseHandler() {
	// public void onSuccess(JSONObject response) {
	// Log.i("successful response",
	// response.toString());
	//
	// }
	//
	// public void onFailure(Throwable e, String response) {
	// // Response failed :(
	//
	// Log.i("failure response", response);
	// Log.i("fail", e.toString());
	//
	// }
	//
	// @Override
	// public void onStart() {
	// // TODO Auto-generated method stub
	// super.onStart();
	// showLoading(mContext);
	// }
	//
	// @Override
	// public void onFinish() {
	// // TODO Auto-generated method stub
	// super.onFinish();
	// try {
	// // if (progress.isShowing()) {
	// // progress.dismiss();
	// // }
	// dimissDialog();
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	//
	// }
	// });
	// } else {
	// final ToastDialog dialog=new ToastDialog(mContext,
	// mContext.getResources().getString(R.string.no_network));
	// dialog.show();
	// dialog.btnOk.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// dialog.dismiss();
	// }
	// });
	// }
	// } catch (UnsupportedEncodingException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	public void addParticipant(Participant participant) {
		String ParticipantUrl = BaseUrl.BASEURL + "members" + "?"
				+ BaseUrl.URL_POST_FIX;
		final int id = participant.getID();
		try {
			JSONObject participantParams = new JSONObject();
			participantParams.put("email", participant.getEmail());
			participantParams.put("mobile", participant.getMobile());
			participantParams.put("membername", participant.getName());
			participantParams.put("memberid", participant.getID());

			JSONObject params = new JSONObject();
			params.put("ownerid", participant.getOwnerID());
			params.put("members", participantParams);

			// client.addHeader("Content-type", "application/json");
			Log.i("add participant", params.toString());
			StringEntity entity = new StringEntity(params.toString());

			// client.addHeader("Content-type", "application/json");
			client.addHeader("User-Agent", userAgentString);
			if (Utils.isNetworkOnline(mContext)) {
				client.post(null, ParticipantUrl, entity, "application/json",
						new JsonHttpResponseHandler() {

							@Override
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								Log.i("add participant response",
										response.toString());
								try {
									String last_modified = response
											.getString("lastmodified");
									if (last_modified != null
											&& (!last_modified.equals(""))) {
										ContentValues cv = new ContentValues();
										cv.put(ParticipantTable.last_Modified,
												last_modified);
										cv.put(ParticipantTable.is_Sychronized,
												1);

										dbHelper.updateParticipant(id, cv);
									} else {
										Toast.makeText(mContext,
												response.toString(),
												Toast.LENGTH_LONG).show();
									}
									((Activity) mContext).finish();
									Utils.postLeftToRight(mContext);
									Intent intent = new Intent(
											CommConstant.ADD_CONTACT_SUCCESS);
									mContext.sendBroadcast(intent);

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							public void onSuccess(JSONObject response) {
								Log.i("add participant response",
										response.toString());
								try {
									String last_modified = response
											.getString("lastmodified");
									if (last_modified != null
											&& (!last_modified.equals(""))) {
										ContentValues cv = new ContentValues();
										cv.put(ParticipantTable.last_Modified,
												last_modified);
										cv.put(ParticipantTable.is_Sychronized,
												1);

										dbHelper.updateParticipant(id, cv);
									} else {
										Toast.makeText(mContext,
												response.toString(),
												Toast.LENGTH_LONG).show();
									}
									((Activity) mContext).finish();
									Utils.postLeftToRight(mContext);
									Intent intent = new Intent(
											CommConstant.ADD_CONTACT_SUCCESS);
									mContext.sendBroadcast(intent);

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}

							@Override
							public void onFailure(int statusCode,
									Header[] headers, String responseString,
									Throwable throwable) {
								// TODO Auto-generated method stub
								Log.d("add participant failure", responseString);
								super.onFailure(statusCode, headers,
										responseString, throwable);
								final ToastDialog dialog = new ToastDialog(
										mContext,
										mContext.getResources().getString(
												R.string.add_participant_error));
								dialog.show();
								dialog.btnOk
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												dialog.dismiss();
											}
										});
							}

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								super.onStart();
								showLoading(mContext);
							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub
								super.onFinish();
								try {
									// if (progress.isShowing()) {
									// progress.dismiss();
									// }
									dimissDialog();
								} catch (Exception ex) {
									ex.printStackTrace();
								}

							}
						});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Update participant: update table ParticipantTable and SharedMemberTable
	 * */
	public void updateParticipant(final Participant participant) {
		String ParticipantUrl = BaseUrl.BASEURL + "members/"
				+ participant.getID() + "?" + BaseUrl.URL_POST_FIX;
		Log.i("updateParticipant ParticipantUrl ", ParticipantUrl);
		final int id = participant.getID();
		try {
			JSONObject participantParams = new JSONObject();
			participantParams.put("email", participant.getEmail());
			participantParams.put("mobile", participant.getMobile());
			participantParams.put("membername", participant.getName());

			JSONObject params = new JSONObject();
			params.put("ownerid", participant.getOwnerID());
			params.put("members", participantParams);

			// client.addHeader("Content-type", "application/json");
			Log.i("update participant", params.toString());
			StringEntity entity = new StringEntity(params.toString());
			if (Utils.isNetworkOnline(mContext)) {
				client.put(null, ParticipantUrl, entity, "application/json",
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								Log.d("update participant", response.toString());
								try {

									ContentValues cv = new ContentValues();
									String last_modified = response
											.getString("lastmodified");
									cv.put(ParticipantTable.last_Modified,
											last_modified);
									cv.put(ParticipantTable.is_Sychronized, 1);
									dbHelper.updateParticipant(id, cv);

									ContentValues contentValues = new ContentValues();
									contentValues.put(
											SharedMemberTable.member_email,
											participant.getEmail());
									contentValues.put(
											SharedMemberTable.member_mobile,
											participant.getMobile());
									contentValues.put(
											SharedMemberTable.member_name,
											participant.getName());
									dbHelper.updateSharedmember(
											participant.getID(), contentValues);

									((Activity) mContext).finish();
									Utils.postLeftToRight(mContext);
									Intent intent = new Intent(
											CommConstant.ADD_CONTACT_SUCCESS);
									mContext.sendBroadcast(intent);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							public void onSuccess(JSONObject response) {
								Log.d("update participant", response.toString());
								try {

									ContentValues cv = new ContentValues();
									String last_modified = response
											.getString("lastmodified");
									cv.put(ParticipantTable.last_Modified,
											last_modified);
									cv.put(ParticipantTable.is_Sychronized, 1);
									dbHelper.updateParticipant(id, cv);

									ContentValues contentValues = new ContentValues();
									contentValues.put(
											SharedMemberTable.member_email,
											participant.getEmail());
									contentValues.put(
											SharedMemberTable.member_mobile,
											participant.getMobile());
									contentValues.put(
											SharedMemberTable.member_name,
											participant.getName());
									dbHelper.updateSharedmember(
											participant.getID(), contentValues);

									((Activity) mContext).finish();
									Utils.postLeftToRight(mContext);
									Intent intent = new Intent(
											CommConstant.ADD_CONTACT_SUCCESS);
									mContext.sendBroadcast(intent);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							@Override
							public void onFailure(int statusCode,
									Header[] headers, String responseString,
									Throwable throwable) {
								// TODO Auto-generated method stub
								super.onFailure(statusCode, headers,
										responseString, throwable);
								final ToastDialog dialog = new ToastDialog(
										mContext,
										mContext.getResources()
												.getString(
														R.string.update_participant_error));
								dialog.show();
								dialog.btnOk
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												dialog.dismiss();
											}
										});
							}

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								super.onStart();
								showLoading(mContext);
							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub
								super.onFinish();
								try {
									// if (progress.isShowing()) {
									// progress.dismiss();
									// }
									dimissDialog();
								} catch (Exception ex) {
									ex.printStackTrace();
								}

							}
						});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deleteParticipant(final Participant participant) {
		String urlDeleteContact = BaseUrl.BASEURL + "members/"
				+ participant.getID() + "?" + BaseUrl.URL_POST_FIX;
		Log.d("delete contact", urlDeleteContact);
		final int id = participant.getID();
		if (Utils.isNetworkOnline(mContext)) {
			client.delete(urlDeleteContact, new JsonHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers,
						JSONObject response) {
					// TODO Auto-generated method stub
					super.onSuccess(statusCode, headers, response);
					Log.d("delete str", response.toString());
					try {
						if (response.getString("lastmodified") != null) {

							ContentValues cv = new ContentValues();
							cv.put(ParticipantTable.is_Deleted, 1);
							cv.put(ParticipantTable.is_Sychronized, 1);
							// dbHelper.updateParticipant(id, cv);
							dbHelper.deleteParticipant(id);

						}
						((Activity) mContext).finish();
						Utils.postLeftToRight(mContext);
						Intent intent = new Intent(
								CommConstant.DELETE_CONTACT_COMPLETE);
						mContext.sendBroadcast(intent);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				public void onSuccess(JSONObject response) {
					Log.d("delete str", response.toString());
					try {
						if (response.getString("lastmodified") != null) {

							ContentValues cv = new ContentValues();
							cv.put(ParticipantTable.is_Deleted, 1);
							cv.put(ParticipantTable.is_Sychronized, 1);
							// dbHelper.updateParticipant(id, cv);
							dbHelper.deleteParticipant(id);

						}
						((Activity) mContext).finish();
						Utils.postLeftToRight(mContext);
						Intent intent = new Intent(
								CommConstant.DELETE_CONTACT_COMPLETE);
						mContext.sendBroadcast(intent);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers,
						String responseString, Throwable throwable) {
					// TODO Auto-generated method stub
					super.onFailure(statusCode, headers, responseString,
							throwable);
					final ToastDialog dialog = new ToastDialog(mContext,
							mContext.getResources().getString(
									R.string.delete_contact_error));
					dialog.show();
					dialog.btnOk.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
				}

				@Override
				public void onStart() {
					// TODO Auto-generated method stub
					super.onStart();
					showLoading(mContext);
				}

				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					super.onFinish();
					try {
						// if (progress.isShowing()) {
						// progress.dismiss();
						// }
						dimissDialog();
					} catch (Exception ex) {
						ex.printStackTrace();
					}

				}
			});
		} else {
			final ToastDialog dialog = new ToastDialog(mContext, mContext
					.getResources().getString(R.string.no_network));
			dialog.show();
			dialog.btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
	}

	// /**
	// * Delete contact
	// * */
	// public void deleteContact(final int contact_id,
	// JsonHttpResponseHandler handler) {
	// String urlDeleteContact = BaseUrl.BASEURL + "members/" + contact_id
	// + "?" + BaseUrl.URL_POST_FIX;
	// Log.d("delete contact", urlDeleteContact);
	// if (Utils.isNetworkOnline(mContext)) {
	// client.delete(urlDeleteContact, handler);
	// } else {
	// final ToastDialog dialog = new ToastDialog(mContext, mContext
	// .getResources().getString(R.string.no_network));
	// dialog.show();
	// dialog.btnOk.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// dialog.dismiss();
	// }
	// });
	// }
	// }

	public void updateActivity(MyActivity activity) {
		String ActivityUrl = BaseUrl.BASEURL + "services/"
				+ activity.getActivity_ID() + "?" + BaseUrl.URL_POST_FIX;
		final String id = activity.getActivity_ID();
		try {
			JSONObject activityParams = new JSONObject();

			activityParams.put("servicename", activity.getActivity_name());
			activityParams.put("desp", activity.getDesp());
			// activityParams.put("startdatetime", activity.getStarttime());
			// activityParams.put("enddatetime", activity.getEndtime());

			JSONObject params = new JSONObject();
			params.put("ownerid", activity.getOwner_ID());
			params.put("services", activityParams);

			// client.addHeader("Content-type", "application/json");

			Log.i("update activity", params.toString());
			StringEntity entity = new StringEntity(params.toString());
			if (Utils.isNetworkOnline(mContext)) {
				client.put(null, ActivityUrl, entity, "application/json",
						new JsonHttpResponseHandler() {

							@Override
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								Log.i("update activity", response.toString());
								try {
									ContentValues cv = new ContentValues();
									String last_modified = response
											.getString("lastmodified");
									cv.put(ActivityTable.last_ModifiedTime,
											last_modified);
									cv.put(ActivityTable.is_Synchronized, 1);
									dbHelper.updateActivity(id, cv);
									Log.i("last_modified", last_modified);
									SharedReference ref = new SharedReference();
									ref.setLastestServiceLastModifiedTime(
											mContext, last_modified);

									((Activity) mContext).finish();
									Utils.postLeftToRight(mContext);
									Intent intent = new Intent(
											CommConstant.ACTIVITY_DOWNLOAD_SUCCESS);
									mContext.sendBroadcast(intent);

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							public void onSuccess(JSONObject response) {
								Log.i("update activity", response.toString());
								try {
									ContentValues cv = new ContentValues();
									String last_modified = response
											.getString("lastmodified");
									cv.put(ActivityTable.last_ModifiedTime,
											last_modified);
									cv.put(ActivityTable.is_Synchronized, 1);
									dbHelper.updateActivity(id, cv);
									Log.i("last_modified", last_modified);
									SharedReference ref = new SharedReference();
									ref.setLastestServiceLastModifiedTime(
											mContext, last_modified);

									((Activity) mContext).finish();
									Utils.postLeftToRight(mContext);
									Intent intent = new Intent(
											CommConstant.ACTIVITY_DOWNLOAD_SUCCESS);
									mContext.sendBroadcast(intent);

								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							@Override
							public void onFailure(int statusCode,
									Header[] headers, String responseString,
									Throwable throwable) {
								// TODO Auto-generated method stub
								super.onFailure(statusCode, headers,
										responseString, throwable);
								final ToastDialog dialog = new ToastDialog(
										mContext,
										mContext.getResources().getString(
												R.string.edit_activity_error));
								dialog.show();
								dialog.btnOk
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												dialog.dismiss();
											}
										});

							}

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								super.onStart();
								showLoading(mContext);
							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub
								super.onFinish();
								try {
									// if (progress.isShowing()) {
									// progress.dismiss();
									// }
									dimissDialog();
								} catch (Exception ex) {
									ex.printStackTrace();
								}

							}
						});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * delete activity
	 * */
	public void deleteActivity(MyActivity activity) {
		String ActivityUrl = BaseUrl.BASEURL + "services/"
				+ activity.getActivity_ID() + "?" + BaseUrl.URL_POST_FIX;
		final String id = activity.getActivity_ID();
		client.addHeader("Content-type", "application/json");
		if (Utils.isNetworkOnline(mContext)) {
			client.delete(ActivityUrl, new JsonHttpResponseHandler() {

				@Override
				public void onSuccess(int statusCode, Header[] headers,
						JSONObject response) {
					// TODO Auto-generated method stub
					super.onSuccess(statusCode, headers, response);
					Log.d("delete activity", response.toString());
					try {
						if (response.getString("lastmodified") != null) {
							ArrayList<Sharedmember> listSharedMemberOfActivity = dbHelper
									.getSharedMemberForActivity(id);
							if (listSharedMemberOfActivity != null
									&& listSharedMemberOfActivity.size() > 0) {
								for (Sharedmember sharedMember : listSharedMemberOfActivity) {
									dbHelper.deleteSharedmember(
											sharedMember.getID(), id);
								}
							}

							List<Schedule> sbelongtoa = dbHelper
									.getSchedulesBelongtoActivity(id);
							for (int i = 0; i < sbelongtoa.size(); i++) {
								ContentValues scv = new ContentValues();
								scv.put(ScheduleTable.is_Deleted, 1);
								scv.put(ScheduleTable.is_Synchronized, 0);
								int schedule_id = sbelongtoa.get(i)
										.getSchedule_ID();
								// (sharedMember.getID(),id);

								List<Integer> onduties = dbHelper
										.getOndutyRecordsForSchedule(schedule_id);
								for (int j = 0; j < onduties.size(); j++) {
									dbHelper.deleteRelatedOnduty(schedule_id);
								}
								dbHelper.deleteSchedule(Integer.parseInt(id));
							}

							dbHelper.deleteActivity(id);

							Log.i("delete activity", "successfully");
							Intent intent = new Intent(
									CommConstant.DELETE_ACTIVITY_COMPLETE);
							mContext.sendBroadcast(intent);

							// ((Activity) mContext).finish();
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				public void onSuccess(JSONObject response) {
					Log.d("delete activity", response.toString());
					try {
						if (response.getString("lastmodified") != null) {
							ArrayList<Sharedmember> listSharedMemberOfActivity = dbHelper
									.getSharedMemberForActivity(id);
							if (listSharedMemberOfActivity != null
									&& listSharedMemberOfActivity.size() > 0) {
								for (Sharedmember sharedMember : listSharedMemberOfActivity) {
									dbHelper.deleteSharedmember(
											sharedMember.getID(), id);
								}
							}

							List<Schedule> sbelongtoa = dbHelper
									.getSchedulesBelongtoActivity(id);
							for (int i = 0; i < sbelongtoa.size(); i++) {
								ContentValues scv = new ContentValues();
								scv.put(ScheduleTable.is_Deleted, 1);
								scv.put(ScheduleTable.is_Synchronized, 0);
								int schedule_id = sbelongtoa.get(i)
										.getSchedule_ID();
								// (sharedMember.getID(),id);

								List<Integer> onduties = dbHelper
										.getOndutyRecordsForSchedule(schedule_id);
								for (int j = 0; j < onduties.size(); j++) {
									dbHelper.deleteRelatedOnduty(schedule_id);
								}
								dbHelper.deleteSchedule(Integer.parseInt(id));
							}

							dbHelper.deleteActivity(id);

							Log.i("delete activity", "successfully");
							Intent intent = new Intent(
									CommConstant.DELETE_ACTIVITY_COMPLETE);
							mContext.sendBroadcast(intent);

							// ((Activity) mContext).finish();
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void onStart() {
					// TODO Auto-generated method stub
					super.onStart();
					showLoading(mContext);
				}

				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					super.onFinish();
					try {
						// if (progress.isShowing()) {
						// progress.dismiss();
						// }
						dimissDialog();
					} catch (Exception ex) {
						ex.printStackTrace();
					}

				}

				@Override
				public void onFailure(int statusCode, Header[] headers,
						String responseString, Throwable throwable) {
					// TODO Auto-generated method stub
					super.onFailure(statusCode, headers, responseString,
							throwable);
					final ToastDialog dialog = new ToastDialog(mContext,
							mContext.getResources().getString(
									R.string.delete_activity_error));
					dialog.show();
					dialog.btnOk.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
				}
			});
		} else {
			final ToastDialog dialog = new ToastDialog(mContext, mContext
					.getResources().getString(R.string.no_network));
			dialog.show();
			dialog.btnOk.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}
	}

	/**
	 * reset password
	 * */
	public void resetPassword(final int memberid, final int role,
			final String activityid) {
		String sharedmemberUrl = BaseUrl.BASEURL + "resetpw" + "?"
				+ BaseUrl.URL_POST_FIX;
		Log.d("share member url", sharedmemberUrl);
		try {
			JSONObject sharedmemberParams = new JSONObject();
			sharedmemberParams.put("ownerid",
					new SharedReference().getCurrentOwnerId(mContext));
			sharedmemberParams.put("sharedrole", role);
			sharedmemberParams.put("memberid", memberid);

			Log.d("post shared member", sharedmemberParams.toString());

			client.addHeader("Content-type", "application/json");
			client.addHeader("User-Agent", userAgentString);

			// Log.i("add participant", sharedmemberParams.toString());
			StringEntity entity = new StringEntity(
					sharedmemberParams.toString());
			if (Utils.isNetworkOnline(mContext)) {
				client.post(null, sharedmemberUrl, entity, "application/json",
						new JsonHttpResponseHandler() {

							@Override
							public void onSuccess(int statusCode,
									Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								Log.i("successful sharedmember",
										response.toString());
								int code = 0;
								try {
									code = response.getInt("code");
									if (code != 200) {
										Toast.makeText(mContext,
												response.getString("message"),
												Toast.LENGTH_LONG).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
								try {
									String lastmodify = "";

									lastmodify = response
											.getString("lastmodified");
									Participant member = dbHelper
											.getParticipant(memberid);
									ContentValues cv = new ContentValues();
									cv.put(SharedMemberTable.member_name,
											member.getName());
									cv.put(SharedMemberTable.member_id,
											member.getID());
									cv.put(SharedMemberTable.member_email,
											member.getEmail());
									cv.put(SharedMemberTable.member_mobile,
											member.getMobile());
									cv.put(SharedMemberTable.role, role);
									cv.put(SharedMemberTable.is_Deleted, 0);
									cv.put(SharedMemberTable.last_modified,
											lastmodify);
									cv.put(SharedMemberTable.is_Synced, 1);
									dbHelper.updateSharedmember(member.getID(),
											cv);
									Intent intent = new Intent(
											CommConstant.GET_SHARED_MEMBER_ACTIVITY_COMPLETE);
									intent.putExtra(CommConstant.ACTIVITY_ID,
											activityid);
									mContext.sendBroadcast(intent);

								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

							}

							@Override
							public void onFailure(int statusCode,
									Header[] headers, String responseString,
									Throwable throwable) {
								// TODO Auto-generated method stub
								super.onFailure(statusCode, headers,
										responseString, throwable);
								final ToastDialog dialog = new ToastDialog(
										mContext,
										mContext.getResources().getString(
												R.string.reset_password_error));
								dialog.show();
								dialog.btnOk
										.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												dialog.dismiss();
											}
										});
							}

							@Override
							public void onStart() {
								// TODO Auto-generated method stub
								super.onStart();
								showLoading(mContext);
							}

							@Override
							public void onFinish() {
								// TODO Auto-generated method stub
								super.onFinish();
								try {
									// if (progress.isShowing()) {
									// progress.dismiss();
									// }
									dimissDialog();
								} catch (Exception ex) {
									ex.printStackTrace();
								}

							}
						});
			} else {
				final ToastDialog dialog = new ToastDialog(mContext, mContext
						.getResources().getString(R.string.no_network));
				dialog.show();
				dialog.btnOk.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
