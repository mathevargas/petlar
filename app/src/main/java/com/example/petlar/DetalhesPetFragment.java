package com.example.petlar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

// Fragmento responsável por exibir os detalhes do pet selecionado
public class DetalhesPetFragment extends Fragment {

    private ImageView imgPet;
    private TextView txtNome, txtInfoBasica, txtGeneroRaca, txtLocalizacao, txtDescricao, txtPublicador;
    private Button btnWhatsApp, btnEmail;

    private Pet petSelecionado;

    // URL da imagem padrão para pets sem imagem personalizada
    private static final String URL_IMAGEM_PADRAO = "https://firebasestorage.googleapis.com/v0/b/SEU_PROJETO.appspot.com/o/ic_pet_dog_cat.png?alt=media";

    public DetalhesPetFragment() {
        // Construtor vazio obrigatório
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Infla o layout da tela de detalhes
        View view = inflater.inflate(R.layout.fragment_detalhes_pet, container, false);

        // Referência aos componentes da interface
        imgPet = view.findViewById(R.id.imgDetalhePet);
        txtNome = view.findViewById(R.id.txtNomeDetalhe);
        txtInfoBasica = view.findViewById(R.id.txtInfoBasica);
        txtGeneroRaca = view.findViewById(R.id.txtGeneroRaca);
        txtLocalizacao = view.findViewById(R.id.txtLocalizacao);
        txtDescricao = view.findViewById(R.id.txtDescricao);
        txtPublicador = view.findViewById(R.id.txtPublicador);
        btnWhatsApp = view.findViewById(R.id.btnWhatsApp);
        btnEmail = view.findViewById(R.id.btnEmail);

        // Verifica se o pet foi passado por argumento
        if (getArguments() != null) {
            petSelecionado = (Pet) getArguments().getSerializable("pet");

            if (petSelecionado != null) {
                exibirDadosPet(petSelecionado);
            }
        }

        return view;
    }

    // Exibe as informações do pet na tela
    private void exibirDadosPet(Pet pet) {
        txtNome.setText(pet.getNome());
        txtInfoBasica.setText(pet.getIdade() + " - " + pet.getTipo() + " - " + pet.getPorte());
        txtGeneroRaca.setText(pet.getGenero() + " - " + (!TextUtils.isEmpty(pet.getRaca()) ? pet.getRaca() : "Sem raça definida"));
        txtLocalizacao.setText(pet.getCidade() + " - " + pet.getEstado());
        txtDescricao.setText(TextUtils.isEmpty(pet.getDescricao()) ? "Sem descrição disponível." : pet.getDescricao());

        String url = pet.getUrlImagem();
        if (TextUtils.isEmpty(url)) url = URL_IMAGEM_PADRAO;

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_pet_dog_cat)
                .error(R.drawable.ic_pet_dog_cat)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgPet);

        boolean estaLogado = FirebaseAuth.getInstance().getCurrentUser() != null;

        if (estaLogado) {
            btnWhatsApp.setVisibility(View.VISIBLE);
            btnEmail.setVisibility(View.VISIBLE);

            btnWhatsApp.setOnClickListener(v -> {
                String numero = pet.getWhatsapp().replaceAll("\\D", "");
                String mensagem = "Olá! Vi o pet " + pet.getNome() + " no app PetLar e gostaria de saber mais.";
                String uri = "https://wa.me/" + numero + "?text=" + Uri.encode(mensagem);

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            });

            btnEmail.setOnClickListener(v -> {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + pet.getEmail()));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Interesse no pet: " + pet.getNome());
                emailIntent.putExtra(Intent.EXTRA_TEXT,
                        "Olá! Vi o pet " + pet.getNome() + " no aplicativo PetLar e estou interessado(a) em saber mais informações. Você pode me contar mais sobre ele?");

                startActivity(Intent.createChooser(emailIntent, "Enviar e-mail"));
            });

            carregarNomePublicador(pet.getUidUsuario());

        } else {
            btnWhatsApp.setVisibility(View.GONE);
            btnEmail.setVisibility(View.GONE);
            txtPublicador.setVisibility(View.GONE);
        }
    }

    // Busca o nome do usuário publicador no Firestore e exibe
    private void carregarNomePublicador(String uidUsuario) {
        FirebaseFirestore.getInstance().collection("usuarios")
                .document(uidUsuario)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nome = documentSnapshot.getString("nome");

                        txtPublicador.setText("Publicado por: " + nome);
                        txtPublicador.setVisibility(View.VISIBLE);

                        txtPublicador.setOnClickListener(v -> {
                            PerfilPublicoFragment perfilPublicoFragment = new PerfilPublicoFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("uidUsuario", uidUsuario);
                            perfilPublicoFragment.setArguments(bundle);

                            if (getActivity() != null) {
                                getActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, perfilPublicoFragment)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        });
                    } else {
                        txtPublicador.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> txtPublicador.setVisibility(View.GONE));
    }
}
