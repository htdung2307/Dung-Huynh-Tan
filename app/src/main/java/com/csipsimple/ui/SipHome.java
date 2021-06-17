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

package com.csipsimple.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.*;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.PermissionRequest;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
import com.actionbarsherlock.internal.nineoldandroids.animation.ValueAnimator;
import com.actionbarsherlock.internal.widget.IcsLinearLayout;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.csipsimple.BuildConfig;
import com.csipsimple.CsipSampleConstant;
import com.csipsimple.R;
import com.csipsimple.api.*;
import com.csipsimple.db.DBProvider;
import com.csipsimple.ui.prefs.GenericPrefs;
import com.csipsimple.ui.rk3326.RKVolFragment;
import com.csipsimple.utils.*;
import com.poc.display.Display;
import com.csipsimple.models.Filter;
import com.csipsimple.ui.account.AccountsEditList;
import com.csipsimple.ui.calllog.CallLogListFragment;
import com.csipsimple.ui.dialpad.DialerFragment;
import com.csipsimple.ui.ec3.Ec3Fragment;
import com.csipsimple.ui.favorites.FavListFragment;
import com.csipsimple.ui.help.Help;
import com.csipsimple.ui.messages.ConversationsListFragment;
import com.csipsimple.ui.warnings.WarningFragment;
import com.csipsimple.ui.warnings.WarningUtils;
import com.csipsimple.ui.warnings.WarningUtils.OnWarningChanged;
import com.csipsimple.utils.NightlyUpdater.UpdaterPopupLauncher;
import com.csipsimple.utils.backup.BackupWrapper;
import com.csipsimple.wizards.BasePrefsWizard;
import com.csipsimple.wizards.WizardIface;
import com.csipsimple.wizards.WizardUtils;
import com.csipsimple.wizards.WizardUtils.WizardInfo;
import com.csipsimple.wizards.impl.Basic;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.Permission;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static java.lang.Thread.sleep;

//import com.digi.android.spi.SPIManager;

public class SipHome extends SherlockFragmentActivity implements OnWarningChanged {
    public static final int ACCOUNTS_MENU = Menu.FIRST + 1;
    public static final int PARAMS_MENU = Menu.FIRST + 2;
    public static final int CLOSE_MENU = Menu.FIRST + 3;
    public static final int HELP_MENU = Menu.FIRST + 4;
    public static final int DISTRIB_ACCOUNT_MENU = Menu.FIRST + 5;


    private static final String THIS_FILE = "SIP_HOME";
    //private static final String XML_PATH = "mnt/sdcard/Download/IpPhone.xml";
// 2021-0529 Added
    public static final String IP_PHONE_XML = "IpPhone.xml";
    public static final String IP_PHONE_BACKUP_XML = "IpPhone_backup.xml";
// End add

    private final static int TAB_ID_DIALER = 0;
    private final static int TAB_ID_CALL_LOG = 1;
    private final static int TAB_ID_FAVORITES = 2;
    private final static int TAB_ID_MESSAGES = 3;
    private final static int TAB_ID_WARNING = 4;
    private final static int TAB_ID_GROUP_CALL = 5;
    // ABHISHEK
    private final static int TAB_ID_EC3 = 5;

    // protected static final int PICKUP_PHONE = 0;
    private static final int REQUEST_EDIT_DISTRIBUTION_ACCOUNT = 0;
    private static final int REQUEST_ENABLE_BT = 1;

// 2021-05-29 Added
    private IpPhoneConfigInfo configIpPhoneInfo = null;

    //private PreferencesWrapper prefWrapper;
    private PreferencesProviderWrapper prefProviderWrapper;

    private boolean hasTriedOnceActivateAcc = false;
    // private ImageButton pickupContact;
    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;
    private boolean mDualPane;
    private Thread asyncSanityChecker;
    private Tab warningTab;
    private ObjectAnimator warningTabfadeAnim;
    private int netId = 0;

    /**
     * Listener interface for Fragments accommodated in {@link ViewPager}
     * enabling them to know when it becomes visible or invisible inside the
     * ViewPager.
     */
    public interface ViewPagerVisibilityListener {
        void onVisibilityChanged(boolean visible);
    }

    /**
     * 2021-05-29 Added
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to grant permissions
     */
    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        this.requestPermissions(permissions, requestCode);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 200: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (tryToOpen_IpPhone_XML()) {
                        if (configIpPhoneInfo == null) {
                            configIpPhoneInfo = new IpPhoneConfigInfo();
                        }

                        // Add current settings to IpPhone_backup.xml
                        if (configIpPhoneInfo.addSettings(IP_PHONE_XML)) {
                            // Rename IpPhone.xml to IpPhone_backup.xml
                            renameFile(IP_PHONE_XML, IP_PHONE_BACKUP_XML);

                            // Restart activity after allow permission
//                            Intent intent = getIntent();
//                            finish();
//                            startActivity(intent);
                        }
                    }
                    //Toast.makeText(this, "Permission allow to read your External storage", Toast.LENGTH_SHORT).show();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return;
    }

    /**
     * 2021-06-03 Added
     * Check existence of IpPhone.xml
     */
    private boolean tryToOpen_IpPhone_XML() {
        File rootDir = Environment.getExternalStorageDirectory().getAbsoluteFile();
        File fileDir = new File(rootDir.getAbsolutePath() + IpPhoneConfigInfo.PATH_OF_CONFIG_FILES);

        if (fileDir.exists() == false) {
            return false;
        }

        File file = new File(fileDir, IP_PHONE_XML);
        if (file.exists() == false) {
            return false;
        }

        return true;
    }

    /**
     * 2021-05-30 Added
     * ReName any file
     */
    public void renameFile(String oldName,String newName){
        File rootDir = Environment.getExternalStorageDirectory().getAbsoluteFile();
        File dirFiles = new File(rootDir.getAbsolutePath() + IpPhoneConfigInfo.PATH_OF_CONFIG_FILES);
        File file = new File(dirFiles, oldName);

        if(dirFiles.exists()){
            File from = new File(dirFiles,oldName);
            File to = new File(dirFiles,newName);
            if(from.exists())
                from.renameTo(to);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //prefWrapper = new PreferencesWrapper(this);
        prefProviderWrapper = new PreferencesProviderWrapper(this);

        super.onCreate(savedInstanceState);

//                if (PermissionChecker.checkSelfPermission(this, Manifest.permission.USE_SIP)
//                        == PackageManager.PERMISSION_GRANTED){
//                }else{
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_SIP}, 0);
//                }

//        boolean bHasPermission = false;

        setContentView(R.layout.sip_home);
        //drawStatusBar();

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // showAbTitle = Compatibility.hasPermanentMenuKey

        Tab dialerTab = ab.newTab()
                 .setContentDescription(R.string.dial_tab_name_text)
                 .setIcon(R.drawable.ic_ab_dialer_holo_dark);
//        Tab callLogTab = ab.newTab()
//                 .setContentDescription(R.string.calllog_tab_name_text)
//                 .setIcon(R.drawable.ic_ab_history_holo_dark);
//
//        Tab favoritesTab = null;
//        if(CustomDistribution.supportFavorites()) {
//            favoritesTab = ab.newTab()
//                    .setContentDescription(R.string.favorites_tab_name_text)
//                    .setIcon(R.drawable.ic_ab_favourites_holo_dark);
//        }
//        Tab messagingTab = null;
//        if (CustomDistribution.supportMessaging()) {
//            messagingTab = ab.newTab()
//                    .setContentDescription(R.string.messages_tab_name_text)
//                    .setIcon(R.drawable.ic_ab_text_holo_dark);
//        }

        warningTab = ab.newTab().setIcon(android.R.drawable.ic_dialog_alert);
        warningTabfadeAnim = ObjectAnimator.ofInt(warningTab.getIcon(), "alpha", 255, 100);
        warningTabfadeAnim.setDuration(1500);
        warningTabfadeAnim.setRepeatCount(ValueAnimator.INFINITE);
        warningTabfadeAnim.setRepeatMode(ValueAnimator.REVERSE);

//        // ABHISHEK Adding EC3 Tab
//        Tab ec3Tab = ab.newTab()
//                .setContentDescription(R.string.ec3_tab_name_text)
//                .setIcon(R.drawable.ericsson);


        mDualPane = getResources().getBoolean(R.bool.use_dual_panes);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabsAdapter = new TabsAdapter(this, getSupportActionBar(), mViewPager);
        mTabsAdapter.addTab(dialerTab, DialerFragment.class, TAB_ID_DIALER);

        // set phone info only once at first.
        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
        if (isFirstRun)
        {
            // Code to run once
            setPhoneInfoAtFirst();

            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("FIRSTRUN", true);
            editor.apply();
        }

        //parseXMLFromSD(XML_PATH);
