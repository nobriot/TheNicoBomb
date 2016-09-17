/*
* Small Android App for sending floods or delayed SMS.
* Copyright (C) 2016  Nicolas Obriot
*
* This program is free software: you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the
* Free Software Foundation, either version 3 of the License,
* or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
* Created on Mon Feb 22 20:50:06 2016
* */

package dk.spaceblog.thenicobomb;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by nicolas on 9/14/16.
 */
public class SpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

    private int spinnerType;

    /**
     * @param type : 1 repeat, 2 delay, 3 interval
     */
    SpinnerOnItemSelectedListener(int type) {
        spinnerType = type;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Log.d("Spinner", "OnItemSelect has been triggered");
        int value = 0;

        switch(spinnerType) { /* TODO: THere is probably a smarter way to do that, instead of copying all teh values from the Resources strings manually*/
            case 1 ://Repeat setting
                switch(pos){
                    case 0 : value = 1 ; break;
                    case 1 : value = 2; break;
                    case 2 : value = 5; break;
                    case 3 : value = 10; break;
                    case 4 : value = 20; break;
                    case 5 : value = 50; break;
                    case 6 : value = 100; break;
                    case 7 : value = 500; break;
                }
                MainActivity.mainActivityInstance.setRepeat(value);
                break;
            case 2 ://Delay setting
                switch(pos){
                    case 0 : value = 0 ; break;
                    case 1 : value = 30*1000; break;
                    case 2 : value = 60*1000; break;
                    case 3 : value = 2*60*1000; break;
                    case 4 : value = 5*60*1000; break;
                    case 5 : value = 10*60*1000; break;
                    case 6 : value = 30*60*1000; break;
                    case 7 : value = 60*60*1000; break;
                    case 8 : value = 2*60*60*1000; break;
                    case 9 : value = 5*60*60*1000; break;
                }
                MainActivity.mainActivityInstance.setDelay(value);
                break;
            case 3 ://Interval setting
                switch(pos){
                    case 0 : value = 5*1000 ; break;
                    case 1 : value = 10*1000; break;
                    case 2 : value = 20*1000; break;
                    case 3 : value = 35*1000; break;
                    case 4 : value = 1*60*1000; break;
                    case 5 : value = 2*60*1000; break;
                    case 6 : value = 5*60*1000; break;
                    case 7 : value = 1*60*60*1000; break;
                    case 8 : value = 2*60*60*1000; break;
                    case 9 : value = 5*60*60*1000; break;
                }
                MainActivity.mainActivityInstance.setInterval(value);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

}
