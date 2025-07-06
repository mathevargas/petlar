package com.example.petlar;

import com.google.firebase.Timestamp;

// Classe que representa um documento da coleção "adotados"
public class Adocao {
    private String idPet;
    private String uidAdotante;
    private Timestamp dataAdocao;
    private String mensagem;

    public Adocao() {
        // Construtor vazio necessário para Firebase
    }

    public Adocao(String idPet, String uidAdotante, Timestamp dataAdocao, String mensagem) {
        this.idPet = idPet;
        this.uidAdotante = uidAdotante;
        this.dataAdocao = dataAdocao;
        this.mensagem = mensagem;
    }

    // Getters e setters
    public String getIdPet() {
        return idPet;
    }

    public void setIdPet(String idPet) {
        this.idPet = idPet;
    }

    public String getUidAdotante() {
        return uidAdotante;
    }

    public void setUidAdotante(String uidAdotante) {
        this.uidAdotante = uidAdotante;
    }

    public Timestamp getDataAdocao() {
        return dataAdocao;
    }

    public void setDataAdocao(Timestamp dataAdocao) {
        this.dataAdocao = dataAdocao;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}