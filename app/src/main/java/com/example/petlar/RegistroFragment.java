package com.example.petlar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistroFragment extends Fragment {

    private EditText etNome, etEmail, etSenha, etConfirmarSenha;
    private Button btnCriarConta;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    public RegistroFragment() {
        // Construtor vazio
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registro, container, false);

        etNome = view.findViewById(R.id.etNome);
        etEmail = view.findViewById(R.id.etEmailLogin);
        etSenha = view.findViewById(R.id.etSenhaLogin);
        etConfirmarSenha = view.findViewById(R.id.etConfirmarSenha);
        btnCriarConta = view.findViewById(R.id.btnAcao);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        btnCriarConta.setText("Criar conta");
        btnCriarConta.setOnClickListener(v -> criarConta());

        return view;
    }

    private void criarConta() {
        String nome = etNome.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase();
        String senha = etSenha.getText().toString().trim();
        String confirmarSenha = etConfirmarSenha.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(getContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            Toast.makeText(getContext(), "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener(authResult -> {
                    String userId = auth.getCurrentUser().getUid();

                    Map<String, Object> dadosUsuario = new HashMap<>();
                    dadosUsuario.put("nome", nome);
                    dadosUsuario.put("email", email);
                    dadosUsuario.put("foto_perfil", "");
                    dadosUsuario.put("bio", "");
                    dadosUsuario.put("isPremium", false);

                    firestore.collection("usuarios").document(userId).set(dadosUsuario)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();

                                // ✅ Redireciona para a tela principal (MainActivity)
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Erro ao salvar dados do usuário", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Erro ao criar conta: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
