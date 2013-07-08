package ru.mainstream.vkstream;
	
import ru.mainstream.vkstream.at.SearchAudiosAsync;

import com.androidquery.AQuery;

import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class CapchaDialog implements OnClickListener {

	private AQuery aq;
	
	private AudioFinder parent;
	private Dialog dialog;
	
	private final String capchaUrl;
	private final String capchaSid;
	private final String query;
	
	ImageView capchaView;
	ProgressBar capchaProgress;
	EditText capchaText;
	
	public CapchaDialog(AudioFinder parent, String capchaUrl, String capchaSid, String query)
	{
		this.parent = parent;
		this.capchaUrl = capchaUrl;
		this.capchaSid = capchaSid;
		this.query = query;
		aq = new AQuery(parent);
		init();
	}
	
	
	private void init()
	{
		dialog = new Dialog(parent);
		dialog.setTitle(parent.getResources().getString(R.string.dialog_capcha_title));
		dialog.setContentView(R.layout.capcha_dialog);
		
		capchaView = (ImageView) dialog.findViewById(R.id.capcha_view);
	    capchaProgress = (ProgressBar) dialog.findViewById(R.id.capcha_progress);
	    capchaText = (EditText) dialog.findViewById(R.id.capcha_text);
	    
	    aq.id(capchaView).progress(capchaProgress).image(capchaUrl, false, false);
	    dialog.findViewById(R.id.capcha_button).setOnClickListener(this);
	    
	}

	public void show()
	{
		dialog.show();
	}
	
	@Override
	public void onClick(View v) {
		if(!capchaText.getText().toString().isEmpty())
		{
			parent.sAudiosAsync = null;
			parent.sAudiosAsync = new SearchAudiosAsync(parent, query, 0, capchaSid, capchaText.getText().toString());
			parent.sAudiosAsync.execute();
			
			dialog.cancel();
		}
	}

}
