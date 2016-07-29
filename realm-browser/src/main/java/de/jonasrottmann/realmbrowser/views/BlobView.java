package de.jonasrottmann.realmbrowser.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewStub;
import android.widget.TextView;

import java.lang.reflect.Field;

import de.jonasrottmann.realmbrowser.R;
import de.jonasrottmann.realmbrowser.utils.Utils;
import io.realm.DynamicRealmObject;
import io.realm.RealmObjectSchema;

public class BlobView extends FieldView {

    private TextView textView;

    public BlobView(Context context, @NonNull RealmObjectSchema realmObjectSchema, @NonNull Field field) {
        super(context, realmObjectSchema, field);
        if (!Utils.isBlob(getField())) throw new IllegalArgumentException();
    }

    @Override
    public void inflateViewStub() {
        ViewStub stub = (ViewStub) findViewById(R.id.realm_browser_stub);
        stub.setLayoutResource(R.layout.realm_browser_fieldview_textview);
        stub.inflate();
    }

    @Override
    public void initViewStubView() {
        textView = (TextView) findViewById(R.id.realm_browser_field_textview);
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void toggleEditMode(boolean enable) {
        super.toggleEditMode(enable);
        textView.setEnabled(false);
    }

    @Override
    public boolean isInputValid() {
        return true;
    }

    @Override
    public void setRealmObject(@NonNull DynamicRealmObject realmObject) {
        if (Utils.isBlob(getField())) {
            textView.setText(Utils.createBlobValueString(realmObject, getField()));
        } else {
            throw new IllegalArgumentException();
        }
    }
}
