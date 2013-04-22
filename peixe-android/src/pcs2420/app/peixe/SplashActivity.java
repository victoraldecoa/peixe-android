<<<<<<< HEAD
package pcs2420.app.peixe;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import br.com.hojeehpeixe.services.android.CardapioAsynkService;
import br.com.hojeehpeixe.services.android.CardapioCompleto;
import br.com.hojeehpeixe.services.android.UpDownService;
import br.com.hojeehpeixe.services.android.exceptions.UpDownException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

public class SplashActivity extends Activity {
	private final int tempoEspera = 1500;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		final long first_time = SystemClock.uptimeMillis();
		
		// busca por ups e downs
		final Thread getUpDownThread = (new Thread() {
			public void run() {
				try {
					PeixeActivity.getAllUpDownFromService(UpDownService.getAllUpDown());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				} catch (UpDownException e) {
					e.printStackTrace();
				}
			}
		});
		getUpDownThread.start();

		CardapioAsynkService cardapioService = new CardapioAsynkService(this, new CardapioAsynkService.OnCardapioServiceResponse() {
			
			private String erro_cardapio = null;

			@Override
			public void onResult(CardapioCompleto result) {
				// Espera a outra thread acabar pra iniciar PeixeActivity
				try {
					getUpDownThread.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				// Aguarda passar o tempo de espera
				while (SystemClock.uptimeMillis() - first_time < tempoEspera);
				
				finish();

				Intent i;
				if (erro_cardapio == null) {
					i = new Intent(SplashActivity.this, pcs2420.app.peixe.PeixeActivity.class);
				} else {
					i = new Intent(SplashActivity.this, pcs2420.app.peixe.ErroActivity.class);
					i.putExtra("erro", erro_cardapio);
				}
				
				startActivity(i);
			}
			
			@Override
			public void onError(String error) {
				erro_cardapio = error;
			}
		});
		
		cardapioService.execute();
	}
}
=======
package pcs2420.app.peixe;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import br.com.hojeehpeixe.services.android.CardapioAsynkService;
import br.com.hojeehpeixe.services.android.UpDownService;
import br.com.hojeehpeixe.services.android.exceptions.UpDownException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

public class SplashActivity extends Activity {
	private final int tempoEspera = 1500;
	private Thread getUpDownThread = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		final long first_time = SystemClock.uptimeMillis();
		
		// busca por ups e downs
		boolean exibirVotacao = false;
		//exibirVotacao = getPreferences(Context.MODE_PRIVATE).getBoolean("exibirVotacao", false);
		if (exibirVotacao) {
			getUpDownThread = (new Thread() {
				public void run() {
					try {
						PeixeActivity.getAllUpDownFromService(UpDownService.getAllUpDown());
					} catch (IOException e) {
						e.printStackTrace();
					} catch (XmlPullParserException e) {
						e.printStackTrace();
					} catch (UpDownException e) {
						e.printStackTrace();
					}
				}
			});
			getUpDownThread.start();
		}

		// atualiza os cardápios
		FileInputStream input;

		try {
			input = openFileInput("cardapios.json");
		} catch (FileNotFoundException e) {
			input = null;
		}

		CardapioAsynkService cardapioService = new CardapioAsynkService(this, new CardapioAsynkService.OnCardapioServiceResponse() {
			
			private String erro_cardapio = null;

			@Override
			public void onResult(boolean result) {
				// Espera a outra thread acabar pra iniciar PeixeActivity
				try {
					if (getUpDownThread != null)
						getUpDownThread.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				// Aguarda passar o tempo de espera
				while (SystemClock.uptimeMillis() - first_time < tempoEspera);
				
				finish();

				Intent i;
				if (erro_cardapio == null) {
					i = new Intent(SplashActivity.this, pcs2420.app.peixe.PeixeActivity.class);
				} else {
					i = new Intent(SplashActivity.this, pcs2420.app.peixe.ErroActivity.class);
					i.putExtra("erro", erro_cardapio);
				}
				
				startActivity(i);
			}
			
			@Override
			public void onError(String error) {
				erro_cardapio = error;
			}
		});
		
		FileInputStream[] params = new FileInputStream[1];
		params[0] = input;
		cardapioService.execute(params);
	}
}
>>>>>>> 4812de959420837426e78cb1d33dd1a32c25a178
