package pcs2420.app.peixe;

import java.io.IOException;

import br.com.hojeehpeixe.services.android.exceptions.MensagemException;
import br.com.hojeehpeixe.services.android.Mensagem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

public class MensagemActivity extends Activity {

	private Mensagem mensagem;
	public String erro;

	public MensagemActivity() {
		mensagem = new Mensagem();
		erro = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.mensagem);

		Spinner tipoSpinner = (Spinner) findViewById(R.id.tipoSpinner);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ArrayAdapter tipoArrayAdapter = new ArrayAdapter(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.tipos));
		tipoSpinner.setAdapter(tipoArrayAdapter);

		Spinner restauranteSpinner = (Spinner) findViewById(R.id.restauranteSpinner);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ArrayAdapter restauranteArrayAdapter = new ArrayAdapter(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.restaurantes));

		restauranteSpinner.setAdapter(restauranteArrayAdapter);

		setEventListeners();
	}

	public void setEventListeners() {
		Button enviar = (Button) findViewById(R.id.botaoEnviar);
		enviar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				enviarMensagem();
				Intent myIntent = new Intent(view.getContext(),
						PeixeActivity.class);
				startActivityForResult(myIntent, 0);
			}
		});
	}

	public void enviarMensagem() {
		EditText emailTextView = (EditText) findViewById(R.id.email);
		mensagem.setEmail(emailTextView.getText().toString());

		Spinner tipoSpinner = (Spinner) findViewById(R.id.tipoSpinner);
		mensagem.setTipo(tipoSpinner.getSelectedItem().toString());

		Spinner restauranteSpinner = (Spinner) findViewById(R.id.restauranteSpinner);
		mensagem.setRestaurante(restauranteSpinner.getSelectedItem().toString());

		RadioButton destinoRadioCoseas = (RadioButton) findViewById(R.id.destinoRadioCoseas);
		RadioButton destinoRadioPeixe = (RadioButton) findViewById(R.id.destinoRadioPeixe);
		if (destinoRadioCoseas.isChecked())
			mensagem.setDestino("Coseas");
		else if (destinoRadioPeixe.isChecked())
			mensagem.setDestino("Peixe");
		else
			mensagem.setDestino("Desconhecido");

		EditText ConteudoTextView = (EditText) findViewById(R.id.conteudo);
		mensagem.setConteudo(ConteudoTextView.getText().toString());

		// Envio de email por servidor smtp
		try {
			mensagem.enviar();
		} catch (IOException e) {
			erro = "Não foi possível enviar a preferência devido à problemas com a conexão. Tente novamente mais tarde";
			e.printStackTrace();
		} catch (MensagemException e) {
			erro = e.getMessage();
			e.printStackTrace();
		} catch (Exception e) {
			erro = "Um erro inesperado ocorreu e por isso não foi possível enviar sua mensagem. Tente novamente mais tarde.";
			e.printStackTrace();
		}

		// Caso haja algum problema com o servidor smtp envia pelo aplicativo de
		// email do android
		if (erro != null) {
			// Envio de email por meio do programa de email instalado no android
			final Intent emailIntent = new Intent(
					android.content.Intent.ACTION_SEND);

			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { "hoje.eh.peixe@gmail.com" });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					mensagem.getAssunto());
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					mensagem.getConteudo());
			this.startActivity(Intent
					.createChooser(emailIntent, "Send mail..."));
		}

		// Toast.makeText(this, erro, Toast.LENGTH_LONG).show();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			View view = (View) findViewById(R.id.conteudo);
			Intent myIntent = new Intent(view.getContext(), PeixeActivity.class);
			startActivityForResult(myIntent, 0);
		}
		return super.onKeyDown(keyCode, event);
	}
}
