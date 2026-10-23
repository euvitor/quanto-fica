package br.com.quantofica.backend.exception;

/**
 * Exceção lançada quando um recurso solicitado por identificador não é encontrado no banco.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message){
        super(message);
    }
}
