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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;
import com.csipsimple.R;
import com.poc.display.Display;

public class RKLockFragment extends Fragment {

    private static TextView pageTxt = null;
    private static TextView pttTxt = null;
    private Display disp = Display.getInstance();
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
        View v = inflater.inflate(R.layout.rk_lock, container, false);
        pageTxt = (TextView) v.findViewById(R.id.txt_page_no);
        pttTxt = (TextView) v.findViewById(R.id.txt_ptt);

        v.requestFocus();
        v.clearFocus();

        setTextPage(1);
        drawPTT(false);

        return v;
    }

    public void setTextPage(int page_no) {
        mLcdPageTxt = "Page " + String.valueOf(page_no) + " ";
        pageTxt.setText("Page " + String.valueOf(page_no));
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

    public void drawScreen() {
        updateDisplay();
    }

    private void updateDisplay() {
        pageTxt.setText(mLcdPageTxt);
        if (mLcdPttUnmute)
            pttTxt.setText("PTT");
        else
            pttTxt.setText("");
        //LCD items should display from top left corner to avoid overlap
        disp.showString(10, 1, mLcdPageTxt);
        disp.drawPtt(mLcdPttUnmute);
        disp.showString(46, 3, "KEY LOCK");
        //Toast.makeText(mContext,"lock",Toast.LENGTH_SHORT).show();
    }

}
