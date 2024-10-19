package br.edu.puccampinas.campusconnect.model;

public class PedidoDeRegistro extends Comunicado{
    private String nome;
    private String email;
    private String senha;


    public PedidoDeRegistro(String nome, String email, String senha){
        this.senha = senha;
        this.nome = nome;
        this.email = email;
    }

    public String getNome(){
        return this.nome;
    }

    public String getEmail(){
        return this.email;
    }

    public String getSenha(){
        return this.senha;
    }

    public String toString(){
        return (""+this.nome+this.email+this.senha);
    }
}
