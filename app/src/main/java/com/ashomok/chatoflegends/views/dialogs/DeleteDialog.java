package com.ashomok.chatoflegends.views.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;

import com.ashomok.chatoflegends.R;


//this will show a Confirmation delete dialog
//with a check box if needed to delete the file from storage
public class DeleteDialog extends AlertDialog.Builder {
    //onClickListener callback
    private OnFragmentInteractionListener mListener;
    private final Context context;
    private String mTitle;

    public DeleteDialog(Context context) {
        super(context);
        this.context = context;
    }

    public void setMTitle(String title) {
        mTitle = title;
    }


    @Override
    public AlertDialog show() {

        Resources resources = context.getResources();
        if (null == mTitle)
            setTitle(R.string.delete_messages_confirmation);
        else
            setTitle(mTitle);

        setPositiveButton(R.string.delete, (dialog, which) -> {
            mListener.onPositiveClick();
        });
        setNegativeButton(resources.getString(R.string.cancel).toUpperCase(), null);


        //show the dialog
        return super.show();
    }

    public void setmListener(OnFragmentInteractionListener mListener) {
        this.mListener = mListener;
    }

    public interface OnFragmentInteractionListener {
        void onPositiveClick();
    }

    public interface OnItemClick {
        void onClick(int pos);
    }
}
