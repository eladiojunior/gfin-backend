package br.com.devd2.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import br.com.devd2.entites.UsuarioEntity;
import br.com.devd2.enums.TipoSituacaoEnum;
import br.com.devd2.helper.UtilHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.LoginRequest;
import br.com.devd2.models.LoginResponse;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AuthService {

    private final Logger log = LoggerFactory.getLogger(AuthService.class);

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "gfin-backend")
    String issuer;

    @ConfigProperty(name = "gfin.jwt.expiration.seconds", defaultValue = "3600")
    long expirationSeconds;

    public LoginResponse autenticar(LoginRequest request) throws ServiceException {

        if (request == null) {
            throw new ServiceException("Credenciais não informadas.");
        }
        if (request.emailUsuario == null || request.emailUsuario.trim().isEmpty()) {
            throw new ServiceException("E-mail não informado.");
        }
        if (request.senhaUsuario == null || request.senhaUsuario.trim().isEmpty()) {
            throw new ServiceException("Senha não informada.");
        }

        UsuarioEntity usuario = UsuarioEntity.find("emailUsuario", request.emailUsuario.trim().toLowerCase())
                .<UsuarioEntity>firstResultOptional()
                .orElseThrow(() -> new ServiceException("Credenciais inválidas."));

        if (usuario.codigoTipoSituacaoUsuario == TipoSituacaoEnum.Inativo.getCodigo()) {
            throw new ServiceException("Usuário inativo. Entre em contato com o suporte.");
        }

        String hashSenhaInformada = UtilHelper.gerarHashPassword(request.senhaUsuario, usuario.saltSenhaUsuario);
        if (!hashSenhaInformada.equals(usuario.senhaUsuario)) {
            log.warn("Tentativa de login com senha invalida para o e-mail: {}", request.emailUsuario);
            throw new ServiceException("Credenciais inválidas.");
        }

        String token = gerarToken(usuario);

        //atualizar último login...
        usuario.dataUltimoAcessoUsuario = LocalDateTime.now();
        usuario.salvar();

        log.info("Login realizado com sucesso para o usuario: {} (ID: {})", usuario.emailUsuario, usuario.id);

        return new LoginResponse(token, expirationSeconds, usuario.nomeUsuario, usuario.emailUsuario);
    }

    private String gerarToken(UsuarioEntity usuario) {

        //verificar se tem data de ultimo aesso.
        usuario.dataUltimoAcessoUsuario =
                (usuario.dataUltimoAcessoUsuario == null) ? LocalDateTime.now() : usuario.dataUltimoAcessoUsuario;

        return Jwt.issuer(issuer)
                .subject(String.valueOf(usuario.id))
                .groups(Set.of("usuario"))
                .claim("email", usuario.emailUsuario)
                .claim("nome", usuario.nomeUsuario)
                .claim("dataUltimoAcesso", usuario.dataUltimoAcessoUsuario.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .expiresIn(Duration.ofSeconds(expirationSeconds))
                .sign();

    }
}
