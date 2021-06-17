/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.csipsimple.ui.dialpad;

import android.annotation.SuppressLint;
import android.app.*;
import android.app.PendingIntent.CanceledException;
import android.content.*;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.*;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.app.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.csipsimple.CsipSampleConstant;
import com.csipsimple.R;
import com.csipsimple.api.*;
import com.csipsimple.api.SipUri.ParsedSipContactInfos;
import com.csipsimple.models.Filter;
import com.csipsimple.ui.SipHome;
import com.csipsimple.ui.SipHome.ViewPagerVisibilityListener;
import com.csipsimple.ui.dialpad.DialerLayout.OnAutoCompleteListVisibilityChangedListener;
import com.csipsimple.ui.rk3326.RKAssignFragment;
import com.csipsimple.ui.rk3326.RKAssignFuncFragment;
import com.csipsimple.ui.rk3326.RKAssignMenuFragment;
import com.csipsimple.ui.rk3326.RKBluetoothMenuFragment;
import com.csipsimple.ui.rk3326.RKBluetoothPairedFragment;
import com.csipsimple.ui.rk3326.RKBluetoothPinFragment;
import com.csipsimple.ui.rk3326.RKBluetoothScanFragment;
import com.csipsimple.ui.rk3326.RKCallFragment;
import com.csipsimple.ui.rk3326.RKHomeFragment;
import com.csipsimple.ui.rk3326.RKLockFragment;
import com.csipsimple.ui.rk3326.RKBluetoothFragment;
import com.csipsimple.ui.rk3326.RKMicFragment;
import com.csipsimple.ui.rk3326.RKMicMenuFragment;
import com.csipsimple.ui.rk3326.RKVolFragment;
import com.csipsimple.utils.*;
import com.csipsimple.utils.CallHandlerPlugin.OnLoadListener;
import com.csipsimple.utils.contacts.ContactsSearchAdapter;
import com.csipsimple.widgets.AccountChooserButton;
import com.csipsimple.widgets.AccountChooserButton.OnAccountChangeListener;
import com.csipsimple.widgets.DialerCallBar;
import com.csipsimple.widgets.DialerCallBar.OnDialActionListener;
import com.csipsimple.widgets.Dialpad;
import com.csipsimple.widgets.Dialpad.OnDialKeyListener;

import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

import com.csipsimple.wizards.WizardIface;
import com.poc.display.Display;


public class DialerFragment extends SherlockFragment implements OnClickListener, OnLongClickListener,
        OnDialKeyListener, TextWatcher, OnDialActionListener, ViewPagerVisibilityListener, OnKeyListener,
        OnAutoCompleteListVisibilityChangedListener {

    private static final String THIS_FILE = "DialerFragment";

    protected static final int PICKUP_PHONE = 0;
    protected static final int GROUP_KEYCODE_1 = 1234;
    protected static final int GROUP_KEYCODE_2 = 1235;
    protected static final int GROUP_KEYCODE_3 = 1236;
    protected static final int GROUP_KEYCODE_4 = 1237;

    private final static int TAB_ID_LOCK = 0;
    private final static int TAB_ID_HOME = 1;
    private final static int TAB_ID_ASSIGN_MENU = 2;
    private final static int TAB_ID_ASSIGN = 3;
    private final static int TAB_ID_ASSIGN_FUNC = 4;
    private final static int TAB_ID_BLUETOOTH_MENU = 5;
    private final static int TAB_ID_BLUETOOTH = 6;
    private final static int TAB_ID_BLUETOOTH_SCAN = 7;
    private final static int TAB_ID_BLUETOOTH_PIN = 8;
    private final static int TAB_ID_BLUETOOTH_PAIRED = 9;
    private final static int TAB_ID_MIC_MENU = 10;
    private final static int TAB_ID_MIC = 11;
    private final static int TAB_ID_VOL = 12;
    private final static int TAB_ID_CALL = 13;

    private final static int NUM_WIFI_LEVEL = 100;

    private Display disp = Display.getInstance();

    //private Drawable digitsBackground, digitsEmptyBackground;
    private DigitsEditText digits;
    private String initTextDigits = null;
    private String initTextUser = null;
    private String initTextGroup = null;
    private String initTextVol = null;
    //private ImageButton switchTextView;

    //private View digitDialer;

    private AccountChooserButton accountChooserButton;
    private Boolean isDigit = null;
    /* , isTablet */
    private DialingFeedback dialFeedback;

    /*
    private final int[] buttonsToAttach = new int[] {
            R.id.switchTextView
    };
    */
    private final int[] buttonsToLongAttach = new int[] {
            R.id.button0, R.id.button1
    };

    // private GestureDetector gestureDetector;
    private Dialpad dialPad;

    //private PreferencesWrapper prefsWrapper;
    private PreferencesProviderWrapper prefsWrapper;
    private AlertDialog missingVoicemailDialog;

    // Auto completion for text mode
    private ListView autoCompleteList;
    private ContactsSearchAdapter autoCompleteAdapter;

    private DialerCallBar callBar;
    private boolean mDualPane;

    private DialerAutocompleteDetailsFragment autoCompleteFragment;
    private PhoneNumberFormattingTextWatcher digitFormater;
    private OnAutoCompleteListItemClicked autoCompleteListItemListener;

    private DialerLayout dialerLayout;

    private MenuItem accountChooserFilterItem;

    private TextView rewriteTextInfo;

    public static ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;
    private static Context mContext;

    private int mBtnIndex = 0;
    private int mFuncIndex = 0;

    private HashMap<Integer, Integer> mAssignFunc = new HashMap<Integer, Integer>();
    private int callerID;
    private String preCallee = "";
    //    mAssignFunc.put("key", "value");

    @Override
	public void onAutoCompleteListVisibiltyChanged() {
        applyTextToAutoComplete();
	}

	IpPhoneConfigInfo configIpPhone = new IpPhoneConfigInfo();
    HashMap<String, String> configOther = null;
    HashMap<String, String> configSip = null;
    HashMap<String, String> configSetting = null;
    int currentVolume = 0;
    int currentMic = 0;

    private TextView batteryTxt;
    private TextView wifiTxt;
    private TextView volTxt;
    private TextView userTxt;
    private TextView groupTxt;
    private ImageView batteryImg;
    private ImageView wifiImg;
    private int batLevel = 0;
//  2021-05-27 add
    private boolean batCharging = false;
//  Enđ add
    private int wifiLevel = 0;
    private int volLevel = 0;
    private boolean useBT = false;

    // TimingLogger timings = new TimingLogger("SIP_HOME", "test");
    private boolean serviceConnected = false;
    private ISipService service;
    SipCallSession[] callsInfo =null;
    private Object callMutex = new Object();
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            System.out.println("SIP Service connected");
            service = ISipService.Stub.asInterface(arg1);
//            try {
//                // Log.d(THIS_FILE,
//                // "Service started get real call info "+callInfo.getCallId());

//                callsInfo = service.getCalls();
                serviceConnected = true;

//                updateUIFromMedia();
                connectGroupDefault();

//                runOnUiThread(new UpdateUIFromCallRunnable());
//                runOnUiThread(new UpdateUIFromMediaRunnable());
//            } catch (RemoteException e) {
//                Log.e(THIS_FILE, "Can't get back the call", e);
//            }

            /*
             * timings.addSplit("Service connected"); if(configurationService !=
             * null) { timings.dumpToLog(); }
             */
            //keyPressed(KeyEvent.KEYCODE_3);
            //connectGroup(1);
            //Toast.makeText(mContext, "Service connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            System.out.println("Service disconnected");
            serviceConnected = false;
            service = null;
        }
    };

    private BroadcastReceiver callStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(SipManager.ACTION_SIP_CALL_CHANGED)) {
                if (service != null) {
                    try {
                        synchronized (callMutex) {
                            callsInfo = service.getCalls();
//                            runOnUiThread(new UpdateUIFromCallRunnable());
                        }
                    } catch (RemoteException e) {
                        Log.e(THIS_FILE, "Not able to retrieve calls");
                    }
                }
            } else if (action.equals(SipManager.ACTION_SIP_MEDIA_CHANGED)) {
                if (service != null) {
                    MediaState mediaState;
                    try {
                        mediaState = service.getCurrentMediaState();
                        Log.d(THIS_FILE, "Media update ...." + mediaState.isSpeakerphoneOn);
//                        synchronized (callMutex) {
//                            if (!mediaState.equals(lastMediaState)) {
//                                lastMediaState = mediaState;
//                                runOnUiThread(new UpdateUIFromMediaRunnable());
//                            }
//                        }
                    } catch (RemoteException e) {
                        Log.e(THIS_FILE, "Can't get the media state ", e);
                    }
                }
            } else if (action.equals(SipManager.ACTION_ZRTP_SHOW_SAS)) {
                SipCallSession callSession = intent.getParcelableExtra(SipManager.EXTRA_CALL_INFO);
                String sas = intent.getStringExtra(Intent.EXTRA_SUBJECT);
//                runOnUiThread(new ShowZRTPInfoRunnable(callSession, sas));
            }
        }
    };

//  2021-05-26 added
    private int afterSeconds = 30 * 1000; // Event every 30 seconds
    private Handler handlerBattery = null;
    private Runnable alarmBattery = new Runnable() {

        @Override
        public void run() {
            // Check battery level
            checkBatteryStatup();
            if(handlerBattery != null) {
                // Delay 30s
                handlerBattery.postDelayed(alarmBattery, afterSeconds);
            }
        }
    };
//  End add

// 2021-05-27 modified
    public BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
//            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
//            Log.d(THIS_FILE, String.valueOf(level) + "%");
//            batteryTxt.setText(String.valueOf(level) + "%");
//            if(level > 90) {
//                batteryImg.setImageResource(R.drawable.battery_3);
//                batLevel = 3;
//                disp.drawBat(3);
//            } else if(level > 60) {
//                batteryImg.setImageResource(R.drawable.battery_2);
//                batLevel = 2;
//                disp.drawBat(2);
//            } else if(level > 30) {
//                batteryImg.setImageResource(R.drawable.battery_1);
//                batLevel = 1;
//                disp.drawBat(1);
//            } else {
//                batLevel = 0;
//                //disp.drawBat(0);
//                //Alarm BatteryAlarm.wav
//                //batteryImg.setImageResource(R.drawable.battery_off);
//            }

            // This will give you battery current status
            try {
//                int level = intent.getIntExtra("level", 0);
//                int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
//                int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                String BStatus = "No Data";
                boolean flagON = false;
                batCharging = false;
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    BStatus = "Charging cable connection";
                    flagON = true;
                    batCharging = true;
                }
                if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                    BStatus = "Charging cable is unplugged";
                    flagON = true;
                }
                if (status == BatteryManager.BATTERY_STATUS_FULL) {
                    BStatus = "Full battery";
                }
                if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                    BStatus = "Not Charging";
                }
                if (status == BatteryManager.BATTERY_STATUS_UNKNOWN) {
                    BStatus = "Unknown";
                }

//                int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
//                String BattPowerSource = "No Data";
//                if (chargePlug == BatteryManager.BATTERY_PLUGGED_AC){BattPowerSource = "AC";}
//                if (chargePlug == BatteryManager.BATTERY_PLUGGED_USB){BattPowerSource = "USB";}
//
//                String BattLevel = String.valueOf(level);
//
//                int BHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
//                String BatteryHealth = "No Data";
//                if (BHealth == BatteryManager.BATTERY_HEALTH_COLD){BatteryHealth = "Cold";}
//                if (BHealth == BatteryManager.BATTERY_HEALTH_DEAD){BatteryHealth = "Dead";}
//                if (BHealth == BatteryManager.BATTERY_HEALTH_GOOD){BatteryHealth = "Good";}
//                if (BHealth == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE){BatteryHealth = "Over-Voltage";}
//                if (BHealth == BatteryManager.BATTERY_HEALTH_OVERHEAT){BatteryHealth = "Overheat";}
//                if (BHealth == BatteryManager.BATTERY_HEALTH_UNKNOWN){BatteryHealth = "Unknown";}
//                if (BHealth == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE){BatteryHealth = "Unspecified Failure";}

                //Do whatever with the data here
                // turnLightOfPower(flagON);

            } catch (Exception e){
                //Log.v(TAG, "Battery Info Error");
            }
        }
    };

// 2021-06-01 Added
    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
//    private BroadcastReceiver mInitialReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // Get extra data included in the Intent
//            keyPressed(KeyEvent.KEYCODE_3);
//        }
//    };

    public void turnLightOfPower(boolean turn_on) {
        if (turn_on) {
            Toast.makeText(mContext, "LED charging is ON", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "LED charging is OFF", Toast.LENGTH_SHORT).show();
        }
    }
