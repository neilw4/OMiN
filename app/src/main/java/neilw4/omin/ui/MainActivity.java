package neilw4.omin.ui;

import android.app.Activity;
import android.os.Bundle;

import neilw4.omin.Logger;
import neilw4.omin.R;
import neilw4.omin.connection.ConnectionServiceStarter;


public class MainActivity extends Activity {

    public UnameManager unameManager = new UnameManager(this);
    public SendMessageManager sendMessageManager = new SendMessageManager(this);
    public ViewMessageManager viewMessageManager = new ViewMessageManager(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger.setupLog(this);

        ConnectionServiceStarter.start(this);

        unameManager.setup();
        sendMessageManager.setup();
        viewMessageManager.setup();
    }

}
