package br.com.hojeehpeixe.android;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import br.com.hojeehpeixe.services.android.exceptions.CardapioException;
import br.com.hojeehpeixe.services.android.exceptions.UpDownException;
import br.com.hojeehpeixe.services.android.CardapioService;
import br.com.hojeehpeixe.services.android.UpDownService;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class AtualizaCardapio extends Thread 
{
	
	private PeixeActivity activity;
	private boolean exibirVotacao;
	private Dialog dialog;

	/**
	 * Esta Thread existe para podermos atualizar o cardápio mostrando uma
	 * ProgressDialog
	 * @param a
	 * @param e
	 * @param d
	 */
	public AtualizaCardapio(PeixeActivity a, boolean e, Dialog d) {
		activity = a;
		exibirVotacao = e;
		dialog = d;
	}

	/**
	 * Aqui vai o processamento demorado de atualização de cardápios
	 * 
	 */
	public void run() {
		Message handlerMessage = new Message();
		try {
			CardapioService cardapioService = CardapioService
					.getInstance();
			// aqui manda como se não houvesse cache
			cardapioService.updateCardapios(activity, null);
			if (exibirVotacao) {
				PeixeActivity.zeraAllJaPegou();
				PeixeActivity.zeraAllUpDown();
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
			handlerMessage.what = 0;
			handlerMessage.obj = cardapioService.message;
		} catch (CardapioException e) {
			handlerMessage.what = 1;
			handlerMessage.obj = e.getMessage();
		}
		handler.sendMessage(handlerMessage);

	}

	
	/**
	 * As modificações da Interface só podem ser feitas dentro de um Handler!
	 * Aqui recebemos uma mensagem com o resultado da atualização de cardápios,
	 * dispensamos o ProgressDialog "Atualizando...", e mostramos um Toast com
	 * o resultado da atualização.
	 */
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			CardapioService cardapioService = CardapioService.getInstance();
			dialog.dismiss();
			if (msg.what == 0) {
				activity.populaCardapios();
				activity.setUpDown();
			}
			//Toast.makeText(activity, (String)msg.obj, Toast.LENGTH_SHORT).show();

			if (cardapioService.noConnection && cardapioService.isCacheAtual()) {
				AlertDialog alertDialog = new AlertDialog.Builder(activity).create();  
			    alertDialog.setTitle("Atenção!");  
			    alertDialog.setMessage(activity.getString(R.string.comCacheSemConexao));  
			    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {  
			      public void onClick(DialogInterface dialog, int which) {  
			        return;  
			    } });   

			    alertDialog.show();
			} else if (cardapioService.message != null) {
				Toast.makeText(activity, cardapioService.message,Toast.LENGTH_LONG).show();
			}
		};

	};
}
