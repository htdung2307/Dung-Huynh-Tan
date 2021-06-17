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
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.widget.Toast;
import com.csipsimple.R;
import com.csipsimple.api.ISipService;

import org.w3c.dom.Text;

import com.poc.display.Display;

public class RKVolFragment extends Fragment {
    public static TextView mSpeaker = null;
    private static Context mContext;
    private static final String THIS_FILE = "RKVolFragment";

    private static int mCurrentVolume = 0;

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
        View v = inflater.inflate(R.layout.rk_vol, container, false);
        mSpeaker = (TextView) v.findViewById(R.id.txt_speaker);
        mContext = container.getContext();
        if (mContext != null) {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            mCurrentVolume = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
        } else {
            Log.d(THIS_FILE, "onCreateView mContext == null");
        }
        drawScreen();
        return v;
    }

    /*!
     *  Increase volume then return increased volume index level
     */
    public int keyUp() {
        //Change system volume
        if (mContext == null) {
            mContext = getActivity().getApplicationContext();
            if (mContext == null) {
                Log.d(THIS_FILE, "mContext == null");
                return -1;
            }
        }

        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mCurrentVolume = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
        if (mCurrentVolume < 7) { //7 is maximum index value
            mCurrentVolume = mCurrentVolume + 1;
        }
        am.setStreamVolume(AudioManager.STREAM_SYSTEM, mCurrentVolume, AudioManager.FLAG_PLAY_SOUND);

        updateDisplay();

        return mCurrentVolume;
    }

    /*!
     *  Increase volume then return increased volume index level
     */
    public int keyDown() {
        //Change system volume
        if (mContext == null) {
            mContext = getActivity().getApplicationContext();
            if (mContext == null) {
                Log.d(THIS_FILE, "mContext == null");
                return -1;
            }
        }
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mCurrentVolume = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
        if (mCurrentVolume >= 1) { //0 is min index value
            mCurrentVolume = mCurrentVolume - 1;
        }
        am.setStreamVolume(AudioManager.STREAM_SYSTEM, mCurrentVolume, AudioManager.FLAG_PLAY_SOUND);

        updateDisplay();

        return mCurrentVolume;
    }

    public void drawScreen() {
        if (mContext == null) {
            mContext = getActivity().getApplicationContext();
            if (mContext == null) {
                Log.d(THIS_FILE, "mContext == null");
                return;
            }
        }
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mCurrentVolume = am.getStreamVolume(AudioManager.STREAM_SYSTEM);
        updateDisplay();
    }

    private void updateDisplay()
    {
        mSpeaker.setText(String.valueOf(mCurrentVolume));

        Display disp = Display.getInstance();
        disp.clearLine(3);
        disp.showString(46, 3, "VOL: " + String.valueOf(mCurrentVolume));
        disp.drawVolume(mCurrentVolume);
    }
}


