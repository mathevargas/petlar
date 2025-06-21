package com.example.petlar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CadastrarPetFragment extends Fragment {

    private EditText etNome, etRaca, etCidade, etWhatsApp, etEmail, etDescricao;
    private Spinner spinnerAgeValue, spinnerAgeType, spinnerTipo, spinnerPorte, spinnerEstado, spinnerGenero;
    private Button btnSelecionarImagem, btnSalvarPet;
    private ImageView ivImagemPet;

    private Uri imagemSelecionadaUri;

    private static final int REQUEST_CODE_IMAGEM = 1;

    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    public CadastrarPetFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cadastrar_pet, container, false);

        // Referência aos campos do layout
        etNome = view.findViewById(R.id.etPetName);
        spinnerAgeValue = view.findViewById(R.id.spinnerAgeValue);
        spinnerAgeType = view.findViewById(R.id.spinnerAgeType);
        spinnerGenero = view.findViewById(R.id.spinnerGender);
        etRaca = view.findViewById(R.id.etPetBreed);
        etCidade = view.findViewById(R.id.etCity);
        spinnerEstado = view.findViewById(R.id.spinnerState);
        etWhatsApp = view.findViewById(R.id.etWhatsApp);
        etEmail = view.findViewById(R.id.etEmail);
        etDescricao = view.findViewById(R.id.etDescricao);
        spinnerTipo = view.findViewById(R.id.spinnerType);
        spinnerPorte = view.findViewById(R.id.spinnerSize);
        btnSelecionarImagem = view.findViewById(R.id.btnSelectImage);
        btnSalvarPet = view.findViewById(R.id.btnSavePet);
        ivImagemPet = view.findViewById(R.id.ivPetImage);

        // Inicializar Firebase
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Botão para selecionar imagem da galeria
        btnSelecionarImagem.setOnClickListener(v -> abrirGaleria());

        // Forçar e-mail para letras minúsculas
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable editable) {
                String email = editable.toString();
                if (!email.equals(email.toLowerCase())) {
                    etEmail.setText(email.toLowerCase());
                    etEmail.setSelection(etEmail.getText().length());
                }
            }
        });

        // Formatar o número para padrão brasileiro ao digitar (sem usar máscara externa)
        etWhatsApp.addTextChangedListener(new TextWatcher() {
            boolean isUpdating;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

                isUpdating = true;
                String digits = s.toString().replaceAll("\\D", "");
                String formatted;

                if (digits.length() <= 2) {
                    formatted = "(" + digits;
                } else if (digits.length() <= 6) {
                    formatted = "(" + digits.substring(0, 2) + ") " + digits.substring(2);
                } else if (digits.length() <= 10) {
                    formatted = "(" + digits.substring(0, 2) + ") " + digits.substring(2, 6) + "-" + digits.substring(6);
                } else {
                    formatted = "(" + digits.substring(0, 2) + ") " + digits.substring(2, 7) + "-" + digits.substring(7, Math.min(11, digits.length()));
                }

                etWhatsApp.setText(formatted);
                etWhatsApp.setSelection(etWhatsApp.getText().length());
                isUpdating = false;
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        // Botão para salvar/publicar o pet
        btnSalvarPet.setOnClickListener(v -> salvarPetNoFirebase());

        return view;
    }

    // Abre a galeria para selecionar imagem
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_IMAGEM);
    }

    // Resultado da seleção de imagem
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
                e.printStackTrace();
                Toast.makeText(getContext(), "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Salva os dados no Firebase (Storage + Firestore)
    private void salvarPetNoFirebase() {
        String nome = etNome.getText().toString().trim();
        String idadeValor = spinnerAgeValue.getSelectedItem().toString();
        String idadeTipo = spinnerAgeType.getSelectedItem().toString();
        String idadeCompleta = idadeValor + " " + idadeTipo;
        String genero = spinnerGenero.getSelectedItem().toString();
        String raca = etRaca.getText().toString().trim();
        String cidade = etCidade.getText().toString().trim();
        String estado = spinnerEstado.getSelectedItem().toString();
        String whatsapp = etWhatsApp.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();
        String tipo = spinnerTipo.getSelectedItem().toString();
        String porte = spinnerPorte.getSelectedItem().toString();

        if (imagemSelecionadaUri != null) {
            String nomeImagem = UUID.randomUUID().toString();
            StorageReference imagemRef = storageReference.child("pets/" + nomeImagem);

            imagemRef.putFile(imagemSelecionadaUri)
                    .addOnSuccessListener(taskSnapshot -> imagemRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String urlImagem = uri.toString();
                        salvarNoFirestore(nome, idadeCompleta, genero, raca, cidade, estado, whatsapp, email, descricao, tipo, porte, urlImagem);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show());
        } else {
            salvarNoFirestore(nome, idadeCompleta, genero, raca, cidade, estado, whatsapp, email, descricao, tipo, porte, "");
        }
    }

    // Salva os dados no Firestore
    private void salvarNoFirestore(String nome, String idade, String genero, String raca, String cidade, String estado,
                                   String whatsapp, String email, String descricao, String tipo, String porte, String urlImagem) {

        Map<String, Object> pet = new HashMap<>();
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
        pet.put("imagemUrl", urlImagem);

        firestore.collection("pets")
                .add(pet)
                .addOnSuccessListener(documentReference -> Toast.makeText(getContext(), "Pet cadastrado com sucesso!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erro ao salvar pet", Toast.LENGTH_SHORT).show());
    }
}
