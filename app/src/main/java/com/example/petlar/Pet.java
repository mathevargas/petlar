package com.example.petlar;

import java.io.Serializable;

// Classe modelo que representa um pet e pode ser enviada entre telas
public class Pet implements Serializable {

    private String nome;
    private String idade;
    private String tipo;
    private String porte;
    private String genero;
    private String raca;
    private String cidade;
    private String estado;
    private String email;
    private String whatsapp;
    private String descricao;
    private String urlImagem;

    // Construtor vazio necessário para o Firebase
    public Pet() {
    }

    // Construtor com todos os campos
    public Pet(String nome, String idade, String tipo, String porte, String genero, String raca,
               String cidade, String estado, String email, String whatsapp,
               String descricao, String urlImagem) {
        this.nome = nome;
        this.idade = idade;
        this.tipo = tipo;
        this.porte = porte;
        this.genero = genero;
        this.raca = raca;
        this.cidade = cidade;
        this.estado = estado;
        this.email = email;
        this.whatsapp = whatsapp;
        this.descricao = descricao;
        this.urlImagem = urlImagem;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getIdade() { return idade; }
    public void setIdade(String idade) { this.idade = idade; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getPorte() { return porte; }
    public void setPorte(String porte) { this.porte = porte; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getUrlImagem() { return urlImagem; }
    public void setUrlImagem(String urlImagem) { this.urlImagem = urlImagem; }
}
