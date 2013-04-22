package pcs2420.app.peixe;

import android.app.TabActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import br.com.hojeehpeixe.services.android.CardapioAsynkService;
import br.com.hojeehpeixe.services.android.CardapioCompleto;
import br.com.hojeehpeixe.services.android.CardapioDia;
import br.com.hojeehpeixe.services.android.UpDownTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Precisa ser TabActivity
 */
public class PeixeActivity extends TabActivity {
	public static final String ALMOCO_STRING = "Almoco";
	public static final String JANTA_STRING = "Janta";
	
	public static final String QUIMICA_STRING = "Quimica";
	public static final String CENTRAL_STRING = "Central";
	public static final String COCESP_STRING = "Prefeitura";
	public static final String PROFESSORES_STRING = "Professores";
	public static final String FISICA_STRING = "Fisica";

	public static final String UP_STRING = "up";
	public static final String DOWN_STRING = "down";

	public static final String HORARIO_SELECIONADO_STRING = "HorarioSelecionado";
	public static final String DATA_SELECIONADA_STRING = "DataSelecionada";

	public static final String EXIBIR_VOTACAO = "exibirVotacao";
	
	// private variables
	
	private static final int ALMOCO = 0;
	private static final int JANTA = 1;

	private static final int QUIMICA = 0;
	private static final int CENTRAL = 1;
	private static final int COCESP = 2;
	private static final int FISICA = 3;
	//private static final int PROFESSORES = 3;


	private static final String ERRO_POPULACARDAPIO = "Não foi possível obter o cardápio, tente novamente mais tarde apertando o botão atualizar no menu.";
	
	private static int colorLaranjaUSP = Color.rgb(252, 180, 33);
	private static int colorAzulUSP = Color.rgb(16, 148, 171);
	
	private static int selectedTab = -1;

	private static SharedPreferences preferencias;
	private ProgressDialog dialog;
	private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	private EnviaUpDown enviaUpDownThread;
	private AtualizaUpDown atualizaCardapioThread;
	
	private CardapioCompleto cardapioCompleto;
	
	private AdView adView;
	private final String MY_AD_UNIT_ID = "a14efdf1f297426";

	/**
	 * Data selecionada em relacao ao dia de hoje (ex: se hoje eh terca, segunda
	 * seria -1);
	 */
	private static int dataSelecionada;

	/**
	 * Almoco = 0 , Janta = 1
	 */
	private static int horarioSelecionado;

	/**
	 * Segunda = 0, Domingo = 6
	 */
	private static int diaDaSemana;

	/**
	 * Abas Habilitadas
	 */
	private Boolean quimicaEnabled = true;
	private Boolean centralEnabled = true;
	private Boolean cocespEnabled = true;
	private Boolean professoresEnabled = false;
	private Boolean fisicaEnabled = false;

	/**
	 * Habilita aquisição e envio de ups e downs
	 */
	private static Boolean votacaoEnabled = false;

	/**
	 * Indica o índice da aba para cada restaurante, esse número é variável pois
	 * é possível habilitar e desabilitar as abas. Os mesmos são populados na
	 * criação da tela
	 */
	private int quimicaTabIndex = -1;
	private int centralTabIndex = -1;
	private int cocespTabIndex = -1;
	private int fisicaTabIndex = -1;
	//private int professoresTabIndex = -1;

	/**
	 * Contagem de ups e downs
	 */
	private static int upQuimicaAlmoco[] = new int[7];
	private static int downQuimicaAlmoco[] = new int[7];
	private static int upCentralAlmoco[] = new int[7];
	private static int downCentralAlmoco[] = new int[7];
	private static int upCocespAlmoco[] = new int[7];
	private static int downCocespAlmoco[] = new int[7];
	private static int upFisicaAlmoco[] = new int[7];
	private static int downFisicaAlmoco[] = new int[7];

	private static int upQuimicaJanta[] = new int[7];
	private static int downQuimicaJanta[] = new int[7];
	private static int upCentralJanta[] = new int[7];
	private static int downCentralJanta[] = new int[7];
	private static int upCocespJanta[] = new int[7];
	private static int downCocespJanta[] = new int[7];
	private static int upFisicaJanta[] = new int[7];
	private static int downFisicaJanta[] = new int[7];

	private static boolean jaPegouUpDownAlmoco[] = new boolean[7];
	private static boolean jaPegouUpDownJanta[] = new boolean[7];

	/*
	 * Elementos da tela
	 */
	private Button botaoMudarHorario;
	private TextView textoHorarioCentral;
	private RelativeLayout barraCentral;

	private TextView textoHorarioCocesp;
	private RelativeLayout barraCocesp;
	
	private TextView textoHorarioQuimica;
	private RelativeLayout barraQuimica;
	
	private TextView textoHorarioFisica;
	private RelativeLayout barraFisica;
	private CardapioAsynkService cardapioAsynkService;
	
