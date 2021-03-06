package com.xenoage.zong.android;

import static com.xenoage.utils.PlatformUtils.platformUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.xenoage.zong.Zong;
import com.xenoage.zong.android.model.Document;
import com.xenoage.zong.android.scoreslist.ScoresListAdapter;
import com.xenoage.zong.android.R;

public class HomeActivity
	extends Activity
	implements OnItemClickListener {

	ScoresListAdapter adapter = new ScoresListAdapter(this);


	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//init view
		setContentView(R.layout.home);
		((TextView) findViewById(R.id.home_version)).setText("Prototype " + Zong.projectVersion + "." +
			Zong.projectIteration);
		ListView list = ((ListView) findViewById(R.id.home_documents));
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);

		//listener
		findViewById(R.id.home_info).setOnClickListener(view -> {
			Intent intent = new Intent(HomeActivity.this, InfoActivity.class);
			startActivity(intent);
		});

		//init app
		try {
			App.init(this);
		} catch (Throwable t) {
			while (t instanceof ExceptionInInitializerError) {
				t = t.getCause();
			}
			//fallback textview for error messages
			TextView tv = new TextView(this);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_PT, 4);
			tv.setText("ERROR: " + platformUtils().getStackTraceString(t));
			setContentView(tv);
			return;
		}

	}

	@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Document document = adapter.getItem(position);
		Intent intent = new Intent(this, ScoreActivity.class);
		intent.putExtra("filename", document.filename);
		startActivity(intent);
	}

}
