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

package com.csipsimple.widgets;

import android.content.Context;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;

import com.csipsimple.R;
import com.csipsimple.ui.dialpad.DialerFragment;
import com.poc.display.Display; //temporary display PTT here

public class DialerCallBar extends LinearLayout implements OnLongClickListener, View.OnTouchListener {

    private boolean muteflag = true;


    public interface OnDialActionListener {
        /**
         * The make call button has been pressed
         */
        void placeCall();

        /**
         * The video button has been pressed
         */
        void placeVideoCall();
        /**
         * The delete button has been pressed
         */
        void deleteChar();
        /**
         * The delete button has been long pressed
         */
        void deleteAll();

        void endCall() throws RemoteException;

        void mute() throws RemoteException;

        void unmute() throws RemoteException;
    }

    private OnDialActionListener actionListener;

    public DialerCallBar(Context context) {
        this(context, null, 0);
    }

    public DialerCallBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialerCallBar(Context context, AttributeSet attrs, int style) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.dialpad_additional_buttons, this, true);
//        findViewById(R.id.dialVideoButton).setOnClickListener(this);
//        findViewById(R.id.dialButton).setOnClickListener(this);
//        findViewById(R.id.deleteButton).setOnClickListener(this);
//        findViewById(R.id.deleteButton).setOnLongClickListener(this);

//        findViewById(R.id.buttonf1).setOnClickListener(this);
//        findViewById(R.id.buttonf2).setOnClickListener(this);
//        findViewById(R.id.buttonf3).setOnClickListener(this);

        findViewById(R.id.buttonf1).setOnTouchListener(this);
        findViewById(R.id.buttonf2).setOnTouchListener(this);
        findViewById(R.id.buttonf3).setOnTouchListener(this);

        if(getOrientation() == LinearLayout.VERTICAL) {
            LayoutParams lp;
            for(int i=0; i < getChildCount(); i++) {
                lp = (LayoutParams) getChildAt(i).getLayoutParams();
                int w = lp.width;
                lp.width = lp.height;
                lp.height = w;
                lp.gravity = Gravity.CENTER_HORIZONTAL;
                // Added for clarity but not necessary
                getChildAt(i).setLayoutParams(lp);

            }
        }
    }

    /**
     * Set a listener for this widget actions
     * @param l the listener called back when some user action is done on this widget
     */
    public void setOnDialActionListener(OnDialActionListener l) {
        actionListener = l;
    }

    /**
     * Set the action buttons enabled or not
     */
    public void setEnabled(boolean enabled) {
//        findViewById(R.id.dialButton).setEnabled(enabled);
//        findViewById(R.id.dialVideoButton).setEnabled(enabled);
//        findViewById(R.id.deleteButton).setEnabled(enabled);
        findViewById(R.id.buttonf1).setEnabled(enabled);
        findViewById(R.id.buttonf2).setEnabled(enabled);
        findViewById(R.id.buttonf3).setEnabled(enabled);
    }

    /**
     * Set the video capabilities
     * @param enabled whether the client is able to make video calls
     */
    public void setVideoEnabled(boolean enabled) {
        findViewById(R.id.dialVideoButton).setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    }

    public void onPress(View v) {
        Display disp = Display.getInstance();
        if (actionListener != null) {
            int viewId = v.getId();
            if (viewId == R.id.buttonf1) {
                if (!muteflag) {
                    muteflag = true;
                    try {
                        actionListener.mute();
                        //disp.drawPtt(false);    //clear PTT if any
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    muteflag = false;
                    try {
                        actionListener.unmute();
                        //disp.drawPtt(true);    //display PTT on screen
                        //muteflag = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }else if(viewId == R.id.buttonf2) {
                //actionListener.placeCall();
            }else if(viewId == R.id.buttonf3) {
                try {
                    actionListener.unmute();
                    disp.drawPtt(true);    //display PTT on screen
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                muteflag = false;
            }
        }
    }

    public void onRelease(View v) {
        Display disp = Display.getInstance();
        if (actionListener != null) {
            int viewId = v.getId();
            if(viewId == R.id.buttonf3) {
                try {
                    actionListener.mute();
                    disp.drawPtt(false);    //clear PTT if any
                    muteflag = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (actionListener != null) {
            int viewId = v.getId();
            if(viewId == R.id.deleteButton) {
                actionListener.deleteAll();
                v.setPressed(false);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                onRelease(v);
                break;
            case MotionEvent.ACTION_DOWN:
                onPress(v);
                break;
            default:
                break;
        }
        return false;
    }
}
