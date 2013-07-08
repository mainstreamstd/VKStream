package com.perm.kate.api;

import org.json.JSONException;
import org.json.JSONObject;


import android.os.Parcel;
import android.os.Parcelable;

public class Audio implements Parcelable {
    public long aid;
    public long owner_id;
    public String artist;
    public String title;
    public long duration;
    public String url;
    public Long lyrics_id;
    
    
    public Audio() {}
    
    public static Audio parse(JSONObject o) throws NumberFormatException, JSONException{
        Audio audio = new Audio();
        audio.aid = Long.parseLong(o.getString("aid"));
        audio.owner_id = Long.parseLong(o.getString("owner_id"));
        if(o.has("performer"))
            audio.artist = Api.unescape(o.getString("performer"));
        else if(o.has("artist"))
            audio.artist = Api.unescape(o.getString("artist"));
        audio.title = Api.unescape(o.getString("title"));
        audio.duration = Long.parseLong(o.getString("duration"));
        audio.url = o.optString("url", null);
        
        String tmp=o.optString("lyrics_id");
        if(tmp!=null && !tmp.equals(""))//otherwise lyrics_id=null 
            audio.lyrics_id = Long.parseLong(tmp);
        return audio;
    }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(aid);
		dest.writeLong(owner_id);
		dest.writeString(artist);
		dest.writeString(title);
		dest.writeLong(duration);
		dest.writeString(url);
		//dest.writeLong(lyrics_id);
		
	}
	
	 public static final Parcelable.Creator<Audio> CREATOR = new Parcelable.Creator<Audio>() {
		    
		    public Audio createFromParcel(Parcel in) {
		      return new Audio(in);
		    }

		    public Audio[] newArray(int size) {
		      return new Audio[size];
		    }
		  };
		  
		  private Audio(Parcel parcel) {
			  
			 aid = parcel.readLong();
			 owner_id = parcel.readLong();
			 artist = parcel.readString();
			 title = parcel.readString();
			 duration = parcel.readLong();
			 url = parcel.readString();
			// lyrics_id = parcel.readLong();
				
	      }
}