	@Override
	protected void onPause()
	{
		super.onPause();
		selectedTab = getSelectedTab();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		cardapioAsynkService = new CardapioAsynkService(this, new PeixeCardapioServiceResponde());
		
		// Não sabemos o motivo, mas as vezes abre o PeixeActivity direto sem o
		// cardápio ter atualizado. Se acontecer isso, volta para a SplashActivity
		if (!CardapioAsynkService.foiAtualizado()) {
			Intent i = new Intent(this, SplashActivity.class);
			Log.d(PeixeActivity.class.getSimpleName(), "Cardápio não atualizado. Voltando para a SplashActivity");
			startActivity(i);
			finish();
			return;
		}
		
		cardapioAsynkService.execute();
		
		inicializaHorario();
		
		getPreferencias();

		// TODO excluir assim que corrigir o bug dos botões up down
		quimicaEnabled = true;
		centralEnabled = true;
		cocespEnabled = true;
		fisicaEnabled = true;
		professoresEnabled = false;

		setData();
		setEventListeners();

		inicializaAbas();
		zeraCardapios();

		setHorario();
		
		//getTabHost().setCurrentTab(0);
		if(selectedTab!=-1)
			pintaTabSelecionada(selectedTab);
		else 
			pintaTabSelecionada(0);
		//setUpDown();
		
		// TODO descomentar no free inicializaPropaganda();
	}
	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
	    //TODO descomentar no free adView.destroy();
	}
	
	private void inicializaPropaganda()
	{

		// Create the adView
	    adView = new AdView(this, AdSize.BANNER, MY_AD_UNIT_ID);

	    // Lookup your LinearLayout assuming it’s been given
	    // the attribute android:id="@+id/mainLayout"
	    LinearLayout layout = (LinearLayout)findViewById(R.id.layoutPropaganda);

	    // Add the adView to it
	    layout.addView(adView);

	    // Initiate a generic request to load it with an ad
	    adView.loadAd(new AdRequest());
	}
	

	private static String semanaIntToString(int dia) {
		switch (dia) {
		case 0:
			return "Seg";
		case 1:
			return "Ter";
		case 2:
			return "Qua";
		case 3:
			return "Qui";
		case 4:
			return "Sex";
		case 5:
			return "Sáb";
		case 6:
			return "Dom";
		default:
			return "Wtf";
		}
	}

	public static String semanaIntToFullString(int dia) {
		switch (dia) {
		case 0:
			return "Segunda";
		case 1:
			return "Terca";
		case 2:
			return "Quarta";
		case 3:
			return "Quinta";
		case 4:
			return "Sexta";
		case 5:
			return "Sabado";
		case 6:
			return "Domingo";
		default:
			return "Wtf";
		}
	}

	/**
	 * Dado um horario, bandejao, data e up/down gera um código hash do dia para
	 * verificar se ja houve votação
	 * 
	 * @param up
	 * @return
	 */
	private static String geraCodigoHashUpDown(String resturante,int dataSelecionada, int horarioSelecionado) {
		String hash = "";

		// Horario
		hash += "h=" + Integer.toString(horarioSelecionado);

		// Aba (restaurante)
		hash += " r=" + resturante;

		// Data
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, dataSelecionada); // number of days to add
		hash += " d=" + dateFormat.format(c.getTime());

		return hash;
	}

	/**
	 * retorna o nome do restaurante selecionado
	 * 
	 * @return
	 */
	private String getCurrentSelectedTab() {
		TabHost mytabs = getTabHost();
		int selectedTab = mytabs.getCurrentTab();

		if (selectedTab == quimicaTabIndex)
			return QUIMICA_STRING;
		if (selectedTab == centralTabIndex)
			return CENTRAL_STRING;
		if (selectedTab == cocespTabIndex)
			return COCESP_STRING;
		if (selectedTab == fisicaTabIndex)
			return FISICA_STRING;

		return PROFESSORES_STRING;

	}

	private void inicializaHorario() {
		DataHoraSelecionados dataHoraSelecionados = (DataHoraSelecionados)getLastNonConfigurationInstance();
		
		if (dataHoraSelecionados != null) {
			dataSelecionada = dataHoraSelecionados.data;
			horarioSelecionado = dataHoraSelecionados.horario;
		} else
		{
			dataSelecionada = 0;
			// Se for mais que 3:00 pm, começa mostrando a janta
			if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 15) {
				horarioSelecionado = JANTA;
			} else {
				horarioSelecionado = ALMOCO;
			}
		}
		textoHorarioCentral = (TextView) findViewById(R.id.textoHorarioCentral);
		barraCentral = (RelativeLayout)findViewById(R.id.barraCentral);

		textoHorarioQuimica = (TextView) findViewById(R.id.textoHorarioQuimica);
		barraQuimica = (RelativeLayout)findViewById(R.id.barraQuimica);
		
		textoHorarioCocesp = (TextView) findViewById(R.id.textoHorarioCocesp);
		barraCocesp = (RelativeLayout)findViewById(R.id.barraCocesp);
		
		textoHorarioFisica = (TextView) findViewById(R.id.textoHorarioFisica);
		barraFisica = (RelativeLayout)findViewById(R.id.barraFisica);
	}
	
	private void setUmHorarioAlmoco(TextView t, RelativeLayout r) {
		t.setText(R.string.Almoco);
		r.setBackgroundColor(getResources().getColor(R.color.usp_orange));
		//t.setTextColor(getResources().getColor(android.R.color.black));
	}
	
	private void setUmHorarioJanta(TextView t, RelativeLayout r) {
		t.setText(R.string.Jantar);
		r.setBackgroundColor(getResources().getColor(R.color.usp_blue));
		//t.setTextColor(getResources().getColor(android.R.color.white));
	}

	private void setHorario() {
		if (horarioSelecionado == 0) {
			botaoMudarHorario.setText(R.string.verJantar);
			
			setUmHorarioAlmoco(textoHorarioCentral, barraCentral);
			setUmHorarioAlmoco(textoHorarioQuimica, barraQuimica);
			setUmHorarioAlmoco(textoHorarioCocesp, barraCocesp);
			setUmHorarioAlmoco(textoHorarioFisica, barraFisica);
		} else {
			botaoMudarHorario.setText(R.string.verAlmoco);

			setUmHorarioJanta(textoHorarioCentral, barraCentral);
			setUmHorarioJanta(textoHorarioQuimica, barraQuimica);
			setUmHorarioJanta(textoHorarioCocesp, barraCocesp);
			setUmHorarioJanta(textoHorarioFisica, barraFisica);
		}
		
	}

	/**
	 * 
	 * @return true se o usuário já votou no dia e horario selecionado
	 */
	private boolean isJaVotou() {
		preferencias = getPreferences(Context.MODE_PRIVATE);
		return preferencias.getBoolean(
				geraCodigoHashUpDown(getCurrentSelectedTab(), dataSelecionada,
						horarioSelecionado), false);
	}

	/**
	 * guarda informacao de que o usuario ja votou naquele dia e horario
	 * 
	 * @param up
	 */
	public void setJaVotou() {
		SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(
				geraCodigoHashUpDown(getCurrentSelectedTab(), dataSelecionada,
						horarioSelecionado), true);
		editor.commit();
	}	
	
	/**
	 * Save UI state changes to the savedInstanceState. This bundle will be
	 * passed to onCreate if the process is killed and restarted.
	 */
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) 
	{
		super.onSaveInstanceState(savedInstanceState);
		
		SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		/*
		 * TODO descomentar apos consertar os botoes up down
		 * editor.putBoolean(QUIMICA_STRING, quimicaEnabled);
		 * editor.putBoolean(CENTRAL_STRING, centralEnabled);
		 * editor.putBoolean(COCESP_STRING, cocespEnabled);
		 * editor.putBoolean(PROFESSORES_STRING, professoresEnabled);
		 */
		
		// TODO descomentar quando o updown tiver funcionando de novo
		//editor.putBoolean(EXIBIR_VOTACAO, votacaoEnabled);
		votacaoEnabled = false;
		
		// editor.putInt(HORARIO_SELECIONADO_STRING,horarioSelecionado);
		// editor.putInt(DATA_SELECIONADA_STRING, dataSelecionada);

		editor.commit();
	}

	private boolean isExibirVotacao() 	{
		preferencias = getPreferences(Context.MODE_PRIVATE);
		// TODO descomentar quando o updown tiver funcionando de novo
		//votacaoEnabled = preferencias.getBoolean(EXIBIR_VOTACAO, true);
		votacaoEnabled = false;
		return votacaoEnabled;
	}

	private void getPreferencias() {
		isExibirVotacao();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Restaura horario para almoço e data para hoje quanto a tela é fechada
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putInt(HORARIO_SELECIONADO_STRING, 0);
			editor.putInt(DATA_SELECIONADA_STRING, 0);
			editor.putBoolean(QUIMICA_STRING, quimicaEnabled);
			editor.putBoolean(CENTRAL_STRING, centralEnabled);
			editor.putBoolean(COCESP_STRING, cocespEnabled);
			editor.putBoolean(FISICA_STRING, fisicaEnabled);
			editor.putBoolean(PROFESSORES_STRING, professoresEnabled);
			editor.putBoolean(EXIBIR_VOTACAO, votacaoEnabled);
			editor.commit();

		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Restore UI state from the savedInstanceState. This bundle has also been
	 * passed to onCreate.
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		getPreferencias();
	}

	private void populaCardapioCentral() {
		// Central
		CardapioDia central;
		if (horarioSelecionado == 0) {
			alteraHorarioCentral("11:00", "14:00");
			central = cardapioCompleto.central.almoco[diaDaSemana];
		} else {
			alteraHorarioCentral("17:30", "19:45");
			central = cardapioCompleto.central.janta[diaDaSemana];
		}

		try
		{
			
			if (central == null){
				alteraPratoCentral(1, "Restaurante fechado");
				alteraPratoCentral(2, "");
				alteraPratoCentral(3, "");
				alteraPratoCentral(4, "");
				alteraPratoCentral(5, "");
				alteraPratoCentral(6, "");
				((TextView) findViewById(R.id.caloriasCentral)).setText("");
			} else if (central.message != null) {
				alteraPratoCentral(1, central.message);
				alteraPratoCentral(2, "");
				alteraPratoCentral(3, "");
				alteraPratoCentral(4, "");
				alteraPratoCentral(5, "");
				alteraPratoCentral(6, "");
				((TextView) findViewById(R.id.caloriasCentral)).setText("");
	
			} else {
				alteraPratoCentral(1, central.mistura);
				alteraPratoCentral(2, central.acompanhamento);
				alteraPratoCentral(3, central.sobremesa);
				alteraPratoCentral(4, central.salada);
				alteraPratoCentral(5, central.opcional);
				alteraPratoCentral(6, "");
				if (central.calorias != null) 
					((TextView) findViewById(R.id.caloriasCentral)).setText(central.calorias + " kcal");
				else
					((TextView) findViewById(R.id.caloriasCentral)).setText("");
			}
		}
		catch (Exception e)
		{
			alteraPratoCentral(1, ERRO_POPULACARDAPIO);
			alteraPratoCentral(2, "");
			alteraPratoCentral(3, "");
			alteraPratoCentral(4, "");
			alteraPratoCentral(5, "");
			alteraPratoCentral(6, "");
			((TextView) findViewById(R.id.caloriasCentral)).setText("");
		}
	}

	private void populaCardapioQuimica() 
	{
		// Se não for sábado nem domingo
		try
		{
			CardapioDia quimica;
			if (horarioSelecionado == 0) {
				quimica = cardapioCompleto.quimica.almoco[diaDaSemana];
				alteraHorarioQuimica("11:00", "14:00");
			} else {
				quimica = cardapioCompleto.quimica.janta[diaDaSemana];
				alteraHorarioQuimica("17:30", "19:45");
			}

			if (quimica != null) {
				if (quimica.message != null) {
					alteraPratoQuimica(1, quimica.message);
					alteraPratoQuimica(2, "");
					alteraPratoQuimica(3, "");
					alteraPratoQuimica(4, "");
					alteraPratoQuimica(5, "");
					alteraPratoQuimica(6, "");
					((TextView) findViewById(R.id.caloriasQuimica)).setText("\n\n");
				} else {
					alteraPratoQuimica(1, quimica.mistura);
					alteraPratoQuimica(2, quimica.acompanhamento);
					alteraPratoQuimica(3, quimica.sobremesa);
					alteraPratoQuimica(4, quimica.salada);
					alteraPratoQuimica(5, "");
					alteraPratoQuimica(6, "");
					if (quimica.calorias != null) 
						((TextView) findViewById(R.id.caloriasQuimica)).setText(quimica.calorias + " kcal");
					else
						((TextView) findViewById(R.id.caloriasQuimica)).setText("");
				}
			} else {
				alteraPratoQuimica(1, "Restaurante fechado\n");
				alteraPratoQuimica(2, "");
				alteraPratoQuimica(3, "");
				alteraPratoQuimica(4, "");
				alteraPratoQuimica(5, "");
				alteraPratoQuimica(6, "");
				((TextView) findViewById(R.id.caloriasQuimica)).setText("");
			}
		}
		catch(Exception e)
		{
			alteraPratoCocesp(1, ERRO_POPULACARDAPIO);
			alteraPratoCocesp(2, "");
			alteraPratoCocesp(3, "");
			alteraPratoCocesp(4, "");
			alteraPratoCocesp(5, "");
			alteraPratoCocesp(6, "");
			((TextView) findViewById(R.id.caloriasCocesp)).setText("");
		}
	}

	private void populaCardapioCocesp() 
	{
		CardapioDia pref;
		if (horarioSelecionado == 0) {
			alteraHorarioCocesp("11:30", "13:50");
			pref = cardapioCompleto.prefeitura.almoco[diaDaSemana];
		} else { 
			alteraHorarioCocesp("17:30", "19:45");
			pref = cardapioCompleto.prefeitura.janta[diaDaSemana];
		}

		// Se não for sábado nem domingo
		try
		{
			if (pref != null) {
				if (pref.message != null) {
					alteraPratoCocesp(1, pref.message);
					alteraPratoCocesp(2, "");
					alteraPratoCocesp(3, "");
					alteraPratoCocesp(4, "");
					alteraPratoCocesp(5, "");
					alteraPratoCocesp(6, "");
					((TextView) findViewById(R.id.caloriasCocesp)).setText("");
				} else {
					alteraPratoCocesp(1, pref.mistura);
					alteraPratoCocesp(2, pref.acompanhamento);
					alteraPratoCocesp(3, pref.sobremesa);
					alteraPratoCocesp(4, pref.salada);
					alteraPratoCocesp(5, pref.opcional);
					alteraPratoCocesp(6, "");
					if (pref.calorias != null) 
						((TextView) findViewById(R.id.caloriasCocesp)).setText(pref.calorias + " kcal");
					else
						((TextView) findViewById(R.id.caloriasCocesp)).setText("");
				}
			} else {
				// Prefeitura
				alteraPratoCocesp(1, "Restaurante fechado");
				alteraPratoCocesp(2, "");
				alteraPratoCocesp(3, "");
				alteraPratoCocesp(4, "");
				alteraPratoCocesp(5, "");
				alteraPratoCocesp(6, "");
				((TextView) findViewById(R.id.caloriasCocesp)).setText("");
			}
		}
		catch(Exception e)
		{
			alteraPratoCocesp(1, ERRO_POPULACARDAPIO);
			alteraPratoCocesp(2, "");
			alteraPratoCocesp(3, "");
			alteraPratoCocesp(4, "");
			alteraPratoCocesp(5, "");
			alteraPratoCocesp(6, "");
			((TextView) findViewById(R.id.caloriasCocesp)).setText("");
		}
	}

	private void populaCardapioProfessores() {
		alteraPratoProfessores(1, getString(R.string.cardapio_nao_disponivel));
		alteraPratoProfessores(2, "");
		alteraPratoProfessores(3, "");
		alteraPratoProfessores(4, "");
		alteraPratoProfessores(5, "");
		alteraPratoProfessores(6, "");
	}
	
	private void populaCardapioFisica() 
	{
		CardapioDia fisica;
		
		try
		{
			// Se é almoço, pega do cardápio almoço
			if (horarioSelecionado == 0) {
				alteraHorarioFisica("11:30", "13:50");
				fisica = cardapioCompleto.fisica.almoco[diaDaSemana];
			}
			// Se é janta, verifica se está entre Segunda e Sexta
			else {
				fisica = cardapioCompleto.fisica.janta[diaDaSemana];
				alteraHorarioFisica("17:30", "19:45");
			} 
	
			if (fisica != null) {
				if (fisica.message != null) {
					alteraPratoFisica(1, fisica.message);
					alteraPratoFisica(2, "");
					alteraPratoFisica(3, "");
					alteraPratoFisica(4, "");
					alteraPratoFisica(5, "");
					alteraPratoFisica(6, "");
					((TextView) findViewById(R.id.caloriasFisica)).setText("");
		
				} else {
					alteraPratoFisica(1, fisica.mistura);
					alteraPratoFisica(2, fisica.acompanhamento);
					alteraPratoFisica(3, fisica.sobremesa);
					alteraPratoFisica(4, fisica.salada);
					alteraPratoFisica(5, fisica.opcional);
					alteraPratoFisica(6, "");
					if (fisica.calorias != null) 
						((TextView) findViewById(R.id.caloriasFisica)).setText(fisica.calorias + " kcal");
					else
						((TextView) findViewById(R.id.caloriasFisica)).setText("");
				}
			} else {
				alteraPratoFisica(1, "Restaurante fechado");
				alteraPratoFisica(2, "");
				alteraPratoFisica(3, "");
				alteraPratoFisica(4, "");
				alteraPratoFisica(5, "");
				alteraPratoFisica(6, "");
				((TextView) findViewById(R.id.caloriasFisica)).setText("");
	
				return;
			}
		}
		catch(Exception e)
		{
			alteraPratoFisica(1, ERRO_POPULACARDAPIO);
			alteraPratoFisica(2, "");
			alteraPratoFisica(3, "");
			alteraPratoFisica(4, "");
			alteraPratoFisica(5, "");
			alteraPratoFisica(6, "");
			((TextView) findViewById(R.id.caloriasFisica)).setText("");
		}
	}

	/**
	 * Pega a lista de cardÃƒÂ¡pios do Web Service e imprime na tela EstÃƒÂ¡
	 * dando erro de "Service not running"
	 */
	void populaCardapios() {

		if (centralEnabled)
			populaCardapioCentral();
		if (cocespEnabled)
			populaCardapioCocesp();
		if (quimicaEnabled)
			populaCardapioQuimica();
		if (professoresEnabled)
			populaCardapioProfessores();
		if (fisicaEnabled)
			populaCardapioFisica();

		setUpDown();
	}

	private void zeraCardapios() {
		for (int i = 1; i < 7; i++) {
			alteraPratoCocesp(i, "");
			alteraPratoQuimica(i, "");
			alteraPratoCentral(i, "");
			alteraPratoProfessores(i, "");
			alteraPratoFisica(i, "");
		}

		((TextView) findViewById(R.id.caloriasProfessores)).setText("");
		((TextView) findViewById(R.id.caloriasCentral)).setText("");
		((TextView) findViewById(R.id.caloriasFisica)).setText("");
		((TextView) findViewById(R.id.caloriasQuimica)).setText("");
		((TextView) findViewById(R.id.caloriasCocesp)).setText("");
		((TextView) findViewById(R.id.horarioCentralAbertura)).setText("");
		((TextView) findViewById(R.id.horarioCentralFechamento)).setText("");
		((TextView) findViewById(R.id.horarioCocespAbertura)).setText("");
		((TextView) findViewById(R.id.horarioCocespFechamento)).setText("");
		((TextView) findViewById(R.id.horarioQuimicaAbertura)).setText("");
		((TextView) findViewById(R.id.horarioQuimicaFechamento)).setText("");
		((TextView) findViewById(R.id.horarioFisicaAbertura)).setText("");
		((TextView) findViewById(R.id.horarioFisicaFechamento)).setText("");
		((TextView) findViewById(R.id.horarioProfessoresAbertura)).setText("");
		((TextView) findViewById(R.id.horarioProfessoresFechamento)).setText("");
	}

	private void inicializaAbas() {
		// Acha o host das abas
		TabHost tabHost = getTabHost();

		int numeroAba = 0;

		// Adiciona as abas uma a uma Indicator Ã¯Â¿Â½ o tÃ¯Â¿Â½tulo content
		// Ã¯Â¿Â½ o arquivo XML que serÃ¯Â¿Â½ o conteudo
		
		if (quimicaEnabled == true) {
			tabHost.addTab(tabHost
					.newTabSpec("tab_quimica")
					.setIndicator("Química",getResources().getDrawable(R.drawable.ic_tab_quimica))
					.setContent(R.id.tabQuimicaLayout));
			quimicaTabIndex = numeroAba;
			numeroAba++;
		}
		if (centralEnabled == true) {
			tabHost.addTab(tabHost
					.newTabSpec("tab_central")
					.setIndicator(
							"Central",
							getResources().getDrawable(
									R.drawable.ic_tab_central))
					.setContent(R.id.tabCentralLayout));
			centralTabIndex = numeroAba;
			numeroAba++;
		}
		if (cocespEnabled == true) {
			tabHost.addTab(tabHost
					.newTabSpec("tab_cocesp")
					.setIndicator(
							"Prefeitura",
							getResources()
									.getDrawable(R.drawable.ic_tab_cocesp))
					.setContent(R.id.tabCocespLayout));
			cocespTabIndex = numeroAba;
			numeroAba++;
		}
		if (fisicaEnabled == true) {
			tabHost.addTab(tabHost
					.newTabSpec("tab_fisica")
					.setIndicator(
							"Fisica",
							getResources()
									.getDrawable(R.drawable.ic_tab_fisica))
					.setContent(R.id.tabFisicaLayout));
			fisicaTabIndex = numeroAba;
			numeroAba++;
		}
		if (professoresEnabled == true) {
			tabHost.addTab(tabHost
					.newTabSpec("tab_clubedosprofessores")
					.setIndicator(
							"Professores",
							getResources().getDrawable(
									R.drawable.ic_tab_professores))
					.setContent(R.id.tabProfessoresLayout));
			//professoresTabIndex = numeroAba;
			numeroAba++;
		}
		
		setEventListenersAbas(tabHost);

	}
	
	private void pintaTabSelecionada(int tabSelecionada)
	{
		
		for(int i=0;i<4;i++)
			getTabHost().getTabWidget().getChildAt(i).setBackgroundColor(Color.BLACK);
		
		if(PeixeActivity.horarioSelecionado==0)
		{
			getTabHost().getTabWidget().getChildAt(tabSelecionada).setBackgroundColor(colorLaranjaUSP);
			//findViewById(R.id.rodape).setBackgroundColor(Color.WHITE);
		}
		else
		{
			getTabHost().getTabWidget().getChildAt(tabSelecionada).setBackgroundColor(colorAzulUSP);
			//findViewById(R.id.rodape).setBackgroundColor(Color.WHITE);
		}
		
	}

	private void setEventListenersAbas(TabHost tabHost) {
		try {
			getTabWidget().getChildAt(0).setOnClickListener(
					new View.OnClickListener() {

						public void onClick(View v) {
							getTabHost().setCurrentTab(0);
							pintaTabSelecionada(0);
							setUpDown();
						}
					});

			getTabWidget().getChildAt(1).setOnClickListener(
					new View.OnClickListener() {

						public void onClick(View v) {
							getTabHost().setCurrentTab(1);
							pintaTabSelecionada(1);
							setUpDown();
						}
					});

			getTabWidget().getChildAt(2).setOnClickListener(
					new View.OnClickListener() {

						public void onClick(View v) {
							getTabHost().setCurrentTab(2);
							pintaTabSelecionada(2);
							setUpDown();
						}
					});

			getTabWidget().getChildAt(3).setOnClickListener(
					new View.OnClickListener() {

						public void onClick(View v) {
							getTabHost().setCurrentTab(3);
							pintaTabSelecionada(3);
							setUpDown();
						}
					});
			
			getTabWidget().getChildAt(4).setOnClickListener(
					new View.OnClickListener() {

						public void onClick(View v) {
							getTabHost().setCurrentTab(4);
							pintaTabSelecionada(4);
							setUpDown();
						}
					});
		} catch (Exception e) {
		}
		;

		tabHost.setCurrentTab(0);
	}

	public void incrementaUpDown(String bandejao, boolean up, int horarioSelecionado, int diaDaSemana) {
		if (horarioSelecionado == ALMOCO) {
			if (up==true) {
				if (bandejao.equalsIgnoreCase(QUIMICA_STRING))
					upQuimicaAlmoco[diaDaSemana]++;
				else if (bandejao.equalsIgnoreCase(CENTRAL_STRING))
					upCentralAlmoco[diaDaSemana]++;
				else if (bandejao.equalsIgnoreCase(FISICA_STRING))
					upFisicaAlmoco[diaDaSemana]++;
				else
					upCocespAlmoco[diaDaSemana]++;
			} else {
				if (bandejao.equalsIgnoreCase(QUIMICA_STRING))
					downQuimicaAlmoco[diaDaSemana]++;
				else if (bandejao.equalsIgnoreCase(CENTRAL_STRING))
					downCentralAlmoco[diaDaSemana]++;
				else if (bandejao.equalsIgnoreCase(FISICA_STRING))
					downFisicaAlmoco[diaDaSemana]++;
				else
					downCocespAlmoco[diaDaSemana]++;
			}
		} else {
			if (up==true) {
				if (bandejao.equalsIgnoreCase(QUIMICA_STRING))
					upQuimicaJanta[diaDaSemana]++;
				else if (bandejao.equalsIgnoreCase(CENTRAL_STRING))
					upCentralJanta[diaDaSemana]++;
				else if (bandejao.equalsIgnoreCase(FISICA_STRING))
					upFisicaJanta[diaDaSemana]++;
				else
					upCocespJanta[diaDaSemana]++;
			} else {
				if (bandejao.equalsIgnoreCase(QUIMICA_STRING))
					downQuimicaJanta[diaDaSemana]++;
				else if (bandejao.equalsIgnoreCase(CENTRAL_STRING))
					downCentralJanta[diaDaSemana]++;
				else if (bandejao.equalsIgnoreCase(FISICA_STRING))
					downFisicaJanta[diaDaSemana]++;
				else
					downCocespJanta[diaDaSemana]++;
			}
		}
	}
	
	private boolean isDiaHorarioValido()
	{
		TabHost mytabs = getTabHost();
		int selectedTab = mytabs.getCurrentTab();

		if (selectedTab == quimicaTabIndex)
		{
			if(diaDaSemana > 4)
				return false;
		}
		else if (selectedTab == centralTabIndex)
		{
			if(diaDaSemana > 4 && horarioSelecionado == JANTA)
				return false;
		}
		else if (selectedTab == cocespTabIndex)
		{
			if(diaDaSemana > 4)
				return false;
		}
		else if (selectedTab == fisicaTabIndex)
		{
			if(cardapioCompleto.fisica.almoco[diaDaSemana] == null)
				return false;
			
			if(diaDaSemana > 4 && horarioSelecionado == JANTA )
				return false;
		}

		return true;
	}

	private void enviaUpDown(String bandejao, boolean up) 
	{

		if (isJaVotou()) {
			Toast.makeText(this, "Você já votou nesse bandejão hoje.",Toast.LENGTH_LONG).show();
			return;
		}
		
		if(isDiaHorarioValido()==false)
		{
			Toast.makeText(this, "Não é possível votar hoje.",Toast.LENGTH_LONG).show();
			return;			
		}

		dialog = ProgressDialog.show(this, "", "Enviando voto... aguarde",
				true, false);

		enviaUpDownThread = new EnviaUpDown(this,dialog,bandejao,horarioSelecionado,diaDaSemana,up);
		enviaUpDownThread.start();

	}

	public static void zeraAllUpDown() {
		for (int i = 0; i < 7; i++) {
			zeraUpDown(ALMOCO, i);
			zeraUpDown(JANTA, i);
		}
	}

	public static void zeraAllJaPegou() {
		for (int i = 0; i < 7; i++) {
			jaPegouUpDownAlmoco[i] = false;
			jaPegouUpDownJanta[i] = false;
		}
	}

	private static void zeraUpDown(int horario, int diaDaSemana) {
		if (horario == ALMOCO) {
			upQuimicaAlmoco[diaDaSemana] = 0;
			upCentralAlmoco[diaDaSemana] = 0;
			upCocespAlmoco[diaDaSemana] = 0;
			downQuimicaAlmoco[diaDaSemana] = 0;
			downCentralAlmoco[diaDaSemana] = 0;
			downCocespAlmoco[diaDaSemana] = 0;
		} else {
			upQuimicaJanta[diaDaSemana] = 0;
			upCentralJanta[diaDaSemana] = 0;
			upCocespJanta[diaDaSemana] = 0;
			downQuimicaJanta[diaDaSemana] = 0;
			downCentralJanta[diaDaSemana] = 0;
			downCocespJanta[diaDaSemana] = 0;
		}
	}
	
	public static void getAllUpDownFromService(int[] resultado) {
		zeraAllJaPegou();

		if (votacaoEnabled == false)
			return;
		
		if (resultado == null) return;

		for (int dia = 0; dia < 7; dia++) {
			jaPegouUpDownAlmoco[dia] = true;
			jaPegouUpDownJanta[dia] = true;

			upQuimicaAlmoco[dia] = resultado[dia * 4 + 28 * QUIMICA];
			downQuimicaAlmoco[dia] = resultado[dia * 4 + 1 + 28 * QUIMICA];
			upQuimicaJanta[dia] = resultado[dia * 4 + 2 + 28 * QUIMICA];
			downQuimicaJanta[dia] = resultado[dia * 4 + 3 + 28 * QUIMICA];

			upCentralAlmoco[dia] = resultado[dia * 4 + 28 * CENTRAL];
			downCentralAlmoco[dia] = resultado[dia * 4 + 1 + 28 * CENTRAL];
			upCentralJanta[dia] = resultado[dia * 4 + 2 + 28 * CENTRAL];
			downCentralJanta[dia] = resultado[dia * 4 + 3 + 28 * CENTRAL];

			upCocespAlmoco[dia] = resultado[dia * 4 + 28 * COCESP];
			downCocespAlmoco[dia] = resultado[dia * 4 + 1 + 28 * COCESP];
			upCocespJanta[dia] = resultado[dia * 4 + 2 + 28 * COCESP];
			downCocespJanta[dia] = resultado[dia * 4 + 3 + 28 * COCESP];
		
			// TODO WS getAllupDown ainda não implementado para fisica tirar o try catch depois
			try {
				upFisicaAlmoco[dia] = resultado[dia * 4 + 28 * FISICA];
				downFisicaAlmoco[dia] = resultado[dia * 4 + 1 + 28 * FISICA];
				upFisicaJanta[dia] = resultado[dia * 4 + 2 + 28 * FISICA];
				downFisicaJanta[dia] = resultado[dia * 4 + 3 + 28 * FISICA];
			}
			catch(Exception e) {
			}
		}
	}

	public void getUpDown(int diaDaSemana) {
		// Não pega ups e downs dos dias futuros, pois não se pode votar neles
		if (dataSelecionada > 0) {
			zeraUpDown(ALMOCO, diaDaSemana);
			zeraUpDown(JANTA, diaDaSemana);
			return;
		}

		if ((jaPegouUpDownAlmoco[diaDaSemana] == false && horarioSelecionado == ALMOCO)
				|| (jaPegouUpDownJanta[diaDaSemana] == false && horarioSelecionado == JANTA)) {
			dialog = ProgressDialog.show(this, "", "Atualizando... aguarde",
					true, false);
			try {
				getAllUpDownFromService(new UpDownTask().execute().get());
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				e1.printStackTrace();
			}
			dialog.dismiss();
		}

	}

	public void setUpDown() {
		TabHost mytabs = getTabHost();
		int selectedTab = mytabs.getCurrentTab();

		Button upQuimicaText = (Button) findViewById(R.id.botaoUpQuimica);
		Button downQuimicaText = (Button) findViewById(R.id.botaoDownQuimica);
		Button upCentralText = (Button) findViewById(R.id.botaoUpCentral);
		Button downCentralText = (Button) findViewById(R.id.botaoDownCentral);
		Button upCocespText = (Button) findViewById(R.id.botaoUpCocesp);
		Button downCocespText = (Button) findViewById(R.id.botaoDownCocesp);
		Button upFisicaText = (Button) findViewById(R.id.botaoUpFisica);
		Button downFisicaText = (Button) findViewById(R.id.botaoDownFisica);

		if (dataSelecionada > 0 || votacaoEnabled == false || isDiaHorarioValido()==false) 
		{
			if (horarioSelecionado == ALMOCO) {
				if (selectedTab == quimicaTabIndex) {
					upQuimicaText.setEnabled(false);
					downQuimicaText.setEnabled(false);
					upQuimicaAlmoco[diaDaSemana] = 0;
					downQuimicaAlmoco[diaDaSemana] = 0;
				} else if (selectedTab == centralTabIndex) {
					upCentralText.setEnabled(false);
					downCentralText.setEnabled(false);
					upCentralAlmoco[diaDaSemana] = 0;
					downCentralAlmoco[diaDaSemana] = 0;
				} else if (selectedTab == cocespTabIndex) {
					upCocespText.setEnabled(false);
					downCocespText.setEnabled(false);
					upCocespAlmoco[diaDaSemana] = 0;
					downCocespAlmoco[diaDaSemana] = 0;
				} else if (selectedTab == fisicaTabIndex) {
					upFisicaText.setEnabled(false);
					downFisicaText.setEnabled(false);
					upFisicaAlmoco[diaDaSemana] = 0;
					downFisicaAlmoco[diaDaSemana] = 0;
				}
			} else {
				if (selectedTab == quimicaTabIndex) {
					upQuimicaText.setEnabled(false);
					downQuimicaText.setEnabled(false);
					upQuimicaJanta[diaDaSemana] = 0;
					downQuimicaJanta[diaDaSemana] = 0;
				} else if (selectedTab == centralTabIndex) {
					upCentralText.setEnabled(false);
					downCentralText.setEnabled(false);
					upCentralJanta[diaDaSemana] = 0;
					downCentralJanta[diaDaSemana] = 0;
				} else if (selectedTab == cocespTabIndex) {
					upCocespText.setEnabled(false);
					downCocespText.setEnabled(false);
					upCocespJanta[diaDaSemana] = 0;
					downCocespJanta[diaDaSemana] = 0;
				} else if (selectedTab == fisicaTabIndex) {
					upFisicaText.setEnabled(false);
					downFisicaText.setEnabled(false);
					upFisicaJanta[diaDaSemana] = 0;
					downFisicaJanta[diaDaSemana] = 0;
				}
			}

		} else {
			upQuimicaText.setEnabled(true);
			downQuimicaText.setEnabled(true);
			upCentralText.setEnabled(true);
			downCentralText.setEnabled(true);
			upCocespText.setEnabled(true);
			downCocespText.setEnabled(true);
			upFisicaText.setEnabled(true);
			downFisicaText.setEnabled(true);

			getUpDown(diaDaSemana);
		}

		// Inivisibiliza todos os botões
		/*
		 * upCocespText.setVisibility(View.INVISIBLE);
		 * downCocespText.setVisibility(View.INVISIBLE);
		 * upCocespText.setVisibility(View.INVISIBLE);
		 * downCocespText.setVisibility(View.INVISIBLE);
		 * upCocespText.setVisibility(View.INVISIBLE);
		 * downCocespText.setVisibility(View.INVISIBLE);
		 */

		// Visibiliza apenas buttons da aba selecionada
		/*
		 * if(getCurrentSelectedTab().equalsIgnoreCase(COCESP_STRING)) {
		 * upCocespText.setVisibility(View.VISIBLE);
		 * downCocespText.setVisibility(View.VISIBLE); } else
		 * if(this.getCurrentSelectedTab().equalsIgnoreCase(CENTRAL_STRING)) {
		 * upCentralText.setVisibility(View.VISIBLE);
		 * downCentralText.setVisibility(View.VISIBLE); } else
		 * if(this.getCurrentSelectedTab().equalsIgnoreCase(QUIMICA_STRING)) {
		 * upQuimicaText.setVisibility(View.VISIBLE);
		 * downQuimicaText.setVisibility(View.VISIBLE); }
		 */

		// Coloca ? em todos os buttons
		upQuimicaText.setText("?");
		downQuimicaText.setText("?");
		upCentralText.setText("?");
		downCentralText.setText("?");
		upCocespText.setText("?");
		downCocespText.setText("?");
		upFisicaText.setText("?");
		downFisicaText.setText("?");

		// Seta valor correto dos buttons quando existem e estao habilitados
		if (horarioSelecionado == ALMOCO
				&& jaPegouUpDownAlmoco[diaDaSemana] == true
				&& votacaoEnabled == true) {
			
			upQuimicaText.setText(Integer.toString(upQuimicaAlmoco[diaDaSemana]));
			downQuimicaText.setText(Integer.toString(downQuimicaAlmoco[diaDaSemana]));
			
			upCentralText.setText(Integer.toString(upCentralAlmoco[diaDaSemana]));
			downCentralText.setText(Integer.toString(downCentralAlmoco[diaDaSemana]));
			
			upCocespText.setText(Integer.toString(upCocespAlmoco[diaDaSemana]));
			downCocespText.setText(Integer.toString(downCocespAlmoco[diaDaSemana]));
			
			upFisicaText.setText(Integer.toString(upFisicaAlmoco[diaDaSemana]));
			downFisicaText.setText(Integer.toString(downFisicaAlmoco[diaDaSemana]));
			
		} else if (horarioSelecionado == JANTA
				&& jaPegouUpDownJanta[diaDaSemana] == true
				&& votacaoEnabled == true) {
			
			upQuimicaText.setText(Integer.toString(upQuimicaJanta[diaDaSemana]));
			downQuimicaText.setText(Integer.toString(downQuimicaJanta[diaDaSemana]));
			
			upCentralText.setText(Integer.toString(upCentralJanta[diaDaSemana]));
			downCentralText.setText(Integer.toString(downCentralJanta[diaDaSemana]));
			
			upCocespText.setText(Integer.toString(upCocespJanta[diaDaSemana]));
			downCocespText.setText(Integer.toString(downCocespJanta[diaDaSemana]));
			
			upFisicaText.setText(Integer.toString(upFisicaJanta[diaDaSemana]));
			downFisicaText.setText(Integer.toString(downFisicaJanta[diaDaSemana]));
		}
	}

	private void setEventListenersUpDown() {
		Button botaoUpQuimica = (Button) findViewById(R.id.botaoUpQuimica);
		botaoUpQuimica.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				enviaUpDown(QUIMICA_STRING, true);
			}
		});

		Button botaoDownQuimica = (Button) findViewById(R.id.botaoDownQuimica);
		botaoDownQuimica.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				enviaUpDown(QUIMICA_STRING, false);
			}
		});

		Button botaoUpCocesp = (Button) findViewById(R.id.botaoUpCocesp);
		botaoUpCocesp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				enviaUpDown(COCESP_STRING, true);
			}
		});

		Button botaoDownCocesp = (Button) findViewById(R.id.botaoDownCocesp);
		botaoDownCocesp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				enviaUpDown(COCESP_STRING, false);
			}
		});

		Button botaoUpCentral = (Button) findViewById(R.id.botaoUpCentral);
		botaoUpCentral.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				enviaUpDown(CENTRAL_STRING, true);
			}
		});

		Button botaoDownCentral = (Button) findViewById(R.id.botaoDownCentral);
		botaoDownCentral.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				enviaUpDown(CENTRAL_STRING, false);
			}
		});
		
		Button botaoUpFisica = (Button) findViewById(R.id.botaoUpFisica);
		botaoUpFisica.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				enviaUpDown(FISICA_STRING, true);
			}
		});

		Button botaoDownFisica = (Button) findViewById(R.id.botaoDownFisica);
		botaoDownFisica.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				enviaUpDown(FISICA_STRING, false);
			}
		});
	}

	private void setEventListenersData() {
		Button dataAnterior = (Button) findViewById(R.id.botaoOntem);
		dataAnterior.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (diaDaSemana != 0) {
					dataSelecionada--;
					setData();
					populaCardapios();
				}
			}
		});

		Button dataProxima = (Button) findViewById(R.id.botaoAmanha);
		dataProxima.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (diaDaSemana != 6) {
					dataSelecionada++;
					setData();
					populaCardapios();
				}
			}
		});

		/*
		 * Button dataHoje = (Button) findViewById(R.id.botaoHoje);
		 * dataHoje.setOnClickListener(new View.OnClickListener() { public void
		 * onClick(View v) { dataSelecionada = 0; setData(); populaCardapios();
		 * } });
		 */
		
		/*
		Button dataData = (Button) findViewById(R.id.botaoData);
		dataData.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dataSelecionada = 0;
				setData();
				populaCardapios();
			}
		});
		*/
	}

	private void setEventListenersHorario() {
		botaoMudarHorario = (Button) findViewById(R.id.botaoVerJantar);
		botaoMudarHorario.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (horarioSelecionado == 1)
					horarioSelecionado = 0;
				else
					horarioSelecionado = 1;

				setHorario();
				pintaTabSelecionada(getSelectedTab());
				populaCardapios();
			}
		});
	}
	
	private void setEventListeners() {
		
		setEventListenersUpDown();

		setEventListenersData();

		setEventListenersHorario();

	}

	/************************************************
	 * Funcoes que alteram os nomes dos pratos
	 **********************************************/

	/**
	 * Altera o nome de um dos pratos do bandeijao da quimica
	 * 
	 * @param n
	 *            NÃ¯Â¿Â½mero de prato, de 1 a 6
	 * @param prato
	 *            Novo nome do prato
	 */
	private void alteraPratoQuimica(int n, String prato) {
		TextView t;
		switch (n) {
		case 1:
			t = (TextView) findViewById(R.id.pratoQuimica1);
			t.setText(prato);
			break;
		case 2:
			t = (TextView) findViewById(R.id.pratoQuimica2);
			t.setText(prato);
			break;
		case 3:
			t = (TextView) findViewById(R.id.pratoQuimica3);
			t.setText(prato);
			break;
		case 4:
			t = (TextView) findViewById(R.id.pratoQuimica4);
			t.setText(prato);
			break;
		case 5:
			t = (TextView) findViewById(R.id.pratoQuimica5);
			t.setText(prato);
			break;
		case 6:
			//t = (TextView) findViewById(R.id.pratoQuimica6);
			//t.setText(prato);
			break;
		default:
			break;
		}
	}

	private void alteraPratoFisica(int n, String prato) {
		TextView t;
		switch (n) {
		case 1:
			t = (TextView) findViewById(R.id.pratoFisica1);
			t.setText(prato);
			break;
		case 2:
			t = (TextView) findViewById(R.id.pratoFisica2);
			t.setText(prato);
			break;
		case 3:
			t = (TextView) findViewById(R.id.pratoFisica3);
			t.setText(prato);
			break;
		case 4:
			t = (TextView) findViewById(R.id.pratoFisica4);
			t.setText(prato);
			break;
		case 5:
			t = (TextView) findViewById(R.id.pratoFisica5);
			t.setText(prato);
			break;
		case 6:
			t = (TextView) findViewById(R.id.pratoFisica6);
			t.setText(prato);
			break;
		default:
			break;
		}
	}

	
	/**
	 * Altera o nome de um dos pratos do bandeijao Central
	 * 
	 * @param n
	 *            NÃ¯Â¿Â½mero de prato, de 1 a 6
	 * @param prato
	 *            Novo nome do prato
	 */
	private void alteraPratoCentral(int n, String prato) 
	{
		TextView t = (TextView) findViewById(R.id.pratoCentral1);
		
		switch (n) 
		{
			case 1:
				break;
			case 2:
				t = (TextView) findViewById(R.id.pratoCentral2);
				break;
			case 3:
				t = (TextView) findViewById(R.id.pratoCentral3);
				break;
			case 4:
				t = (TextView) findViewById(R.id.pratoCentral4);
				break;
			case 5:
				t = (TextView) findViewById(R.id.pratoCentral5);
				break;
			case 6:
				t = (TextView) findViewById(R.id.pratoCentral6);
				break;
			default:
				break;
		}
		
		t.setText(prato);
	}

	/**
	 * Altera o nome de um dos pratos do bandeijao Cocesp
	 * 
	 * @param n
	 *            NÃ¯Â¿Â½mero de prato, de 1 a 6
	 * @param prato
	 *            Novo nome do prato
	 */
	private void alteraPratoCocesp(int n, String prato) {
		TextView t;
		switch (n) {
		case 1:
			t = (TextView) findViewById(R.id.pratoCocesp1);
			t.setText(prato);
			break;
		case 2:
			t = (TextView) findViewById(R.id.pratoCocesp2);
			t.setText(prato);
			break;
		case 3:
			t = (TextView) findViewById(R.id.pratoCocesp3);
			t.setText(prato);
			break;
		case 4:
			t = (TextView) findViewById(R.id.pratoCocesp4);
			t.setText(prato);
			break;
		case 5:
			t = (TextView) findViewById(R.id.pratoCocesp5);
			t.setText(prato);
			break;
		case 6:
			t = (TextView) findViewById(R.id.pratoCocesp6);
			t.setText(prato);
			break;
		default:
			break;
		}
	}

	/**
	 * Altera o nome de um dos pratos do bandeijao do Clube dos Professores
	 * 
	 * @param n
	 *            NÃ¯Â¿Â½mero de prato, de 1 a 6
	 * @param prato
	 *            Novo nome do prato
	 */
	private void alteraPratoProfessores(int n, String prato) {
		TextView t;
		switch (n) {
		case 1:
			t = (TextView) findViewById(R.id.pratoProfessores1);
			t.setText(prato);
			break;
		case 2:
			t = (TextView) findViewById(R.id.pratoProfessores2);
			t.setText(prato);
			break;
		case 3:
			t = (TextView) findViewById(R.id.pratoProfessores3);
			t.setText(prato);
			break;
		case 4:
			t = (TextView) findViewById(R.id.pratoProfessores4);
			t.setText(prato);
			break;
		case 5:
			t = (TextView) findViewById(R.id.pratoProfessores5);
			t.setText(prato);
			break;
		case 6:
			t = (TextView) findViewById(R.id.pratoProfessores6);
			t.setText(prato);
			break;
		default:
			break;
		}
	}

	/*****************************
	 * Fim das alteracoes de pratos
	 *******************************/

	/************************************************
	 * Funcoes que alteram os horarios de funcionamento
	 **********************************************/

	/**
	 * Altera o horario de funcionamento do bandeijao da Quimica
	 * 
	 * @param abertura
	 *            Horario de abertura do bandeijao no formato "hh:mm"
	 * @param fechamento
	 *            Horario de fechamento do bandeijao no formato "hh:mm"
	 */
	private void alteraHorarioQuimica(String abertura, String fechamento) {
		TextView t;
		t = (TextView) findViewById(R.id.horarioQuimicaAbertura);
		t.setText(abertura + "-");

		t = (TextView) findViewById(R.id.horarioQuimicaFechamento);
		t.setText(fechamento);
	}

	/**
	 * Altera o horario de funcionamento do bandeijao Central
	 * 
	 * @param abertura
	 *            Horario de abertura do bandeijao no formato "hh:mm"
	 * @param fechamento
	 *            Horario de fechamento do bandeijao no formato "hh:mm"
	 */
	private void alteraHorarioCentral(String abertura, String fechamento) {
		
		TextView t;
		
		t = (TextView) findViewById(R.id.horarioCentralAbertura);
		t.setText(abertura + "-");

		t = (TextView) findViewById(R.id.horarioCentralFechamento);
		t.setText(fechamento);
		
	}
	
	/**
	 * Altera o horario de funcionamento do bandeijao Fisica
	 * 
	 * @param abertura
	 *            Horario de abertura do bandeijao no formato "hh:mm"
	 * @param fechamento
	 *            Horario de fechamento do bandeijao no formato "hh:mm"
	 */
	private void alteraHorarioFisica(String abertura, String fechamento) {
		TextView t;
		t = (TextView) findViewById(R.id.horarioFisicaAbertura);
		t.setText(abertura + "-");

		t = (TextView) findViewById(R.id.horarioFisicaFechamento);
		t.setText(fechamento);
	}

	/**
	 * Altera o horario de funcionamento do bandeijao COCESP
	 * 
	 * @param abertura
	 *            Horario de abertura do bandeijao no formato "hh:mm"
	 * @param fechamento
	 *            Horario de fechamento do bandeijao no formato "hh:mm"
	 */
	private void alteraHorarioCocesp(String abertura, String fechamento) {
		TextView t;
		t = (TextView) findViewById(R.id.horarioCocespAbertura);
		t.setText(abertura + "-");

		t = (TextView) findViewById(R.id.horarioCocespFechamento);
		t.setText(fechamento);
	}

	/**
	 * Altera o horario de funcionamento do bandeijao do Clube dos Professores
	 * 
	 * @param abertura
	 *            Horario de abertura do bandeijao no formato "hh:mm"
	 * @param fechamento
	 *            Horario de fechamento do bandeijao no formato "hh:mm"
	 */

	@SuppressWarnings("unused")
	private void alteraHorarioProfessores(String abertura, String fechamento) {
		TextView t;
		t = (TextView) findViewById(R.id.horarioProfessoresAbertura);
		t.setText(abertura + "-");

		t = (TextView) findViewById(R.id.horarioProfessoresFechamento);
		t.setText(fechamento);
	}

	/*****************************
	 * Fim das alteracoes de Horarios
	 *******************************/

	/**
	 * Altera o string que representa a data de hoje no botao do cabecalho
	 * 
	 * @param data
	 *            Data do dia no formato dd/mm/aa
	 */
	private void alteraBotaoData(String data) {
		TextView b = (TextView) findViewById(R.id.dataText);
		b.setText(data);
	}

	/**
	 * Coloca a data do dia de hoje no botÃ¯Â¿Â½o de data no menu superior
	 */
	private void setData() {

		String dataFormatada;
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, dataSelecionada); // number of days to add

		// Segunda Ã© 0, Domingo Ã© 6
		diaDaSemana = c.get(Calendar.DAY_OF_WEEK) - 2;
		
		// Se for o ultimo dia da semana, desabilita o botï¿½o de prï¿½ximo dia
		if (diaDaSemana == -1) {
			diaDaSemana = 6;
			((Button) findViewById(R.id.botaoOntem)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.botaoAmanha)).setVisibility(View.INVISIBLE);
		} else if (diaDaSemana == 0) {
			((Button) findViewById(R.id.botaoOntem)).setVisibility(View.INVISIBLE);
			((Button) findViewById(R.id.botaoAmanha)).setVisibility(View.VISIBLE);
		} else {
			((Button) findViewById(R.id.botaoAmanha)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.botaoOntem)).setVisibility(View.VISIBLE);
		}
		
		// o Peixe assume que os dias são de 0 (Seg) a 6 (Dom)
		// porém o calendário assume que Domingo é -1. Se não tiver isto o programa dá pau no Domingo
		if (diaDaSemana == -1) diaDaSemana = 6;

		dataFormatada = semanaIntToString(diaDaSemana) + ", ";
		dataFormatada += dateFormat.format(c.getTime()); // dt is now the new
															// date

		this.alteraBotaoData(dataFormatada);
	}

	/**
	 * Acoes de deteccao de gestos
	 */
	public boolean onDown(MotionEvent arg0) {
		return true;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// localiza o tabHost
		TabHost tabHost = getTabHost();

		int distanciaMinima = 20; // Distancia minima, em pixels, no eixo X para
									// o movimento ser considerado, e efetuar a
									// mudança

		// Calcula direcao do fling
		float dx = e2.getX() - e1.getX();
		if (dx > distanciaMinima) // movimento para a direita, chama a aba da
									// esquerda
		{
			tabHost.setCurrentTab(tabHost.getCurrentTab() - 1);
		} else if (dx < -distanciaMinima) // movimento para a esquerda, chama a
											// aba da direita
		{
			tabHost.setCurrentTab(tabHost.getCurrentTab() + 1);
		}
		return true;
	}

	/**
	 * Fim das acoes de deteccao de gestos
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		return true;
	}

	public void atualizaCardapio() 
	{
		dialog = ProgressDialog.show(this, "Aguarde...", "Atualizando cardápio", true,
				false);
		
		if (isExibirVotacao()) {
			atualizaCardapioThread = new AtualizaUpDown(this, cardapioAsynkService, dialog);

			atualizaCardapioThread.start();
		}
		cardapioAsynkService = new CardapioAsynkService(this, new PeixeCardapioServiceResponde());

		CardapioAsynkService.forcaAtualizar();
		cardapioAsynkService.execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Intent intent;
		switch (item.getItemId()) {
		case R.id.itemRefresh:
			atualizaCardapio();
			return true;

		case R.id.itemPreferences:
			intent = new Intent(this, ConfiguracoesActivity.class);
			startActivityForResult(intent, 0);
			return true;

		case R.id.itemMensagem:
			intent = new Intent(this, MensagemActivity.class);
			startActivityForResult(intent, 0);
			return true;

		case R.id.itemSobre:
			intent = new Intent(this, SobreActivity.class);
			startActivityForResult(intent, 0);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private class DataHoraSelecionados {
		public int data;
		public int horario;
		public DataHoraSelecionados(int data, int horario) {
			this.data = data;
			this.horario = horario;
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return new DataHoraSelecionados(dataSelecionada, horarioSelecionado);
	}
	
	private int getSelectedTab() {
		return getTabHost().getCurrentTab();
	}
	
	public class PeixeCardapioServiceResponde implements CardapioAsynkService.OnCardapioServiceResponse {
		@Override
		public void onResult(CardapioCompleto result) {
			cardapioCompleto = result;
			populaCardapios();
			
			if (cardapioCompleto.semConexao && cardapioCompleto.ehCacheAtual) {
				AlertDialog alertDialog = new AlertDialog.Builder(PeixeActivity.this).create();  
			    alertDialog.setTitle("Atenção!");  
			    alertDialog.setMessage(getString(R.string.comCacheSemConexao));  
			    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int which) {
				    return;
				  }
			    });

			    alertDialog.show();
			} else if (cardapioCompleto.mensagem != null) {
				Toast.makeText(PeixeActivity.this, cardapioCompleto.mensagem, Toast.LENGTH_LONG).show();
			}
			
			if (dialog != null) {
				if (atualizaCardapioThread != null && atualizaCardapioThread.isAlive())
					dialog.setMessage("Atualizando Ups e Downs");
				else
					dialog.dismiss();
			}
		}
		
		@Override
		public void onError(String error) {
		}
	}
}