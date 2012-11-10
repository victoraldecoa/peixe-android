package br.com.hojeehpeixe.android;

import br.com.hojeehpeixe.android.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ConfiguracoesActivity extends Activity{
	
	Boolean quimicaEnabled = true;
	Boolean centralEnabled = true;
	Boolean cocespEnabled = true;
	Boolean professoresEnabled = true;
	
	Boolean votacaoEnabled = true;
	
	private SharedPreferences preferencias;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuracoes);      
        
        getPreferencias();
        setEventListeners();
        
    }
    
	private void getPreferencias()
	{
		preferencias = getPreferences(Context.MODE_PRIVATE);
		
        quimicaEnabled = preferencias.getBoolean(PeixeActivity.QUIMICA_STRING, true);
        centralEnabled = preferencias.getBoolean(PeixeActivity.CENTRAL_STRING, true);
        cocespEnabled = preferencias.getBoolean(PeixeActivity.COCESP_STRING, true);
        professoresEnabled = preferencias.getBoolean(PeixeActivity.PROFESSORES_STRING, true);
        votacaoEnabled = preferencias.getBoolean(PeixeActivity.EXIBIR_VOTACAO, true);
	}
	
	
	private void salvaPreferenciasEVoltaCardapio(View view)
	{
		Intent myIntent = new Intent(view.getContext(), PeixeActivity.class);
        myIntent.putExtra(PeixeActivity.QUIMICA_STRING, quimicaEnabled);
        myIntent.putExtra(PeixeActivity.CENTRAL_STRING, centralEnabled);
        myIntent.putExtra(PeixeActivity.COCESP_STRING, cocespEnabled);
        myIntent.putExtra(PeixeActivity.PROFESSORES_STRING, professoresEnabled);
        myIntent.putExtra(PeixeActivity.EXIBIR_VOTACAO, votacaoEnabled);
        startActivityForResult(myIntent, 0);
	}
	
    private void setEventListeners() 
    {
    	Button cardapio = (Button) findViewById(R.id.botaoCardapio);
        cardapio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	salvaPreferenciasEVoltaCardapio(view);
            }
        });
        
        /*
         * 
        CheckBox checkboxQuimica = (CheckBox) findViewById(R.id.checkBoxQuimica);
        checkboxQuimica.setChecked(quimicaEnabled);
        checkboxQuimica.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked )
                	quimicaEnabled = true;
                else
                	quimicaEnabled = false;
            }
        });
        
        CheckBox checkboxCentral = (CheckBox) findViewById(R.id.checkBoxCentral);
        checkboxCentral.setChecked(centralEnabled);
        checkboxCentral.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked )
                	centralEnabled = true;
                else
                	centralEnabled = false;
            }
        });
        
        CheckBox checkboxCocesp = (CheckBox) findViewById(R.id.checkBoxCocesp);
        checkboxCocesp.setChecked(cocespEnabled);
        checkboxCocesp.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked )
                	cocespEnabled = true;
                else
                	cocespEnabled = false;
            }
        });
        
        CheckBox checkboxProfessores = (CheckBox) findViewById(R.id.checkBoxProfessores);
        checkboxProfessores.setChecked(professoresEnabled);
        checkboxProfessores.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked )
                	professoresEnabled = true;
                else
                	professoresEnabled = false;
            }
        });
        */
        
        CheckBox checkboxVotacao = (CheckBox) findViewById(R.id.checkBoxVotacao);
        checkboxVotacao.setChecked(votacaoEnabled);
        checkboxVotacao.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
        		if(votacaoEnabled==false && isChecked==true)
        		{
        			PeixeActivity.zeraAllJaPegou();
        		}
        		
                if ( isChecked )
                	votacaoEnabled = true;
                else
                	votacaoEnabled = false;
            }
        });
        
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	if((keyCode == KeyEvent.KEYCODE_BACK))
    	{
    		View view = (View) findViewById(R.id.configuracoes);    
    		salvaPreferenciasEVoltaCardapio(view);
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    /**
     * Save UI state changes to the savedInstanceState.
     * This bundle will be passed to onCreate if the process is killed and restarted.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {      
    	salvaPreferencias();
    	super.onSaveInstanceState(savedInstanceState);
    }
    
    private void salvaPreferencias()
    {
    	SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PeixeActivity.QUIMICA_STRING, quimicaEnabled);
        editor.putBoolean(PeixeActivity.CENTRAL_STRING, centralEnabled);
        editor.putBoolean(PeixeActivity.COCESP_STRING, cocespEnabled);
        editor.putBoolean(PeixeActivity.PROFESSORES_STRING, professoresEnabled);
        editor.putBoolean(PeixeActivity.EXIBIR_VOTACAO, votacaoEnabled);
        editor.commit();
    }
    
    @Override
    protected void onPause() {
    	salvaPreferencias();
    	super.onPause();
    }
    
    
    /**
     * Restore UI state from the savedInstanceState.
     * This bundle has also been passed to onCreate.
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
    }

}