// End modified
    /**
     * 2021-05-26 added
     * Update battery status on StatusBarUI
     */
    private int batSpecifiedLevel = 10;
    public void checkBatteryStatup() {
        BatteryManager bm = (BatteryManager) mContext.getSystemService(Context.BATTERY_SERVICE);
        int level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        //Toast.makeText(mContext, "Update Battery State [" + level + "]", Toast.LENGTH_SHORT).show();

        if(level > 90) {
            batLevel = 3;
        } else if(level > 60) {
            batLevel = 2;
        } else if(level > 30) {
            batLevel = 1;
        } else {
            batLevel = 0;
            // Below the specified level
            if (level <= batSpecifiedLevel) {
                // The battery cable is plugging
                if (!batCharging) {
                    // Play BatteryAlarm.wav
                    // And vibrate motor 1 second operation
                    if (configOther != null) {
                        String pathBatteryAlarmFile = configOther.get("BatteryAlarmAudioFile");
                        playAlarmSoundAndVibrator(pathBatteryAlarmFile);
                    }
                }
            }
        }
        // Display battery level
        drawBat(batLevel,String.valueOf(level) + "%");
        // Test Battery Alarm sound & vibrator
//        if (configOther != null) {
//            // WiFiAlarmAudioFile
//            // BatteryAlarmAudioFile
//            String pathBatteryAlarmFile = configOther.get("WiFiAlarmAudioFile");
//            playAlarmSoundAndVibrator(pathBatteryAlarmFile);
//        }
    }

    /**
     * 2021-05-27 added
     * Check and play alarm sound and vibrator
     */
    private Ringtone alarmSound;
    private Vibrator alarmVibrator;
    //final static private String PATH = "/Download/IpPhone";
    public void playAlarmSoundAndVibrator(String filename) {

//        if (configOther != null) {
        // Load playback file setting
        // String pathBatteryAlarmFile = configOther.get("BatteryAlarmAudioFile");
        File rootDir = Environment.getExternalStorageDirectory().getAbsoluteFile();
        File myDir = new File(rootDir.getAbsolutePath() + IpPhoneConfigInfo.PATH_OF_CONFIG_FILES);
        File file = new File(myDir, filename);
        //File file = new File(IpPhoneConfigInfo.PATH_OF_CONFIG_FILES, filename);

        // If playback file setting doesn't exist
        if (!file.exists()) return;

        //Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //Uri alarmUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/raw/battery_alarm");
        Uri alarmUri = Uri.parse(file.toString());
        // Get instance of Vibrator from current Context
        alarmVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        try {
            alarmSound = RingtoneManager.getRingtone(mContext, alarmUri);
            if (alarmSound != null) {
                alarmSound.play();
                // Vibrate for 400 milliseconds
                alarmVibrator.vibrate(400);
                Timer mTimer = new Timer();
                mTimer.scheduleAtFixedRate(new TimerTask() {
                    public void run() {
                        int i = 0;
                        while (i < 2) {
                            if (!alarmSound.isPlaying()) {
                                alarmSound.play();
                                alarmVibrator.vibrate(400);
                                i++;
                            }
                        }
                        this.cancel();
                    }}, 1000*1, 1000*1);
            }

        } catch (Exception e) {
            Log.d(THIS_FILE, "playNotificationSound: " + e.getMessage());
        }
//        }
    }

    /**
     * 2021-06-01 added
     * Check & update wifi status
     */
    public void updateWifiStatus() {
        int level = calculateSignalLevel();

        if(level > 90) {
            wifiLevel = 3;
        } else if(level > 60) {
            wifiLevel = 2;
        } else if(level > 30) {
            wifiLevel = 1;
        } else {
            wifiLevel = 0;
            //Alarm WifiAlarm.wav
// 2021-06-01 Added
            if (configOther != null) {
                String pathWifiAlarmFile = configOther.get("WiFiAlarmAudioFile");
                playAlarmSoundAndVibrator(pathWifiAlarmFile);
            }
            Toast.makeText(getActivity(),"Out of wifi range",Toast.LENGTH_SHORT).show();
// End add
        }
        //Display signal level
        drawSignal(wifiLevel, String.valueOf(level) + "%");
    }

    public int calculateSignalLevel() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), NUM_WIFI_LEVEL);
        System.out.println("Wifi level (calculateSignalLevel): " + level); // wifiManager.getWifiState()

        return level;
    }

    /**
     * 2021-06-12 Added
     * Read from MAC address from system and write to xml file
     */
    private void saveMacAddress() {
        List<NetworkInterface> networkInterfaceList = null;
        try {
            networkInterfaceList = Collections.list(NetworkInterface.getNetworkInterfaces());

            String stringMac = "";
            for (NetworkInterface networkInterface:networkInterfaceList){
                if (networkInterface.getName().equalsIgnoreCase("wlan0")){
                    for (int i=0; i<networkInterface.getHardwareAddress().length;i++){
                        String stringMacByte = Integer.toHexString(networkInterface.getHardwareAddress()[i] & 0xFF);

                        if (stringMacByte.length() == 1){
                            stringMacByte = "0" + stringMacByte;
                        }

                        stringMac = stringMac + stringMacByte.toUpperCase() + ":";
                    }
                    System.out.println("networkInterface: " + networkInterface.getSubInterfaces().toString());
                    break;
                }
            }
            System.out.println("Network list: " + networkInterfaceList.toString());
            System.out.println("Wifi Mac Address: " + stringMac);
            //configInfo.updateSetting("WifiMacAddress", stringMac, SipHome.IP_PHONE_BACKUP_XML);
            saveSettingMacAddress(stringMac);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public int getAudioOutput() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        //        PackageManager packageManager = mContext.getPackageManager();
        volLevel = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);  //khanh: change STREAM_RING to STREAM_SYSTEM
        return volLevel;
        //        int volMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        //        audioManager.setStreamVolume(AudioManager.STREAM_RING, vol, 1);

        //        if (audioManager.isBluetoothA2dpOn()) {
        //            // Adjust output for Bluetooth.
        //            return true;
        //        } else if (audioManager.isBluetoothScoOn()) {
        //            // Adjust output for Bluetooth of sco.
        //            return true;
        //        } else if (audioManager.isSpeakerphoneOn()) {
        //            // Adjust output for Speakerphone.
        //            return true;
        //        } else if (packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
        //            // Has internal speaker or other form of audio output.
        //            return true;
        //        } else {
        //            // No device for audio output.
        //            return false;
        //        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity().getApplicationContext();
        mDualPane = getResources().getBoolean(R.bool.use_dual_panes);
        digitFormater = new PhoneNumberFormattingTextWatcher();
        // Auto complete list in case of text
        autoCompleteAdapter = new ContactsSearchAdapter(getActivity());
        autoCompleteListItemListener = new OnAutoCompleteListItemClicked(autoCompleteAdapter);

        if(isDigit == null) {
            isDigit = !prefsWrapper.getPreferenceBooleanValue(SipConfigManager.START_WITH_TEXT_DIALER);
        }
        setHasOptionsMenu(true);
// 2021-05-28 Added
//        if (shouldAskPermissions()) {
//            askPermissions();

//        boolean flag = configIpPhone.parseXMLFromSD(SipHome.IP_PHONE_BACKUP_XML);
//        if (flag) {
//// 2021-06-01
//            ArrayList<HashMap<String, String >> listSip = configIpPhone.getConfigSIP();
//            if (listSip != null && listSip.isEmpty() == false) {
//                configSip = listSip.get(0);
//            }
//            ArrayList<HashMap<String, String >> listOther = configIpPhone.getConfigOther();
//            if (listOther != null && listOther.isEmpty() == false) {
//                configOther = listOther.get(0);
//            }
//            ArrayList<HashMap<String, String >> listSetting = configIpPhone.getConfigSetting();
//            if (listSetting != null && listSetting.isEmpty() == false) {
//                configSetting = listSetting.get(0);
//            }
//        }
//        }
// End add
// 2021-06-01
        // Register to receive messages.
        // We are registering an observer (mInitialReceiver) to receive Intents
        // with actions named "custom-event-name".
        //LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mInitialReceiver, new IntentFilter("Connect to Group 1"));
// End add
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dialer_digit, container, false);
        // Store the backgrounds objects that will be in use later
        /*
        Resources r = getResources();
        
        digitsBackground = r.getDrawable(R.drawable.btn_dial_textfield_active);
        digitsEmptyBackground = r.getDrawable(R.drawable.btn_dial_textfield_normal);
        */
// 2021-05-26 added
        handlerBattery = new Handler();
        handlerBattery.postDelayed(alarmBattery, afterSeconds);
// End add
        setStatusBarUI(v);
        //initialize();
        //drawLockScreenRK();
// 2021-06-02 added
//        handlerLockScreen = new Handler();
//        handlerLockScreen.postDelayed(timerLockScreen, 1000);
// End add

// 2021-06-01
//        Intent intent = new Intent("Connect to Group 1");
//        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
// End add

        //Tabs for RK3326 UI fragments
        final ActionBar abRK = this.getSherlockActivity().getSupportActionBar();
        abRK.setDisplayShowHomeEnabled(true);
        abRK.setDisplayShowTitleEnabled(true);
        abRK.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // showAbTitle = Compatibility.hasPermanentMenuKey

        Tab homeTab = abRK.newTab();
        Tab bluetoothTab = abRK.newTab();
        Tab bluetoothMenuTab = abRK.newTab();
        Tab bluetoothPairedTab = abRK.newTab();
        Tab bluetoothPinTab = abRK.newTab();
        Tab bluetoothScanTab = abRK.newTab();
        Tab assignTab = abRK.newTab();
        Tab assignMenuTab = abRK.newTab();
        Tab assignFunTab = abRK.newTab();
        Tab micTab = abRK.newTab();
        Tab micMenuTab = abRK.newTab();
        Tab volTab = abRK.newTab();
        Tab lockTab = abRK.newTab();
        Tab callTab = abRK.newTab();
        //        abRK.hide();

        mViewPager = (ViewPager) v.findViewById(R.id.pager_rk);
        mTabsAdapter = new TabsAdapter(this.getActivity(), this.getSherlockActivity().getSupportActionBar(), mViewPager);
        mTabsAdapter.addTab(lockTab, RKLockFragment.class, TAB_ID_LOCK);
        mTabsAdapter.addTab(homeTab, RKHomeFragment.class, TAB_ID_HOME);
        mTabsAdapter.addTab(assignMenuTab, RKAssignMenuFragment.class, TAB_ID_ASSIGN_MENU);
        mTabsAdapter.addTab(assignTab, RKAssignFragment.class, TAB_ID_ASSIGN);
        mTabsAdapter.addTab(assignFunTab, RKAssignFuncFragment.class, TAB_ID_ASSIGN_FUNC);
        mTabsAdapter.addTab(bluetoothMenuTab, RKBluetoothMenuFragment.class, TAB_ID_BLUETOOTH_MENU);
        mTabsAdapter.addTab(bluetoothTab, RKBluetoothFragment.class, TAB_ID_BLUETOOTH);
        mTabsAdapter.addTab(bluetoothScanTab, RKBluetoothScanFragment.class, TAB_ID_BLUETOOTH_SCAN);
        mTabsAdapter.addTab(bluetoothPinTab, RKBluetoothPinFragment.class, TAB_ID_BLUETOOTH_PIN);
        mTabsAdapter.addTab(bluetoothPairedTab, RKBluetoothPairedFragment.class, TAB_ID_BLUETOOTH_PAIRED);
        mTabsAdapter.addTab(micMenuTab, RKMicMenuFragment.class, TAB_ID_MIC_MENU);
        mTabsAdapter.addTab(micTab, RKMicFragment.class, TAB_ID_MIC);
        mTabsAdapter.addTab(volTab, RKVolFragment.class, TAB_ID_VOL);
        mTabsAdapter.addTab(callTab, RKCallFragment.class, TAB_ID_CALL);
        // Store some object that could be useful later
        digits = (DigitsEditText) v.findViewById(R.id.digitsText);
        dialPad = (Dialpad) v.findViewById(R.id.dialPad);
        callBar = (DialerCallBar) v.findViewById(R.id.dialerCallBar);
        autoCompleteList = (ListView) v.findViewById(R.id.autoCompleteList);
        rewriteTextInfo = (TextView) v.findViewById(R.id.rewriteTextInfo);
        accountChooserButton = (AccountChooserButton) v.findViewById(R.id.accountChooserButton);
        accountChooserFilterItem = accountChooserButton.addExtraMenuItem(R.string.apply_rewrite);
        accountChooserFilterItem.setCheckable(true);
        accountChooserFilterItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setRewritingFeature(!accountChooserFilterItem.isChecked());
                return true;
            }
        });
        setRewritingFeature(prefsWrapper.getPreferenceBooleanValue(SipConfigManager.REWRITE_RULES_DIALER));
        dialerLayout = (DialerLayout) v.findViewById(R.id.top_digit_dialer);
        //switchTextView = (ImageButton) v.findViewById(R.id.switchTextView);

        // isTablet = Compatibility.isTabletScreen(getActivity());

        // Digits field setup
        if(savedInstanceState != null) {
            isDigit = savedInstanceState.getBoolean(TEXT_MODE_KEY, isDigit);
        }
        digits.setText(String.valueOf(GROUP_KEYCODE_1));
        digits.setOnEditorActionListener(keyboardActionListener);
        // Layout 
        dialerLayout.setForceNoList(mDualPane);
        dialerLayout.setAutoCompleteListVisibiltyChangedListener(this);

        // Account chooser button setup
        accountChooserButton.setShowExternals(true);
        accountChooserButton.setOnAccountChangeListener(accountButtonChangeListener);

        // Dialpad
        dialPad.setOnDialKeyListener(this);

        // We only need to add the autocomplete list if we
        autoCompleteList.setAdapter(autoCompleteAdapter);
        autoCompleteList.setOnItemClickListener(autoCompleteListItemListener);
        autoCompleteList.setFastScrollEnabled(true);

        // Bottom bar setup
        callBar.setOnDialActionListener(this);
        callBar.setVideoEnabled(prefsWrapper.getPreferenceBooleanValue(SipConfigManager.USE_VIDEO));

        //switchTextView.setVisibility(Compatibility.isCompatible(11) ? View.GONE : View.VISIBLE);

        // Init other buttons
        initButtons(v);
        // Ensure that current mode (text/digit) is applied
        setTextDialing(!isDigit, true);
        if(initTextDigits != null) {
            digits.setText(initTextDigits);
            initTextDigits = null;
        }
        //digits.setText(String.valueOf(GROUP_KEYCODE_1));

        // Apply third party theme if any
        applyTheme(v);
        v.setOnKeyListener(this);
        applyTextToAutoComplete();

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void initialize() {
        boolean flag = configIpPhone.parseXMLFromSD(SipHome.IP_PHONE_BACKUP_XML);
        if (flag) {
// 2021-06-01
            ArrayList<HashMap<String, String >> listSip = configIpPhone.getConfigSIP();
            if (listSip != null && listSip.isEmpty() == false) {
                configSip = listSip.get(0);
            }
            ArrayList<HashMap<String, String >> listOther = configIpPhone.getConfigOther();
            if (listOther != null && listOther.isEmpty() == false) {
                configOther = listOther.get(0);
            }
            ArrayList<HashMap<String, String >> listSetting = configIpPhone.getConfigSetting();
            if (listSetting != null && listSetting.isEmpty() == false) {
                configSetting = listSetting.get(0);
            }
        }
        saveMacAddress();
        setDefaultAssignFunc();

// 2021-06-02 Added
        if (configSip != null) {
            userTxt.setText(configSip.get("TerminalNumber"));
        }
        int currentGroup = 0;
        if (configSetting != null) {
            currentGroup = Integer.parseInt(configSetting.get("ConnectGroupCurrent"));
            if (currentGroup % 4 == 0) {
                mCurrentGroupNo = 4;
                mCurrentGroupPage = currentGroup / 4;
            } else {
                mCurrentGroupNo = currentGroup % 4;
                mCurrentGroupPage = (currentGroup / 4) + 1;
            }
            if (checkSettingGroup(mCurrentGroupNo)) {
                groupTxt.setText(group);
            }
        } else {
            if (checkSettingGroup(1)) {
                groupTxt.setText(group);
            }
        }

        if (configOther != null) {
            currentMic = Integer.parseInt(configOther.get("MicVolume"));
        }
        // Set the current value to volume speaker
        if (configSetting != null) {
            currentVolume = Integer.parseInt(configSetting.get("SpeakerVolumeCurrent"));
        }
        //currentVolume = 1;
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_SYSTEM, currentVolume, AudioManager.FLAG_PLAY_SOUND);
        volTxt.setText(String.valueOf(currentVolume));
