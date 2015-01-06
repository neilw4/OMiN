package neilw4.omin.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import neilw4.omin.R;
import neilw4.omin.connection.ConnectionServiceStarter;


public class MainActivity extends Activity {

    private UnameManager unameManager = new UnameManager(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectionServiceStarter.start(this);

        unameManager.setup();

    }

}
