package pcs2420.app.peixe;

import br.com.hojeehpeixe.services.android.CardapioAsynkService;
import br.com.hojeehpeixe.services.android.CardapioCompleto;
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
		
		CardapioAsynkService cardapioService = new CardapioAsynkService(this, new CardapioAsynkService.OnCardapioServiceResponse() {
			
			private String erro_cardapio = null;

			@Override
			public void onResult(CardapioCompleto result) {
				// Espera a outra thread acabar pra iniciar PeixeActivity
				
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