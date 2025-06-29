package com.example.petlar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

// Atividade principal que controla a navegação entre os fragments via BottomNavigationView
public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView; // Barra de navegação inferior
    FirebaseAuth auth; // Autenticação do Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Define o layout da atividade

        // Inicializa os componentes
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        auth = FirebaseAuth.getInstance();

        // Abre a tela de início (InicioFragment) por padrão, acessível a todos
        loadFragment(new InicioFragment());

        // Define as ações ao clicar nos itens da barra inferior
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            // Quando o usuário clicar em "Início"
            if (itemId == R.id.menu_home) {
                fragment = new InicioFragment();
            }

            // Quando o usuário clicar em "Cadastrar pet"
            else if (itemId == R.id.menu_add) {
                if (auth.getCurrentUser() != null) {
                    // Se estiver logado, abre a tela de cadastro
                    fragment = new CadastrarPetFragment();
                } else {
                    // Se não estiver logado, redireciona para o login
                    Toast.makeText(this, "Faça login para cadastrar um pet.", Toast.LENGTH_SHORT).show();
                    fragment = new LoginFragment();
                }
            }

            // Quando o usuário clicar em "Perfil"
            else if (itemId == R.id.menu_profile) {
                if (auth.getCurrentUser() != null) {
                    // Se estiver logado, mostra o perfil
                    fragment = new PerfilFragment();
                } else {
                    // Se não estiver logado, redireciona para o login
                    Toast.makeText(this, "Faça login para acessar seu perfil.", Toast.LENGTH_SHORT).show();
                    fragment = new LoginFragment();
                }
            }

            // Troca o fragmento visível, se definido
            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    // Método auxiliar para trocar o fragmento visível
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
