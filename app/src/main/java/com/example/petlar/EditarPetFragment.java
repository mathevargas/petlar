package com.example.petlar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
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
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

// Fragment responsável por editar um pet já cadastrado
public class EditarPetFragment extends Fragment {

    // Campos do layout
    private EditText etPetName, etPetBreed, etCity, etWhatsApp, etEmail, etDescricao;
    private Spinner spinnerType, spinnerSize, spinnerGender, spinnerAgeValue, spinnerAgeType, spinnerState, spinnerCountryCode;
    private ImageView ivPetImage;
    private Button btnSelectImage, btnSavePet;

    // Dados
    private Uri imagemSelecionadaUri;
    private Pet petAtual;

    // Firebase
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private StorageReference storageRef;

    private ActivityResultLauncher<Intent> seletorImagemLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_editar_pet, container, false);

        // Inicializa Firebase
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Liga campos do layout às variáveis
        etPetName = view.findViewById(R.id.etPetName);
        etPetBreed = view.findViewById(R.id.etPetBreed);
        etCity = view.findViewById(R.id.etCity);
        etWhatsApp = view.findViewById(R.id.etWhatsApp);
        etEmail = view.findViewById(R.id.etEmail);
        etDescricao = view.findViewById(R.id.etDescricao);
        ivPetImage = view.findViewById(R.id.ivPetImage);
        btnSelectImage = view.findViewById(R.id.btnSelectImage);
        btnSavePet = view.findViewById(R.id.btnSavePet);

        spinnerType = view.findViewById(R.id.spinnerType);
        spinnerSize = view.findViewById(R.id.spinnerSize);
        spinnerGender = view.findViewById(R.id.spinnerGender);
        spinnerAgeValue = view.findViewById(R.id.spinnerAgeValue);
        spinnerAgeType = view.findViewById(R.id.spinnerAgeType);
        spinnerState = view.findViewById(R.id.spinnerState);
        spinnerCountryCode = view.findViewById(R.id.spinnerCountryCode);

        // Recupera pet enviado por argumento
        Bundle args = getArguments();
        if (args != null) {
            petAtual = (Pet) args.getSerializable("pet");
        }

        if (petAtual == null) {
            Toast.makeText(requireContext(), "Erro ao carregar dados do pet.", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return view;
        }

        // Preenche campos com os dados do pet
        preencherCampos();

        // Configura seletor de imagem
        seletorImagemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        imagemSelecionadaUri = result.getData().getData();
                        ivPetImage.setImageURI(imagemSelecionadaUri);
                        ivPetImage.setVisibility(View.VISIBLE);
                    }
                });

        // Botão de selecionar imagem
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            seletorImagemLauncher.launch(Intent.createChooser(intent, "Selecionar imagem"));
        });

        // Botão de salvar alterações
        btnSavePet.setOnClickListener(v -> {
            if (imagemSelecionadaUri != null) {
                enviarImagemParaStorageEAtualizar();
            } else {
                salvarAlteracoes(petAtual.getUrlImagem());
            }
        });

        return view;
    }

    // Preenche os campos com os dados do pet
    private void preencherCampos() {
        etPetName.setText(petAtual.getNome());
        etPetBreed.setText(petAtual.getRaca());
        etCity.setText(petAtual.getCidade());
        etWhatsApp.setText(petAtual.getWhatsapp());
        etEmail.setText(petAtual.getEmail());
        etDescricao.setText(petAtual.getDescricao());

        Glide.with(requireContext())
                .load(petAtual.getUrlImagem())
                .placeholder(R.drawable.ic_pet_dog_cat)
                .into(ivPetImage);
        ivPetImage.setVisibility(View.VISIBLE);

        // Preenche os spinners
        setSpinnerByValue(spinnerType, petAtual.getTipo());
        setSpinnerByValue(spinnerSize, petAtual.getPorte());
        setSpinnerByValue(spinnerGender, petAtual.getGenero());
        setSpinnerByValue(spinnerState, petAtual.getEstado());

        // Divide idade
        if (petAtual.getIdade() != null && petAtual.getIdade().contains(" ")) {
            String[] partes = petAtual.getIdade().split(" ");
            if (partes.length == 2) {
                setSpinnerByValue(spinnerAgeValue, partes[0]);
                setSpinnerByValue(spinnerAgeType, partes[1]);
            }
        }
    }

    // Posiciona o spinner com base no texto
    private void setSpinnerByValue(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    // Envia nova imagem ao Firebase Storage e salva alterações
    private void enviarImagemParaStorageEAtualizar() {
        String nomeImagem = UUID.randomUUID().toString() + ".jpg";
        StorageReference imagemRef = storageRef.child("pets/" + nomeImagem);

        imagemRef.putFile(imagemSelecionadaUri)
                .addOnSuccessListener(taskSnapshot -> imagemRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String novaUrlImagem = uri.toString();
                    salvarAlteracoes(novaUrlImagem);
                }))
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Erro ao enviar imagem", Toast.LENGTH_SHORT).show());
    }

    // Salva as alterações no Firestore
    private void salvarAlteracoes(String urlImagemFinal) {
        String nome = etPetName.getText().toString().trim();
        String idade = spinnerAgeValue.getSelectedItem().toString() + " " + spinnerAgeType.getSelectedItem().toString();
        String tipo = spinnerType.getSelectedItem().toString();
        String porte = spinnerSize.getSelectedItem().toString();
        String genero = spinnerGender.getSelectedItem().toString();
        String raca = etPetBreed.getText().toString().trim();
        String cidade = etCity.getText().toString().trim();
        String estado = spinnerState.getSelectedItem().toString();
        String whatsapp = etWhatsApp.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();

        if (nome.isEmpty() || cidade.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Atualiza dados no objeto
        petAtual.setNome(nome);
        petAtual.setIdade(idade);
        petAtual.setTipo(tipo);
        petAtual.setPorte(porte);
        petAtual.setGenero(genero);
        petAtual.setRaca(raca);
        petAtual.setCidade(cidade);
        petAtual.setEstado(estado);
        petAtual.setWhatsapp(whatsapp);
        petAtual.setEmail(email);
        petAtual.setDescricao(descricao);
        petAtual.setUrlImagem(urlImagemFinal);

        // Substituído getId() → getIdPet(), pois agora o ID no Firestore está salvo no campo 'idPet'
        firestore.collection("pets")
                .document(petAtual.getIdPet()) // <-- Aqui foi o ajuste principal
                .set(petAtual)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Pet atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed(); // volta para tela anterior
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Erro ao atualizar pet.", Toast.LENGTH_SHORT).show());
    }
}