// End add
        if (configSetting != null) {
            is_Locked = configSetting.get("KeyLockMode").equals("1")? true : false;
            if (is_Locked) {
                drawScreen(TAB_ID_LOCK);
            } else {
                drawScreen(TAB_ID_HOME);
            }
        }
        //checkBatteryStatup();
        //updateWifiStatus();
        // Connect to G1 by default
        //keyPressed(KeyEvent.KEYCODE_3);
        //connectGroupDefault();
    }

    private void setStatusBarUI(View v) {
        batteryTxt = (TextView) v.findViewById(R.id.txt_battery);
        wifiTxt = (TextView) v.findViewById(R.id.txt_wifi);
        volTxt = (TextView) v.findViewById(R.id.txt_volumn);
// 2021-05-29
        userTxt = (TextView) v.findViewById(R.id.txt_user_id);
        groupTxt = (TextView) v.findViewById(R.id.txt_group_id);
        wifiImg = (ImageView) v.findViewById(R.id.img_wifi);
        batteryImg = (ImageView) v.findViewById(R.id.img_battery);

// 2021-05-26 modified
        this.getActivity().registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
//        context.registerReceiver(wifiScanReceiver, intentFilter);


// 2021-06-01 Added
        checkBatteryStatup();
        updateWifiStatus();
// End add
    }

    protected void drawLockScreenRK() {
        drawStatusBarRK();
        disp.showString(46, 3, "KEY LOCK");
        Toast.makeText(mContext,"lock",Toast.LENGTH_SHORT).show();
    }

    protected void drawStatusBarRK() {
        disp.clearScreen();

        /* status bar */
        disp.drawBat(batLevel);
        disp.drawSignal(wifiLevel);
        drawDeviceNo();
        drawGroup();
        disp.drawVolume(volLevel);
        disp.drawBluetooth(useBT);
        //disp.drawPtt(true); --> temporary move to DialerCallBar.java
    }

    protected void drawBat(int level, String percent) {
        batteryTxt.setText(percent);
        switch (level) {
            case 0:
                batteryImg.setImageResource(R.drawable.battery_0);
                break;
            case 1:
                batteryImg.setImageResource(R.drawable.battery_1);
                break;
            case 2:
                batteryImg.setImageResource(R.drawable.battery_2);
                break;
            case 3:
                batteryImg.setImageResource(R.drawable.battery_3);
                break;
            default:
                break;
        }
        // Display info on LCD
        //disp.showString();
        disp.drawBat(batLevel);
    }

    protected void drawSignal(int level, String percent) {
        wifiTxt.setText(percent);
        switch (level) {
            case 0:
                wifiImg.setImageResource(R.drawable.net_0);
                break;
            case 1:
                wifiImg.setImageResource(R.drawable.net_1);
                break;
            case 2:
                wifiImg.setImageResource(R.drawable.net_2);
                break;
            case 3:
                wifiImg.setImageResource(R.drawable.net_3);
                break;
            default:
                break;
        }
        // Display info on LCD
        //disp.showString();
        disp.drawSignal(wifiLevel);
    }

    protected void drawDeviceNo() {
        disp.showString(16, 0, phone_no + " ");
    }

    protected void drawGroup() {
        disp.showString(58, 0, group + " ");
    }

    protected void drawScreen(int tabID) {
        String str = "";

        drawStatusBarRK();

        switch (tabID) {
            case TAB_ID_LOCK: str = "LOCK";
                //disp.showString(46, 3*LCD_STRING_UNIT, str);
                mViewPager.setCurrentItem(TAB_ID_LOCK);
                if (mLockFragment == null) { //update fragment
                    mLockFragment = (RKLockFragment) mTabsAdapter.getItem(TAB_ID_LOCK);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(mViewPager.getChildAt(TAB_ID_LOCK) != null) {
                                mLockFragment.setTextPage(mCurrentGroupPage);
                                mLockFragment.drawScreen(); //should be called last after updating related variable
                            }
                        }
                    }, 1);
                } else {    //still need to call display
                    //TODO: Check if need to call in handler postDelayed ?
                    mLockFragment.drawScreen();
                }
                break;
            case TAB_ID_HOME: str = "HOME";
                mViewPager.setCurrentItem(TAB_ID_HOME);
                if (mHomeFragment == null) { //update fragment
                    mHomeFragment = (RKHomeFragment) mTabsAdapter.getItem(TAB_ID_HOME);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(mViewPager.getChildAt(TAB_ID_HOME) != null) {
                                mHomeFragment.setTextPage(mCurrentGroupPage);
                                mHomeFragment.drawScreen(mAssignFunc); //should be called last after updating related variable
                            }
                        }
                    }, 1);
                } else {    //still need to call display
                    //TODO: Check if need to call in handler postDelayed ?
                    mHomeFragment.setTextPage(mCurrentGroupPage);
                    mHomeFragment.drawScreen(mAssignFunc);
                }
                break;
            case TAB_ID_ASSIGN_MENU: str = "ASSIGN";
                mViewPager.setCurrentItem(TAB_ID_ASSIGN_MENU);
                if (mAssignMenuFragment == null) { //update fragment
                    mAssignMenuFragment = (RKAssignMenuFragment) mTabsAdapter.getItem(TAB_ID_ASSIGN_MENU);
                }
                mAssignMenuFragment.drawScreen();
                break;
            case TAB_ID_ASSIGN: str = "ASSIGN: SELECT BTN";
                if (mAssignFragment != null)
                    mAssignFragment.drawScreen();
                break;
            case TAB_ID_ASSIGN_FUNC: str = "ASSIGN: SELECT FUN";
                if (mAssignFuncFragment != null) {
                    int funcIndex = mAssignFunc.get(mBtnIndex);
                    mAssignFuncFragment.drawScreen(funcIndex);
                }
                break;
            case TAB_ID_BLUETOOTH: str = "BLUETOOTH";
                if (mBluetoothFragment != null)
                    mBluetoothFragment.drawScreen();
                break;
            case 7: str = "SCAN";
                disp.showString(46, 3, str);
                break;
            case 8: str = "PIN:";
                disp.showString(46, 3, str);
                break;
            case 9: str = "PAIRED:OK";
                disp.showString(46, 3, str);
                break;
            case TAB_ID_MIC: str = "MIC";
                if (mMicFragment != null)
                    mMicFragment.drawScreen();
                break;
            case TAB_ID_VOL: str = "VOL";
                if (mVolFragment != null)
                    mVolFragment.drawScreen();
                break;
            case 13: str = "CALL...";
                disp.showString(46, 3, str);
                break;
            default: str = "";
                break;
        }

        //Toast.makeText(getActivity(),str,Toast.LENGTH_SHORT).show();
    }

    private class TabsAdapter extends FragmentPagerAdapter implements
            ViewPager.OnPageChangeListener, ActionBar.TabListener {
        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final List<String> mTabs = new ArrayList<String>();
        private final List<Integer> mTabsId = new ArrayList<Integer>();
        private boolean hasClearedDetails = false;


        private int mCurrentPosition = -1;
        /**
         * Used during page migration, to remember the next position
         * {@link #onPageSelected(int)} specified.
         */
        private int mNextPosition = -1;

        public TabsAdapter(FragmentActivity activity, ActionBar actionBar, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mActionBar = actionBar;
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab, Class<?> clss, int tabId) {
            mTabs.add(clss.getName());
            mTabsId.add(tabId);
            mActionBar.addTab(tab.setTabListener(this));
            notifyDataSetChanged();
        }

        public void removeTabAt(int location) {
            mTabs.remove(location);
            mTabsId.remove(location);
            mActionBar.removeTabAt(location);
            notifyDataSetChanged();
        }

        public Integer getIdForPosition(int position) {
            if(position >= 0 && position < mTabsId.size()) {
                return mTabsId.get(position);
            }
            return null;
        }

        public Integer getPositionForId(int id) {
            int fPos = mTabsId.indexOf(id);
            if(fPos >= 0) {
                return fPos;
            }
            return null;
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            //Fragment f = Fragment.instantiate(mContext, mTabs.get(position), new Bundle());
//            switch (position) {
//                case TAB_ID_HOME:
//                    mHomeFragment = (RKHomeFragment) Fragment.instantiate(mContext, mTabs.get(position), new Bundle());
//                    //mHomeFragment.drawScreen();
//                    return mHomeFragment;
//            }
            return Fragment.instantiate(mContext, mTabs.get(position), new Bundle());
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
//            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
//            // save the appropriate reference depending on position
//            switch (position) {
//                case TAB_ID_LOCK:
//                    break;
//                case TAB_ID_HOME:
//                    mHomeFragment = (RKHomeFragment) createdFragment;
//                    //mHomeFragment.drawScreen();
//                    mHomeFragment.
//                    return mHomeFragment;
//                default:
//                    break;
//            }
//            return createdFragment;
            return super.instantiateItem(container, position);
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            clearDetails();
            if (mViewPager.getCurrentItem() != tab.getPosition()) {
                mViewPager.setCurrentItem(tab.getPosition(), true);
            }
        }

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);

            if (mCurrentPosition == position) {
                Log.w(THIS_FILE, "Previous position and next position became same (" + position
                        + ")");
            }

            mNextPosition = position;
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // Nothing to do
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // Nothing to do
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // Nothing to do
        }

        /*
         * public void setCurrentPosition(int position) { mCurrentPosition =
         * position; }
         */

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case ViewPager.SCROLL_STATE_IDLE: {
                    if (mCurrentPosition >= 0) {
                        sendFragmentVisibilityChange(mCurrentPosition, false);
                    }
                    if (mNextPosition >= 0) {
                        sendFragmentVisibilityChange(mNextPosition, true);
                    }
                    mCurrentPosition = mNextPosition;
                    break;
                }
                case ViewPager.SCROLL_STATE_DRAGGING:
                    clearDetails();
                    hasClearedDetails = true;
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    hasClearedDetails = false;
                    break;
                default:
                    break;
            }
        }

        private void clearDetails() {
            if (mDualPane && !hasClearedDetails) {

            }
        }
    }

    private RKLockFragment mLockFragment;
    private RKHomeFragment mHomeFragment;
    private RKAssignMenuFragment mAssignMenuFragment;
    private RKAssignFragment mAssignFragment;
    private RKAssignFuncFragment mAssignFuncFragment;
    private RKBluetoothMenuFragment mBluetoothMenuFragment;
    private RKBluetoothFragment mBluetoothFragment;
    private RKBluetoothScanFragment mBluetoothScanFragment;
    private RKBluetoothPinFragment mBluetoothPinFragment;
    private RKBluetoothPairedFragment mBluetoothPairedFragment;
    private RKMicMenuFragment mMicMenuFragment;
    private RKMicFragment mMicFragment;
    private RKVolFragment mVolFragment;
    private RKCallFragment mCallFragment;

    private Fragment getFragmentAt(int position) {
        Integer id = mTabsAdapter.getIdForPosition(position);
        if(id != null) {
            if (id == TAB_ID_HOME) {
                return mHomeFragment;
            } else if (id == TAB_ID_BLUETOOTH_MENU) {
                return mBluetoothMenuFragment;
            } else if (id == TAB_ID_BLUETOOTH) {
                return mBluetoothFragment;
            } else if (id == TAB_ID_BLUETOOTH_SCAN) {
                return mBluetoothScanFragment;
            } else if (id == TAB_ID_BLUETOOTH_PIN) {
                return mBluetoothPinFragment;
            } else if (id == TAB_ID_BLUETOOTH_PAIRED) {
                return mBluetoothPairedFragment;
            } else if (id == TAB_ID_ASSIGN_MENU) {
                return mAssignMenuFragment;
            } else if (id == TAB_ID_ASSIGN) {
                //mBtnIndex = 0;
                return mAssignFragment;
            } else if (id == TAB_ID_ASSIGN_FUNC) {
                return mAssignFuncFragment;
            } else if (id == TAB_ID_MIC_MENU) {
                return mMicMenuFragment;
            } else if (id == TAB_ID_MIC) {
                return mMicFragment;
            } else if (id == TAB_ID_VOL) {
                return mVolFragment;
            } else if (id == TAB_ID_LOCK) {
                return mLockFragment;
            } else if (id == TAB_ID_CALL) {
                return mCallFragment;
            }
        }
        throw new IllegalStateException("Unknown fragment index: " + position);
    }

    public Fragment getCurrentFragment() {
        if (mViewPager != null) {
            return getFragmentAt(mViewPager.getCurrentItem());
        }
        return null;
    }

    private void sendFragmentVisibilityChange(int position, boolean visibility) {
        try {
            final Fragment fragment = getFragmentAt(position);
            if (fragment instanceof ViewPagerVisibilityListener) {
                ((ViewPagerVisibilityListener) fragment).onVisibilityChanged(visibility);
            }
        }catch(IllegalStateException e) {
            Log.e(THIS_FILE, "Fragment not anymore managed");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(callBar != null) {
            callBar.setVideoEnabled(prefsWrapper.getPreferenceBooleanValue(SipConfigManager.USE_VIDEO));
        }

        initialize();

        //connectGroup(mCurrentGroupNo);
        //connectGroupDefault();
    }

    private void applyTheme(View v) {
        Theme t = Theme.getCurrentTheme(getActivity());
        if (t != null) {
            dialPad.applyTheme(t);

            View subV;
            // Delete button
            subV = v.findViewById(R.id.deleteButton);
            if(subV != null) {
                t.applyBackgroundDrawable(subV, "btn_dial_delete");
                t.applyLayoutMargin(subV, "btn_dial_delete_margin");
                t.applyImageDrawable((ImageView) subV, "ic_dial_action_delete");
            }

            // Dial button
            subV = v.findViewById(R.id.dialButton);
            if(subV != null) {
                t.applyBackgroundDrawable(subV, "btn_dial_action");
                t.applyLayoutMargin(subV, "btn_dial_action_margin");
                t.applyImageDrawable((ImageView) subV, "ic_dial_action_call");
            }

            // Additional button
            subV = v.findViewById(R.id.dialVideoButton);
            if(subV != null) {
                t.applyBackgroundDrawable(subV, "btn_add_action");
                t.applyLayoutMargin(subV, "btn_dial_add_margin");
            }

            // Action dividers
            subV = v.findViewById(R.id.divider1);
            if(subV != null) {
                t.applyBackgroundDrawable(subV, "btn_bar_divider");
                t.applyLayoutSize(subV, "btn_dial_divider");
            }
            subV = v.findViewById(R.id.divider2);
            if(subV != null) {
                t.applyBackgroundDrawable(subV, "btn_bar_divider");
                t.applyLayoutSize(subV, "btn_dial_divider");
            }

            // Dialpad background
            subV = v.findViewById(R.id.dialPad);
            if(subV != null) {
                t.applyBackgroundDrawable(subV, "dialpad_background");
            }

            // Callbar background
            subV = v.findViewById(R.id.dialerCallBar);
            if(subV != null) {
                t.applyBackgroundDrawable(subV, "dialer_callbar_background");
            }

            // Top field background
            subV = v.findViewById(R.id.topField);
            if(subV != null) {
                t.applyBackgroundDrawable(subV, "dialer_textfield_background");
            }

            subV = v.findViewById(R.id.digitsText);
            if(subV != null) {
                t.applyTextColor((TextView) subV, "textColorPrimary");
            }

        }

        // Fix dialer background
        if(callBar != null) {
            Theme.fixRepeatableBackground(callBar);
        }
    }

    PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        SipCallSession initialSession = getActivity().getIntent().getParcelableExtra(SipManager.EXTRA_CALL_INFO);
        synchronized (callMutex) {
            callsInfo = new SipCallSession[1];
            callsInfo[0] = initialSession;
        }
        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        // Optional, but here we bundle so just ensure we are using csipsimple package
        serviceIntent.setPackage(activity.getPackageName());
        getActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        // timings.addSplit("Bind asked for two");

        if (prefsWrapper == null) {
            prefsWrapper = new PreferencesProviderWrapper(getActivity().getApplicationContext());
            prefsWrapper.setPreferenceBooleanValue(SipConfigManager.SUPPORT_MULTIPLE_CALLS, true);
            //prefsWrapper = new PreferencesWrapper(getActivity());
        }

         //Log.d(THIS_FILE, "Creating call handler for " + callsInfo.getCallId()+" state "+callsInfo.getRemoteContact());
        powerManager = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
                "com.csipsimple.onIncomingCall");
        wakeLock.setReferenceCounted(false);

        // Listen to media & sip events to update the UI
        getActivity().registerReceiver(callStateReceiver, new IntentFilter(SipManager.ACTION_SIP_CALL_CHANGED));
        getActivity().registerReceiver(callStateReceiver, new IntentFilter(SipManager.ACTION_SIP_MEDIA_CHANGED));
        getActivity().registerReceiver(callStateReceiver, new IntentFilter(SipManager.ACTION_ZRTP_SHOW_SAS));

        if (dialFeedback == null) {
            dialFeedback = new DialingFeedback(getActivity(), false);
        }

        dialFeedback.resume();

