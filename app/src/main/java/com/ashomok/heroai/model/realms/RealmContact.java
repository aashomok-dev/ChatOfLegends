package com.ashomok.heroai.model.realms;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

/**
 * Created by Devlomi on 17/01/2018.
 */


//Contact to send or receive
public class RealmContact extends RealmObject implements Parcelable {
    public static final Parcelable.Creator<RealmContact> CREATOR = new Parcelable.Creator<RealmContact>() {
        public RealmContact createFromParcel(Parcel source) {
            return new RealmContact(source);
        }

        public RealmContact[] newArray(int size) {
            return new RealmContact[size];
        }
    };
    //contact name
    private String name;
    //list of phoneNumber of the contact

    public RealmContact(){}

    protected RealmContact(Parcel in) {
        name = in.readString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }
}
