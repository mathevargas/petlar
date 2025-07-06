package com.example.petlar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class RegistroFragment extends Fragment {

    // Componentes da interface
    private ImageView imgFotoPerfil;
    private Button btnSelecionarFoto, btnCriarConta;
    private EditText etNome, etEmail, etSenha, etConfirmarSenha;
    private EditText etNumero, etCidade, etBio;
    private Spinner spinnerEstado, spinnerCountryCode;

    // Firebase
    private Uri imagemSelecionada = null;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    // Launcher para selecionar imagem da galeria
    private ActivityResultLauncher<Intent> seletorImagemLauncher;

    public RegistroFragment() {
        // Construtor padrão obrigatório
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registro, container, false);

        // Inicializa os componentes da interface
        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfilRegistro);
        btnSelecionarFoto = view.findViewById(R.id.btnSelecionarFoto);
        etNome = view.findViewById(R.id.etNome);
        etEmail = view.findViewById(R.id.etEmailLogin);
        etSenha = view.findViewById(R.id.etSenhaLogin);
        etConfirmarSenha = view.findViewById(R.id.etConfirmarSenha);
        etNumero = view.findViewById(R.id.etNumero);
        etCidade = view.findViewById(R.id.etCidade);
        etBio = view.findViewById(R.id.etBio);
        spinnerEstado = view.findViewById(R.id.spinnerEstado);
        spinnerCountryCode = view.findViewById(R.id.spinnerCountryCode);
        btnCriarConta = view.findViewById(R.id.btnAcao);

        // Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Carrega os estados no Spinner
        ArrayAdapter<CharSequence> adapterEstados = ArrayAdapter.createFromResource(
                getContext(), R.array.ufs, android.R.layout.simple_spinner_item);
        adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterEstados);

        // Carrega os códigos de país no Spinner
        ArrayAdapter<CharSequence> adapterCodigos = ArrayAdapter.createFromResource(
                getContext(), R.array.country_codes, android.R.layout.simple_spinner_item);
        adapterCodigos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(adapterCodigos);

        // Configura o seletor de imagem da galeria
        seletorImagemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        imagemSelecionada = result.getData().getData();
                        Glide.with(this).load(imagemSelecionada).into(imgFotoPerfil);
                    }
                });

        // Ação do botão de selecionar foto
        btnSelecionarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            seletorImagemLauncher.launch(intent);
        });

        // Ação do botão de criar conta
        btnCriarConta.setOnClickListener(v -> criarConta());

        return view;
    }

    // Cria a conta com autenticação e salva os dados no Firestore
    private void criarConta() {
        String nome = etNome.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase();
        String senha = etSenha.getText().toString().trim();
        String confirmarSenha = etConfirmarSenha.getText().toString().trim();
        String ddi = spinnerCountryCode.getSelectedItem().toString().trim(); // Ex: +55
        String numero = etNumero.getText().toString().trim();
        String whatsapp = ddi + numero;
        String cidade = etCidade.getText().toString().trim();
        String estado = spinnerEstado.getSelectedItem().toString();
        String bio = etBio.getText().toString().trim();

        // Validações básicas
        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(getContext(), "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            Toast.makeText(getContext(), "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cria o usuário no Firebase Authentication
        auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener(authResult -> {
                    String userId = auth.getCurrentUser().getUid();

                    if (imagemSelecionada != null) {
                        // Se houver imagem, envia para o Storage
                        storage.getReference().child("usuarios/" + userId + "/foto_perfil.jpg")
                                .putFile(imagemSelecionada)
                                .addOnSuccessListener(taskSnapshot ->
                                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                            salvarDadosUsuario(userId, nome, email, uri.toString(), whatsapp, cidade, estado, bio);
                                        }));
                    } else {
                        // Usa imagem padrão
                        salvarDadosUsuario(userId, nome, email, "default", whatsapp, cidade, estado, bio);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Erro ao criar conta: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Salva os dados do novo usuário no Firestore
    private void salvarDadosUsuario(String uid, String nome, String email, String urlFoto, String whatsapp, String cidade, String estado, String bio) {
        Map<String, Object> dadosUsuario = new HashMap<>();
        dadosUsuario.put("nome", nome);
        dadosUsuario.put("email", email);
        dadosUsuario.put("urlFotoPerfil", urlFoto);
        dadosUsuario.put("whatsapp", whatsapp);
        dadosUsuario.put("cidade", cidade);
        dadosUsuario.put("estado", estado);
        dadosUsuario.put("bio", bio);
        dadosUsuario.put("isPremium", false);

        firestore.collection("usuarios").document(uid).set(dadosUsuario)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getActivity(), MainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Erro ao salvar dados do usuário", Toast.LENGTH_SHORT).show());
    }
}
