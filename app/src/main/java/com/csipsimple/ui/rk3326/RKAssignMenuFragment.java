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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.csipsimple.R;

import com.poc.display.Display;

public class RKAssignMenuFragment extends Fragment {
    public static TextView mAssignMenu = null;
    private static Context mContext = null;
    private static final String THIS_FILE = "RKAssignMenuFragment";

    public static final int MENU_ASSIGN = 0;
    public static final int MENU_BLUETOOTH = 1;
    public static final int MENU_MIC = 2;
    public static final int MENU_MIN = MENU_ASSIGN;
    public static final int MENU_MAX = MENU_MIC;

    private static int mCurrentMenu = MENU_ASSIGN;

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
        View v = inflater.inflate(R.layout.rk_assign_menu, container, false);
        mAssignMenu = (TextView) v.findViewById(R.id.txt_assign_menu);
        mContext = container.getContext();
        return v;
    }

    public void drawScreen() {
        updateDisplay();
    }

    public int keyUp() {
        if (mCurrentMenu >= MENU_MAX) {
            mCurrentMenu = MENU_MIN;
        } else {
            mCurrentMenu++;
        }
        updateDisplay();

        return mCurrentMenu;
    }

    public int keyDown() {
        if (mCurrentMenu == MENU_MIN) {
            mCurrentMenu = MENU_MAX;
        } else {
            mCurrentMenu--;
        }
        updateDisplay();

        return mCurrentMenu;
    }

    public int getCurrentMenu() {
        return mCurrentMenu;
    }

    private void updateDisplay()
    {
        Display disp = Display.getInstance();
        disp.clearLine(3);

        switch (mCurrentMenu) {
            case MENU_ASSIGN:
                mAssignMenu.setText("Assign");
                disp.showString(46, 3, "Assign");
                break;

            case MENU_BLUETOOTH:
                mAssignMenu.setText("BT Pairing");
                disp.showString(36, 3, "BT Pairing");
                break;

            case MENU_MIC:
                mAssignMenu.setText("Mic Volume");
                disp.showString(36, 3, "Mic Volume");
                break;
        }
    }
}
