package br.com.devd2.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.devd2.helper.ResultApiHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.UsuarioEdicaoModel;
import br.com.devd2.services.UsuarioService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Usuários", description = "Operações de gerenciamento de usuários do sistema.")
public class UsuarioResource {

    private final Logger log = LoggerFactory.getLogger(UsuarioResource.class);

    @Inject
    UsuarioService usuarioService;

    @Inject JsonWebToken jwt;

    private String idUsuarioLogado() { return jwt.getSubject(); }

    @GET
    @RolesAllowed("usuario")
    @SecurityRequirement(name = "bearerAuth")
    @APIResponse(responseCode = "200", description = "Lista o usuário autenticado.")
    @Operation(summary = "Listar usuários", description = "Retorna apenas o usuário autenticado.")
    public Response listar() {
        try {
            return Response.ok(usuarioService.listarUsuarios(idUsuarioLogado())).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @GET
    @RolesAllowed("usuario")
    @SecurityRequirement(name = "bearerAuth")
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Usuário recuperado com sucesso.")
    @APIResponse(responseCode = "400", description = "Usuário não encontrado.")
    @Operation(summary = "Obter usuário", description = "Recupera um usuário pelo seu ID.")
    public Response obter(@PathParam("id") String id) {
        try {
            return Response.ok(usuarioService.obterUsuario(id, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @POST
    @PermitAll
    @APIResponse(responseCode = "200", description = "Usuário registrado com sucesso.")
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao registrar usuário.")
    @Operation(summary = "Registrar usuário", description = "Cria um novo usuário no sistema. Este endpoint é público e não requer autenticação.")
    public Response registrar(UsuarioEdicaoModel model) {
        try {
            return Response.ok(usuarioService.registrarUsuario(model)).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao registrar usuário.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @PUT
    @RolesAllowed("usuario")
    @SecurityRequirement(name = "bearerAuth")
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Usuário alterado com sucesso.")
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao alterar usuário.")
    @Operation(summary = "Alterar usuário", description = "Atualiza os dados de um usuário existente.")
    public Response alterar(@PathParam("id") String id, UsuarioEdicaoModel model) {
        try {
            return Response.ok(usuarioService.alterarUsuario(id, model, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao alterar usuário.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @DELETE
    @RolesAllowed("usuario")
    @SecurityRequirement(name = "bearerAuth")
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Usuário inativado com sucesso.")
    @APIResponse(responseCode = "400", description = "Usuário não encontrado.")
    @APIResponse(responseCode = "500", description = "Erro ao excluir usuário.")
    @Operation(summary = "Excluir usuário", description = "Inativa um usuário pelo seu ID.")
    public Response excluir(@PathParam("id") String id) {
        try {
            usuarioService.excluirUsuario(id, idUsuarioLogado());
            return Response.ok().build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao excluir usuário.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }
}
