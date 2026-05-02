package br.edu.senac.sistema_ac.domain.enums;

public enum TipoArquivoComprovante {
    PDF,
    IMAGEM;

    public static TipoArquivoComprovante fromContentType(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("Tipo de arquivo nao informado");
        }

        if ("application/pdf".equalsIgnoreCase(contentType)) {
            return PDF;
        }

        if ("image/png".equalsIgnoreCase(contentType)
            || "image/jpeg".equalsIgnoreCase(contentType)
            || "image/jpg".equalsIgnoreCase(contentType)) {
            return IMAGEM;
        }

        throw new IllegalArgumentException("Tipo de arquivo não suportado. Envie PDF, PNG, JPG ou JPEG.");
    }
}
