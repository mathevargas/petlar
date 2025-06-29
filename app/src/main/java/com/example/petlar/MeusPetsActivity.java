package com.example.petlar;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

// Activity responsável por exibir os pets cadastrados pelo usuário (disponíveis e adotados)
public class MeusPetsActivity extends AppCompatActivity {

    private Button btnDisponiveis, btnAdotados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_pets);

        // Inicializa os botões da interface
        btnDisponiveis = findViewById(R.id.btnDisponiveis);
        btnAdotados = findViewById(R.id.btnAdotados);

        // Mostra inicialmente os pets disponíveis
        trocarFragment(new MeusPetsDisponiveisFragment());

        // Define ação do botão "Disponíveis"
        btnDisponiveis.setOnClickListener(v -> {
            trocarFragment(new MeusPetsDisponiveisFragment());
        });

        // Define ação do botão "Adotados"
        btnAdotados.setOnClickListener(v -> {
            trocarFragment(new MeusPetsAdotadosFragment());
        });
    }

    // Método que troca o fragmento dentro do container da tela
    private void trocarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.meusPetsFragmentContainer, fragment)
                .commit();
    }
}
