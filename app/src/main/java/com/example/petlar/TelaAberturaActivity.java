package com.example.petlar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

// Tela de abertura (Splash Screen) exibida ao iniciar o app
public class TelaAberturaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_abertura);

        // Aguarda 2 segundos (2000 milissegundos) e abre a tela principal
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(TelaAberturaActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Fecha a splash screen para não voltar nela
        }, 500); // Duração da splash screen em milissegundos
    }
}
