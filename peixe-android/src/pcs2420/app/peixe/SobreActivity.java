package pcs2420.app.peixe;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SobreActivity extends Activity {
	private TextView textViewVersion;
	private Button buttonMarket;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sobre);
		
		getContent();
		setEventListeners();
		
		try {
			String app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
			textViewVersion.setText("Versão " + app_ver);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	private void getContent() {
		textViewVersion = (TextView)findViewById(R.id.textViewVersion);
		buttonMarket = (Button)findViewById(R.id.buttonMarket);
	}
	
	private void setEventListeners() {
		buttonMarket.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent updateIntent = null;
				updateIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("market://details?id=pcs2420.app.peixe"));
				startActivity(updateIntent); 
			}
		});	
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	if((keyCode == KeyEvent.KEYCODE_BACK))
    	{
    		View view = (View) findViewById(R.id.sobre);
    		
    		Intent myIntent = new Intent(view.getContext(), PeixeActivity.class);
            startActivityForResult(myIntent, 0);
    	}
    	return super.onKeyDown(keyCode, event);
    }
}
