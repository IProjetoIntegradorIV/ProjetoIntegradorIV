package br.edu.puccampinas.campusconnect.model;

public class Resultado extends Comunicado{
    private String valorResultante;

    public Resultado(String valorResultante){
        this.valorResultante = valorResultante;
    }

    public String getValorResultante() {
        return this.valorResultante;
    }

    public String toString(){
        return (""+this.valorResultante);
    }
}
