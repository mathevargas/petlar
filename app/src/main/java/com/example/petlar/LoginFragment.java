package com.example.petlar;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private EditText etEmailLogin, etSenhaLogin;
    private Button btnAcao;
    private TextView txtAlternarModo, txtRecuperarSenha;

    private FirebaseAuth auth;

    public LoginFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        etEmailLogin = view.findViewById(R.id.etEmailLogin);
        etSenhaLogin = view.findViewById(R.id.etSenhaLogin);
        btnAcao = view.findViewById(R.id.btnAcao);
        txtAlternarModo = view.findViewById(R.id.txtAlternarModo);
        txtRecuperarSenha = view.findViewById(R.id.txtRecuperarSenha);

        auth = FirebaseAuth.getInstance();

        // Ação do botão de login
        btnAcao.setOnClickListener(v -> realizarLogin());

        // Ação do link "Esqueci minha senha"
        txtRecuperarSenha.setOnClickListener(v -> mostrarDialogRecuperarSenha());

        // Alternar para tela de cadastro
        txtAlternarModo.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RegistroFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void realizarLogin() {
        String email = etEmailLogin.getText().toString().trim();
        String senha = etSenhaLogin.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(getContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(getContext(), "Login realizado com sucesso", Toast.LENGTH_SHORT).show();

                    // Redireciona para a tela inicial
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new InicioFragment())
                            .commit();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erro ao fazer login: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Exibe um AlertDialog solicitando o e-mail para recuperar senha.
     */
    private void mostrarDialogRecuperarSenha() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Recuperar Senha");

        final EditText input = new EditText(getContext());
        input.setHint("Digite seu e-mail");
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String email = input.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getContext(), "Digite um e-mail válido", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "E-mail de recuperação enviado", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
