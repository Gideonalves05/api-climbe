package com.climbe.api_climbe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "AtualizarEmpresa", description = "Payload de atualização de empresa contratante")
public record AtualizarEmpresaDto(
        @NotBlank(message = "Nome fantasia é obrigatório")
        @Size(max = 255)
        String nomeFantasia,

        @Size(max = 255)
        String razaoSocial,

        @NotBlank(message = "CNPJ é obrigatório")
        @Pattern(regexp = "^[0-9./\\-]{14,18}$", message = "CNPJ inválido")
        String cnpj,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 255)
        String email,

        @Size(max = 50)
        String telefone,

        @Size(max = 255)
        String representanteNome,

        @Email(message = "E-mail do representante inválido")
        @Size(max = 255)
        String representanteEmail,

        @Size(max = 50)
        String representanteContato,

        @Size(max = 14)
        String representanteCpf,

        @Size(max = 255)
        String logradouro,
        @Size(max = 255)
        String numero,
        @Size(max = 255)
        String bairro,
        @Size(max = 255)
        String cidade,
        @Size(max = 2)
        String uf,
        @Size(max = 9)
        String cep
) {
}
