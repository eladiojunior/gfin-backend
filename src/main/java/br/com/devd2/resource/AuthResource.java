package br.com.devd2.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.devd2.helper.ResultApiHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.LoginRequest;
import br.com.devd2.models.LoginResponse;
import br.com.devd2.services.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Autenticação", description = "Operações de autenticação de usuários.")
public class AuthResource {

    private final Logger log = LoggerFactory.getLogger(AuthResource.class);

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @PermitAll
    @APIResponse(responseCode = "200", description = "Login realizado com sucesso.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = LoginResponse.class)))
    @APIResponse(responseCode = "400", description = "Credenciais inválidas ou regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro interno ao processar autenticação.")
    @Operation(
            summary = "Autenticar usuário",
            description = "Autentica um usuário com e-mail e senha, retornando um token JWT (Bearer Token) para uso nos demais endpoints.")
    public Response login(LoginRequest request) {
        try {
            LoginResponse response = authService.autenticar(request);
            return Response.ok(response).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao processar autenticação.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }
}
