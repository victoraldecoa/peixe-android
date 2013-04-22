package pcs2420.app.peixe;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import br.com.hojeehpeixe.services.android.exceptions.UpDownException;
import br.com.hojeehpeixe.services.android.CardapioAsynkService;
import br.com.hojeehpeixe.services.android.UpDownService;
import android.app.Dialog;
import android.os.Handler;
import android.os.Message;

public class AtualizaUpDown extends Thread 
{
	
	private static PeixeActivity activity;
	private static CardapioAsynkService cardapioAsynkService;
	private static Dialog dialog;
	private static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				activity.setUpDown();
			}
			
			if (!cardapioAsynkService.isAlive()) {
				dialog.dismiss();
			}
		};
	};

	/**
	 * Esta Thread existe para podermos atualizar o cardápio mostrando uma
	 * ProgressDialog
	 * @param a
	 * @param cardapioAsynkService 
	 * @param e
	 * @param d
	 */
	public AtualizaUpDown(PeixeActivity a, CardapioAsynkService cardapioAsynkService, Dialog dialog) {
		activity = a;
		AtualizaUpDown.dialog = dialog;
		AtualizaUpDown.cardapioAsynkService = cardapioAsynkService;
	}

	/**
	 * Aqui vai o processamento demorado de atualização de ups e downs
	 * 
	 */
	public void run() {
		Message handlerMessage = new Message();
		
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
		
		handlerMessage.what = 0;
		handler.sendMessage(handlerMessage);
	}
}
