package br.edu.senac.sistema_ac.exception;

public class LimiteHorasExcedidoException extends RuntimeException {

    public LimiteHorasExcedidoException(String message) {
        super(message);
    }
}
