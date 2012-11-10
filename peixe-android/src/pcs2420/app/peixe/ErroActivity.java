package pcs2420.app.peixe;

import pcs2420.app.peixe.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class ErroActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.erro);
		String erro_message = getIntent().getStringExtra("erro");
		((TextView) findViewById(R.id.textViewErro)).setText(erro_message);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menuerro, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Intent intent;
		switch (item.getItemId()) {
		case R.id.itemRefresh:
			intent = new Intent(this, SplashActivity.class);
			startActivity(intent);
			finish();
			return true;
			
		case R.id.itemSobre:
			intent = new Intent(this, SobreActivity.class);
			startActivityForResult(intent, 0);
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