// 2021-05-29 Added
        if (shouldAskPermissions()) {
            askPermissions();
        }
        //startupProcess();
        loadConfigInfo();
        turnOnWifiConnection();
//        initSipService();
//        registerAccount();
        //        mTabsAdapter.addTab(callLogTab, CallLogListFragment.class, TAB_ID_CALL_LOG);
        //
        //        if(favoritesTab != null) {
        //            mTabsAdapter.addTab(favoritesTab, FavListFragment.class, TAB_ID_FAVORITES);
        //        }
        //        if (messagingTab != null) {
        //            mTabsAdapter.addTab(messagingTab, ConversationsListFragment.class, TAB_ID_MESSAGES);
        //        }
        //
        //        // ABHISHEK
        //        mTabsAdapter.addTab(ec3Tab, Ec3Fragment.class, TAB_ID_EC3);

        ab.hide();

        hasTriedOnceActivateAcc = false;

        if (!prefProviderWrapper.getPreferenceBooleanValue(SipConfigManager.PREVENT_SCREEN_ROTATION)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        selectTabWithAction(getIntent());
        Log.setLogLevel(prefProviderWrapper.getLogLevel());


        // Async check
        asyncSanityChecker = new Thread() {
            public void run() {
                asyncSanityCheck();
            };
        };
        asyncSanityChecker.start();
    }

    ArrayList<HashMap<String, String>> lstNetwork = null;
    ArrayList<HashMap<String, String>> lstSip = null;
    ArrayList<HashMap<String, String>> lstOther = null;
    ArrayList<HashMap<String, String>> lstSetting = null;
    private void loadConfigInfo() {
        if (configIpPhoneInfo == null) {
            configIpPhoneInfo = new IpPhoneConfigInfo();
        }

        // Initial attempt
        // Try to load the config info of IpPhone from IpPhone.xml file
        boolean loadStatus = configIpPhoneInfo.parseXMLFromSD(IP_PHONE_BACKUP_XML);

        // Successful loading
        if (loadStatus) {
            // Save assignable key as initial state
            lstNetwork = configIpPhoneInfo.getConfigNetwork();
            lstSip = configIpPhoneInfo.getConfigSIP();
            lstOther = configIpPhoneInfo.getConfigOther();
            lstSetting = configIpPhoneInfo.getConfigSetting();

            HashMap<String, String> configSip = null;
            if (lstSip != null && lstSip.isEmpty() == false) {
                configSip = lstSip.get(0);
                //initDialerWithText = lstSip.get(0).get("Connect1PhoneNumber");
                address = configSip.get("Connect1SIPServerIPAddress");   // sipServerIPAddress
                port = configSip.get("Connect1SIPServerPort");           // sipServerPort
                group = configSip.get("Connect1Name");                   // userDisplayName >> item 1~17
                user = configSip.get("Connect1User");                    // userName
                pass = configSip.get("Connect1Password");                // userPassword
            }
        }
    }

    private void turnOnWifiConnection() {
        lstNetwork = configIpPhoneInfo.getConfigNetwork();
        if (lstNetwork != null && lstNetwork.isEmpty() == false) {
            HashMap<String, String> infoNetwork = lstNetwork.get(0);
            String ssid = infoNetwork.get("SSID");
            String psk = infoNetwork.get("PSK");
            String address = infoNetwork.get("IPAddress"); //"192.0.2.51";
            String mask = infoNetwork.get("SubnetMask");
            String gateway = infoNetwork.get("DefaltGateway"); // "192.0.2.254";
            String dns1 = infoNetwork.get("DNS1"); //"192.0.2.254";
            String dns2 = infoNetwork.get("DNS2");
//            int type = WifiUtils.WIFI_CIPHER_NOPASS;
//            if (infoNetwork.get("Cryptography").equals("WPA2")) {
//                type = WifiUtils.WIFI_CIPHER_WPA_OR_WPA2;
//            }
//            else if (infoNetwork.get("Cryptography").equals("WEP")) {
//                type = WifiUtils.WIFI_CIPHER_WEP;
//            }
//            netId = WifiUtils.connectWifiApByName(getApplicationContext(), ssid, psk, WifiUtils.WIFI_CIPHER_WPA_OR_WPA2);
            connectWifi(ssid, psk);
            setNetworkInfo(address, mask, gateway, dns1, dns2);
        }
    }

    private String address = null;
    private String port = null;
    private String group = null;
    private String user = null;
    private String pass = null;

    private void registerAccount() {
//        HashMap<String, String> configSip = null;
//        if (lstSip != null && lstSip.isEmpty() == false) {
//            configSip = lstSip.get(0);

//            initDialerWithText = configSip.get("Connect1PhoneNumber");

//            address = configSip.get("Connect1SIPServerIPAddress");   // sipServerIPAddress
//            port = configSip.get("Connect1SIPServerPort");           // sipServerPort
//            group = configSip.get("Connect1Name");                   // userDisplayName >> item 1~17
//            user = configSip.get("Connect1User");                    // userName
//            pass = configSip.get("Connect1Password");                // userPassword

            account = buildAccountFromXml(account, address, port, group, user, pass);
            saveAccount("BASIC", account);
//        }
    }

    private int level = 0;
    boolean isStarted = false;
    private final Lock lock = new ReentrantLock(true);
    private void initSipService() {

        Runnable configureRunnable = new Runnable() {
            public void run() {
                //int count = 0;
                //WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                while (!isStarted) {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 100);
                    System.out.println("Wifi level: " + level);
                    if (level > 0 && isStarted == false) {
                        isStarted = true;
                        break;
                    }
//                    if (count >= 10) {
//                        Log.i(THIS_FILE, "Took too long to enable wi-fi, quitting");
//                        //return;
//                    }
//                    Log.i(THIS_FILE, "Still waiting for wi-fi to enable...");
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ie) {
                        // continue
                    }
                    //count++;
                }
                if (isStarted) {
                    lock.lock();
                    try {
                        // do something
                        mDialpadFragment.updateWifiStatus();
                        //isStarted = false;
                        Log.d(THIS_FILE,"WE CAN NOW start SIP service");
                        //startSipService();
                        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
                        // Optional, but here we bundle so just ensure we are using csipsimple package
                        serviceIntent.setPackage(SipHome.this.getPackageName());
                        serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(SipHome.this, SipHome.class));
                        startService(serviceIntent);
                        postStartSipService();

                        //mDialpadFragment.connectGroupDefault();
                    } catch (Exception e) {
                        // handle the exception
                    } finally {
                        if (lock.tryLock()) {
                            lock.unlock();
                        }
                    }

                }
            }
        };
        new Thread(configureRunnable).start();
    }

    /**
     * 2021-05-29 added
     * Startup Process
     */
