package pcs2420.app.peixe;

import java.io.IOException;

import br.com.hojeehpeixe.services.android.exceptions.UpDownException;
import br.com.hojeehpeixe.services.android.UpDownService;
import android.app.Dialog;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class EnviaUpDown extends Thread 
{
	
	private PeixeActivity activity;
	private Dialog dialog;
	private String bandejao;
	private int horarioSelecionado;
	private int diaDaSemana;
	boolean up;

	/**
	 * Esta Thread existe para podermos apresentar o dialog enquanto o programa tenta enviar um up down
	 * ProgressDialog
	 * @param a
	 * @param d
	 */
	public EnviaUpDown(PeixeActivity a, Dialog d, String restaurante, int horarioSelecionado, int diaDaSemana, boolean up) {
		
		
		this.activity = a;
		this.dialog = d;
		this.bandejao = restaurante;
		this.horarioSelecionado = horarioSelecionado;
		this.diaDaSemana = diaDaSemana;
		this.up = up;
	}

	/**
	 * Aqui vai o processamento demorado
	 * 
	 */
	public void run() 
	{
		String horarioString = PeixeActivity.ALMOCO_STRING;
		if (horarioSelecionado == 1)
			horarioString = PeixeActivity.JANTA_STRING;

		String upDownString = PeixeActivity.DOWN_STRING;
		if (up)
			upDownString = PeixeActivity.UP_STRING;
		
		Message handlerMessage = new Message();
		
		try {
			UpDownService.upDown(bandejao, horarioString,PeixeActivity.semanaIntToFullString(diaDaSemana),upDownString);
			activity.setJaVotou();
			activity.incrementaUpDown(bandejao,up,horarioSelecionado,diaDaSemana);
			handlerMessage.what = 0;
			handlerMessage.obj = "Voto Computado.";
		} catch (UpDownException e) {
			e.printStackTrace();
			handlerMessage.what = 1;
			handlerMessage.obj = "Não foi possível enviar seu voto. Tente novamente mais tarde. " + e.getMessage();
		} catch (IOException e) {
			handlerMessage.what = 1;
			handlerMessage.obj = "Não foi possível enviar seu voto devido a problemas com a conexão. " + e.getMessage();
			e.printStackTrace();
		}
		
		handler.sendMessage(handlerMessage);

	}

	
	/**
	 * As modificações da Interface só podem ser feitas dentro de um Handler!
	 * Aqui recebemos uma mensagem com o resultado e a apresentamos para o usuário.
	 */
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			dialog.dismiss();
			activity.setUpDown();
			
			if (msg.what == 0)
				Toast.makeText(activity, (String)msg.obj, Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(activity, (String)msg.obj, Toast.LENGTH_LONG).show();

		};

	};
}