//        connectGroup(mCurrentGroupNo);

    }

    @Override
    public void onDetach() {
        try {
            getActivity().unbindService(connection);
        } catch (Exception e) {
            // Just ignore that
            Log.w(THIS_FILE, "Unable to un bind", e);
        }
        service = null;
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        try {
            getActivity().unregisterReceiver(callStateReceiver);
        } catch (IllegalArgumentException e) {
            // That's the case if not registered (early quit)
        }
        dialFeedback.pause();
        mContext.unregisterReceiver(mBatInfoReceiver);
        super.onDetach();
    }


    private final static String TEXT_MODE_KEY = "text_mode";
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TEXT_MODE_KEY, isDigit);
        super.onSaveInstanceState(outState);
    }

    private OnEditorActionListener keyboardActionListener = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView tv, int action, KeyEvent arg2) {
            if (action == EditorInfo.IME_ACTION_GO) {
                placeCall();
                return true;
            }
            return false;
        }
    };

    OnAccountChangeListener accountButtonChangeListener = new OnAccountChangeListener() {
        @Override
        public void onChooseAccount(SipProfile account) {
            long accId = SipProfile.INVALID_ID;
            if (account != null) {
                accId = account.id;
            }
            autoCompleteAdapter.setSelectedAccount(accId);
            applyRewritingInfo();
        }
    };

    private void attachButtonListener(View v, int id, boolean longAttach) {
        ImageButton button = (ImageButton) v.findViewById(id);
        if(button == null) {
            Log.w(THIS_FILE, "Not found button " + id);
            return;
        }
        if(longAttach) {
            button.setOnLongClickListener(this);
        }else {
            button.setOnClickListener(this);
        }
    }

    private void initButtons(View v) {
        /*
        for (int buttonId : buttonsToAttach) {
            attachButtonListener(v, buttonId, false);
        }
        */
        for (int buttonId : buttonsToLongAttach) {
            attachButtonListener(v, buttonId, true);
        }

        digits.setOnClickListener(this);
        digits.setKeyListener(DialerKeyListener.getInstance());
        digits.addTextChangedListener(this);
        digits.setCursorVisible(false);
        afterTextChanged(digits.getText());
    }

    private SipProfile account = null;
    private WizardIface wizard = null;
    private String address = null;
    private String port = null;
    private String domain = null;
    private String user = null;
    private String password = null;
    private String group = null;
    private String phone_no = null;

    private String regex_IpAddress = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
    private String regex_Number = "^[0-9]*$";
    /**
     * 2021-06-02 Added
     * Checks group information existence
     */
    private boolean checkSettingGroup(int groupNo) {
        int replaceNo;

        if (groupNo == 17) {
            replaceNo = groupNo;
        } else {
            replaceNo = 4 * (mCurrentGroupPage - 1) + groupNo;
        }

        String tag_address = "Connect00SIPServerIPAddress".replace("00", String.valueOf(replaceNo));
        String tag_port = "Connect00SIPServerPort".replace("00", String.valueOf(replaceNo));
        String tag_domain = "Connect00SIPDomain".replace("00", String.valueOf(replaceNo));
        String tag_user = "Connect00User".replace("00", String.valueOf(replaceNo));
        String tag_password = "Connect00Password".replace("00", String.valueOf(replaceNo));
        String tag_group = "Connect00Name".replace("00", String.valueOf(replaceNo));
        String tag_phone_no = "Connect00PhoneNumber".replace("00", String.valueOf(replaceNo));

        if (configSip == null) {
            return false;
        }

        address = configSip.get(tag_address).trim();       // sipServerIPAddress
        port = configSip.get(tag_port).trim();             // sipServerPort
        domain = configSip.get(tag_domain).trim();         // ConnectSIPDomain
        user = configSip.get(tag_user).trim();             // userName
        password = configSip.get(tag_password).trim();     // userPassword
        group = configSip.get(tag_group).trim();           // userDisplayName >> item 1~17
        phone_no = configSip.get(tag_phone_no).trim();     // ConnectPhoneNumber

        //System.out.println("Info of Group = " + configSip.toString());
        System.out.println("Info of Group (" + replaceNo + ") = " + address + ", " + port +  ", " + domain +  ", " + user +  ", " +  password +  ", " + group + ", " + phone_no);

        if (address == null || port == null || domain == null ||
                user == null || password == null || group == null ||
                phone_no == null) {
            Toast.makeText(mContext, "There are some null values in setting of Group "+ groupNo + ". ", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (address.isEmpty()) {
            Toast.makeText(mContext, "The value of server address is empty.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (!address.matches(regex_IpAddress)) {
                Toast.makeText(mContext, "The value of server address is malformation.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (port.isEmpty()) {
            Toast.makeText(mContext, "The value of server port is empty.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (!port.matches(regex_Number)) {
                Toast.makeText(mContext, "The value of server port is not number.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (domain.isEmpty()) {
            Toast.makeText(mContext, "The value of server domain is empty.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (!domain.matches(regex_IpAddress)) {
                Toast.makeText(mContext, "The value of server domain is malformation.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (user.isEmpty()) {
            Toast.makeText(mContext, "The value of username is empty.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.isEmpty()) {
            Toast.makeText(mContext, "The value of password is empty.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (group.isEmpty()) {
            Toast.makeText(mContext, "The value of group name is empty.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (phone_no.isEmpty()) {
            Toast.makeText(mContext, "The value of phone number is empty.", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (!phone_no.matches(regex_Number)) {
                Toast.makeText(mContext, "The value of phone number is not number.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private boolean is_Locked = false;
    /**
     * 2021-06-06 Added
     * Change the state of the screen from [Lock] to [Unlock] and vice versa
     */
    private void showLocked_Or_UnLocked() {
        Integer id = mViewPager.getCurrentItem();
        switch (id) {
            case TAB_ID_LOCK:
                is_Locked = false;
                //mViewPager.setCurrentItem(TAB_ID_HOME);
                drawScreen(TAB_ID_HOME);
//                if (mHomeFragment != null) {
//                    mHomeFragment.setTextPage(mCurrentGroupPage);
//                }
                break;
            case TAB_ID_HOME:
                is_Locked = true;
                //mViewPager.setCurrentItem(TAB_ID_LOCK);
                drawScreen(TAB_ID_LOCK);
//                if (mLockFragment != null) {
//                    mLockFragment.setTextPage(mCurrentGroupPage);
//                }
                break;
            default:
                is_Locked = false;
                break;
        }
        saveSettingKeyLock(is_Locked);
    }

    long currentAccountId = -1;
    private class AsyncTaskCheckWaitForConnectGroup extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            // Wait for the initializations to complete
            while (currentAccountId < 0) {
                // System.out.println("Account ID = " + currentAccountId);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (isConnecting(mCurrentGroupNo)) return;
            System.out.println("INITIATE CONNECTION to the default group (" + mCurrentGroupNo + ")");
            // Start current group connection
            connectGroup(mCurrentGroupNo);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

    private boolean is_G1_Connecting = false;
    private boolean is_G2_Connecting = false;
    private boolean is_G3_Connecting = false;
    private boolean is_G4_Connecting = false;
    private boolean is_PIC_Connecting = false;

    private boolean isConnecting(int groupNo) {
        switch (groupNo) {
            case 1:
                return is_G1_Connecting;
            case 2:
                return is_G2_Connecting;
            case 3:
                return is_G3_Connecting;
            case 4:
                return is_G4_Connecting;
            case 17: // Connecting PIC
                return is_PIC_Connecting;
            default:
                break;
        }

        return false;
    }

    private void setStatusOfGroupConnection(int groupNo) {
        switch (groupNo) {
            case 1:
                //digits.setText(String.valueOf(GROUP_KEYCODE_1));
                is_G1_Connecting = true;
                is_G2_Connecting = false;
                is_G3_Connecting = false;
                is_G4_Connecting = false;
                is_PIC_Connecting = false;
                break;
            case 2:
                //digits.setText(String.valueOf(GROUP_KEYCODE_2));
                is_G1_Connecting = false;
                is_G2_Connecting = true;
                is_G3_Connecting = false;
                is_G4_Connecting = false;
                is_PIC_Connecting = false;
                break;
            case 3:
                //digits.setText(String.valueOf(GROUP_KEYCODE_3));
                is_G1_Connecting = false;
                is_G2_Connecting = false;
                is_G3_Connecting = true;
                is_G4_Connecting = false;
                is_PIC_Connecting = false;
                break;
            case 4:
                //digits.setText(String.valueOf(GROUP_KEYCODE_4));
                is_G1_Connecting = false;
                is_G2_Connecting = false;
                is_G3_Connecting = false;
                is_G4_Connecting = true;
                is_PIC_Connecting = false;
                break;
            case 17:
                is_G1_Connecting = false;
                is_G2_Connecting = false;
                is_G3_Connecting = false;
                is_G4_Connecting = false;
                is_PIC_Connecting = true;
                break;
            default:
                is_G1_Connecting = false;
                is_G2_Connecting = false;
                is_G3_Connecting = false;
                is_G4_Connecting = false;
                is_PIC_Connecting = false;
                break;
        }
    }

    public void connectGroupDefault() {
        (new AsyncTaskCheckWaitForConnectGroup()).execute();
    }

    /**
     * 2021-06-08 Added
     * Group connection
     */
    public void connectGroup(int groupNo) {
        // 2021-06-02 Added
        // Check G1 information existence
        if (!checkSettingGroup(groupNo)) {
            // If there no setting for ConnectGroup in the xml file, the screen
            // will not transition after that number
            return;
        } else {
            mCurrentGroupNo = groupNo;
            groupTxt.setText(group);
            userTxt.setText(user);

            //update LCD
            drawDeviceNo();
            drawGroup();
        }
//        //groupTxt.setText("G1");
//            if (digits.getText().toString().equals(preCallee)) {
//                endCall();
//                preCallee = "";
//                mViewPager.setCurrentItem(1);
//                drawScreen(1);
//            } else {
//                placeCall();
//                mViewPager.setCurrentItem(13);
//                drawScreen(13);
//            }
        if (isConnecting(groupNo) == false) {
            setStatusOfGroupConnection(groupNo);
            digits.setText(phone_no);
            preCallee = "";

            // Save current group connection to xml file
            saveSettingConnectGroup();

            // Register
            placeCall();

            // Invite
            //invite();

        } else {
            Toast.makeText(mContext, "You are connecting to " + group, Toast.LENGTH_SHORT).show();
        }

    }

    public void keyLongPressed(KeyEvent inputKeyEvent) {
        int keyCode = inputKeyEvent.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_7) {
            showLocked_Or_UnLocked();
        }
    }

    //add function to process with keyflags, current for LONG_PRESS unlock
    int previousKeyCode = -1;
    public void keyPressed(KeyEvent inputKeyEvent) {
        int keyCode = inputKeyEvent.getKeyCode();

        if (keyCode == KeyEvent.KEYCODE_DEL) {
            keyCode = previousKeyCode;
        } else {
            previousKeyCode = keyCode;
        }

        Toast.makeText(mContext, "Keycode:" + keyCode, Toast.LENGTH_SHORT).show();
        keyPressed(keyCode);
    }

    public void keyPressed(int keyCode) {
        Integer id = mViewPager.getCurrentItem();

        calculateSignalLevel();

        if (keyCode == KeyEvent.KEYCODE_3) {    //10: Group 1
            if (is_Locked) return;
            connectGroup(1);
        }
        else if (keyCode == KeyEvent.KEYCODE_4) {    //11: Group 2
            if (is_Locked) return;
            connectGroup(2);
        }
        else if (keyCode == KeyEvent.KEYCODE_1) {    //8: Group 3
            if (is_Locked) return;
            connectGroup(3);
        }
        else if (keyCode == KeyEvent.KEYCODE_2) {    //9: Group 4
            if (is_Locked) return;
            connectGroup(4);
        }
        else if (keyCode == KeyEvent.KEYCODE_9) {    //16: UP button
            if (is_Locked) return;
//            if (isCalling()) {
//                return;
//            }
            switch (id) {
                case TAB_ID_BLUETOOTH_MENU:
                    //mViewPager.setCurrentItem(TAB_ID_ASSIGN_MENU);
                    //drawScreen(2);
                    break;
                case TAB_ID_BLUETOOTH:
                    break;
                case TAB_ID_BLUETOOTH_SCAN:
                    break;
                case TAB_ID_BLUETOOTH_PIN:
                    break;
                case TAB_ID_BLUETOOTH_PAIRED:
                    break;
                case TAB_ID_ASSIGN_MENU:
                    if (mAssignMenuFragment != null)
                        mAssignMenuFragment.keyUp();
                    break;
                case TAB_ID_ASSIGN:
                    if (mAssignFragment != null)
                        mBtnIndex = mAssignFragment.keyUp();
                    Toast.makeText(mContext, "mBtnIndex = " + mBtnIndex, Toast.LENGTH_SHORT).show();
                    break;
                case TAB_ID_ASSIGN_FUNC:
                    if (mAssignFuncFragment != null)
                        mFuncIndex = mAssignFuncFragment.keyUp();
                    break;
                case TAB_ID_MIC_MENU:
                    mViewPager.setCurrentItem(TAB_ID_BLUETOOTH_MENU);
                    break;
                case TAB_ID_MIC:
                    if (mMicFragment != null) {
                        mMicFragment.keyUp();
                    }
                    break;
                case TAB_ID_VOL:
                    int newValue = mVolFragment.keyUp();
                    volLevel = newValue;
                    saveSettingSpeakerVolume(volLevel);
                    volTxt.setText(String.valueOf(newValue));
                    //onSoundChanged(true, newValue);   //khanh: TODO update volume change to SIP service
                    break;
                case TAB_ID_LOCK:
                case TAB_ID_HOME:
//                    mViewPager.setCurrentItem(TAB_ID_VOL);
//                    if (mVolFragment == null) { //update fragment
//                        mVolFragment = (RKVolFragment) mTabsAdapter.getItem(TAB_ID_VOL);
//                    }
//                    drawScreen(TAB_ID_VOL);
                    assignFunc(RKAssignFragment.BUTTON_NUM_UP);
                    break;
                default:
                    //Nothing to do
                    break;
            }
        } else if (keyCode == KeyEvent.KEYCODE_5) { //12: DOWN button
            if (is_Locked) return;
//            if (isCalling()) {
//                return;
//            }
            switch (id) {
                case TAB_ID_BLUETOOTH_MENU:
                    //mViewPager.setCurrentItem(TAB_ID_MIC_MENU);
                    //drawScreen(10);
                    break;
                case TAB_ID_BLUETOOTH:
                    break;
                case TAB_ID_BLUETOOTH_SCAN:
                    break;
                case TAB_ID_BLUETOOTH_PIN:
                    break;
                case TAB_ID_BLUETOOTH_PAIRED:
                    break;
                case TAB_ID_ASSIGN_MENU:
                    if (mAssignMenuFragment != null)
                        mAssignMenuFragment.keyDown();
                    break;
                case TAB_ID_ASSIGN:
                    if (mAssignFragment != null)
                        mBtnIndex = mAssignFragment.keyDown();
                    Toast.makeText(mContext, "mBtnIndex = " + mBtnIndex, Toast.LENGTH_SHORT).show();
                    break;
                case TAB_ID_ASSIGN_FUNC:
                    if (mAssignFuncFragment != null)
                        mFuncIndex = mAssignFuncFragment.keyDown();
                    break;
                case TAB_ID_MIC_MENU:
                    break;
                case TAB_ID_MIC:
                    if (mMicFragment != null)
                        mMicFragment.keyDown();
                    break;
                case TAB_ID_VOL:
                    if (mVolFragment != null) {
                        int newValue = mVolFragment.keyDown();
                        volLevel = newValue;
                        volTxt.setText(String.valueOf(newValue));
                        saveSettingSpeakerVolume(volLevel);
                        //onSoundChanged(true, newValue);   //khanh: TODO update volume change to SIP service
                    }
                    break;
                case TAB_ID_LOCK:
                case TAB_ID_HOME:
                    assignFunc(RKAssignFragment.BUTTON_NUM_DOWN);
                    break;
                default:
                    //Nothing to do
                    break;
            }
        } else if (keyCode == KeyEvent.KEYCODE_6) { //13: LEFT button
            if (is_Locked) return;
            switch (id) {
                case TAB_ID_BLUETOOTH:
                case TAB_ID_BLUETOOTH_PAIRED:
                case TAB_ID_BLUETOOTH_SCAN:
                case TAB_ID_BLUETOOTH_PIN:
                case TAB_ID_ASSIGN:
                case TAB_ID_ASSIGN_FUNC:
                case TAB_ID_MIC:
                    mViewPager.setCurrentItem(TAB_ID_ASSIGN_MENU);
                    drawScreen(TAB_ID_ASSIGN_MENU);
                    break;

                case TAB_ID_LOCK:
                case TAB_ID_HOME:
                    assignFunc(RKAssignFragment.BUTTON_NUM_LEFT);
                    break;

                case TAB_ID_VOL:
                case TAB_ID_ASSIGN_MENU:
                    mViewPager.setCurrentItem(TAB_ID_HOME);
                    if (mHomeFragment != null) {
                        mHomeFragment.drawScreen(mAssignFunc);
                    }
                    drawScreen(TAB_ID_HOME);
                    break;

                default:
                    //Nothing to do
                    break;
            }
        } else if (keyCode == KeyEvent.KEYCODE_8) { //15: RIGHT button
            switch (id) {
                case TAB_ID_HOME:
                    mViewPager.setCurrentItem(TAB_ID_ASSIGN_MENU);
                    if (mAssignMenuFragment == null) { //update fragment
                        mAssignMenuFragment = (RKAssignMenuFragment) mTabsAdapter.getItem(TAB_ID_ASSIGN_MENU);
                    }
                    drawScreen(TAB_ID_ASSIGN_MENU);
                    break;
                default:
                    //Nothing to do
                    break;
            }
        } else if (keyCode == KeyEvent.KEYCODE_7) { //14: ENTER button
            switch (id) {
                case TAB_ID_LOCK: //-> move out of normal key press for lock/unlock screen
                case TAB_ID_HOME:
                    break;
                case TAB_ID_BLUETOOTH_MENU:
                    mViewPager.setCurrentItem(TAB_ID_BLUETOOTH);
                    drawScreen(6);
                    break;
                case TAB_ID_BLUETOOTH:
                    mViewPager.setCurrentItem(TAB_ID_BLUETOOTH_SCAN);
                    drawScreen(7);
                    break;
                case TAB_ID_BLUETOOTH_SCAN:
                    mViewPager.setCurrentItem(TAB_ID_BLUETOOTH_PIN);
                    drawScreen(8);
                    break;
                case TAB_ID_BLUETOOTH_PIN:
                    mViewPager.setCurrentItem(TAB_ID_BLUETOOTH_PAIRED);
                    drawScreen(9);
                    break;
                case TAB_ID_BLUETOOTH_PAIRED:
                    break;
                case TAB_ID_ASSIGN_MENU:
                    if (mAssignMenuFragment != null) {
                        int currentMenu = mAssignMenuFragment.getCurrentMenu();
                        if (currentMenu == RKAssignMenuFragment.MENU_ASSIGN) {
                            mViewPager.setCurrentItem(TAB_ID_ASSIGN);
                            if (mAssignFragment == null) {
                                mAssignFragment = (RKAssignFragment) mTabsAdapter.getItem(TAB_ID_ASSIGN);
                            }
                            drawScreen(TAB_ID_ASSIGN);
                        } else if (currentMenu == RKAssignMenuFragment.MENU_BLUETOOTH) {
                            mViewPager.setCurrentItem(TAB_ID_BLUETOOTH);
                            if (mBluetoothFragment == null) {
                                mBluetoothFragment = (RKBluetoothFragment) mTabsAdapter.getItem(TAB_ID_BLUETOOTH);
                            }
                            drawScreen(TAB_ID_BLUETOOTH);
                        } else if (currentMenu == RKAssignMenuFragment.MENU_MIC) {
                            mViewPager.setCurrentItem(TAB_ID_MIC);
                            if (mMicFragment == null) {
                                mMicFragment = (RKMicFragment) mTabsAdapter.getItem(TAB_ID_MIC);
                            }
                            mMicFragment.setCurrentMicLevel(service, currentMic);
                            drawScreen(TAB_ID_MIC);
                        } else {
                            /* nothing: should not go here*/
                        }
                    }

                    break;
                case TAB_ID_ASSIGN:
                    mViewPager.setCurrentItem(TAB_ID_ASSIGN_FUNC);
                    if (mAssignFuncFragment == null) { //update fragment
                        mAssignFuncFragment = (RKAssignFuncFragment) mTabsAdapter.getItem(TAB_ID_ASSIGN_FUNC);
                    }

                    mAssignFuncFragment.drawScreen(mAssignFunc.get(mBtnIndex));
                    //drawScreen(TAB_ID_ASSIGN_FUNC);
                    break;
                case TAB_ID_ASSIGN_FUNC:
                    if (mAssignFuncFragment != null) {
                        mFuncIndex = mAssignFuncFragment.keyEnter();
                        //mAssignFunc.put(mBtnIndex, mFuncIndex);
                        saveSettingAssignFunc(mBtnIndex, mFuncIndex);
                    }
                    System.out.println(mBtnIndex +"--->"+mFuncIndex);
                    System.out.println(mAssignFunc);
                    break;
                case TAB_ID_MIC_MENU:
                    mViewPager.setCurrentItem(TAB_ID_MIC);
                    drawScreen(TAB_ID_MIC);
                    break;
                case TAB_ID_MIC:
                    break;
                case TAB_ID_VOL:
                    break;
                default:
                    //Nothing to do
                    break;
            }
        } else if (keyCode == KeyEvent.KEYCODE_A) { //29: F1 button
            if (is_Locked) return;
            assignFunc(RKAssignFragment.BUTTON_NUM_F1);
        } else if (keyCode == KeyEvent.KEYCODE_B) { //30: F2 button
            if (is_Locked) return;
            assignFunc(RKAssignFragment.BUTTON_NUM_F2);
        } else if (keyCode == KeyEvent.KEYCODE_C) { //31: F3 button
            if (is_Locked) return;
            assignFunc(RKAssignFragment.BUTTON_NUM_F3);
        } else if (keyCode == KeyEvent.KEYCODE_D) { //32: PT1 button
            assignFunc(RKAssignFragment.BUTTON_NUM_PT1);
        } else if (keyCode == KeyEvent.KEYCODE_E) { //33: PT2 button
            assignFunc(RKAssignFragment.BUTTON_NUM_PT2);
        } else if (keyCode == KeyEvent.KEYCODE_F) { //34: PT3 button
            assignFunc(RKAssignFragment.BUTTON_NUM_PT3);
        }
        else {
            //Toast.makeText(mContext, "Keycode: " + keyCode, Toast.LENGTH_SHORT).show();
            //KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
            //digits.onKeyDown(keyCode, event);
        }
    }

    public void setDefaultAssignFunc() {
        if (configSetting != null) {
            //Toast.makeText(mContext, "setDefaultAssignFunc", Toast.LENGTH_SHORT).show();
            //System.out.println("setDefaultAssignFunc != NULL");
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_UP, Integer.parseInt(configSetting.get("Button_UP")));      // Key 9
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_DOWN, Integer.parseInt(configSetting.get("Button_DW")));    // Key 5
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_LEFT, Integer.parseInt(configSetting.get("Button_LFT")));   // Key 6
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_F1, Integer.parseInt(configSetting.get("Button_F1")));      // Key A
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_F2, Integer.parseInt(configSetting.get("Button_F2")));      // Key B
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_F3, Integer.parseInt(configSetting.get("Button_F3")));      // Key C
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_PT1, Integer.parseInt(configSetting.get("Button_PT1")));    // Key D
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_PT2, Integer.parseInt(configSetting.get("Button_PT2")));    // Key E
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_PT3, Integer.parseInt(configSetting.get("Button_PT3")));    // Key F
        } else {
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_UP, RKAssignFuncFragment.ASSIGN_NUM_VOL);       // Key 9
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_DOWN, RKAssignFuncFragment.ASSIGN_NUM_NONE);    // Key 5
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_LEFT, RKAssignFuncFragment.ASSIGN_NUM_NONE);    // Key 6
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_F1, RKAssignFuncFragment.ASSIGN_NUM_TALK);      // Key A
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_F2, RKAssignFuncFragment.ASSIGN_NUM_NONE);      // Key B
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_F3, RKAssignFuncFragment.ASSIGN_NUM_NONE);      // Key C
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_PT1, RKAssignFuncFragment.ASSIGN_NUM_PIC);      // Key D
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_PT2, RKAssignFuncFragment.ASSIGN_NUM_TALK);     // Key E
            mAssignFunc.put(RKAssignFragment.BUTTON_NUM_PT3, RKAssignFuncFragment.ASSIGN_NUM_VOL);      // Key F
        }
    }

    /**
     * 2021-06-10 Added
     * Save info of AssignFunc to xml file
     * @param butNumber
     * @param funcNumber
     */
    private void saveSettingAssignFunc(int butNumber, int funcNumber) {
        mAssignFunc.put(butNumber, funcNumber);
        if (configSetting == null) return;
        switch (butNumber) {
            case RKAssignFragment.BUTTON_NUM_UP:
                configSetting.put("Button_UP", String.valueOf(funcNumber));
                configIpPhone.updateSetting("Button_UP", String.valueOf(funcNumber), SipHome.IP_PHONE_BACKUP_XML);
                break;
            case RKAssignFragment.BUTTON_NUM_DOWN:
                configSetting.put("Button_DW", String.valueOf(funcNumber));
                configIpPhone.updateSetting("Button_DW", String.valueOf(funcNumber), SipHome.IP_PHONE_BACKUP_XML);
                break;
            case RKAssignFragment.BUTTON_NUM_LEFT:
                configSetting.put("Button_LFT", String.valueOf(funcNumber));
                configIpPhone.updateSetting("Button_LFT", String.valueOf(funcNumber), SipHome.IP_PHONE_BACKUP_XML);
                break;
            case RKAssignFragment.BUTTON_NUM_F1:
                configSetting.put("Button_F1", String.valueOf(funcNumber));
                configIpPhone.updateSetting("Button_F1", String.valueOf(funcNumber), SipHome.IP_PHONE_BACKUP_XML);
                break;
            case RKAssignFragment.BUTTON_NUM_F2:
                configSetting.put("Button_F2", String.valueOf(funcNumber));
                configIpPhone.updateSetting("Button_F2", String.valueOf(funcNumber), SipHome.IP_PHONE_BACKUP_XML);
                break;
            case RKAssignFragment.BUTTON_NUM_F3:
                configSetting.put("Button_F3", String.valueOf(funcNumber));
                configIpPhone.updateSetting("Button_F3", String.valueOf(funcNumber), SipHome.IP_PHONE_BACKUP_XML);
                break;
            case RKAssignFragment.BUTTON_NUM_PT1:
                configSetting.put("Button_PT1", String.valueOf(funcNumber));
                configIpPhone.updateSetting("Button_PT1", String.valueOf(funcNumber), SipHome.IP_PHONE_BACKUP_XML);
                break;
            case RKAssignFragment.BUTTON_NUM_PT2:
                configSetting.put("Button_PT2", String.valueOf(funcNumber));
                configIpPhone.updateSetting("Button_PT2", String.valueOf(funcNumber), SipHome.IP_PHONE_BACKUP_XML);
                break;
            case RKAssignFragment.BUTTON_NUM_PT3:
                configSetting.put("Button_PT3", String.valueOf(funcNumber));
                configIpPhone.updateSetting("Button_PT3", String.valueOf(funcNumber), SipHome.IP_PHONE_BACKUP_XML);
                break;
            default:
                break;
        }
    }

    /**
     * 2021-06-10 Added
     * Save the status of key lock mode to xml file
     * @param isLocked
     */
    private void saveSettingKeyLock(boolean isLocked) {
        if (configSetting != null) {
            if (isLocked) {
                configSetting.put("KeyLockMode", "1");
                configIpPhone.updateSetting("KeyLockMode", "1", SipHome.IP_PHONE_BACKUP_XML);
            } else {
                configSetting.put("KeyLockMode", "0");
                configIpPhone.updateSetting("KeyLockMode", "0", SipHome.IP_PHONE_BACKUP_XML);
            }
        }
    }

    /**
     * 2021-06-10 Added
     * Save the current status of speaker volume to xml file
     * @param level
     */
    private void saveSettingSpeakerVolume(int level) {
        if (configSetting != null) {
            configSetting.put("SpeakerVolumeCurrent", String.valueOf(level));
            configIpPhone.updateSetting("SpeakerVolumeCurrent", String.valueOf(level), SipHome.IP_PHONE_BACKUP_XML);
        }
    }

    /**
     * 2021-06-10 Added
     * Save the current group connection to xml file
     */
    private void saveSettingConnectGroup() {
        if (configSetting != null) {
            int currentGroup = ((mCurrentGroupPage - 1) * 4) + mCurrentGroupNo;
            configSetting.put("ConnectGroupCurrent", String.valueOf(currentGroup));
            configIpPhone.updateSetting("ConnectGroupCurrent", String.valueOf(currentGroup), SipHome.IP_PHONE_BACKUP_XML);
        }
    }

    /**
     * Save the wifi mac address to xml file
     * @param macAddress
     */
    private void saveSettingMacAddress(String macAddress) {
        if (configSetting != null) {
            configSetting.put("WifiMacAddress", macAddress);
            configIpPhone.updateSetting("WifiMacAddress", macAddress, SipHome.IP_PHONE_BACKUP_XML);
        }
    }

    private void assignFunc(int btnIndex) {
        int funcIndex = mAssignFunc.get(btnIndex);
        switch (funcIndex) {
            case RKAssignFuncFragment.ASSIGN_NUM_TALK:
                funcTALK();
                break;
            case RKAssignFuncFragment.ASSIGN_NUM_VOL:
                funcVOL();
                break;
//            case RKAssignFuncFragment.ASSIGN_NUM_AWTK:
//                //funcAWTK();
//                break;
            case RKAssignFuncFragment.ASSIGN_NUM_GPAG:
                funcGPAG();
                break;
            case RKAssignFuncFragment.ASSIGN_NUM_PIC:
                funcPIC();
                break;
            case RKAssignFuncFragment.ASSIGN_NUM_NONE:
                //Toast.makeText(getActivity(),"ASSIGN_NUM_NONE!",Toast.LENGTH_SHORT).show();
                break;
            default:
                //Nothing to do
        }
    }

    private int mCurrentGroupPage = 1;
    /**
     * 2021-06-07 Added
     * Group selection page switch (GPAG)
     */
    private void funcGPAG() {
        if (configOther == null) {
            return;
        }

        String tag_group_selection = "ConnectGroup00";
        int increaseGroupPage = mCurrentGroupPage + 1;
        String pageGroupSelection = null;

        if (mCurrentGroupPage < 4) {
            mCurrentGroupPage ++;
        } else {
            mCurrentGroupPage = 1;
        }

        tag_group_selection = tag_group_selection.replace("00", String.valueOf(mCurrentGroupPage));
        pageGroupSelection = configOther.get(tag_group_selection);
        if (pageGroupSelection == null ||
                pageGroupSelection.isEmpty() == true ||
                    !checkSettingGroup(1)) // Check Group information existence
        {
            mCurrentGroupPage = 1;
        }
        Toast.makeText(getActivity(),"ASSIGN_NUM_GPAG! " + mCurrentGroupPage,Toast.LENGTH_SHORT).show();
//        if (checkSettingGroup(1)) {
//            mCurrentGroupNo = 1;
//            groupTxt.setText(group);
//            userTxt.setText(user);
//        }
        // Reset status of group connection
        setStatusOfGroupConnection(0);
        connectGroup(1);
        if (mHomeFragment != null) {
            mHomeFragment.setTextPage(mCurrentGroupPage);
        }
        if (mLockFragment != null) {
            mLockFragment.setTextPage(mCurrentGroupPage);
        }
    }

    private int mCurrentGroupNo = 0;
    private boolean funcPIC() {
        Toast.makeText(getActivity(),"ASSIGN_NUM_PIC!",Toast.LENGTH_SHORT).show();
        // Check PIC information existence
        if (!checkSettingGroup(17)) {
            if (!muteflag) {
                mute();
                muteflag = true;
            }
            // Connect to the memorized group
            connectGroup(mCurrentGroupNo);
            return false;
        } else {
            digits.setText(String.valueOf(GROUP_KEYCODE_1));
            if (digits.getText().toString().equals(preCallee)) {
                // Disconnect to group 17
                endCall();
                preCallee = "";
                if (!muteflag) {
                    mute();
                    muteflag = true;
                }
                // Connect to the memorized group
                connectGroup(mCurrentGroupNo);
            } else {
                // Connect to group 17
                if (muteflag) {
                    muteflag = false;
                    unmute();
                }
                groupTxt.setText(group);
                userTxt.setText(user);
                setStatusOfGroupConnection(17);
                placeCall();
                invite();
            }
        }
        return true;
    }

    private boolean muteflag = true;
    private void funcTALK() {
        if (muteflag) {
            unmute();
            muteflag = false;
            // Check out of wifi range
            updateWifiStatus();
        } else {
            mute();
            muteflag = true;
        }
        //placeCall();
        Toast.makeText(getActivity(),"ASSIGN_NUM_TALK! = " + (muteflag?"MUTE":"UNMUTE"),Toast.LENGTH_SHORT).show();
    }

    private void funcAWTK() {
        Toast.makeText(getActivity(),"ASSIGN_NUM_AWTK!",Toast.LENGTH_SHORT).show();
        updateWifiStatus();
        if (wifiLevel > 0) {
            unmute();
            placeCall();
        }
    }

    private void funcVOL() {
        Toast.makeText(getActivity(),"ASSIGN_NUM_VOL!",Toast.LENGTH_SHORT).show();
        mViewPager.setCurrentItem(TAB_ID_VOL);
        if (mVolFragment == null) { //update fragment
            mVolFragment = (RKVolFragment) mTabsAdapter.getItem(TAB_ID_VOL);
        }
        drawScreen(TAB_ID_VOL);
    }

    private ISipService sipService;
    private ServiceConnection sipConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            Log.d(THIS_FILE, "SipService is connected");
            sipService = ISipService.Stub.asInterface(arg1);

            try {
                SipCallSession[] info = sipService.getCalls();
                System.out.println("SipCallSession: " + info.length);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            updateUIFromMedia();
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    /* get micro level, range from 0 to 100 */
//    private int sipGetMicLevel() {
//        int micLevel = 0;
//        Float micLevelFloat;
//        boolean useBT = false;
//        if (sipService != null) {
//            try {
//                useBT = sipService.getCurrentMediaState().isBluetoothScoOn;
//            } catch (RemoteException e) {
//                Log.e(THIS_FILE, "Sip service not avail for request ", e);
//            }
//        }else {
//            Log.d(THIS_FILE, "sipSetMicLevel sipService==null");
//            //return 0;
//        }
//
//        if (mContext != null) {
//            micLevelFloat = SipConfigManager.getPreferenceFloatValue(mContext, useBT ?
//                    SipConfigManager.SND_BT_MIC_LEVEL : SipConfigManager.SND_MIC_LEVEL);
//            micLevelFloat = micLevel * 100.0f;
//            micLevel = micLevelFloat.intValue();
//        } else {
//            Log.d(THIS_FILE, "sipGetMicroLevel mContext == null");
//        }
//
//        return micLevel;
//    }
    /* get micro level, range from 0 to 100, return 0 if success */
//    private int sipSetMicLevel(int micLevel) {
//        int result = -1;
//        Float micLevelFloat = (float) micLevel/100.0f;
//        boolean useBT = false;
//
//        if (sipService != null) {
//            try {
//                useBT = sipService.getCurrentMediaState().isBluetoothScoOn;
//            } catch (RemoteException e) {
//                Log.e(THIS_FILE, "Sip service not avail for request ", e);
//            }
//        } else {
//            Log.d(THIS_FILE, "sipSetMicLevel sipService==null");
//            //return 0;
//        }
//
//        try {
//            String key;
//            if (sipService != null) {
//                sipService.confAdjustRxLevel(0, micLevelFloat);
//            }
//            if (mContext != null) {
//                key =  useBT ? SipConfigManager.SND_BT_MIC_LEVEL : SipConfigManager.SND_MIC_LEVEL;
//                SipConfigManager.setPreferenceFloatValue(mContext, key, micLevelFloat);
//                result = 0;
//            } else {
//                Log.d(THIS_FILE, "sipGetMicroLevel mContext == null");
//            }
//        } catch (RemoteException e) {
//            Log.e(THIS_FILE, "Impossible to set mic/ level", e);
//        }
//
//        return result;
//    }

    private void updateUIFromMedia() {
        System.out.println("UPDATE MEDIA");
        useBT = false;
        if (service != null) {
            try {
                useBT = service.getCurrentMediaState().isBluetoothScoOn;
            } catch (RemoteException e) {
                Log.e(THIS_FILE, "Sip service not avail for request ", e);
            }
        }

        Float speakerLevel = SipConfigManager.getPreferenceFloatValue(
                getActivity().getApplicationContext(),
                useBT ? SipConfigManager.SND_BT_SPEAKER_LEVEL : SipConfigManager.SND_SPEAKER_LEVEL);

        System.out.println("UPDATE MEDIA (speaker) = " + currentVolume);
        if (currentVolume != speakerLevel.intValue()) {
            speakerLevel = Float.valueOf(currentVolume);
            SipConfigManager.setPreferenceFloatValue(
                    getActivity().getApplicationContext(),
                    useBT ? SipConfigManager.SND_BT_SPEAKER_LEVEL : SipConfigManager.SND_SPEAKER_LEVEL,
                    speakerLevel);
        }

//        if (mVolFragment != null) {
//            mVolFragment.mSpeaker.setText(String.valueOf(speakerLevel));
//        }
//        speakerAmplification.setProgress(valueToProgressUnit(speakerLevel));



//        Float microLevel = SipConfigManager.getPreferenceFloatValue(
//                getActivity().getApplicationContext(),
//                useBT ? SipConfigManager.SND_BT_MIC_LEVEL : SipConfigManager.SND_MIC_LEVEL);

        System.out.println("UPDATE MEDIA (microphone) = " + currentMic);
        Float microLevel = (float)currentMic/100.0F;//Float.valueOf(currentMic);
        if (service != null) {
            try {
                service.confAdjustRxLevel(0, microLevel);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (mContext != null) {
            SipConfigManager.setPreferenceFloatValue(
                    getActivity().getApplicationContext(),
                    useBT ? SipConfigManager.SND_BT_MIC_LEVEL : SipConfigManager.SND_MIC_LEVEL,
                    microLevel);
        }

//        microAmplification.setProgress(valueToProgressUnit(microLevel));
    }

    private double subdivision = 5;
    private double max = 15;

    private int valueToProgressUnit(float val) {
        Log.d(THIS_FILE, "Value is " + val);
        double dB = (10.0f * Math.log10(val));
        return (int) ( (dB + max) * subdivision);
    }
    private float progressUnitToValue(int pVal) {
        Log.d(THIS_FILE, "Progress is " + pVal);
        double dB = pVal / subdivision - max;
        return (float) Math.pow(10, dB / 10.0f);
    }

    public void onSoundChanged(boolean speaker_flag, int value) {
        Log.d(THIS_FILE, "Progress has changed");
        if(service != null) {
            try {
                float newValue = progressUnitToValue( value );
                String key;
                boolean useBT = service.getCurrentMediaState().isBluetoothScoOn;
                if (speaker_flag) {
                    service.confAdjustTxLevel(0, newValue);
                    key =  useBT ? SipConfigManager.SND_BT_SPEAKER_LEVEL : SipConfigManager.SND_SPEAKER_LEVEL;
                    SipConfigManager.setPreferenceFloatValue(getActivity().getApplicationContext(), key, newValue);
                } else {
                    service.confAdjustRxLevel(0, newValue);
                    key =  useBT ? SipConfigManager.SND_BT_MIC_LEVEL : SipConfigManager.SND_MIC_LEVEL;
                    SipConfigManager.setPreferenceFloatValue(getActivity().getApplicationContext(), key, newValue);
                }
            } catch (RemoteException e) {
                Log.e(THIS_FILE, "Impossible to set mic/speaker level", e);
            }

        }else {
            //TODO : revert changes here !
        }
    }

    private class OnAutoCompleteListItemClicked implements OnItemClickListener {
        private ContactsSearchAdapter searchAdapter;

        /**
         * Instanciate with a ContactsSearchAdapter adapter to search in when a
         * contact entry is clicked
         *
         * @param adapter the adapter to use
         */
        public OnAutoCompleteListItemClicked(ContactsSearchAdapter adapter) {
            searchAdapter = adapter;
        }

        @Override
        public void onItemClick(AdapterView<?> list, View v, int position, long id) {
            Object selectedItem = searchAdapter.getItem(position);
            if (selectedItem != null) {
                CharSequence newValue = searchAdapter.getFilter().convertResultToString(
                        selectedItem);
                //setTextFieldValue(newValue);
            }
        }

    }

    public void onClick(View view) {
        // ImageButton b = null;
        int viewId = view.getId();
        /*
        if (view_id == R.id.switchTextView) {
            // Set as text dialing if we are currently digit dialing
            setTextDialing(isDigit);
        } else */
        if (viewId == digits.getId()) {
            if (digits.length() != 0) {
                digits.setCursorVisible(true);
            }
        }
    }

    public boolean onLongClick(View view) {
        // ImageButton b = (ImageButton)view;
        int vId = view.getId();
        if (vId == R.id.button0) {
            dialFeedback.hapticFeedback();
            keyPressed(KeyEvent.KEYCODE_PLUS);
            return true;
        }else if(vId == R.id.button1) {
            if(digits.length() == 0) {
                placeVMCall();
                return true;
            }
        }
        return false;
    }

    public void afterTextChanged(Editable input) {
        // Change state of digit dialer
        final boolean notEmpty = digits.length() != 0;
        //digitsWrapper.setBackgroundDrawable(notEmpty ? digitsBackground : digitsEmptyBackground);
        callBar.setEnabled(notEmpty);

        if (!notEmpty && isDigit) {
            digits.setCursorVisible(false);
        }
        applyTextToAutoComplete();
    }

    private void applyTextToAutoComplete() {

        // If single pane for smartphone use autocomplete list
        if (hasAutocompleteList()) {
            String filter = digits.getText().toString();
            autoCompleteAdapter.setSelectedText(filter);
            //else {
            //    autoCompleteAdapter.swapCursor(null);
            //}
        }
        // Dual pane : always use autocomplete list
        if (mDualPane && autoCompleteFragment != null) {
            autoCompleteFragment.filter(digits.getText().toString());
        }
    }

    /**
     * Set the mode of the text/digit input.
     *
     * @param textMode True if text mode. False if digit mode
     */
    public void setTextDialing(boolean textMode) {
        Log.d(THIS_FILE, "Switch to mode " + textMode);
        setTextDialing(textMode, false);
    }


    /**
     * Set the mode of the text/digit input.
     *
     * @param textMode True if text mode. False if digit mode
     */
    public void setTextDialing(boolean textMode, boolean forceRefresh) {
        if(!forceRefresh && (isDigit != null && isDigit == !textMode)) {
            // Nothing to do
            return;
        }
        isDigit = !textMode;
        if(digits == null) {
            return;
        }
        if(isDigit) {
            // We need to clear the field because the formatter will now 
            // apply and unapply to this field which could lead to wrong values when unapplied
            digits.getText().clear();
            digits.addTextChangedListener(digitFormater);
        }else {
            digits.removeTextChangedListener(digitFormater);
        }
        digits.setCursorVisible(!isDigit);
        digits.setIsDigit(isDigit, true);

        // Update views visibility
        dialPad.setVisibility(isDigit ? View.VISIBLE : View.GONE);
        autoCompleteList.setVisibility(hasAutocompleteList() ? View.VISIBLE : View.GONE);
        //switchTextView.setImageResource(isDigit ? R.drawable.ic_menu_switch_txt
        //        : R.drawable.ic_menu_switch_digit);

        // Invalidate to ask to require the text button to a digit button
        getSherlockActivity().supportInvalidateOptionsMenu();
    }

    private boolean hasAutocompleteList() {
        if(!isDigit) {
            return true;
        }
        return dialerLayout.canShowList();
    }

    /**
     * Set the value of the text field and put caret at the end
     *
     * @param value the new text to see in the text field
     */
    public void setTextFieldValue(CharSequence value) {
        if(digits == null) {
            initTextDigits = value.toString();
            return;
        }
        digits.setText(value);

        // make sure we keep the caret at the end of the text view
        Editable spannable = digits.getText();
        Selection.setSelection(spannable, spannable.length());
    }

     @Override
    public void onTrigger(int keyCode, int dialTone) {
        dialFeedback.giveFeedback(dialTone);
        keyPressed(keyCode);
    }

    @Override
    public void onTriggerLong(int keyCode, int dialTone) {
        dialFeedback.giveFeedback(dialTone);
        if (keyCode == KeyEvent.KEYCODE_7) {
            showLocked_Or_UnLocked();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        // Nothing to do here
    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        afterTextChanged(digits.getText());
        String newText = digits.getText().toString().trim();
        //System.out.println("onTextChanged (digits) = " + newText);
        // Allow account chooser button to automatically change again as we have clear field
        accountChooserButton.setChangeable(TextUtils.isEmpty(newText));
        applyRewritingInfo();
    }

    // Options
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        int action = getResources().getBoolean(R.bool.menu_in_bar) ? MenuItem.SHOW_AS_ACTION_IF_ROOM : MenuItem.SHOW_AS_ACTION_NEVER;
        MenuItem delMenu = menu.add(isDigit ? R.string.switch_to_text : R.string.switch_to_digit);
        delMenu.setIcon(
                isDigit ? R.drawable.ic_menu_switch_txt
                        : R.drawable.ic_menu_switch_digit).setShowAsAction( action );
        delMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setTextDialing(isDigit);
                return true;
            }
        });
    }


    @Override
    public void mute() {
        Log.i(CsipSampleConstant.TAG, "going to mute microphone");
        Toast.makeText(getActivity(),"MUTE!",Toast.LENGTH_SHORT).show();
        try {
            service.setMicrophoneMute(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (mHomeFragment != null) {
            mHomeFragment.drawPTT(false);
        }
        if (mLockFragment != null) {
            mLockFragment.drawPTT(true);
        }
    }

    @Override
    public void unmute() {
        Log.i(CsipSampleConstant.TAG, "going to unmute microphone");
        Toast.makeText(getActivity(),"UNMUTE!",Toast.LENGTH_SHORT).show();
        try {
            service.setMicrophoneMute(false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (mHomeFragment != null) {
            mHomeFragment.drawPTT(true);

        }
        if (mLockFragment != null) {
            mLockFragment.drawPTT(true);
        }
    }

    private void invite() {
        try {
            int callid = service.getCalls()[service.getCalls().length - 1].getCallId();
            service.reinvite(callid, true);
            //Thread.sleep(1000);
        } catch (Exception e) {
            //e.printStackTrace();
            Log.e(THIS_FILE, "Something went wrong while invite call", e);
        }
    }

    private String statusOfAccount() {
        String strStatus = null;
        try {
            int callid = service.getCalls()[service.getCalls().length - 1].getCallId();
            SipProfileState accountInfo = service.getSipProfileState(callid);

            System.out.println("accountInfo" + accountInfo.getStatusText());
            if (accountInfo != null && accountInfo.isActive()) {
                if (accountInfo.getAddedStatus() >= SipManager.SUCCESS) {
                    strStatus = getString(R.string.acct_unregistered);
                    if( TextUtils.isEmpty( accountInfo.getRegUri()) ) {
                        strStatus = getString(R.string.acct_registered);
                    } else if (accountInfo.isAddedToStack()) {
                        String pjStat = accountInfo.getStatusText();	// Used only on error status message
                        int statusCode = accountInfo.getStatusCode();
                        if (statusCode == SipCallSession.StatusCode.OK) {
                            // "Now account "+account.display_name+" has expires "+accountInfo.getExpires());
                            if (accountInfo.getExpires() > 0) {
                                strStatus = getString(R.string.acct_registered);
                            } else {
                                strStatus = getString(R.string.acct_unregistered);
                            }
                        } else if(statusCode != -1 ) {
                            if (statusCode == SipCallSession.StatusCode.PROGRESS || statusCode == SipCallSession.StatusCode.TRYING) {
                                strStatus = getString(R.string.acct_registering);
                            } else {
                                //TODO : treat 403 with special message
                                // Red : error
                                strStatus = getString(R.string.acct_regerror) + " - " + pjStat;	// Why can't ' - ' be in resource?
                            }
                        } else {
                            strStatus = getString(R.string.acct_registering);
                        }
                    }
                } else {
                    if(accountInfo.isAddedToStack()) {
                        strStatus = getString(R.string.acct_regfailed);
                    } else {
                        strStatus = getString(R.string.acct_registering);
                    }
                }
            }
            //Thread.sleep(1000);
        } catch (Exception e) {
            //e.printStackTrace();
            Log.e(THIS_FILE, "Something went wrong while invite call", e);
        }
        return strStatus;
    }

    @Override
    public void placeCall() {
        endCall();
        Log.i(CsipSampleConstant.TAG, "going to place call DialerFragement");
        placeCallWithOption(null, null);
    }

    @Override
    public void endCall() {
        Log.i(CsipSampleConstant.TAG, "going to end call DialerFragement");
        // placeCallWithOption(null, null);
        //SipCallSession[] lstSipCalls = service.getCalls();
        // int callid = lstSipCalls[service.getCalls().length - 1].getCallId();
        if (callsInfo != null && callsInfo.length > 0) {
            if (callsInfo[callsInfo.length - 1] != null) {
                int callid = callsInfo[callsInfo.length - 1].getCallId();
                try {
                    System.out.println("END CALLING");
                    service.hangup(callid, 0);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.e(THIS_FILE, "Something went wrong while hangup call", e);
                }
            }
        }
    }

    //    @Override
    public void placeGroupCall(int gNum) {
        Log.i(CsipSampleConstant.TAG, "going to place group call DialerFragement");
        placeCallWithOption(null, gNum);
    }

    @Override
    public void placeVideoCall() {
        Bundle b = new Bundle();
        b.putBoolean(SipCallSession.OPT_CALL_VIDEO, true);
        placeCallWithOption(b, null);
    }

    private void placeCallWithOption(Bundle b, Integer gNum) {
        Log.i(CsipSampleConstant.TAG, "going to place call with option DialerFragement");

        // Find number to dial
        String toCall = "";
        toCall = digits.getText().toString();
        preCallee = toCall;

        if (service == null) {
            return;
        }

        Long accountToUse = SipProfile.INVALID_ID;
        // Find account to use
        SipProfile acc = accountChooserButton.getSelectedAccount();
        if(acc == null) {
            return;
        }

        //System.out.println("placeCallWithOption (acc.id) = " + acc.id);
        if (acc.id > 0 && acc.id != currentAccountId) {
            accountToUse = acc.id;
        } else {
            accountToUse =currentAccountId;
        }

        if(isDigit) {
            toCall = PhoneNumberUtils.stripSeparators(toCall);
        }

        if(accountChooserFilterItem != null && accountChooserFilterItem.isChecked()) {
            toCall = rewriteNumber(toCall);
        }

        if (TextUtils.isEmpty(toCall)) {
            return;
        }

        // Well we have now the fields, clear theses fields
        // digits.getText().clear();

        // -- MAKE THE CALL --//
        Log.i(CsipSampleConstant.TAG, "going to make the call DialerFragment");
        System.out.println("placeCallWithOption (accountToUse) = " + accountToUse + ", (toCall) = " + toCall);
        if (accountToUse >= 0) {
            // It is a SIP account, try to call service for that
            try {
                callerID = Integer.parseInt(toCall);
                service.makeCallWithOptions(toCall, accountToUse.intValue(), b);
            } catch (RemoteException e) {
                Log.e(THIS_FILE, "Service can't be called to make the call");
            }
        } else if (accountToUse != SipProfile.INVALID_ID) {
            // It's an external account, find correct external account
            CallHandlerPlugin ch = new CallHandlerPlugin(getActivity());
            ch.loadFrom(accountToUse, toCall, new OnLoadListener() {
                @Override
                public void onLoad(CallHandlerPlugin ch) {
                    placePluginCall(ch);
                }
            });
        }
    }

    public void placeVMCall() {
        Long accountToUse = SipProfile.INVALID_ID;
        SipProfile acc = null;
        acc = accountChooserButton.getSelectedAccount();
        if (acc == null) {
            // Maybe we could inform user nothing will happen here?
            return;
        }
        System.out.println("placeVMCall (acc.id) = " + acc.id);
        accountToUse = acc.id;

        if (accountToUse >= 0) {
            SipProfile vmAcc = SipProfile.getProfileFromDbId(getActivity(), acc.id, new String[] {
                    SipProfile.FIELD_VOICE_MAIL_NBR
            });
            if (!TextUtils.isEmpty(vmAcc.vm_nbr)) {
                // Account already have a VM number
                try {
                    service.makeCall(vmAcc.vm_nbr, (int) acc.id);
                } catch (RemoteException e) {
                    Log.e(THIS_FILE, "Service can't be called to make the call");
                }
            } else {
                // Account has no VM number, propose to create one
                final long editedAccId = acc.id;
                LayoutInflater factory = LayoutInflater.from(getActivity());
                final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);

                missingVoicemailDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(acc.display_name)
                        .setView(textEntryView)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                if (missingVoicemailDialog != null) {
                                    TextView tf = (TextView) missingVoicemailDialog
                                            .findViewById(R.id.vmfield);
                                    if (tf != null) {
                                        String vmNumber = tf.getText().toString();
                                        if (!TextUtils.isEmpty(vmNumber)) {
                                            ContentValues cv = new ContentValues();
                                            cv.put(SipProfile.FIELD_VOICE_MAIL_NBR, vmNumber);

                                            int updated = getActivity().getContentResolver()
                                                    .update(ContentUris.withAppendedId(
                                                            SipProfile.ACCOUNT_ID_URI_BASE,
                                                            editedAccId),
                                                            cv, null, null);
                                            Log.d(THIS_FILE, "Updated accounts " + updated);
                                        }
                                    }
                                    missingVoicemailDialog.hide();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (missingVoicemailDialog != null) {
                                    missingVoicemailDialog.hide();
                                }
                            }
                        })
                        .create();

                // When the dialog is up, completely hide the in-call UI
                // underneath (which is in a partially-constructed state).
                missingVoicemailDialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                missingVoicemailDialog.show();
            }
        } else if (accountToUse == CallHandlerPlugin.getAccountIdForCallHandler(getActivity(),
                (new ComponentName(getActivity(), com.csipsimple.plugins.telephony.CallHandler.class).flattenToString()))) {
            // Case gsm voice mail
            TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(
                    Context.TELEPHONY_SERVICE);
            String vmNumber = tm.getVoiceMailNumber();

            if (!TextUtils.isEmpty(vmNumber)) {
                if(service != null) {
                    try {
                        service.ignoreNextOutgoingCallFor(vmNumber);
                    } catch (RemoteException e) {
                        Log.e(THIS_FILE, "Not possible to ignore next");
                    }
                }
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", vmNumber, null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {

                missingVoicemailDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.gsm)
                        .setMessage(R.string.no_voice_mail_configured)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (missingVoicemailDialog != null) {
                                    missingVoicemailDialog.hide();
                                }
                            }
                        })
                        .create();

                // When the dialog is up, completely hide the in-call UI
                // underneath (which is in a partially-constructed state).
                missingVoicemailDialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                missingVoicemailDialog.show();
            }
        }
        // TODO : manage others ?... for now, no way to do so cause no vm stored
    }

    private void placePluginCall(CallHandlerPlugin ch) {
        try {
            String nextExclude = ch.getNextExcludeTelNumber();
            if (service != null && nextExclude != null) {
                try {
                    service.ignoreNextOutgoingCallFor(nextExclude);
                } catch (RemoteException e) {
                    Log.e(THIS_FILE, "Impossible to ignore next outgoing call", e);
                }
            }
            ch.getIntent().send();
        } catch (CanceledException e) {
            Log.e(THIS_FILE, "Pending intent cancelled", e);
        }
    }

    @Override
    public void deleteChar() {
        keyPressed(KeyEvent.KEYCODE_DEL);
    }

    @Override
    public void deleteAll() {
        digits.getText().clear();
    }

    private final static String TAG_AUTOCOMPLETE_SIDE_FRAG = "autocomplete_dial_side_frag";

    @Override
    public void onVisibilityChanged(boolean visible) {
        if (visible && getResources().getBoolean(R.bool.use_dual_panes)) {
            // That's far to be optimal we should consider uncomment tests for reusing fragment
            // if (autoCompleteFragment == null) {
            autoCompleteFragment = new DialerAutocompleteDetailsFragment();

            if (digits != null) {
                Bundle bundle = new Bundle();
                bundle.putCharSequence(DialerAutocompleteDetailsFragment.EXTRA_FILTER_CONSTRAINT,
                        digits.getText().toString());

                autoCompleteFragment.setArguments(bundle);

            }
            // }
            // if
            // (getFragmentManager().findFragmentByTag(TAG_AUTOCOMPLETE_SIDE_FRAG)
            // != autoCompleteFragment) {
            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.details, autoCompleteFragment, TAG_AUTOCOMPLETE_SIDE_FRAG);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commitAllowingStateLoss();

            // }
        }
    }

    @Override
    public boolean onKey(View arg0, int keyCode, KeyEvent arg2) {
        return true;
    }

    // In dialer rewriting feature

    private void setRewritingFeature(boolean active) {
//        accountChooserFilterItem.setChecked(active);
//        rewriteTextInfo.setVisibility(active?View.VISIBLE:View.GONE);
        if(active) {
             applyRewritingInfo();
        }
        prefsWrapper.setPreferenceBooleanValue(SipConfigManager.REWRITE_RULES_DIALER, active);
    }

    private String rewriteNumber(String number) {
        SipProfile acc = accountChooserButton.getSelectedAccount();
        if (acc == null) {
            return number;
        }

        //System.out.println("rewriteNumber (acc.id) = " + acc.id);
        currentAccountId = acc.id;

        String numberRewrite = Filter.rewritePhoneNumber(getActivity(), acc.id, number);
        if(TextUtils.isEmpty(numberRewrite)) {
            return "";
        }

        ParsedSipContactInfos finalCallee = acc.formatCalleeNumber(numberRewrite);
        if(!TextUtils.isEmpty(finalCallee.displayName)) {
            return finalCallee.toString();
        }

        return finalCallee.getReadableSipUri();
    }

    private void applyRewritingInfo() {
        // Rewrite information textView update
        String newText = digits.getText().toString();
//        if(accountChooserFilterItem != null && accountChooserFilterItem.isChecked()) {
            if(isDigit) {
                newText = PhoneNumberUtils.stripSeparators(newText);
            }
            rewriteTextInfo.setText(rewriteNumber(newText));
//        }else {
//            rewriteTextInfo.setText("");
//        }
    }

}
