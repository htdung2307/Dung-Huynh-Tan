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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.csipsimple.R;

import com.poc.display.Display;

public class RKAssignFragment extends Fragment {
    public static TextView mSelectBtn = null;
    private static Context mContext = null;
    private static final String THIS_FILE = "RKAssignFragment";
    private final String[] mButtons = {"UP", "DOWN", "LEFT", "F1", "F2", "F3", "PT1", "PT2", "PT3"};

    public static final int BUTTON_NUM_UP = 0;
    public static final int BUTTON_NUM_DOWN = 1;
    public static final int BUTTON_NUM_LEFT = 2;
    public static final int BUTTON_NUM_F1 = 3;
    public static final int BUTTON_NUM_F2 = 4;
    public static final int BUTTON_NUM_F3 = 5;
    public static final int BUTTON_NUM_PT1 = 6;
    public static final int BUTTON_NUM_PT2 = 7;
    public static final int BUTTON_NUM_PT3 = 8;


    public static final int BUTTON_NUM_MIN = BUTTON_NUM_UP;
    public static final int BUTTON_NUM_MAX = BUTTON_NUM_PT3;

    private static int mBtnIndex = BUTTON_NUM_UP;

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
        View v =  inflater.inflate(R.layout.rk_assign, container, false);
        mSelectBtn = (TextView) v.findViewById(R.id.txt_select_btn);
        mContext = container.getContext();
        return v;
    }

    public void drawScreen() {
        updateDisplay();
    }

    public int keyUp() {
        if (mBtnIndex >= BUTTON_NUM_MAX) {
            mBtnIndex = BUTTON_NUM_MIN;
        } else {
            mBtnIndex++;
        }

        updateDisplay();

        return mBtnIndex;
    }

    public int keyDown() {
        if (mBtnIndex == BUTTON_NUM_MIN) {
            mBtnIndex = BUTTON_NUM_MAX;
        } else {
            mBtnIndex--;
        }
        updateDisplay();

        return mBtnIndex;
    }

    private void updateDisplay()
    {
        mSelectBtn.setText(mButtons[mBtnIndex]);

        Display disp = Display.getInstance();
        disp.clearLine(3);
        disp.showString(16, 3, "Assign: SelectBTN");
        disp.clearLine(5);
        disp.showString(46, 5, mButtons[mBtnIndex]);
    }
}
