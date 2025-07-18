package com.example.petlar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Fragmento responsável por cadastrar um novo pet no sistema
public class CadastrarPetFragment extends Fragment {

    private EditText etNome, etRaca, etCidade, etWhatsApp, etEmail, etDescricao;
    private Spinner spinnerAgeValue, spinnerAgeType, spinnerTipo, spinnerPorte, spinnerEstado, spinnerGenero, spinnerCountryCode;
    private Button btnSelecionarImagem, btnSalvarPet;
    private ImageView ivImagemPet;

    private Uri imagemSelecionadaUri;
    private static final int REQUEST_CODE_IMAGEM = 1;

    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private FirebaseAuth auth;

    public CadastrarPetFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cadastrar_pet, container, false);

        // Inicializa os campos do layout
        etNome = view.findViewById(R.id.etPetName);
        spinnerAgeValue = view.findViewById(R.id.spinnerAgeValue);
        spinnerAgeType = view.findViewById(R.id.spinnerAgeType);
        spinnerGenero = view.findViewById(R.id.spinnerGender);
        etRaca = view.findViewById(R.id.etPetBreed);
        etCidade = view.findViewById(R.id.etCity);
        spinnerEstado = view.findViewById(R.id.spinnerState);
        spinnerCountryCode = view.findViewById(R.id.spinnerCountryCode);
        etWhatsApp = view.findViewById(R.id.etWhatsApp);
        etEmail = view.findViewById(R.id.etEmail);
        etDescricao = view.findViewById(R.id.etDescricao);
        spinnerTipo = view.findViewById(R.id.spinnerType);
        spinnerPorte = view.findViewById(R.id.spinnerSize);
        btnSelecionarImagem = view.findViewById(R.id.btnSelectImage);
        btnSalvarPet = view.findViewById(R.id.btnSavePet);
        ivImagemPet = view.findViewById(R.id.ivPetImage);

        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        // Botão para abrir a galeria de imagens
        btnSelecionarImagem.setOnClickListener(v -> abrirGaleria());

        // Força o e-mail para letras minúsculas
        etEmail.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable editable) {
                String email = editable.toString();
                if (!email.equals(email.toLowerCase())) {
                    etEmail.setText(email.toLowerCase());
                    etEmail.setSelection(etEmail.getText().length());
                }
            }
        });

        // Botão para salvar o pet
        btnSalvarPet.setOnClickListener(v -> salvarPetNoFirebase());

        return view;
    }

    // Abre a galeria do dispositivo para selecionar imagem
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_IMAGEM);
    }

    // Resultado da seleção da imagem
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGEM && resultCode == Activity.RESULT_OK && data != null) {
            imagemSelecionadaUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imagemSelecionadaUri);
                ivImagemPet.setImageBitmap(bitmap);
                ivImagemPet.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                Toast.makeText(getContext(), "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Valida campos e inicia upload da imagem, se necessário
    private void salvarPetNoFirebase() {
        String nome = etNome.getText().toString().trim();
        String idade = spinnerAgeValue.getSelectedItem().toString() + " " + spinnerAgeType.getSelectedItem().toString(); // usa "Ano(s)", etc.
        String genero = spinnerGenero.getSelectedItem().toString();
        String raca = etRaca.getText().toString().trim();
        String cidade = etCidade.getText().toString().trim();
        String estado = spinnerEstado.getSelectedItem().toString();
        String ddiSelecionado = spinnerCountryCode.getSelectedItem().toString();
        String ddi = ddiSelecionado.replaceAll("\\D+", "");
        String whatsapp = "+" + ddi + etWhatsApp.getText().toString().replaceAll("\\D+", "");
        String email = etEmail.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();
        String tipo = spinnerTipo.getSelectedItem().toString();
        String porte = spinnerPorte.getSelectedItem().toString();

        // Verifica se os campos obrigatórios foram preenchidos
        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(cidade)
                || TextUtils.isEmpty(whatsapp) || TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        String idUsuario = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        if (imagemSelecionadaUri == null) {
            // Salva pet com imagem padrão
            salvarPetComImagem("https://firebasestorage.googleapis.com/v0/b/SEU_PROJETO.appspot.com/o/ic_pet_dog_cat.png?alt=media",
                    nome, idade, genero, raca, cidade, estado, whatsapp, email, descricao, tipo, porte, idUsuario);
        } else {
            // Faz upload da imagem
            String nomeImagem = UUID.randomUUID().toString();
            StorageReference imagemRef = storageReference.child("pets/" + nomeImagem);

            imagemRef.putFile(imagemSelecionadaUri)
                    .addOnSuccessListener(taskSnapshot -> imagemRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        salvarPetComImagem(uri.toString(), nome, idade, genero, raca, cidade, estado, whatsapp, email, descricao, tipo, porte, idUsuario);
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();
                        salvarPetComImagem("https://firebasestorage.googleapis.com/v0/b/SEU_PROJETO.appspot.com/o/ic_pet_dog_cat.png?alt=media",
                                nome, idade, genero, raca, cidade, estado, whatsapp, email, descricao, tipo, porte, idUsuario);
                    });
        }
    }

    // Método para salvar os dados do pet no Firestore (com URL da imagem já definida)
    private void salvarPetComImagem(String imagemUrl, String nome, String idade, String genero,
                                    String raca, String cidade, String estado, String whatsapp, String email,
                                    String descricao, String tipo, String porte, String idUsuario) {

        // Cria referência com ID gerado automaticamente
        String idPet = firestore.collection("pets").document().getId();

        Map<String, Object> pet = new HashMap<>();
        pet.put("idPet", idPet); // ← campo usado em várias telas
        pet.put("nome", nome);
        pet.put("idade", idade);
        pet.put("genero", genero);
        pet.put("raca", raca);
        pet.put("cidade", cidade);
        pet.put("estado", estado);
        pet.put("whatsapp", whatsapp);
        pet.put("email", email);
        pet.put("descricao", descricao);
        pet.put("tipo", tipo);
        pet.put("porte", porte);
        pet.put("urlImagem", imagemUrl);
        pet.put("adotado", false); // ← fundamental para filtros e botão "Já foi adotado"
        pet.put("uidUsuario", idUsuario); // ← vínculo com o dono
        pet.put("criadoEm", com.google.firebase.Timestamp.now());

        firestore.collection("pets").document(idPet).set(pet)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Pet cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    limparCampos();
                    redirecionarParaInicio();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erro ao salvar pet", Toast.LENGTH_SHORT).show());
    }

    // Limpa os campos após o cadastro
    private void limparCampos() {
        etNome.setText("");
        etRaca.setText("");
        etCidade.setText("");
        etWhatsApp.setText("");
        etEmail.setText("");
        etDescricao.setText("");
        ivImagemPet.setVisibility(View.GONE);
        imagemSelecionadaUri = null;
    }

    // Redireciona para a tela inicial após o cadastro
    private void redirecionarParaInicio() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new InicioFragment()).addToBackStack(null).commit();
    }
}
