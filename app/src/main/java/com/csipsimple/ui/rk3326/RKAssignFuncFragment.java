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
import com.csipsimple.utils.clipboard.Clipboard1;
import com.poc.display.Display;

public class RKAssignFuncFragment extends Fragment {
    public static TextView mAssignFunc = null;
    private static Context mContext = null;
    private static final String THIS_FILE = "RKAssignFuncFragment";
    private final String[] mFunctions = {"TALK", "VOL", "GPAG", "PIC", "NONE"};/*"AWTK",*/

    //public static final int ASSIGN_NUM_AWTK = 0;
    public static final int ASSIGN_NUM_TALK = 0;
    public static final int ASSIGN_NUM_VOL = 1;
    public static final int ASSIGN_NUM_GPAG = 2;
    public static final int ASSIGN_NUM_PIC = 3;
    public static final int ASSIGN_NUM_NONE = 4;

    //public static final int ASSIGN_NUM_MIN = ASSIGN_NUM_AWTK;
    public static final int ASSIGN_NUM_MIN = ASSIGN_NUM_TALK;
    public static final int ASSIGN_NUM_MAX = ASSIGN_NUM_NONE;

    //private static int mFuncIndex = ASSIGN_NUM_AWTK;
    private static int mFuncIndex = ASSIGN_NUM_TALK;
    private static int mAssignedFuncIndex = -1;

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
        View v = inflater.inflate(R.layout.rk_assign_func, container, false);
        mAssignFunc = (TextView) v.findViewById(R.id.txt_assign_func);
        mContext = container.getContext();
        return v;
    }

    public void drawScreen(int assignedFunc) {
        if ( (assignedFunc>=ASSIGN_NUM_MIN) &&  (assignedFunc<=ASSIGN_NUM_MAX)) {
            mAssignedFuncIndex = assignedFunc;
            mFuncIndex = assignedFunc;
        } else {
            mAssignedFuncIndex = -1;
            mFuncIndex = 0;
        }
        updateDisplay();
    }

    public int keyUp() {
        if (mFuncIndex >= ASSIGN_NUM_MAX) {
            mFuncIndex = ASSIGN_NUM_MIN;
        } else {
            mFuncIndex++;
        }

        updateDisplay();

        return mFuncIndex;
    }

    public int keyDown() {
        if (mFuncIndex == ASSIGN_NUM_MIN) {
            mFuncIndex = ASSIGN_NUM_MAX;
        } else {
            mFuncIndex--;
        }
        updateDisplay();

        return mFuncIndex;
    }

    public int keyEnter() {
        mAssignedFuncIndex = mFuncIndex;
        updateDisplay();

        return mFuncIndex;
    }

    private void updateDisplay()
    {
        mAssignFunc.setText(mFunctions[mFuncIndex]);

        Display disp = Display.getInstance();
        disp.clearLine(3);
        disp.showString(36, 3, "SelectFunc");
        disp.clearLine(5);
        disp.showString(46, 5, mFunctions[mFuncIndex]);
        if (mAssignedFuncIndex == mFuncIndex)
            disp.showString(100, 5, "v");
        else
            disp.showString(100, 5, " ");
    }
}
