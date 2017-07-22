package com.xindun.bluetoothtest.util;

import android.media.AudioFormat;
import android.os.Environment;

public class Contacts {

	/************************** HTTP请求接口 *****************************/
	private static final String BASE_URL_N = "http://192.168.1.215:8080/station/api/terminalAction_%s.action";
	private static final String  TCP_URL_N = "192.168.1.215";
//	private static final String BASE_URL_W = "http://223.223.186.190:8080/station/api/terminalAction_%s.action";
//	private static final String  TCP_URL_W = "223.223.186.190";
	private static final String BASE_URL_W = "http://223.223.186.188:8080/station/api/terminalAction_%s.action";
	private static final String  TCP_URL_W = "223.223.186.188";
	public static final String UPDATE_URL = "http://192.168.1.143:8083/mamDev/widgetStartup/postAppVersion/1111";
	
	public static boolean isOutUrl = true;
	
	public static final String BASE_URL = isOutUrl ? BASE_URL_W : BASE_URL_N;
	public static final String  TCP_URL = isOutUrl ? TCP_URL_W : TCP_URL_N;
	
	public static final int PORT_CONNECT = 1236;//指令信道端口号
	public static final int PORT_AUDIO = 1235;//音频信道端口号
	public static final String URL_LOGIN = "login";//登陆接口
	public static final String URL_GET_USER_LIST = "getUsersByRoom";//获取频道用户列表
	public static final String URL_REPORT_LOCATION = "reportLocation";//上报位置信息
	public static final String URL_REPORT_INFO = "reportInfo";//上报图片信息
	public static final String URL_GET_BROADCASTS = "getUserBroadcasts";//获取用户广播通知
	public static final String URL_GET_BROADCAST_DETAIL = "getBroadcastDetail";//获取广播通知详情
	public static final String URL_UPDATE_BROADCAST_STATE = "updateBroadcastState";//更改广播状态为已读
	public static final String URL_GET_ORG_DATA = "getOrgData";//获取企业下所有成员
	/**************************** 指令 *******************************/
	//以下指令为客户端上传指令
	public static final String HEADS_USERINFO = "HEADS,USERINFO,%s,%s";
	public static final String HEADS_INTERPH = "HEADS,INTERPH";//抢占话语权
	public static final String HEADS_INTERPHSTOP = "HEADS,INTERPHSTOP";//释放话语权
	public static final String HEADS_GETONLINEUSERS = "HEADS,GETONLINEUSERS";//获取所有在线用户
	
	//以下指令为服务端下发指令
	public static final String HEADR_INTERPHCOM = "HEADR,INTERPHCOM";//话语权被抢占
	public static final String HEADR_INTERPHSTOPCOM = "HEADR,INTERPHSTOPCOM";//话语权被释放
	public static final String HEADR_INTERPHBACK_0 = "HEADR,INTERPHBACK";//话语权请求结果(空闲)
	public static final String HEADR_INTERPHBACK_1 = "HEADR,INTERPHBACK";//话语权请求结果(忙碌)
	public static final String HEADR_REPEATCONNECT = "HEADR,REPEATCONNECT";//重复连接
	public static final String HEADR_ONLINEUSERS = "HEADR,ONLINEUSERS";//所有在线用户列表
	public static final String HEADR_BROADCAST = "HEADR,BROADCAST";//服务端下发通知
	
	public static final String HEARTBEAT_REQUEST = "HEADS,HT";//接收的心跳指令
	
	/************************* 音频录制参数 ****************************/
	public static final int FREQUENCY = 8000; //录制频率，单位hz
	@SuppressWarnings("deprecation")
	public static final int CHANNEL_CONFIG_RECORD = AudioFormat.CHANNEL_IN_MONO;//AudioFormat.CHANNEL_CONFIGURATION_MONO;//貌似被弃用，值是2
	public static final int CHANNEL_CONFIG_PLAY = AudioFormat.CHANNEL_OUT_MONO;//AudioFormat.CHANNEL_CONFIGURATION_MONO;//貌似被弃用，值是2
	public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	
	public static final int GET_LOCAL_INTERVAL = 60 * 1000;//获取位置信息的时间间隔
	
	public static final String ACTION_FLOOR_CHANGE = "com.xindun.poc.Action_floor_change";//话语权状态发生变化
	public static final String ACTION_TIME_CHANGE = "com.xindun.poc.Action_time_change";//通话时长发生变化
	public static final String ACTION_CHANNEL_CONNECT = "com.xindun.poc.Action_channel_connect";//频道连接成功
	public static final String ACTION_CHANNEL_DISCONNECT = "com.xindun.poc.Action_channel_disconnect";//信道已断开
	public static final String ACTION_MESSAGE_NOTIFY = "com.xindun.poc.Action_message_notify";//有新信息
	public static final String ACTION_LOGIN_REPEAT = "com.xindun.poc.Action_login_repeat";//重复登录
	public static final String ACTION_ONLINE_USERS = "com.xindun.poc.Action_online_users";//收到所有在线用户的列表
	public static final String ACTION_CHANNEL_MESSAGE_CHANGED = "com.xindun.poc.Action_channel_message_change";//对讲信息有变化
	public static final String ACTION_GET_CONTACTS_FINISH = "com.xindun.poc.Action_get_contacts_finish";//对讲信息有变化
	
	public static final String ACTION_LOGIN_SUCCESS = "com.xindun.poc.Action_login_success";//登录成功
	public static final String ACTION_LOGIN_FAILD = "com.xindun.poc.Action_login_failed";//登录失败
	
	public static final String BASE_PATH = Environment.getExternalStorageDirectory().getPath() + "/.xindun/.POC/";
	public static final String BASE_DOWNLOAD_PATH = BASE_PATH + ".download/";
	
	public static final int ENCODE_MODE_MAP_1_2 = 1;
	public static final int ENCODE_MODE_MAP_2_4 = 2;
	public static final int ENCODE_MODE_SPEEX_4 = 3;
	public static final int ENCODE_MODE_SPEEX_6 = 4;
	public static final int ENCODE_MODE_SPEEX_8 = 5;
	public static final int ENCODE_MODE_SPEEX_11 = 6;
	public static final int ENCODE_MODE_SPEEX_15 = 7;
	public static int ENCODE_MODE = ENCODE_MODE_SPEEX_8;
	
	public static final int GROUP_COUNT = 20;
}
