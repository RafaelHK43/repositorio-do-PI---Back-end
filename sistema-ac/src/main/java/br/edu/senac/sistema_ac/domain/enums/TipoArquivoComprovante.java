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

        if (contentType.startsWith("image/")) {
            return IMAGEM;
        }

        throw new IllegalArgumentException("Tipo de arquivo nao suportado. Envie PDF ou imagem");
    }
}
