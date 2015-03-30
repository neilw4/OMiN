package neilw4.omin.ui;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;

import neilw4.omin.R;

public class UidProvider extends ActionProvider {

    Context mContext;
    public UidProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View onCreateActionView() {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.provider_uid, null);
        return view;
    }
}
