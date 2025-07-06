package com.example.petlar;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * PainelFiltroFragment:
 * Este painel é exibido como um BottomSheet (painel deslizante)
 * e permite que o usuário selecione filtros para os pets:
 * Estado, Cidade, Tipo, Porte e Raça.
 */
public class PainelFiltroFragment extends BottomSheetDialogFragment {

    private Spinner spinnerEstado, spinnerTipo, spinnerPorte;
    private EditText editCidade, editRaca;

    // Listener para retornar os dados filtrados ao fragmento pai (InicioFragment)
    private OnFiltroSelecionadoListener listener;

    /**
     * Interface usada para comunicar os filtros selecionados com o fragmento que chamou o painel
     */
    public interface OnFiltroSelecionadoListener {
        void onFiltroSelecionado(String estado, String cidade, String tipo, String porte, String raca);
    }

    /**
     * Construtor que recebe o listener como parâmetro.
     * @param listener interface implementada por quem chama o painel
     */
    public PainelFiltroFragment(OnFiltroSelecionadoListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla o layout personalizado (painel_filtro.xml)
        View view = inflater.inflate(R.layout.painel_filtro, container, false);

        // Referencia os componentes do layout
        spinnerEstado = view.findViewById(R.id.spinnerEstado);
        spinnerTipo = view.findViewById(R.id.spinnerTipo);
        spinnerPorte = view.findViewById(R.id.spinnerPorte);
        editCidade = view.findViewById(R.id.editCidade);
        editRaca = view.findViewById(R.id.editRaca); // Novo campo de filtro por raça

        configurarSpinners();   // Preenche os spinners
        configurarEventos();    // Adiciona listeners para alterações

        return view;
    }

    /**
     * Preenche os spinners com as opções de Estado, Tipo e Porte
     */
    private void configurarSpinners() {
        // Lista de estados (UF)
        String[] estados = {"Todos", "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES",
                "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI",
                "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"};
        spinnerEstado.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, estados));

        // Lista de tipos de pet
        String[] tipos = {"Todos", "Cachorro", "Gato", "Outro"};
        spinnerTipo.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, tipos));

        // Lista de portes
        String[] portes = {"Todos", "Pequeno", "Médio", "Grande"};
        spinnerPorte.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, portes));
    }

    /**
     * Configura os eventos de mudança em cada campo
     */
    private void configurarEventos() {
        // Listener para os Spinners
        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                notificarAlteracao(); // Quando algo for selecionado
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };

        // Aplica o listener nos spinners
        spinnerEstado.setOnItemSelectedListener(itemSelectedListener);
        spinnerTipo.setOnItemSelectedListener(itemSelectedListener);
        spinnerPorte.setOnItemSelectedListener(itemSelectedListener);

        // Detecta mudança em cidade ou raça
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                notificarAlteracao(); // Sempre que houver digitação
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        editCidade.addTextChangedListener(textWatcher);
        editRaca.addTextChangedListener(textWatcher); // Novo campo
    }

    /**
     * Método que envia os valores selecionados para o fragmento pai
     */
    private void notificarAlteracao() {
        if (listener != null) {
            String estado = spinnerEstado.getSelectedItem().toString();
            String tipo = spinnerTipo.getSelectedItem().toString();
            String porte = spinnerPorte.getSelectedItem().toString();
            String cidade = editCidade.getText().toString().trim();
            String raca = editRaca.getText().toString().trim(); // Novo campo

            // Envia os filtros selecionados
            listener.onFiltroSelecionado(estado, cidade, tipo, porte, raca);
        }
    }
}
