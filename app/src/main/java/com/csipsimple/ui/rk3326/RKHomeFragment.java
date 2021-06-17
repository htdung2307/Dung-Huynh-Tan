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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import com.csipsimple.R;
import com.poc.display.Display;

import com.poc.display.Display;

import java.util.HashMap;

public class RKHomeFragment extends Fragment {

    private static TextView pageTxt = null;
    private static TextView pttTxt = null;
    private static TextView key_up_Txt = null;
    private static TextView key_down_Txt = null;
    private static TextView key_left_Txt = null;
    private static TextView key_right_Txt = null;
    private static TextView key_f1_Txt = null;
    private static TextView key_f2_Txt = null;
    private static TextView key_f3_Txt = null;
    private Display disp = Display.getInstance();
    private HashMap<Integer, Integer> assignFunc = null;
    //add local variable to only display LCD with updateDisplay
    private boolean mLcdPttUnmute = false;
    private String mLcdPageTxt = "Page 1";

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
        View v = inflater.inflate(R.layout.rk_home, container, false);
        pageTxt = (TextView) v.findViewById(R.id.txt_page_no);
        pttTxt = (TextView) v.findViewById(R.id.txt_ptt);
        key_up_Txt = (TextView) v.findViewById(R.id.txt_key_up);
        key_down_Txt = (TextView) v.findViewById(R.id.txt_key_down);
        key_left_Txt = (TextView) v.findViewById(R.id.txt_key_left);
        key_right_Txt = (TextView) v.findViewById(R.id.txt_key_right);
        key_f1_Txt = (TextView) v.findViewById(R.id.txt_key_f1);
        key_f2_Txt = (TextView) v.findViewById(R.id.txt_key_f2);
        key_f3_Txt = (TextView) v.findViewById(R.id.txt_key_f3);

        v.requestFocus();
        v.clearFocus();

        setTextPage(1);
        drawPTT(false);

        return v;
    }

    public void setTextPage(int page_no) {
        mLcdPageTxt = "Page " + String.valueOf(page_no) + " ";
        pageTxt.setText(mLcdPageTxt);
        disp.showString(10, 1, mLcdPageTxt);
    }
    public void drawPTT(boolean unmute) {
        mLcdPttUnmute = unmute;
        if (unmute)
            pttTxt.setText("PTT");
        else
            pttTxt.setText("");
        disp.drawPtt(mLcdPttUnmute);
    }

    public void drawScreen(HashMap<Integer, Integer> assignFunc) {
        updateDisplay(assignFunc);
    }

    private final String[] mFunctions = {"TALK", "VOL", "GPAG", "PIC", ""};
    private final int[] mFunctions_LCD = {Display.IMG_TALK, Display.IMG_VOLUME, Display.IMG_GPAG, Display.IMG_PIC, Display.IMG_BLANK};
    private void updateDisplay(HashMap<Integer, Integer> assignFunc)
    {
        System.out.println(assignFunc);

        pageTxt.setText(mLcdPageTxt);
        if (mLcdPttUnmute)
            pttTxt.setText("PTT");
        else
            pttTxt.setText("");
        //LCD items should display from top left corner to avoid overlap
        disp.showString(10, 1, mLcdPageTxt);
        disp.drawPtt(mLcdPttUnmute);

        key_up_Txt.setText(mFunctions[assignFunc.get(RKAssignFragment.BUTTON_NUM_UP)]);
        disp.drawIMG(46, 1*8+4, 46+36, 1*8+4 + 12,
                    mFunctions_LCD[assignFunc.get(RKAssignFragment.BUTTON_NUM_UP)]); //vol up key

        key_down_Txt.setText(mFunctions[assignFunc.get(RKAssignFragment.BUTTON_NUM_DOWN)]);
        disp.drawIMG(46, 4*8+4, 46+36, 4*8+4 + 12,
                    mFunctions_LCD[assignFunc.get(RKAssignFragment.BUTTON_NUM_DOWN)]); //blank down key

        key_left_Txt.setText(mFunctions[assignFunc.get(RKAssignFragment.BUTTON_NUM_LEFT)]);
        disp.drawIMG(8, 3*8, 8+36, 3*8 + 12,
                    mFunctions_LCD[assignFunc.get(RKAssignFragment.BUTTON_NUM_LEFT)]); //blank left key

        key_right_Txt.setText("MENU");
        disp.drawIMG(84, 3*8, 84+36, 3*8 + 12,Display.IMG_MENU); //menu right key

        key_f1_Txt.setText(mFunctions[assignFunc.get(RKAssignFragment.BUTTON_NUM_F1)]);
        disp.drawIMG(5, 6*8+4, 5+36, 6*8+4 + 12,
                    mFunctions_LCD[assignFunc.get(RKAssignFragment.BUTTON_NUM_F1)]); //talk left down key

        key_f2_Txt.setText(mFunctions[assignFunc.get(RKAssignFragment.BUTTON_NUM_F2)]);
        disp.drawIMG(46,6*8+4, 46+36, 6*8+4 + 12,
                    mFunctions_LCD[assignFunc.get(RKAssignFragment.BUTTON_NUM_F2)]); //blank middle key

        key_f3_Txt.setText(mFunctions[assignFunc.get(RKAssignFragment.BUTTON_NUM_F3)]);
        disp.drawIMG(87, 6*8+4, 87+36, 6*8+4 + 12,
                    mFunctions_LCD[assignFunc.get(RKAssignFragment.BUTTON_NUM_F3)]); //blank right down key

        //disp.drawIMG(46, 3*8, 46+36, 3*8 + 12, Display.IMG_BLANK); //blank center key
    }
}

