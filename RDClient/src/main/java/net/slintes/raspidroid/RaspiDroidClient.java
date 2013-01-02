package net.slintes.raspidroid;

import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ToggleButton;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.*;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: slintes
 * Date: 30.12.12
 * Time: 16:37
 *
 */
@EActivity(R.layout.main)
public class RaspiDroidClient extends SherlockActivity {

    public static final String LOGTAG = "raspidroid";

    private MenuItem helpMenu;

    @ViewById
    EditText url;

    @ViewById
    ToggleButton connect;

    @ViewById
    ToggleButton loop;
    private boolean looping;

    @ViewById
    ImageView space;
    private int spaceWidth;
    private int spaceHeight;

    private WSClient ws;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        helpMenu = menu.findItem(R.id.menu_help);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_help:
                return onHelpClick();
            default: return super.onOptionsItemSelected(item);
        }
    }

    private boolean onHelpClick(){
        // TODO
        return true;
    }

    @Click
    void connect(){
        if(connect.isChecked()){
            try {
                ws = new WSClient(new URI(url.getText().toString()));
                ws.connect();
            } catch (Exception e) {
                log("error creating ws client: " + e.getMessage());
            }
        } else {
            if(looping){
                looping = false;
                loop.setChecked(false);
            }
            ws.close();
            ws = null;
        }
    }

    @Click
    void loop(){
        if(loop.isChecked()){
            if(ws != null){
                looping = true;
                loopInBackground();
            }
        }
        else{
            looping = false;
        }
    }

    @Background
    void loopInBackground(){
        log("loopInBackground");

        int x = 0, y = 0;

        while(looping){

            if(y==8){
                y = 0;
                x += 1;
                if(x == 8){
                    x = 0;
                }
            }

            ws.send("" + x + y);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}

            y++;

        }

    }

    @Touch
    void space(MotionEvent me) {
        if(spaceWidth == 0){
            spaceWidth = space.getWidth();
            spaceHeight = space.getHeight();
            log("width: " + spaceWidth + ", height: " + spaceHeight);
        }

        float x = me.getX();
        float y = me.getY();
        int xPos = (int)(x / spaceWidth * 8);
        int yPos = (int)(y / spaceHeight * 8);

        // when touching and holding, y might be > 7 when we reach the Android buttons at the bottom of the screen
        yPos = Math.min(yPos, 7);

        log("xPos: " + xPos + ", yPos: " + yPos);

        if(ws != null){
            ws.send("" + yPos + xPos);
        }

        try {
            Thread.sleep(50); // prevent to fast sending of messages
        } catch (InterruptedException e) {}
    }

    private void log(String msg){
        Log.i(LOGTAG, msg);
    }

}