package com.example.petlar;

import android.app.Activity;
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
import java.util.HashMap;
import java.util.Map;

// Fragment responsável por permitir que o usuário edite seu perfil completo
public class EditarPerfilFragment extends Fragment {

    // Componentes da interface
    private ImageView imgFotoPerfil;
    private EditText edtNome, edtBio, etWhatsApp, edtCidade, edtEmail, edtNovaSenha;
    private Spinner spinnerEstado, spinnerCountryCode;
    private Button btnSelecionarFoto, btnSalvar;

    // Firebase
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private String uid;
    private String urlFotoPerfil = "default";
    private Uri novaImagemSelecionada = null;

    private ActivityResultLauncher<Intent> seletorImagemLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editar_perfil, container, false);

        // Inicializa os componentes da interface
        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfilEditar);
        btnSelecionarFoto = view.findViewById(R.id.btnSelecionarFotoEditar);
        edtNome = view.findViewById(R.id.edtNomeUsuario);
        edtBio = view.findViewById(R.id.edtBioUsuario);
        spinnerCountryCode = view.findViewById(R.id.spinnerCountryCode);
        etWhatsApp = view.findViewById(R.id.etWhatsApp);
        edtCidade = view.findViewById(R.id.edtCidadeUsuario);
        edtEmail = view.findViewById(R.id.edtEmailUsuario);
        edtNovaSenha = view.findViewById(R.id.edtNovaSenha);
        spinnerEstado = view.findViewById(R.id.spinnerEstadoEditar);
        btnSalvar = view.findViewById(R.id.btnSalvarPerfil);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Adapta os estados e códigos de país nos Spinners
        ArrayAdapter<CharSequence> adapterEstados = ArrayAdapter.createFromResource(
                getContext(), R.array.ufs, android.R.layout.simple_spinner_item);
        adapterEstados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterEstados);

        ArrayAdapter<CharSequence> adapterCountry = ArrayAdapter.createFromResource(
                getContext(), R.array.country_codes, android.R.layout.simple_spinner_item);
        adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(adapterCountry);

        // Configura seletor de imagem da galeria
        seletorImagemLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        novaImagemSelecionada = result.getData().getData();
                        Glide.with(requireContext()).load(novaImagemSelecionada).into(imgFotoPerfil);
                    }
                });

        btnSelecionarFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            seletorImagemLauncher.launch(intent);
        });

        // Ação de salvar alterações
        btnSalvar.setOnClickListener(v -> salvarAlteracoes());

        // Preenche os campos com os dados existentes
        carregarDadosAtuais();

        return view;
    }

    // Carrega os dados do usuário
    private void carregarDadosAtuais() {
        firestore.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        edtNome.setText(doc.getString("nome"));
                        edtBio.setText(doc.getString("bio"));
                        edtCidade.setText(doc.getString("cidade"));
                        edtEmail.setText(doc.getString("email"));
                        edtEmail.setEnabled(false);

                        // Carrega número com DDI e ajusta spinner + campo número
                        String whatsapp = doc.getString("whatsapp");
                        if (whatsapp != null && whatsapp.startsWith("+")) {
                            String numeroSemEspacos = whatsapp.replaceAll("\\s+", "");
                            boolean encontrou = false;

                            for (int i = 0; i < spinnerCountryCode.getCount(); i++) {
                                String item = spinnerCountryCode.getItemAtPosition(i).toString(); // ex: +55 (Brasil)
                                String ddi = item.replaceAll("\\D+", ""); // extrai só os números

                                if (numeroSemEspacos.startsWith("+" + ddi)) {
                                    spinnerCountryCode.setSelection(i);
                                    String numeroAposDDI = numeroSemEspacos.substring(("+" + ddi).length());
                                    etWhatsApp.setText(numeroAposDDI); // só o número
                                    encontrou = true;
                                    break;
                                }
                            }

                            // Se não encontrou, mostra número completo
                            if (!encontrou) {
                                etWhatsApp.setText(numeroSemEspacos.replaceAll("\\D+", ""));
                            }
                        }

                        // Estado
                        String estado = doc.getString("estado");
                        if (estado != null) {
                            int pos = ((ArrayAdapter) spinnerEstado.getAdapter()).getPosition(estado);
                            if (pos >= 0) spinnerEstado.setSelection(pos);
                        }

                        // Foto de perfil
                        urlFotoPerfil = doc.getString("urlFotoPerfil");
                        if (urlFotoPerfil == null || urlFotoPerfil.equals("default")) {
                            imgFotoPerfil.setImageResource(R.drawable.img_perfil_default);
                        } else {
                            Glide.with(requireContext())
                                    .load(urlFotoPerfil)
                                    .placeholder(R.drawable.img_perfil_default)
                                    .into(imgFotoPerfil);
                        }
                    }
                });
    }

    // Salva alterações no Firestore
    private void salvarAlteracoes() {
        Map<String, Object> atualizacoes = new HashMap<>();
        atualizacoes.put("nome", edtNome.getText().toString().trim());
        atualizacoes.put("bio", edtBio.getText().toString().trim());
        atualizacoes.put("cidade", edtCidade.getText().toString().trim());
        atualizacoes.put("estado", spinnerEstado.getSelectedItem().toString());

        // Formata WhatsApp (com DDI e somente números)
        String ddiSelecionado = spinnerCountryCode.getSelectedItem().toString();
        String ddi = ddiSelecionado.replaceAll("\\D+", "");
        String whatsapp = "+" + ddi + etWhatsApp.getText().toString().replaceAll("\\D+", "");
        atualizacoes.put("whatsapp", whatsapp);

        // Se uma nova imagem foi selecionada, faz upload dela
        if (novaImagemSelecionada != null) {
            storage.getReference().child("usuarios/" + uid + "/foto_perfil.jpg")
                    .putFile(novaImagemSelecionada)
                    .addOnSuccessListener(task -> task.getStorage().getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                atualizacoes.put("urlFotoPerfil", uri.toString());
                                atualizarFirestore(atualizacoes);
                            }))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show());
        } else {
            // Usa imagem existente
            atualizacoes.put("urlFotoPerfil", urlFotoPerfil != null ? urlFotoPerfil : "default");
            atualizarFirestore(atualizacoes);
        }

        // Atualiza a senha se foi preenchida
        String novaSenha = edtNovaSenha.getText().toString().trim();
        if (!novaSenha.isEmpty()) {
            FirebaseAuth.getInstance().getCurrentUser()
                    .updatePassword(novaSenha)
                    .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Senha atualizada com sucesso!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Erro ao atualizar senha", Toast.LENGTH_LONG).show());
        }
    }

    // Atualiza Firestore
    private void atualizarFirestore(Map<String, Object> atualizacoes) {
        firestore.collection("usuarios").document(uid)
                .update(atualizacoes)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Erro ao atualizar dados", Toast.LENGTH_SHORT).show());
    }
}
