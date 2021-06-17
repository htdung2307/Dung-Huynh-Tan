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

package com.csipsimple.ui.rk3326;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.csipsimple.R;
import com.csipsimple.api.ISipService;
import com.csipsimple.api.SipConfigManager;
import com.csipsimple.utils.Log;
import com.poc.display.Display;

import com.poc.display.Display;

public class RKMicFragment extends Fragment {
    public static TextView mMicLevel = null;
    private static Context mContext = null;
    private static final String THIS_FILE = "RKMicFragment";
    private static int mCurrentMicLevel = 0;
    ISipService sipService = null;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View v = inflater.inflate(R.layout.rk_mic, container, false);
        mMicLevel = (TextView) v.findViewById(R.id.txt_mic_level);
        mContext = container.getContext();
        return v;
    }

    public int getCurrentMicLevel() {
        return mCurrentMicLevel;
    }
    public void setCurrentMicLevel(ISipService service, int micLevel) {
        sipService = service;
        mCurrentMicLevel = micLevel;
        sipSetMicLevel(mCurrentMicLevel);
        updateDisplay();
    }

    public void drawScreen() {
        updateDisplay();
    }

    public void keyUp() {
        if (mCurrentMicLevel >= 100) {
            /* do nothing, maximum */
        } else {
            mCurrentMicLevel++;
            sipSetMicLevel(mCurrentMicLevel);
        }
        updateDisplay();

        //return mCurrentMicLevel;
    }

    public void keyDown() {
        if (mCurrentMicLevel == 0) {
            /* do nothing, minimum */
        } else {
            mCurrentMicLevel--;
            sipSetMicLevel(mCurrentMicLevel);
        }
        updateDisplay();

        //return mCurrentMicLevel;
    }

    private void updateDisplay()
    {
        mMicLevel.setText(String.valueOf(mCurrentMicLevel));

        Display disp = Display.getInstance();
        disp.clearLine(3);
        disp.showString(36, 3, "MIC Volume");
        disp.clearLine(5);
        disp.showString(46, 5, String.valueOf(mCurrentMicLevel));
    }

    /* set micro level, range from 0 to 100, return 0 if success */
    private int sipSetMicLevel(int micLevel) {
        int result = -1;
        Float micLevelFloat = (float) micLevel/100.0f;
        boolean useBT = false;

        if (sipService != null) {
            try {
                useBT = sipService.getCurrentMediaState().isBluetoothScoOn;
            } catch (RemoteException e) {
                Log.e(THIS_FILE, "Sip service not avail for request ", e);
            }
        } else {
            Log.d(THIS_FILE, "sipSetMicLevel sipService==null");
            //return 0;
        }

        try {
            String key;
            if (sipService != null) {
                sipService.confAdjustRxLevel(0, micLevelFloat);
            }
            if (mContext != null) {
                key =  useBT ? SipConfigManager.SND_BT_MIC_LEVEL : SipConfigManager.SND_MIC_LEVEL;
                SipConfigManager.setPreferenceFloatValue(mContext, key, micLevelFloat);
                result = 0;
            } else {
                Log.d(THIS_FILE, "sipGetMicroLevel mContext == null");
            }
        } catch (RemoteException e) {
            Log.e(THIS_FILE, "Impossible to set mic/ level", e);
        }

        return result;
    }

    /* get micro level, range from 0 to 100 */
    private int sipGetMicLevel() {
        int micLevel = 0;
        Float micLevelFloat;
        boolean useBT = false;
        if (sipService != null) {
            try {
                useBT = sipService.getCurrentMediaState().isBluetoothScoOn;
            } catch (RemoteException e) {
                Log.e(THIS_FILE, "Sip service not avail for request ", e);
            }
        }else {
            Log.d(THIS_FILE, "sipSetMicLevel sipService==null");
            //return 0;
        }

        if (mContext != null) {
            micLevelFloat = SipConfigManager.getPreferenceFloatValue(mContext, useBT ?
                    SipConfigManager.SND_BT_MIC_LEVEL : SipConfigManager.SND_MIC_LEVEL);
            micLevelFloat = micLevel * 100.0f;
            micLevel = micLevelFloat.intValue();
        } else {
            Log.d(THIS_FILE, "sipGetMicroLevel mContext == null");
        }

        return micLevel;
    }
}
