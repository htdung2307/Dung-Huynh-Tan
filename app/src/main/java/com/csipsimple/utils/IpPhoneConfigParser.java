package com.csipsimple.utils;

import android.os.Environment;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class IpPhoneConfigParser {

    final static public String PATH = "/Download/IpPhone";

    private XmlPullParserFactory parserFactory;
    private XmlPullParser parser;

    public IpPhoneConfigParser() { }

    public void load(String filename) {
        try {
            File rootDir = Environment.getExternalStorageDirectory().getAbsoluteFile();
            File myDir = new File(rootDir.getAbsolutePath() + PATH);
            File file = new File(myDir, filename);

            if (file.exists() == false) {
                return;
            }

            InputStream inputSource = new FileInputStream(file.getPath());

            //creating a XmlPull parse Factory instance
            parserFactory = XmlPullParserFactory.newInstance();
            parser = parserFactory.newPullParser();

            // setting the namespaces feature to false
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

            // setting the input to the parser
            parser.setInput(inputSource, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<HashMap<String, String>> getConfigOther() {
        if (parser != null) {
            try {
                int event = parser.getEventType();
                String tag = "" , content = "";
                ArrayList<HashMap<String, String>> itemList = new ArrayList<>();
                HashMap<String,String> item = new HashMap<>();
                while (event != XmlPullParser.END_DOCUMENT) {
                    tag = parser.getName();
                    switch (event){
                        case XmlPullParser.START_TAG:
                            if(tag.equals("Other"))
                                item = new HashMap<>();
                            break;
                        case XmlPullParser.TEXT:
                            content = parser.getText();
                            break;
                        case XmlPullParser.END_TAG:
                            switch (tag){
                                case "BatteryAlarmAudioFile":
                                    item.put("BatteryAlarmAudioFile", content);
                                    break;
                                case "WiFiAlarmAudioFile":
                                    item.put("WiFiAlarmAudioFile", content);
                                    break;
                                case "MicVolume":
                                    item.put("MicVolume", content);
                                    break;
                                case "SpeakerVolume0":
                                    item.put("SpeakerVolume0", content);
                                    break;
                                case "SpeakerVolume1":
                                    item.put("SpeakerVolume1", content);
                                    break;
                                case "SpeakerVolume2":
                                    item.put("SpeakerVolume2", content);
                                    break;
                                case "SpeakerVolume3":
                                    item.put("SpeakerVolume3", content);
                                    break;
                                case "SpeakerVolume4":
                                    item.put("SpeakerVolume4", content);
                                    break;
                                case "SpeakerVolume5":
                                    item.put("SpeakerVolume5", content);
                                    break;
                                case "SpeakerVolume6":
                                    item.put("SpeakerVolume6", content);
                                    break;
                                case "SpeakerVolume7":
                                    item.put("SpeakerVolume7", content);
                                    break;
                                case "ConnectGroup1":
                                    item.put("ConnectGroup1", content);
                                    break;
                                case "ConnectGroup2":
                                    item.put("ConnectGroup2", content);
                                    break;
                                case "ConnectGroup3":
                                    item.put("ConnectGroup3", content);
                                    break;
                                case "ConnectGroup4":
                                    item.put("ConnectGroup4", content);
                                    break;
                                case "Other":
                                    if(item != null)
                                        itemList.add(item);
                                    break;
                            }
                            break;
                    }
                    event = parser.next();
                }
                return itemList;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