// 2021-05-29 Added
//    private String initUser = null;
//    private String initGroup = null;
//    private String initDeviceNumber = null;
// End add
    private void startupProcess() {
        if (configIpPhoneInfo == null) {
            configIpPhoneInfo = new IpPhoneConfigInfo();
        }

        // Initial attempt
        // Try to load the config info of IpPhone from IpPhone.xml file
        boolean loadStatus = configIpPhoneInfo.parseXMLFromSD(IP_PHONE_BACKUP_XML);
        boolean isLoad_IpPhone_Backup = false;

        // Successful loading
        if (loadStatus) {
            // Save assignable key as initial state
            ArrayList<HashMap<String, String>> listNetworkInfo = configIpPhoneInfo.getConfigNetwork();
            ArrayList<HashMap<String, String>> listSipInfo = configIpPhoneInfo.getConfigSIP();
            ArrayList<HashMap<String, String>> listOtherInfo = configIpPhoneInfo.getConfigOther();

            HashMap<String, String> configNetwork = null;
            if (listNetworkInfo != null && listNetworkInfo.isEmpty() == false) {
                configNetwork = listNetworkInfo.get(0);
                connectWifi(configNetwork.get("SSID"), configNetwork.get("PSK"));
                setNetworkInfo(
                        configNetwork.get("IPAddress"),
                        configNetwork.get("SubnetMask"),
                        configNetwork.get("DefaltGateway"),
                        configNetwork.get("DNS1"),
                        configNetwork.get("DNS2"));
            } else {
                Toast.makeText(this, "Can not load network config info", Toast.LENGTH_SHORT).show();
                return;
            }

//            Log.d(THIS_FILE, "WE CAN NOW start SIP service");
//            startSipService();

            HashMap<String, String> configSip = null;
            if (listSipInfo != null && listSipInfo.isEmpty() == false) {
                configSip = listSipInfo.get(0);

                //initUser = configSip.get("Connect1User");
                //initGroup = configSip.get("Connect1Name");
                //initDeviceNumber = configSip.get("TerminalNumber");
                //initDialerWithText = configSip.get("Connect1PhoneNumber");

                account = buildAccountFromXml(account,
                        configSip.get("Connect1SIPServerIPAddress"),    // sipServerIPAddress
                        configSip.get("Connect1SIPServerPort"),         // sipServerPort
                        configSip.get("Connect1Name"),                  // userDisplayName >> item 1~17
                        configSip.get("Connect1User"),                  // userName
                        configSip.get("Connect1Password"));             // userPassword
                //setWizardId("BASIC");
                saveAccount("BASIC", account);

                //Toast.makeText(this, getAccountStatus(account.id), Toast.LENGTH_SHORT).show();

                // Rename IpPhone.xml to IpPhone_backup.xml
                //if (!isLoad_IpPhone_Backup) {
                //    renameFile(IP_PHONE_XML, IP_PHONE_BACKUP_XML);
                //}

            } else {
                Toast.makeText(this, "Can not load SIP config info", Toast.LENGTH_SHORT).show();
                return;
            }

        }
        // Failed
        else {
            // set phone info only once at first.
            SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
            boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
            if (isFirstRun)
            {
                // Code to run once
                //setPhoneInfoAtFirst();

                SharedPreferences.Editor editor = wmbPreference.edit();
                editor.putBoolean("FIRSTRUN", true);
                editor.apply();
            }
        }
    }

    //private static final String APP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyAppFolderInStorage/";
    static final String NAME_APK_FILE = "some.apk";
    public static final int REQUEST_INSTALL = 0;
    /**
     * 2021-06-11
     * Execute the apk file and then run the update
     * @param fileName
     */
    private void updateApp(String fileName) {
        try {

            File filePath = Environment.getExternalStorageDirectory();// path to file apk
            File file = new File(filePath, NAME_APK_FILE);

            Uri uri = getApkUri( file.getPath() ); // get Uri for  each SDK Android

            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData( uri );
            intent.setFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK );
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, getApplicationInfo().packageName);

            if ( getPackageManager().queryIntentActivities(intent, 0 ) != null ) {// checked on start Activity
                startActivityForResult(intent, REQUEST_INSTALL);
            } else {
                throw new Exception("don`t start Activity.");
            }

        } catch ( Exception e ) {

            Log.i(THIS_FILE + ":InstallApk", "Failed installl APK file", e);
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

        }
    }

    /**
     * Returns a Uri pointing to the APK to install.
     */
    private Uri getApkUri(String path) {

        // Before N, a MODE_WORLD_READABLE file could be passed via the ACTION_INSTALL_PACKAGE
        // Intent. Since N, MODE_WORLD_READABLE files are forbidden, and a FileProvider is
        // recommended.
        boolean useFileProvider = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;

        String tempFilename = "tmp.apk";
        byte[] buffer = new byte[16384];
        int fileMode = useFileProvider ? Context.MODE_PRIVATE : Context.MODE_WORLD_READABLE;
        try (InputStream is = new FileInputStream(new File(path));
             FileOutputStream fout = openFileOutput(tempFilename, fileMode)) {

            int n;
            while ((n = is.read(buffer)) >= 0) {
                fout.write(buffer, 0, n);
            }
        } catch (IOException e) {
            Log.i(THIS_FILE + ":getApkUri", "Failed to write temporary APK file", e);
        }

        if (useFileProvider) {
            File toInstall = new File(this.getFilesDir(), tempFilename);
            return FileProvider.getUriForFile(this,  BuildConfig.APPLICATION_ID, toInstall);
        } else {
            return Uri.fromFile(getFileStreamPath(tempFilename));
        }

    }

    Calendar calendar = Calendar.getInstance();
    int hour = calendar.get(Calendar.HOUR);
    int minute = calendar.get(Calendar.MINUTE);
    String timeStr = hour + ":" + minute;

    protected void drawStatusBar() {
        Display disp = Display.getInstance();
        disp.clearScreen();
        disp.showString(10, 0, timeStr);
        /* status bar */
        disp.drawBat(0);
        disp.drawSignal(0);
        disp.drawVolume(0);
        disp.drawBluetooth(true);
        /* buttons */
        disp.drawIMG(10, 3*8, 10+36, 3*8 + 12, Display.IMG_BLANK); //blank left key
        disp.drawIMG(46, 1*8+4, 46+36, 1*8+4 + 12, Display.IMG_VOLUME); //vol up key
        disp.drawIMG(46, 4*8+4, 46+36, 4*8+4 + 12, Display.IMG_BLANK); //blank down key
        disp.drawIMG(46, 3*8, 46+36, 3*8 + 12, Display.IMG_BLANK); //blank center key
        disp.drawIMG(82, 3*8, 82+36, 3*8 + 12, Display.IMG_MENU); //menu right key
        disp.drawIMG(5, 6*8+4, 5+36, 6*8+4 + 12, Display.IMG_AWTK); //awtk left down key
        disp.drawIMG(46,6*8+4, 46+36, 6*8+4 + 12, Display.IMG_BLANK); //blank middle key
        disp.drawIMG(87, 6*8+4, 87+36, 6*8+4 + 12, Display.IMG_TALK); //talk right down key
    }

    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost. It relies on a
     * trick. Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show. This is not sufficient for switching
     * between pages. So instead we make the content part of the tab host 0dp
     * high (it is not shown) and the TabsAdapter supplies its own dummy view to
     * show as the tab content. It listens to changes in tabs, and takes care of
     * switch to the correct paged in the ViewPager whenever the selected tab
     * changes.
     */
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

        public void addTab(Tab tab, Class<?> clss, int tabId) {
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
            return Fragment.instantiate(mContext, mTabs.get(position), new Bundle());
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
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
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // Nothing to do
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
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
                    supportInvalidateOptionsMenu();

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
                FragmentTransaction ft = SipHome.this.getSupportFragmentManager()
                        .beginTransaction();
                ft.replace(R.id.details, new Fragment(), null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }
        }
    }

    private DialerFragment mDialpadFragment;
    private CallLogListFragment mCallLogFragment;
    private ConversationsListFragment mMessagesFragment;
    private FavListFragment mPhoneFavoriteFragment;
    private WarningFragment mWarningFragment;

    private Fragment getFragmentAt(int position) {
        Integer id = mTabsAdapter.getIdForPosition(position);
        if(id != null) {
            if (id == TAB_ID_DIALER) {
                return mDialpadFragment;
            } else if (id == TAB_ID_CALL_LOG) {
                return mCallLogFragment;
            } else if (id == TAB_ID_MESSAGES) {
                return mMessagesFragment;
            } else if (id == TAB_ID_FAVORITES) {
                return mPhoneFavoriteFragment;
            } else if (id == TAB_ID_WARNING) {
                return mWarningFragment;
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
    public void onAttachFragment(Fragment fragment) {
        // This method can be called before onCreate(), at which point we cannot
        // rely on ViewPager.
        // In that case, we will setup the "current position" soon after the
        // ViewPager is ready.
        final int currentPosition = mViewPager != null ? mViewPager.getCurrentItem() : -1;
        Integer tabId = null; 
        if(mTabsAdapter != null) {
            tabId = mTabsAdapter.getIdForPosition(currentPosition);
        }
        if (fragment instanceof DialerFragment) {
            mDialpadFragment = (DialerFragment) fragment;
            if (initTabId == tabId && tabId != null && tabId == TAB_ID_DIALER) {
                mDialpadFragment.onVisibilityChanged(true);
                initTabId = null;
            }
            if(initDialerWithText != null) {
                //mDialpadFragment.setTextDialing(true);
                mDialpadFragment.setTextDialing(false);
                mDialpadFragment.setTextFieldValue(initDialerWithText);
                initDialerWithText = null;
            }
            //mDialpadFragment.connectGroup(1);
        } else if (fragment instanceof CallLogListFragment) {
            mCallLogFragment = (CallLogListFragment) fragment;
            if (initTabId == tabId && tabId != null && tabId == TAB_ID_CALL_LOG) {
                mCallLogFragment.onVisibilityChanged(true);
                initTabId = null;
            }
        } else if (fragment instanceof ConversationsListFragment) {
            mMessagesFragment = (ConversationsListFragment) fragment;
            if (initTabId == tabId && tabId != null && tabId == TAB_ID_MESSAGES) {
                mMessagesFragment.onVisibilityChanged(true);
                initTabId = null;
            }
        } else if (fragment instanceof FavListFragment) {
            mPhoneFavoriteFragment = (FavListFragment) fragment;
            if (initTabId == tabId && tabId != null && tabId == TAB_ID_FAVORITES) {
                mPhoneFavoriteFragment.onVisibilityChanged(true);
                initTabId = null;
            }
        } else if (fragment instanceof WarningFragment) {
            mWarningFragment = (WarningFragment) fragment;
            synchronized (warningList) {
                mWarningFragment.setWarningList(warningList);
                mWarningFragment.setOnWarningChangedListener(this);
            }
            
        }

    }


    private void asyncSanityCheck() {
        // if(Compatibility.isCompatible(9)) {
        // // We check now if something is wrong with the gingerbread dialer
        // integration
        // Compatibility.getDialerIntegrationState(SipHome.this);
        // }
        
        // Nightly build check
        if(NightlyUpdater.isNightlyBuild(this)) {
            Log.d(THIS_FILE, "Sanity check : we have a nightly build here");
            ConnectivityManager connectivityService = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo ni = connectivityService.getActiveNetworkInfo();
            // Only do the process if we are on wifi
            if (ni != null && ni.isConnected() && ni.getType() == ConnectivityManager.TYPE_WIFI) {
                // Only do the process if we didn't dismissed previously
                NightlyUpdater nu = new NightlyUpdater(this);

                if (!nu.ignoreCheckByUser()) {
                    long lastCheck = nu.lastCheck();
                    long current = System.currentTimeMillis();
                    long oneDay = 43200000; // 12 hours
                    if (current - oneDay > lastCheck) {
                        if (onForeground) {
                            // We have to check for an update
                            UpdaterPopupLauncher ru = nu.getUpdaterPopup(false);
                            if (ru != null && asyncSanityChecker != null) {
                                runOnUiThread(ru);
                            }
                        }
                    }
                }
            }
        }
        
        applyWarning(WarningUtils.WARNING_PRIVILEGED_INTENT, WarningUtils.shouldWarnPrivilegedIntent(this, prefProviderWrapper));
        applyWarning(WarningUtils.WARNING_NO_STUN, WarningUtils.shouldWarnNoStun(prefProviderWrapper));
        applyWarning(WarningUtils.WARNING_VPN_ICS, WarningUtils.shouldWarnVpnIcs(prefProviderWrapper));
        applyWarning(WarningUtils.WARNING_SDCARD, WarningUtils.shouldWarnSDCard(this, prefProviderWrapper));
    }

    private SipProfile account = null;
    private WizardIface wizard = null;
    private String wizardId = "";

    private boolean setWizardId(String wId) {
        if (wizardId == null) {
            return setWizardId(WizardUtils.EXPERT_WIZARD_TAG);
        }

        WizardInfo wizardInfo = WizardUtils.getWizardClass(wId);
        if (wizardInfo == null) {
            if (!wizardId.equals(WizardUtils.EXPERT_WIZARD_TAG)) {
                return setWizardId(WizardUtils.EXPERT_WIZARD_TAG);
            }
            return false;
        }

        try {
            wizard = (WizardIface) wizardInfo.classObject.newInstance();
        } catch (IllegalAccessException e) {
            Log.e(THIS_FILE, "Can't access wizard class", e);
            if (!wizardId.equals(WizardUtils.EXPERT_WIZARD_TAG)) {
                return setWizardId(WizardUtils.EXPERT_WIZARD_TAG);
            }
            return false;
        } catch (InstantiationException e) {
            Log.e(THIS_FILE, "Can't access wizard class", e);
            if (!wizardId.equals(WizardUtils.EXPERT_WIZARD_TAG)) {
                return setWizardId(WizardUtils.EXPERT_WIZARD_TAG);
            }
            return false;
        }
        wizardId = wId;
//        wizard.setParent(this);
//        if(getSupportActionBar() != null) {
//            getSupportActionBar().setIcon(WizardUtils.getWizardIconRes(wizardId));
//        }
        return true;
    }

    private void saveAccount(String wizardId, SipProfile d) {
        boolean needRestart = false;

        getContentResolver().delete(SipProfile.ACCOUNT_URI, null, null);

// 2021-06-01 Added
        //setWizardId(wizardId);
// End add
        PreferencesWrapper prefs = new PreferencesWrapper(getApplicationContext());
        account.wizard = wizardId;
        if (account.id == SipProfile.INVALID_ID) {
            // This account does not exists yet
            prefs.startEditing();
            WizardInfo wizardInfo = new WizardInfo("BASIC", "Basic",
                    R.drawable.ic_wizard_basic, 50,
                    new Locale[] {}, true, false,
                    Basic.class);
            try {
                wizard = (WizardIface) wizardInfo.classObject.newInstance();
            } catch (IllegalAccessException e) {
                Log.e(THIS_FILE, "Can't access wizard class", e);
                return ;
            } catch (InstantiationException e) {
                Log.e(THIS_FILE, "Can't access wizard class", e);
                return ;
            }
            wizard.setDefaultParams(prefs);
            prefs.endEditing();
            applyNewAccountDefault(account);
            Uri uri = getContentResolver().insert(SipProfile.ACCOUNT_URI, account.getDbContentValues());

            System.out.println("saveAccount = " + account.getDbContentValues());

            // After insert, add filters for this wizard
            account.id = ContentUris.parseId(uri);
            List<Filter> filters = wizard.getDefaultFilters(account);
            if (filters != null) {
                for (Filter filter : filters) {
                    // Ensure the correct id if not done by the wizard
                    filter.account = (int) account.id;
                    getContentResolver().insert(SipManager.FILTER_URI, filter.getDbContentValues());
                }
            }
            // Check if we have to restart
            needRestart = wizard.needRestart();
            //Toast.makeText(this, getAccountStatus(account.id), Toast.LENGTH_SHORT).show();
        } else {
            // TODO : should not be done there but if not we should add an
            // option to re-apply default params
            prefs.startEditing();
            wizard.setDefaultParams(prefs);
            prefs.endEditing();
            getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, account.id), account.getDbContentValues(), null, null);
        }
       //Toast.makeText(this, "ACTION_SIP_REQUEST_RESTART = " + needRestart, Toast.LENGTH_LONG).show();
        // Mainly if global preferences were changed, we have to restart sip stack
        System.out.println("saveAccount (needRestart) = " + needRestart);
        if (needRestart) {
            Intent intent = new Intent(SipManager.ACTION_SIP_REQUEST_RESTART);
            sendBroadcast(intent);
        }
    }

    private SipProfile buildAccountFromXml(SipProfile account, String sipServerIPAddress,
                                          String sipServerPort, String userDisplayName,
                                          String userName, String userPassword) {

        account = SipProfile.getProfileFromDbId(this, SipProfile.INVALID_ID, DBProvider.ACCOUNT_FULL_PROJECTION);
        Log.d(THIS_FILE, "begin of save ....");
        account.display_name = userDisplayName.trim();

        account.acc_id = "<sip:" + SipUri.encodeUser(userName.trim()) + "@"+sipServerIPAddress.trim()+">";

        String regUri = "sip:" + sipServerIPAddress.concat(":").concat(sipServerPort);
        account.reg_uri = regUri;
        account.proxies = new String[] { regUri } ;


        account.realm = "*";
        account.username = userName.trim();
        account.data = userPassword;
        account.scheme = SipProfile.CRED_SCHEME_DIGEST;
        account.datatype = SipProfile.CRED_DATA_PLAIN_PASSWD;
        //By default auto transport
        account.transport = SipProfile.TRANSPORT_UDP;
        account.rtp_enable_qos = 1;
        account.rtp_qos_dscp = 48;
        account.reg_timeout = 30;
        account.mwi_enabled = false;

        return account;
    }

    private void applyNewAccountDefault(SipProfile account) {
        if(account.use_rfc5626) {
            if(TextUtils.isEmpty(account.rfc5626_instance_id)) {
                String autoInstanceId = (UUID.randomUUID()).toString();
                account.rfc5626_instance_id = "<urn:uuid:"+autoInstanceId+">";
            }
        }
    }

    // Service monitoring stuff
    private void startSipService() {
        Log.i(CsipSampleConstant.TAG, "going to start SipService");
        Thread t = new Thread("StartSip") {
            public void run() {
                Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
                // Optional, but here we bundle so just ensure we are using csipsimple package
                serviceIntent.setPackage(SipHome.this.getPackageName());
                serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(SipHome.this, SipHome.class));
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    startForegroundService(serviceIntent);
//                } else {
                startService(serviceIntent);
//                }
                postStartSipService();
            };
        };
        t.start();
        Toast.makeText(this, "SipService has started.... ", Toast.LENGTH_LONG).show();

    }

    private void disconnect(boolean quit) {
        Log.d(THIS_FILE, "True disconnection...");
        Intent intent = new Intent(SipManager.ACTION_OUTGOING_UNREGISTER);
        intent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(this, SipHome.class));
        sendBroadcast(intent);
        if(quit) {
            finish();
        }
    }

    private void postStartSipService() {
        Log.i(CsipSampleConstant.TAG, "going to start Post - SipService");
        // If we have never set fast settings
//        if (CustomDistribution.showFirstSettingScreen()) {
//            if (!prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false)) {
//                Intent prefsIntent = new Intent(SipManager.ACTION_UI_PREFS_FAST);
//                prefsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(prefsIntent);
//                return;
//            }
//        } else {
//            boolean doFirstParams = !prefProviderWrapper.getPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, false);
//            prefProviderWrapper.setPreferenceBooleanValue(PreferencesWrapper.HAS_ALREADY_SETUP, true);
//            if (doFirstParams) {
//                prefProviderWrapper.resetAllDefaultValues();
//            }
//        }
        // If we have no account yet, open account panel,
        if (!hasTriedOnceActivateAcc) {

            Cursor c = getContentResolver().query(SipProfile.ACCOUNT_URI, new String[] {
                    SipProfile.FIELD_ID
            }, null, null, null);
            int accountCount = 0;
            if (c != null) {
                try {
                    accountCount = c.getCount();
                } catch (Exception e) {
                    Log.e(THIS_FILE, "Something went wrong while retrieving the account", e);
                } finally {
                    c.close();
                }
            }

            System.out.println("postStartSipService (accountCount) = " + accountCount);
            registerAccount();
            hasTriedOnceActivateAcc = true;
//            if (accountCount == 0) {
//                Intent accountIntent = null;
//                WizardInfo distribWizard = CustomDistribution.getCustomDistributionWizard();
//                if (distribWizard != null) {
//                    accountIntent = new Intent(this, BasePrefsWizard.class);
//                    accountIntent.putExtra(SipProfile.FIELD_WIZARD, distribWizard.id);
//                } else {
//                    accountIntent = new Intent(this, AccountsEditList.class);
//                }
//                //System.out.println("Add account - Open");
//                if (accountIntent != null) {
//                    accountIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(accountIntent);
//                    hasTriedOnceActivateAcc = true;
//                    return;
//                }
//            }
//            hasTriedOnceActivateAcc = true;
        }
    }

    public String getAccountStatus(long accountId) {
        SipProfileState accountInfo = null;
        Cursor c = getContentResolver().query(ContentUris.withAppendedId(SipProfile.ACCOUNT_STATUS_ID_URI_BASE, accountId),
                null, null, null, null);
        if (c != null) {
            try {
                if(c.getCount() > 0) {
                    c.moveToFirst();
                    accountInfo = new SipProfileState(c);
                }
            } catch (Exception e) {
                Log.e(THIS_FILE, "Error on looping over sip profiles states", e);
            } finally {
                c.close();
            }
        }
        System.out.println("accountInfo" + accountInfo.getStatusText());
        String strStatus = null;
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
        return strStatus;
    }

    private boolean onForeground = false;

    @Override
    protected void onPause() {
        Log.d(THIS_FILE, "On Pause SIPHOME");
        onForeground = false;
        if(asyncSanityChecker != null) {
            if(asyncSanityChecker.isAlive()) {
                asyncSanityChecker.interrupt();
                asyncSanityChecker = null;
            }
        }
        super.onPause();

    }

    @Override
    protected void onResume() {
        Log.d(THIS_FILE, "On Resume SIPHOME");
        super.onResume();

        onForeground = true;

        prefProviderWrapper.setPreferenceBooleanValue(PreferencesWrapper.HAS_BEEN_QUIT, false);

        // Set visible the currently selected account
        sendFragmentVisibilityChange(mViewPager.getCurrentItem(), true);

//        Log.d(THIS_FILE, "WE CAN NOW start SIP service");
//        startSipService();
        initSipService();
//        registerAccount();

        applyTheme();
    }
    
    private ArrayList<View> getVisibleLeafs(View v) {
        ArrayList<View> res = new ArrayList<View>();
        if(v.getVisibility() != View.VISIBLE) {
            return res;
        }
        if(v instanceof ViewGroup) {
            for(int i = 0; i < ((ViewGroup) v).getChildCount(); i++) {
                ArrayList<View> subLeafs = getVisibleLeafs(((ViewGroup) v).getChildAt(i));
                res.addAll(subLeafs);
            }
            return res;
        }
        res.add(v);
        return res;
    }

    private void applyTheme() {
//        Theme t = Theme.getCurrentTheme(this);
//        if (t != null) {
//            ActionBar ab = getSupportActionBar();
//            if (ab != null) {
//                View vg = getWindow().getDecorView().findViewById(android.R.id.content);
//                // Action bar container
//                ViewGroup abc = (ViewGroup) ((ViewGroup) vg.getParent()).getChildAt(0);
//                //
//                ArrayList<View> leafs = getVisibleLeafs(abc);
//                int i = 0;
//                for (View leaf : leafs) {
//                    if (leaf instanceof ImageView) {
//                        Integer id = mTabsAdapter.getIdForPosition(i);
//                        if (id != null) {
//                            int tabId = id;
//                            Drawable customIcon = null;
//                            switch (tabId) {
//                                case TAB_ID_DIALER:
//                                    customIcon = t.getDrawableResource("ic_ab_dialer");
//                                    break;
//                                case TAB_ID_CALL_LOG:
//                                    customIcon = t.getDrawableResource("ic_ab_history");
//                                    break;
//                                case TAB_ID_MESSAGES:
//                                    customIcon = t.getDrawableResource("ic_ab_text");
//                                    break;
//                                case TAB_ID_FAVORITES:
//                                    customIcon = t.getDrawableResource("ic_ab_favourites");
//                                    break;
//                                default:
//                                    break;
//                            }
//                            if (customIcon != null) {
//                                ((ImageView) leaf).setImageDrawable(customIcon);
//                            }
//
//                            t.applyBackgroundStateListSelectableDrawable((View) leaf.getParent(),
//                                    "tab");
//                            if (i == 0) {
//                                ViewParent tabLayout = leaf.getParent().getParent();
//                                if (tabLayout instanceof LinearLayout) {
//                                    Drawable d = t.getDrawableResource("tab_divider");
//                                    if (d != null) {
//                                       /* UtilityWrapper.getInstance()
//                                                .setLinearLayoutDividerDrawable(
//                                                        (LinearLayout) tabLayout, d);*/
//                                        //change tqc
//                                        /*if(tabLayout instanceof IcsLinearLayout) {
//                                            ((IcsLinearLayout)tabLayout).supportSetDividerDrawable(d);
//                                        }*/
//                                    }
//                                    Integer dim = t.getDimension("tab_divider_padding");
//                                    if (dim != null) {
//                                        //change tqc
//                                       /* UtilityWrapper.getInstance().setLinearLayoutDividerPadding(
//                                                (LinearLayout) tabLayout, dim);*/
//                                    }
//                                }
//                            }
//                            i++;
//                        }
//                    }
//                }
//                if(i > 0) {
//                    t.applyBackgroundDrawable((View) leafs.get(0).getParent().getParent(), "abs_background");
//                }
//
//                Drawable d = t.getDrawableResource("split_background");
//                if (d != null) {
//                    ab.setSplitBackgroundDrawable(d);
//                }
//
//                t.applyBackgroundDrawable(vg, "content_background");
//            }
//        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        selectTabWithAction(intent);
    }

    private String initDialerWithText = null;
    Integer initTabId = null;
    private void selectTabWithAction(Intent intent) {
        if (intent != null) {
            String callAction = intent.getAction();
            if (!TextUtils.isEmpty(callAction)) {
                ActionBar ab = getSupportActionBar();
                Tab toSelectTab = null;
                Integer toSelectId = null;
                if (callAction.equalsIgnoreCase(SipManager.ACTION_SIP_DIALER)
                        || callAction.equalsIgnoreCase(Intent.ACTION_DIAL)
                        || callAction.equalsIgnoreCase(Intent.ACTION_VIEW)
                        || callAction.equalsIgnoreCase(Intent.ACTION_SENDTO) /* TODO : sendto should im if not csip? */) {
                    Integer pos = mTabsAdapter.getPositionForId(TAB_ID_DIALER);
                    if(pos != null) {
                        toSelectTab = ab.getTabAt(pos);
                        Uri data = intent.getData();
                        String nbr = UriUtils.extractNumberFromIntent(intent, this);

                        if (!TextUtils.isEmpty(nbr)) {
                            if (data != null && mDialpadFragment != null) {
                                mDialpadFragment.setTextDialing(true);
                                mDialpadFragment.setTextFieldValue(nbr);
                            } else {
                                initDialerWithText = nbr;
                            }
                        }
                        toSelectId = TAB_ID_DIALER;
                    }
                } else if (callAction.equalsIgnoreCase(SipManager.ACTION_SIP_CALLLOG)) {
                    Integer pos = mTabsAdapter.getPositionForId(TAB_ID_CALL_LOG);
                    if(pos != null) {
                        toSelectTab = ab.getTabAt(pos);
                        toSelectId = TAB_ID_CALL_LOG;
                    }
                } else if (callAction.equalsIgnoreCase(SipManager.ACTION_SIP_FAVORITES)) {
                    Integer pos = mTabsAdapter.getPositionForId(TAB_ID_FAVORITES);
                    if(pos != null) {
                        toSelectTab = ab.getTabAt(pos);
                        toSelectId = TAB_ID_FAVORITES;
                    }
                } else if (callAction.equalsIgnoreCase(SipManager.ACTION_SIP_MESSAGES)) {
                    Integer pos = mTabsAdapter.getPositionForId(TAB_ID_MESSAGES);
                    if(pos != null) {
                        toSelectTab = ab.getTabAt(pos);
                        toSelectId = TAB_ID_MESSAGES;
                    }
                }
                if (toSelectTab != null) {
                    ab.selectTab(toSelectTab);
                    initTabId = toSelectId;
                }else {
                    initTabId = 0;
                }
                
            }
        }
    }

    @Override
    protected void onDestroy() {
        disconnect(false);
        super.onDestroy();
        Log.d(THIS_FILE, "---DESTROY SIP HOME END---");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        int actionRoom = getResources().getBoolean(R.bool.menu_in_bar) ? MenuItem.SHOW_AS_ACTION_IF_ROOM : MenuItem.SHOW_AS_ACTION_NEVER;
        
        WizardInfo distribWizard = CustomDistribution.getCustomDistributionWizard();
        if (distribWizard != null) {
            menu.add(Menu.NONE, DISTRIB_ACCOUNT_MENU, Menu.NONE, "My " + distribWizard.label)
                    .setIcon(distribWizard.icon)
                    .setShowAsAction(actionRoom);
        }
        if (CustomDistribution.distributionWantsOtherAccounts()) {
            int accountRoom = actionRoom;
            if(Compatibility.isCompatible(13)) {
                accountRoom |= MenuItem.SHOW_AS_ACTION_WITH_TEXT;
            }
            menu.add(Menu.NONE, ACCOUNTS_MENU, Menu.NONE,
                    (distribWizard == null) ? R.string.accounts : R.string.other_accounts)
                    .setIcon(R.drawable.ic_menu_account_list)
                    .setAlphabeticShortcut('a')
                    .setShowAsAction( accountRoom );
        }
        menu.add(Menu.NONE, PARAMS_MENU, Menu.NONE, R.string.prefs)
                .setIcon(android.R.drawable.ic_menu_preferences)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        menu.add(Menu.NONE, HELP_MENU, Menu.NONE, R.string.help)
                .setIcon(android.R.drawable.ic_menu_help)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(Menu.NONE, CLOSE_MENU, Menu.NONE, R.string.menu_disconnect)
                .setIcon(R.drawable.ic_lock_power_off)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ACCOUNTS_MENU:
                startActivity(new Intent(this, AccountsEditList.class));
                return true;
            case PARAMS_MENU:
                startActivityForResult(new Intent(SipManager.ACTION_UI_PREFS_GLOBAL), CHANGE_PREFS);
                return true;
            case CLOSE_MENU:
                Log.d(THIS_FILE, "CLOSE");
                boolean currentlyActiveForIncoming = prefProviderWrapper.isValidConnectionForIncoming();
                boolean futureActiveForIncoming = (prefProviderWrapper.getAllIncomingNetworks().size() > 0);
                if (currentlyActiveForIncoming || futureActiveForIncoming) {
                    // Alert user that we will disable for all incoming calls as
                    // he want to quit
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.warning)
                            .setMessage(
                                    getString(currentlyActiveForIncoming ? R.string.disconnect_and_incoming_explaination
                                            : R.string.disconnect_and_future_incoming_explaination))
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    prefProviderWrapper.setPreferenceBooleanValue(PreferencesWrapper.HAS_BEEN_QUIT, true);
                                    disconnect(true);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                } else {
                    disconnect(true);
                }
                return true;
            case HELP_MENU:
                // Create the fragment and show it as a dialog.
                DialogFragment newFragment = Help.newInstance();
                newFragment.show(getSupportFragmentManager(), "dialog");
                return true;
            case DISTRIB_ACCOUNT_MENU:
                WizardInfo distribWizard = CustomDistribution.getCustomDistributionWizard();

                Cursor c = getContentResolver().query(SipProfile.ACCOUNT_URI, new String[] {
                        SipProfile.FIELD_ID
                }, SipProfile.FIELD_WIZARD + "=?", new String[] {
                        distribWizard.id
                }, null);

                Intent it = new Intent(this, BasePrefsWizard.class);
                it.putExtra(SipProfile.FIELD_WIZARD, distribWizard.id);
                Long accountId = null;
                if (c != null && c.getCount() > 0) {
                    try {
                        c.moveToFirst();
                        accountId = c.getLong(c.getColumnIndex(SipProfile.FIELD_ID));
                    } catch (Exception e) {
                        Log.e(THIS_FILE, "Error while getting wizard", e);
                    } finally {
                        c.close();
                    }
                }
                if (accountId != null) {
                    it.putExtra(SipProfile.FIELD_ID, accountId);
                }
                startActivityForResult(it, REQUEST_EDIT_DISTRIBUTION_ACCOUNT);

                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private final static int CHANGE_PREFS = 1;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CHANGE_PREFS) {
            sendBroadcast(new Intent(SipManager.ACTION_SIP_REQUEST_RESTART));
            BackupWrapper.getInstance(this).dataChanged();
        }
        if(requestCode == REQUEST_INSTALL) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this,"Install succeeded!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this,"Install canceled!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,"Install Failed!", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Warning view
    private List<String> warningList = new ArrayList<String>();
    private void applyWarning(String warnCode, boolean active) {
        synchronized (warningList) {
            if(active) {
                warningList.add(warnCode);
            }else {
                warningList.remove(warnCode);
            }
        }
        runOnUiThread(refreshWarningTabRunnable);
    }
    
    Runnable refreshWarningTabRunnable = new Runnable() {
        @Override
        public void run() {
            refreshWarningTabDisplay();
        }
    };
    
    private void refreshWarningTabDisplay() {
        List<String> warnList = new ArrayList<String>();
        synchronized (warningList) {
            warnList.addAll(warningList);
        }
        if(mWarningFragment != null) {
            mWarningFragment.setWarningList(warnList);
            mWarningFragment.setOnWarningChangedListener(this);
        }
        if(warnList.size() > 0) {
            // Show warning tab if any to display
            if(mTabsAdapter.getPositionForId(TAB_ID_WARNING) == null) {
                // And not yet displayed
                Log.w(THIS_FILE, "Reason to warn " + warnList);
                
                mTabsAdapter.addTab(warningTab, WarningFragment.class, TAB_ID_WARNING);
                warningTabfadeAnim.start();
            }
        }else {
            // Hide warning tab since nothing to warn about
            ActionBar ab = getSupportActionBar();
            int selPos = -1;
            if(ab != null) {
                selPos = ab.getSelectedTab().getPosition();
            }
            Integer pos = mTabsAdapter.getPositionForId(TAB_ID_WARNING);
            if(pos != null) {
                mTabsAdapter.removeTabAt(pos);
                if(selPos == pos && ab != null) {
                    ab.selectTab(ab.getTabAt(0));
                }
            }
            if(warningTabfadeAnim.isStarted()) {
                warningTabfadeAnim.end();
            }
        }
    }

    @Override
    public void onWarningRemoved(String warnKey) {
        applyWarning(warnKey, false);
    }

    public void setPhoneInfoAtFirst() {
        account = buildAccountFromXml(account, "192.0.2.2", "5060", "2001", "2001", "123456");
        //setWizardId("BASIC");
        saveAccount("BASIC", account);
    }

    public void parseXMLFromSD(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                InputStream is = new FileInputStream(file.getPath());
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(is));
                doc.getDocumentElement().normalize();

                NodeList nodeList = doc.getElementsByTagName("Network");
                Node node = nodeList.item(0);
                Element fstElmnt = (Element) node;
                String ssid=fstElmnt.getElementsByTagName("SSID").item(0).getTextContent();
                String wpaPass=fstElmnt.getElementsByTagName("PSK").item(0).getTextContent();
                String iPAddress=fstElmnt.getElementsByTagName("IPAddress").item(0).getTextContent();
                String subnetMask=fstElmnt.getElementsByTagName("SubnetMask").item(0).getTextContent();
                String defaltGateway=fstElmnt.getElementsByTagName("DefaltGateway").item(0).getTextContent();
                String dns1=fstElmnt.getElementsByTagName("DNS1").item(0).getTextContent();
                String dns2=fstElmnt.getElementsByTagName("DNS2").item(0).getTextContent();

                connectWifi(ssid, wpaPass);
                setNetworkInfo(iPAddress, subnetMask, defaltGateway, dns1, dns2);

                nodeList = doc.getElementsByTagName("SIP");
                node = nodeList.item(0);
                fstElmnt = (Element) node;
                String terminalNumber=fstElmnt.getElementsByTagName("TerminalNumber").item(0).getTextContent();
                String sipServerIPAddress=fstElmnt.getElementsByTagName("Connect1SIPServerIPAddress").item(0).getTextContent();
                String sipServerPort=fstElmnt.getElementsByTagName("Connect1SIPServerPort").item(0).getTextContent();
                String sipDomain=fstElmnt.getElementsByTagName("Connect1SIPDomain").item(0).getTextContent();
                String userName=fstElmnt.getElementsByTagName("Connect1User").item(0).getTextContent();
                String userPassword=fstElmnt.getElementsByTagName("Connect1Password").item(0).getTextContent();
                String userDisplayName=fstElmnt.getElementsByTagName("Connect1Name").item(0).getTextContent();  //item 1~17
                String userPhoneNumber=fstElmnt.getElementsByTagName("Connect1PhoneNumber").item(0).getTextContent();

                account = buildAccountFromXml(account, sipServerIPAddress, sipServerPort, userDisplayName, userName, userPassword);
                saveAccount("BASIC", account);
                //if (file.delete()) {
                //    System.out.println("file Deleted :" + path);
                //}
            }

        }
        catch (Exception e)
        {
            System.out.println("XML Pasing Excpetion = " + e);
        }
    }

    private static Object newInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
    {
        return newInstance(className, new Class<?>[0], new Object[0]);
    }

    private static Object newInstance(String className, Class<?>[] parameterClasses, Object[] parameterValues) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException
    {
        Class<?> clz = Class.forName(className);
        Constructor<?> constructor = clz.getConstructor(parameterClasses);
        return constructor.newInstance(parameterValues);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object getEnumValue(String enumClassName, String enumValue) throws ClassNotFoundException
    {
        Class<Enum> enumClz = (Class<Enum>)Class.forName(enumClassName);
        return Enum.valueOf(enumClz, enumValue);
    }

    private static void setField(Object object, String fieldName, Object value) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException
    {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.set(object, value);
    }

    private static <T> T getField(Object object, String fieldName, Class<T> type) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException
    {
        Field field = object.getClass().getDeclaredField(fieldName);
        return type.cast(field.get(object));
    }

    private static void callMethod(Object object, String methodName, String[] parameterTypes, Object[] parameterValues) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
    {
        Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++)
            parameterClasses[i] = Class.forName(parameterTypes[i]);

        Method method = object.getClass().getDeclaredMethod(methodName, parameterClasses);
        method.invoke(object, parameterValues);
    }

    private void connectWifi(String ssid, String key) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", key);

        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        //force to turn on wifi
        wifiManager.setWifiEnabled(true);
        //remember id
        netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    private void setNetworkInfo(String ip, String mask, String gateway, String dns1, String dns2) {
        WifiConfiguration wifiConf = null;
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration conf : configuredNetworks){
            if (conf.networkId == netId){
                System.out.println("WifiConfiguration (ID): " + netId);
                wifiConf = conf;
                break;
            }
        }

        if (wifiConf != null)
        {
            try
            {
                setStaticIpConfiguration(
                        wifiManager,
                        wifiConf,
                        InetAddress.getByName(ip.trim()),
                        24,
                        InetAddress.getByName(gateway.trim()),
                        new InetAddress[] { InetAddress.getByName(dns1.trim()), InetAddress.getByName(dns2.trim()) });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void setStaticIpConfiguration(WifiManager manager, WifiConfiguration config, InetAddress ipAddress, int prefixLength, InetAddress gateway,
                                                 InetAddress[] dns) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException, InstantiationException
    {
        // First set up IpAssignment to STATIC.
        Object ipAssignment = getEnumValue("android.net.IpConfiguration$IpAssignment", "STATIC");
        callMethod(config, "setIpAssignment", new String[] { "android.net.IpConfiguration$IpAssignment" }, new Object[] { ipAssignment });

        // Then set properties in StaticIpConfiguration.
        Object staticIpConfig = newInstance("android.net.StaticIpConfiguration");
        Object linkAddress = newInstance("android.net.LinkAddress", new Class<?>[] { InetAddress.class, int.class }, new Object[] { ipAddress, prefixLength });

        setField(staticIpConfig, "ipAddress", linkAddress);
        setField(staticIpConfig, "gateway", gateway);
        getField(staticIpConfig, "dnsServers", ArrayList.class).clear();
        for (int i = 0; i < dns.length; i++)
            getField(staticIpConfig, "dnsServers", ArrayList.class).add(dns[i]);

        callMethod(config, "setStaticIpConfiguration", new String[] { "android.net.StaticIpConfiguration" }, new Object[] { staticIpConfig });
        manager.updateNetwork(config);
        manager.saveConfiguration();
    }

    public void scanBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    }

    long checkAfter = 1000;
    boolean is_Enter_Pressed = false;
    boolean is_Long_Pressed = false;
    //final Lock lockThread = new ReentrantLock();
    long startPressed = 0, endPressed = 0, currentPress = 0;
    private Handler handlerLockScreen = new Handler();
    private Runnable timerLockScreen = new Runnable() {

        @Override
        public void run() {
            long longTime = endPressed - startPressed;

            if (longTime > 10000) {
                if (is_Enter_Pressed && !is_Long_Pressed) {
                    if (currentKeyEvent != null) {
                        System.out.println("Enter key long press time = " + longTime);
                        mDialpadFragment.keyLongPressed(currentKeyEvent);
                        currentKeyEvent = null;
                    }

                    is_Enter_Pressed = false;
                    is_Long_Pressed = true;
                    handlerLockScreen.removeCallbacks(timerLockScreen);
                    return;
                }
            } else {
                if (currentPress == endPressed) {
                    if (is_Enter_Pressed && !is_Long_Pressed) {
                        if (currentKeyEvent != null) {
                            System.out.println("Enter key press time = " + longTime);
                            mDialpadFragment.keyPressed(currentKeyEvent);
                            currentKeyEvent = null;
                        }

                        is_Long_Pressed = false;
                        is_Enter_Pressed = false;
                        handlerLockScreen.removeCallbacks(timerLockScreen);
                        return;
                    }
                } else {
                    if (!is_Long_Pressed) {
                        currentPress = endPressed;
                        handlerLockScreen.postDelayed(timerLockScreen, checkAfter/10);
                    } else {
                        is_Long_Pressed = false;
                        is_Enter_Pressed = false;
                    }
                }
            }
        }
    };
// End add

    private KeyEvent currentKeyEvent = null;
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        //This is the filter
//        if (e.getAction()!=KeyEvent.ACTION_DOWN) {
//            return true;
//        }

//        System.out.println("==BCR==onKeyDown - keycode: " + e.getKeyCode() + " | isCanceled: " + e.isCanceled() + " | info: "  + e.toString());
//
        if (e.getKeyCode() == KeyEvent.KEYCODE_7) {
            currentKeyEvent = e;
            //System.out.println("" + is_Enter_Pressed + " - " + is_Long_Pressed);
            if (is_Enter_Pressed == false) {
                // Start log key time down
                startPressed = System.currentTimeMillis();//e.getDownTime();//
                currentPress = System.currentTimeMillis();//e.getDownTime();
                is_Enter_Pressed = true;

                handlerLockScreen.postDelayed(timerLockScreen, checkAfter);
            } else {
                endPressed = System.currentTimeMillis();//e.getDownTime();
            }
        } else {
            if (e.getAction()!=KeyEvent.ACTION_DOWN) {
                return true;
            }
            mDialpadFragment.keyPressed(e);
        }

//        if (e.getAction()==KeyEvent.ACTION_DOWN) {
//            System.out.println("KEYDOWN" + e.getKeyCode() + "");
//        }
//        else if (e.getAction()==KeyEvent.ACTION_UP) {
//            System.out.println("KEYUP" + e.getKeyCode() + "");
//        }
//        Log.d("KEY", e.getKeyCode()+"");
//        return false;

        if (e.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {

        } else if (e.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {

        }

        //prevent default key event
        if (e.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT || e.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT
                || e.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || e.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN
                || e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            //System.out.println(e.toString());
            return true;
        }

        //return false;
        return super.dispatchKeyEvent(e);
    };

    boolean is_Down = false;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Display kDisp = Display.getInstance();
//        android.util.Log.d("Key Event", "keyCode:" + keyCode);
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            return false;
//        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
//            return false;
//        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
//            return false;
//        } else if (keyCode == KeyEvent.KEYCODE_A) {
//            //  tv_bt.setText("F1");
//            kDisp.drawIMG(5, 6*8+4, 5+36, 6*8+4 + 12, Display.IMG_AWTK); // awtk left down key
//            kDisp.drawIMG(5, 6*8+4, 5+36, 6*8+4 + 12, Display.IMG_AWTK_INVERSE); // awtk reverse left down key
//            try {
//                sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            kDisp.drawIMG(5, 6*8+4, 5+36, 6*8+4 + 12, Display.IMG_AWTK); // awtk left down key
//            return false;
//        } else if (keyCode == KeyEvent.KEYCODE_B) {
//            //   tv_bt.setText("F2");
//            kDisp.drawIMG(46,6*8+4, 46+36, 6*8+4 + 12, Display.IMG_BLANK); //blank middle key
//            kDisp.drawIMG(46,6*8+4, 46+36, 6*8+4 + 12, Display.IMG_BLANK_INVERSE); //blank reverse middle key
//            try {
//                sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            kDisp.drawIMG(46,6*8+4, 46+36, 6*8+4 + 12, Display.IMG_BLANK); //blank middle key
//            return false;
//        } else if (keyCode == KeyEvent.KEYCODE_C) {
//            // tv_bt.setText("F3");
//            kDisp.drawIMG(87, 6*8+4, 87+36, 6*8+4 + 12, Display.IMG_TALK); //talk right down key
//            kDisp.drawIMG(87, 6*8+4, 87+36, 6*8+4 + 12, Display.IMG_TALK_INVERSE); //talk reverse right down key
//            try {
//                sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            kDisp.drawIMG(87, 6*8+4, 87+36, 6*8+4 + 12, Display.IMG_TALK); //talk right down key
//            return false;
//        } else if (keyCode == KeyEvent.KEYCODE_9) {
//            //  tv_bt.setText("DPAD_UP");
//            int volLevel = Integer.valueOf((String) RKVolFragment.mSpeaker.getText());
//            volLevel++;
//            if (volLevel >= 6)
//                volLevel = 6;
//            kDisp.drawVolume(volLevel);
//            kDisp.drawIMG(46, 1*8+4, 46+36, 1*8+4 + 12, Display.IMG_VOLUME); //vol up key
//            kDisp.drawIMG(46, 1*8+4, 46+36, 1*8+4 + 12, Display.IMG_VOLUME_INVERSE); //vol up key
//            try {
//                sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            kDisp.drawIMG(46, 1*8+4, 46+36, 1*8+4 + 12, Display.IMG_VOLUME); //vol up key
//            return false;
//        } else if (keyCode == KeyEvent.KEYCODE_5) {
//            //   tv_bt.setText("DPAD_DOWN");
//            int volLevel = Integer.valueOf((String) RKVolFragment.mSpeaker.getText());
//            volLevel--;
//            if (volLevel <= 0)
//                volLevel = 0;
//            kDisp.drawVolume(volLevel);
//            kDisp.drawIMG(46, 4*8+4, 46+36, 4*8+4 + 12, Display.IMG_BLANK); //blank down key
//            kDisp.drawIMG(46, 4*8+4, 46+36, 4*8+4 + 12, Display.IMG_BLANK_INVERSE); //blank down key
//            try {
//                sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            kDisp.drawIMG(46, 4*8+4, 46+36, 4*8+4 + 12, Display.IMG_BLANK); //blank down key
//            return false;
//        } else if (keyCode == KeyEvent.KEYCODE_6) {
//            //   tv_bt.setText("DPAD_LEFT");
//            kDisp.drawIMG(10, 3*8, 10+36, 3*8 + 12, Display.IMG_BLANK); //blank left key
//            kDisp.drawIMG(10, 3*8, 10+36, 3*8 + 12, Display.IMG_BLANK_INVERSE); //blank left key
//            try {
//                sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            kDisp.drawIMG(10, 3*8, 10+36, 3*8 + 12, Display.IMG_BLANK); //blank left key
//            return false;
//        } else if (keyCode == KeyEvent.KEYCODE_8) {
//            //   tv_bt.setText("DPAD_RIGHT");
//            kDisp.drawIMG(82, 3*8, 82+36, 3*8 + 12, Display.IMG_MENU); //menu right key
//            kDisp.drawIMG(82, 3*8, 82+36, 3*8 + 12, Display.IMG_MENU_INVERSE); //menu reverse right key
//            try {
//                sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            kDisp.drawIMG(82, 3*8, 82+36, 3*8 + 12, Display.IMG_MENU); //menu right key
//            return false;
//        } else if (keyCode == KeyEvent.KEYCODE_7) {
//            //   tv_bt.setText("DPAD_CENTER");
//            kDisp.drawIMG(46, 3*8, 46+36, 3*8 + 12, Display.IMG_BLANK); //blank center key
//            kDisp.drawIMG(46, 3*8, 46+36, 3*8 + 12, Display.IMG_BLANK_INVERSE); //blank center key
//            try {
//                sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            kDisp.drawIMG(46, 3*8, 46+36, 3*8 + 12, 0); //blank center key
//            return false;
//        } else if (keyCode == KeyEvent.KEYCODE_3) {
//            //  tv_bt.setText("1");
//            kDisp.showString(60,64,"1");
//            return false;
//        } else if (keyCode == KeyEvent.KEYCODE_4) {
//            //   tv_bt.setText("2");
//            kDisp.showString(60,64,"2");
//            return false;
//        } else if (keyCode == KeyEvent.KEYCODE_1) {
//            //  tv_bt.setText("3");
//            kDisp.showString(60,64,"3");
//            return false;
//        } else if (keyCode == KeyEvent.KEYCODE_2) {
//            // tv_bt.setText("4");
//            kDisp.showString(60,64,"4");
//            return false;
//        }
//        if (event.getKeyCode() == KeyEvent.ACTION_MULTIPLE) {
//            System.out.println("ACTION_DOWN >> " + event.toString());
//        }
//        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//            is_Down = true;
//            System.out.println("ACTION_DOWN >> " + event.toString());
//            return true;
//        } else {
//            lockThread.lock();
//            is_Down = false;
//            lockThread.unlock();
//        }
        return super.onKeyDown(keyCode, event);
    }

    //定义onKeyDown(int keyCode, KeyEvent event)来监听用户的触摸单击事件
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

//    @Override
//    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
//        System.out.println("onKeyLongPress");
//        return super.onKeyLongPress(keyCode, event);
//    }
//
//    @Override
//    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
//        System.out.println("onKeyMultiple");
//        return super.onKeyMultiple(keyCode, repeatCount, event);
//    }
}
