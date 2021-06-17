package com.csipsimple.utils;

import android.os.Environment;
import android.util.Xml;
import com.csipsimple.ui.rk3326.RKAssignFragment;
import com.csipsimple.ui.rk3326.RKAssignFuncFragment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class IpPhoneConfigInfo {
    //mnt/sdcard/Download/
    //final static public String PATH_OF_CONFIG_FILES = "mnt/sdcard/Download/IpPhone";
    final static public String PATH_OF_CONFIG_FILES = "/Download/IpPhone";

    private XmlPullParserFactory parserFactory;
    private XmlPullParser parser;

    private ArrayList<HashMap<String, String>> otherList = null;
    private ArrayList<HashMap<String, String>> networkList = null;
    private ArrayList<HashMap<String, String>> sipList = null;
    private ArrayList<HashMap<String, String>> settingList = null;

    public IpPhoneConfigInfo() { }

    public void initialize() {
        settingList = new ArrayList<>();
        HashMap<String, String> item = new HashMap<>();
        // Current connection group 1～16
        item.put("ConnectGroupCurrent", "1");

        // Volume 0～7
        item.put("SpeakerVolumeCurrent", "1");

        // Key Lock Mode 0(Unlocked)／1(Locked)
        item.put("KeyLockMode", "0");

        // Button assignment
        // 0: Talk (TALK) 1: Volume (VOL) 2: Group selection page switching (GPAG) 3: Pick up (PIC) 4: Do nothing (NONE)
        item.put("Button_UP", "1");     // Key 9
        item.put("Button_DW", "4");     // Key 5
        item.put("Button_LFT", "4");    // Key 6
        item.put("Button_F1", "0");     // Key A
        item.put("Button_F2", "4");     // Key B
        item.put("Button_F3", "4");     // Key C
        item.put("Button_PT1", "3");    // Key D
        item.put("Button_PT2", "0");    // Key E
        item.put("Button_PT3", "1");    // Key F

        // WiFi MAC address 11:22:33:44:55:66
        item.put("WifiMacAddress", "");

        settingList.add(item);
    }

    public boolean parseXMLFromSD(String filename) {
        try {
            File rootDir = Environment.getExternalStorageDirectory().getAbsoluteFile();

            File fileDir = new File(rootDir.getAbsolutePath() + PATH_OF_CONFIG_FILES);
            if (fileDir.exists() == false) {
                return false;
            }

            File file = new File(fileDir, filename);
            //File file = new File(PATH_OF_CONFIG_FILES, filename);
            if (file.exists() == false) {
                return false;
            }

            InputStream inputSource = new FileInputStream(file.getPath());

            //creating a XmlPull parse Factory instance
            parserFactory = XmlPullParserFactory.newInstance();
            parser = parserFactory.newPullParser();

            // setting the namespaces feature to false
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

            // setting the input to the parser
            parser.setInput(inputSource, null);

            if (!readConfig()) {
                return false;
            }

            inputSource.close();
        }
        catch (XmlPullParserException e) {
            e.printStackTrace();
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean addSettings(String filename) {
        File rootDir = Environment.getExternalStorageDirectory().getAbsoluteFile();

        File fileDir = new File(rootDir.getAbsolutePath() + PATH_OF_CONFIG_FILES);
        if (fileDir.exists() == false) {
            return false;
        }

        File file = new File(fileDir, filename);
        RandomAccessFile randomAccessFile = null;
        final boolean fileExists = file.exists();
        String lastLine = null;
        if (fileExists) {
            try {
                randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(0);

                if (null != randomAccessFile) {
                    final Scanner scanner = new Scanner(file);
                    int lastLineOffset = 0;
                    int lastLineLength = 0;

                    while (scanner.hasNextLine()) {
                        // +1 is for end line symbol
                        lastLine = scanner.nextLine();
                        lastLineLength = lastLine.length() + 2;
                        lastLineOffset += lastLineLength;
                    }

                    // don't need last </root> line offset
                    lastLineOffset -= lastLineLength;

                    // got to string before last
                    randomAccessFile.seek(lastLineOffset);
                }
            }
            catch(FileNotFoundException e) {
                Log.e("FileNotFoundException", "can't create FileOutputStream");
            } catch (IOException e) {
                Log.e("IOException", "Failed to find last line");
            }
        } else {
            try {
                file.createNewFile();
            } catch(IOException e) {
                Log.e("IOException", "exception in createNewFile() method");
            }

            try {
                randomAccessFile = new RandomAccessFile(file, "rw");
            } catch(FileNotFoundException e) {
                Log.e("FileNotFoundException", "can't create FileOutputStream");
            }
        }

        //we create a XmlSerializer in order to write xml data
        XmlSerializer serializer = Xml.newSerializer();

        if (randomAccessFile == null) {
            return false;
        }

        try {
            final StringWriter writer = new StringWriter();

            serializer.setOutput(writer);

            if (!fileExists) {
                serializer.startDocument(null, true);
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startTag(null, "IPPhone");
            } else {
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            }

            HashMap<String, String> val = null;
            if (settingList == null) {
                initialize();
                val = settingList.get(0);
            }

            serializer.startTag(null, "Setting");
            // Current connection group 1～16
            serializer.startTag(null, "ConnectGroupCurrent");
            serializer.text(val.get("ConnectGroupCurrent"));
            serializer.endTag(null, "ConnectGroupCurrent");

            // Volume 0～7
            serializer.startTag(null, "SpeakerVolumeCurrent");
            serializer.text(val.get("SpeakerVolumeCurrent"));
            serializer.endTag(null, "SpeakerVolumeCurrent");

            // Key Lock Mode 0(Unlocked)／1(Locked)
            serializer.startTag(null, "KeyLockMode");
            serializer.text(val.get("KeyLockMode"));
            serializer.endTag(null, "KeyLockMode");

            // Button assignment
            // 0: Talk (TALK) 1: Volume (VOL) 2: Group selection page switching (GPAG) 3: Pick up (PIC) 4: Do nothing (NONE)
            serializer.startTag(null, "Button_UP");
            serializer.text(val.get("Button_UP"));
            serializer.endTag(null, "Button_UP");
            serializer.startTag(null, "Button_DW");
            serializer.text(val.get("Button_DW"));
            serializer.endTag(null, "Button_DW");
            serializer.startTag(null, "Button_LFT");
            serializer.text(val.get("Button_LFT"));
            serializer.endTag(null, "Button_LFT");
            serializer.startTag(null, "Button_F1");
            serializer.text(val.get("Button_F1"));
            serializer.endTag(null, "Button_F1");
            serializer.startTag(null, "Button_F2");
            serializer.text(val.get("Button_F2"));
            serializer.endTag(null, "Button_F2");
            serializer.startTag(null, "Button_F3");
            serializer.text(val.get("Button_F3"));
            serializer.endTag(null, "Button_F3");
            serializer.startTag(null, "Button_PT1");
            serializer.text(val.get("Button_PT1"));
            serializer.endTag(null, "Button_PT1");
            serializer.startTag(null, "Button_PT2");
            serializer.text(val.get("Button_PT2"));
            serializer.endTag(null, "Button_PT2");
            serializer.startTag(null, "Button_PT3");
            serializer.text(val.get("Button_PT3"));
            serializer.endTag(null, "Button_PT3");

            // WiFi MAC address 11:22:33:44:55:66
            serializer.startTag(null, "WifiMacAddress");
            serializer.text(val.get("WifiMacAddress"));
            serializer.endTag(null, "WifiMacAddress");

            serializer.endTag(null, "Setting");

            if (!fileExists) {
                serializer.endTag(null, "IPPhone");
            }

            serializer.flush();

            if (lastLine != null) {
                serializer.endDocument();
                writer.append(lastLine);
            }

            // Add \n just for better output in console
            randomAccessFile.writeBytes(writer.toString() + "\n");
            randomAccessFile.close();

            //Toast.makeText(getApplicationContext(), "Save!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("Exception","error occurred while creating xml file");
            e.printStackTrace();
        }

        return true;
    }

    public boolean updateSetting(String key, String value, String filename) {
        //System.out.println("updateSetting");
        File rootDir = Environment.getExternalStorageDirectory().getAbsoluteFile();

        File fileDir = new File(rootDir.getAbsolutePath() + PATH_OF_CONFIG_FILES);
        if (fileDir.exists() == false) {
            return false;
        }

        File file = new File(fileDir, filename);
        if (file.exists() == false) {
            return false;
        }
        String xmlFile = file.getPath();
        //System.out.println(xmlFile);
        try {
            InputStream is = new FileInputStream(file.getPath());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new InputSource(is));
            document.getDocumentElement().normalize();

            NodeList settings = document.getElementsByTagName("Setting");
            Element node = (Element) settings.item(0);

            // Update value
            Element item = (Element) node.getElementsByTagName(key).item(0);
            //System.out.println(item.getTextContent());
            item.setTextContent(value);
            //System.out.println(item.getTextContent());

            saveXMLContent(document, xmlFile);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    private void saveXMLContent(Document document, String xmlFile) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(xmlFile);
            transformer.transform(domSource, streamResult);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    private boolean readConfig()
            throws XmlPullParserException, IOException
    {
        boolean flag = false;
        if (parser != null) {
            int event = parser.getEventType();
            String tag = "" , content = "";
            networkList = new ArrayList<>();
            sipList = new ArrayList<>();
            otherList = new ArrayList<>();
            settingList = new ArrayList<>();
            HashMap<String,String> itemOfNetwork = new HashMap<>();
            HashMap<String,String> itemOfSIP = new HashMap<>();
            HashMap<String,String> itemOfOther = new HashMap<>();
            HashMap<String,String> itemOfSetting = new HashMap<>();
            while (event != XmlPullParser.END_DOCUMENT) {
                tag = parser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        // Get network config info
                        if(tag.equals("Network"))
                            itemOfNetwork = new HashMap<>();
                        // Get SIP config info
                        if(tag.equals("SIP"))
                            itemOfSIP = new HashMap<>();
                        // Get other config info
                        if(tag.equals("Other"))
                            itemOfOther = new HashMap<>();
                        // Get other config info
                        if(tag.equals("Setting"))
                            itemOfSetting = new HashMap<>();
                        break;
                    case XmlPullParser.TEXT:
                        content = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        switch (tag) {

                            case "SSID":
                                itemOfNetwork.put("SSID", content);
                                break;
                            case "Cryptography":
                                itemOfNetwork.put("Cryptography", content);
                                break;
                            case "PSK":
                                itemOfNetwork.put("PSK", content);
                                break;
                            case "IPAddress":
                                itemOfNetwork.put("IPAddress", content);
                                break;
                            case "SubnetMask":
                                itemOfNetwork.put("SubnetMask", content);
                                break;
                            case "DefaltGateway":
                                itemOfNetwork.put("DefaltGateway", content);
                                break;
                            case "DNS1":
                                itemOfNetwork.put("DNS1", content);
                                break;
                            case "DNS2":
                                itemOfNetwork.put("DNS2", content);
                                break;
                            // End of Network Tag
                            case "Network":
                                if(networkList != null)
                                    networkList.add(itemOfNetwork);
                                break;

                            case "TerminalNumber":
                                itemOfSIP.put("TerminalNumber", content);
                                break;
                            case "SIPLocalPort":
                                itemOfSIP.put("SIPLocalPort", content);
                                break;
                            case "SIPTimeout":
                                itemOfSIP.put("SIPTimeout", content);
                                break;
                            case "RTPTimeout":
                                itemOfSIP.put("RTPTimeout", content);
                                break;

                            //    <!--G1-->
                            case "Connect1SIPServerIPAddress":
                                itemOfSIP.put("Connect1SIPServerIPAddress", content);
                                break;
                            case "Connect1SIPServerPort":
                                itemOfSIP.put("Connect1SIPServerPort", content);
                                break;
                            case "Connect1SIPDomain":
                                itemOfSIP.put("Connect1SIPDomain", content);
                                break;
                            case "Connect1User":
                                itemOfSIP.put("Connect1User", content);
                                break;
                            case "Connect1Password":
                                itemOfSIP.put("Connect1Password", content);
                                break;
                            case "Connect1Name":
                                itemOfSIP.put("Connect1Name", content);
                                break;
                            case "Connect1PhoneNumber":
                                itemOfSIP.put("Connect1PhoneNumber", content);
                                break;
                            //    <!--G2-->
                            case "Connect2SIPServerIPAddress":
                                itemOfSIP.put("Connect2SIPServerIPAddress", content);
                                break;
                            case "Connect2SIPServerPort":
                                itemOfSIP.put("Connect2SIPServerPort", content);
                                break;
                            case "Connect2SIPDomain":
                                itemOfSIP.put("Connect2SIPDomain", content);
                                break;
                            case "Connect2User":
                                itemOfSIP.put("Connect2User", content);
                                break;
                            case "Connect2Password":
                                itemOfSIP.put("Connect2Password", content);
                                break;
                            case "Connect2Name":
                                itemOfSIP.put("Connect2Name", content);
                                break;
                            case "Connect2PhoneNumber":
                                itemOfSIP.put("Connect2PhoneNumber", content);
                                break;
                            //    <!--G3-->
                            case "Connect3SIPServerIPAddress":
                                itemOfSIP.put("Connect3SIPServerIPAddress", content);
                                break;
                            case "Connect3SIPServerPort":
                                itemOfSIP.put("Connect3SIPServerPort", content);
                                break;
                            case "Connect3SIPDomain":
                                itemOfSIP.put("Connect3SIPDomain", content);
                                break;
                            case "Connect3User":
                                itemOfSIP.put("Connect3User", content);
                                break;
                            case "Connect3Password":
                                itemOfSIP.put("Connect3Password", content);
                                break;
                            case "Connect3Name":
                                itemOfSIP.put("Connect3Name", content);
                                break;
                            case "Connect3PhoneNumber":
                                itemOfSIP.put("Connect3PhoneNumber", content);
                                break;
                            //    <!--G4-->
                            case "Connect4SIPServerIPAddress":
                                itemOfSIP.put("Connect4SIPServerIPAddress", content);
                                break;
                            case "Connect4SIPServerPort":
                                itemOfSIP.put("Connect4SIPServerPort", content);
                                break;
                            case "Connect4SIPDomain":
                                itemOfSIP.put("Connect4SIPDomain", content);
                                break;
                            case "Connect4User":
                                itemOfSIP.put("Connect4User", content);
                                break;
                            case "Connect4Password":
                                itemOfSIP.put("Connect4Password", content);
                                break;
                            case "Connect4Name":
                                itemOfSIP.put("Connect4Name", content);
                                break;
                            case "Connect4PhoneNumber":
                                itemOfSIP.put("Connect4PhoneNumber", content);
                                break;
                            //    <!--G5-->
                            case "Connect5SIPServerIPAddress":
                                itemOfSIP.put("Connect5SIPServerIPAddress", content);
                                break;
                            case "Connect5SIPServerPort":
                                itemOfSIP.put("Connect5SIPServerPort", content);
                                break;
                            case "Connect5SIPDomain":
                                itemOfSIP.put("Connect5SIPDomain", content);
                                break;
                            case "Connect5User":
                                itemOfSIP.put("Connect5User", content);
                                break;
                            case "Connect5Password":
                                itemOfSIP.put("Connect5Password", content);
                                break;
                            case "Connect5Name":
                                itemOfSIP.put("Connect5Name", content);
                                break;
                            case "Connect5PhoneNumber":
                                itemOfSIP.put("Connect5PhoneNumber", content);
                                break;
                            //    <!--G6-->
                            case "Connect6SIPServerIPAddress":
                                itemOfSIP.put("Connect6SIPServerIPAddress", content);
                                break;
                            case "Connect6SIPServerPort":
                                itemOfSIP.put("Connect6SIPServerPort", content);
                                break;
                            case "Connect6SIPDomain":
                                itemOfSIP.put("Connect6SIPDomain", content);
                                break;
                            case "Connect6User":
                                itemOfSIP.put("Connect6User", content);
                                break;
                            case "Connect6Password":
                                itemOfSIP.put("Connect6Password", content);
                                break;
                            case "Connect6Name":
                                itemOfSIP.put("Connect6Name", content);
                                break;
                            case "Connect6PhoneNumber":
                                itemOfSIP.put("Connect6PhoneNumber", content);
                                break;
                            //    <!--G7-->
                            case "Connect7SIPServerIPAddress":
                                itemOfSIP.put("Connect7SIPServerIPAddress", content);
                                break;
                            case "Connect7SIPServerPort":
                                itemOfSIP.put("Connect7SIPServerPort", content);
                                break;
                            case "Connect7SIPDomain":
                                itemOfSIP.put("Connect7SIPDomain", content);
                                break;
                            case "Connect7User":
                                itemOfSIP.put("Connect7User", content);
                                break;
                            case "Connect7Password":
                                itemOfSIP.put("Connect7Password", content);
                                break;
                            case "Connect7Name":
                                itemOfSIP.put("Connect7Name", content);
                                break;
                            case "Connect7PhoneNumber":
                                itemOfSIP.put("Connect7PhoneNumber", content);
                                break;
                            //    <!--G8-->
                            case "Connect8SIPServerIPAddress":
                                itemOfSIP.put("Connect8SIPServerIPAddress", content);
                                break;
                            case "Connect8SIPServerPort":
                                itemOfSIP.put("Connect8SIPServerPort", content);
                                break;
                            case "Connect8SIPDomain":
                                itemOfSIP.put("Connect8SIPDomain", content);
                                break;
                            case "Connect8User":
                                itemOfSIP.put("Connect8User", content);
                                break;
                            case "Connect8Password":
                                itemOfSIP.put("Connect8Password", content);
                                break;
                            case "Connect8Name":
                                itemOfSIP.put("Connect8Name", content);
                                break;
                            case "Connect8PhoneNumber":
                                itemOfSIP.put("Connect8PhoneNumber", content);
                                break;
                            //    <!--G9-->
                            case "Connect9SIPServerIPAddress":
                                itemOfSIP.put("Connect9SIPServerIPAddress", content);
                                break;
                            case "Connect9SIPServerPort":
                                itemOfSIP.put("Connect9SIPServerPort", content);
                                break;
                            case "Connect9SIPDomain":
                                itemOfSIP.put("Connect9SIPDomain", content);
                                break;
                            case "Connect9User":
                                itemOfSIP.put("Connect9User", content);
                                break;
                            case "Connect9Password":
                                itemOfSIP.put("Connect9Password", content);
                                break;
                            case "Connect9Name":
                                itemOfSIP.put("Connect9Name", content);
                                break;
                            case "Connect9PhoneNumber":
                                itemOfSIP.put("Connect9PhoneNumber", content);
                                break;
                            //    <!--G10-->
                            case "Connect10SIPServerIPAddress":
                                itemOfSIP.put("Connect10SIPServerIPAddress", content);
                                break;
                            case "Connect10SIPServerPort":
                                itemOfSIP.put("Connect10SIPServerPort", content);
                                break;
                            case "Connect10SIPDomain":
                                itemOfSIP.put("Connect10SIPDomain", content);
                                break;
                            case "Connect10User":
                                itemOfSIP.put("Connect10User", content);
                                break;
                            case "Connect10Password":
                                itemOfSIP.put("Connect10Password", content);
                                break;
                            case "Connect10Name":
                                itemOfSIP.put("Connect10Name", content);
                                break;
                            case "Connect10PhoneNumber":
                                itemOfSIP.put("Connect10PhoneNumber", content);
                                break;
                            //    <!--G11-->
                            case "Connect11SIPServerIPAddress":
                                itemOfSIP.put("Connect11SIPServerIPAddress", content);
                                break;
                            case "Connect11SIPServerPort":
                                itemOfSIP.put("Connect11SIPServerPort", content);
                                break;
                            case "Connect11SIPDomain":
                                itemOfSIP.put("Connect11SIPDomain", content);
                                break;
                            case "Connect11User":
                                itemOfSIP.put("Connect11User", content);
                                break;
                            case "Connect11Password":
                                itemOfSIP.put("Connect11Password", content);
                                break;
                            case "Connect11Name":
                                itemOfSIP.put("Connect11Name", content);
                                break;
                            case "Connect11PhoneNumber":
                                itemOfSIP.put("Connect11PhoneNumber", content);
                                break;
                            //    <!--G12-->
                            case "Connect12SIPServerIPAddress":
                                itemOfSIP.put("Connect12SIPServerIPAddress", content);
                                break;
                            case "Connect12SIPServerPort":
                                itemOfSIP.put("Connect12SIPServerPort", content);
                                break;
                            case "Connect12SIPDomain":
                                itemOfSIP.put("Connect12SIPDomain", content);
                                break;
                            case "Connect12User":
                                itemOfSIP.put("Connect12User", content);
                                break;
                            case "Connect12Password":
                                itemOfSIP.put("Connect12Password", content);
                                break;
                            case "Connect12Name":
                                itemOfSIP.put("Connect12Name", content);
                                break;
                            case "Connect12PhoneNumber":
                                itemOfSIP.put("Connect12PhoneNumber", content);
                                break;
                            //    <!--G13-->
                            case "Connect13SIPServerIPAddress":
                                itemOfSIP.put("Connect13SIPServerIPAddress", content);
                                break;
                            case "Connect13SIPServerPort":
                                itemOfSIP.put("Connect13SIPServerPort", content);
                                break;
                            case "Connect13SIPDomain":
                                itemOfSIP.put("Connect13SIPDomain", content);
                                break;
                            case "Connect13User":
                                itemOfSIP.put("Connect13User", content);
                                break;
                            case "Connect13Password":
                                itemOfSIP.put("Connect13Password", content);
                                break;
                            case "Connect13Name":
                                itemOfSIP.put("Connect13Name", content);
                                break;
                            case "Connect13PhoneNumber":
                                itemOfSIP.put("Connect13PhoneNumber", content);
                                break;
                            //    <!--G14-->
                            case "Connect14SIPServerIPAddress":
                                itemOfSIP.put("Connect14SIPServerIPAddress", content);
                                break;
                            case "Connect14SIPServerPort":
                                itemOfSIP.put("Connect14SIPServerPort", content);
                                break;
                            case "Connect14SIPDomain":
                                itemOfSIP.put("Connect14SIPDomain", content);
                                break;
                            case "Connect14User":
                                itemOfSIP.put("Connect14User", content);
                                break;
                            case "Connect14Password":
                                itemOfSIP.put("Connect14Password", content);
                                break;
                            case "Connect14Name":
                                itemOfSIP.put("Connect14Name", content);
                                break;
                            case "Connect14PhoneNumber":
                                itemOfSIP.put("Connect14PhoneNumber", content);
                                break;
                            //    <!--G15-->
                            case "Connect15SIPServerIPAddress":
                                itemOfSIP.put("Connect15SIPServerIPAddress", content);
                                break;
                            case "Connect15SIPServerPort":
                                itemOfSIP.put("Connect15SIPServerPort", content);
                                break;
                            case "Connect15SIPDomain":
                                itemOfSIP.put("Connect15SIPDomain", content);
                                break;
                            case "Connect15User":
                                itemOfSIP.put("Connect15User", content);
                                break;
                            case "Connect15Password":
                                itemOfSIP.put("Connect15Password", content);
                                break;
                            case "Connect15Name":
                                itemOfSIP.put("Connect15Name", content);
                                break;
                            case "Connect15PhoneNumber":
                                itemOfSIP.put("Connect15PhoneNumber", content);
                                break;
                            //    <!--G16-->
                            case "Connect16SIPServerIPAddress":
                                itemOfSIP.put("Connect16SIPServerIPAddress", content);
                                break;
                            case "Connect16SIPServerPort":
                                itemOfSIP.put("Connect16SIPServerPort", content);
                                break;
                            case "Connect16SIPDomain":
                                itemOfSIP.put("Connect16SIPDomain", content);
                                break;
                            case "Connect16User":
                                itemOfSIP.put("Connect16User", content);
                                break;
                            case "Connect16Password":
                                itemOfSIP.put("Connect16Password", content);
                                break;
                            case "Connect16Name":
                                itemOfSIP.put("Connect16Name", content);
                                break;
                            case "Connect16PhoneNumber":
                                itemOfSIP.put("Connect16PhoneNumber", content);
                                break;
                            //    <!--PIC-->
                            case "Connect17SIPServerIPAddress":
                                itemOfSIP.put("Connect17SIPServerIPAddress", content);
                                break;
                            case "Connect17SIPServerPort":
                                itemOfSIP.put("Connect17SIPServerPort", content);
                                break;
                            case "Connect17SIPDomain":
                                itemOfSIP.put("Connect17SIPDomain", content);
                                break;
                            case "Connect17User":
                                itemOfSIP.put("Connect17User", content);
                                break;
                            case "Connect17Password":
                                itemOfSIP.put("Connect17Password", content);
                                break;
                            case "Connect17Name":
                                itemOfSIP.put("Connect17Name", content);
                                break;
                            case "Connect17PhoneNumber":
                                itemOfSIP.put("Connect17PhoneNumber", content);
                                break;
                            // End of SIP Tag
                            case "SIP":
                                if(sipList != null)
                                    sipList.add(itemOfSIP);
                                break;

                            case "BatteryAlarmAudioFile":
                                itemOfOther.put("BatteryAlarmAudioFile", content);
                                break;
                            case "WiFiAlarmAudioFile":
                                itemOfOther.put("WiFiAlarmAudioFile", content);
                                break;
                            case "MicVolume":
                                itemOfOther.put("MicVolume", content);
                                break;
                            case "SpeakerVolume0":
                                itemOfOther.put("SpeakerVolume0", content);
                                break;
                            case "SpeakerVolume1":
                                itemOfOther.put("SpeakerVolume1", content);
                                break;
                            case "SpeakerVolume2":
                                itemOfOther.put("SpeakerVolume2", content);
                                break;
                            case "SpeakerVolume3":
                                itemOfOther.put("SpeakerVolume3", content);
                                break;
                            case "SpeakerVolume4":
                                itemOfOther.put("SpeakerVolume4", content);
                                break;
                            case "SpeakerVolume5":
                                itemOfOther.put("SpeakerVolume5", content);
                                break;
                            case "SpeakerVolume6":
                                itemOfOther.put("SpeakerVolume6", content);
                                break;
                            case "SpeakerVolume7":
                                itemOfOther.put("SpeakerVolume7", content);
                                break;
                            case "ConnectGroup1":
                                itemOfOther.put("ConnectGroup1", content);
                                break;
                            case "ConnectGroup2":
                                itemOfOther.put("ConnectGroup2", content);
                                break;
                            case "ConnectGroup3":
                                itemOfOther.put("ConnectGroup3", content);
                                break;
                            case "ConnectGroup4":
                                itemOfOther.put("ConnectGroup4", content);
                                break;
                            // End of Other Tag
                            case "Other":
                                if(otherList != null)
                                    otherList.add(itemOfOther);
                                break;

                            case "ConnectGroupCurrent":
                                itemOfSetting.put("ConnectGroupCurrent", content);
                                break;
                            case "SpeakerVolumeCurrent":
                                itemOfSetting.put("SpeakerVolumeCurrent", content);
                                break;
                            case "KeyLockMode":
                                itemOfSetting.put("KeyLockMode", content);
                                break;
                            case "Button_UP":
                                itemOfSetting.put("Button_UP", content);
                                break;
                            case "Button_DW":
                                itemOfSetting.put("Button_DW", content);
                                break;
                            case "Button_LFT":
                                itemOfSetting.put("Button_LFT", content);
                                break;
                            case "Button_F1":
                                itemOfSetting.put("Button_F1", content);
                                break;
                            case "Button_F2":
                                itemOfSetting.put("Button_F2", content);
                                break;
                            case "Button_F3":
                                itemOfSetting.put("Button_F3", content);
                                break;
                            case "Button_PT1":
                                itemOfSetting.put("Button_PT1", content);
                                break;
                            case "Button_PT2":
                                itemOfSetting.put("Button_PT2", content);
                                break;
                            case "Button_PT3":
                                itemOfSetting.put("Button_PT3", content);
                                break;
                            case "WifiMacAddress":
                                itemOfSetting.put("WifiMacAddress", content);
                                break;
                            // End of Setting Tag
                            case "Setting":
                                if(settingList != null)
                                    settingList.add(itemOfSetting);
                                break;
                        }
                        break;
                }
                event = parser.next();
            }
            flag = true;
        }
        return flag;
    }

    public ArrayList<HashMap<String, String>> getConfigOther() {
        return otherList;
    }

    public ArrayList<HashMap<String, String>> getConfigNetwork() {
        return networkList;
    }

    public ArrayList<HashMap<String, String>> getConfigSIP() {
        return sipList;
    }

    public ArrayList<HashMap<String, String>> getConfigSetting() {
        return settingList;
    }

}
