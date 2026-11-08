package br.com.devd2.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.devd2.helper.ResultApiHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.ContaCorrenteEdicaoModel;
import br.com.devd2.services.ContaCorrenteService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/contas-correntes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("usuario")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Contas Correntes", description = "Operações de gerenciamento de contas correntes.")
public class ContaCorrenteResource {

    private final Logger log = LoggerFactory.getLogger(ContaCorrenteResource.class);

    @Inject
    ContaCorrenteService contaCorrenteService;

    @Inject
    JsonWebToken jwt;

    private String idUsuarioLogado() {
        return jwt.getSubject();
    }

    @GET
    @APIResponse(responseCode = "200", description = "Lista as contas correntes.")
    @Operation(summary = "Listar contas correntes", description = "Lista contas correntes do usuário autenticado.")
    public Response listar() {
        try {
            return Response.ok(contaCorrenteService.listarContasCorrentes(idUsuarioLogado())).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @GET
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Conta corrente recuperada com sucesso.")
    @APIResponse(responseCode = "400", description = "Conta corrente não encontrada.")
    @Operation(summary = "Obter conta corrente", description = "Recupera uma conta corrente pelo seu ID.")
    public Response obter(@PathParam("id") String id) {
        try {
            return Response.ok(contaCorrenteService.obterContaCorrente(id, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @POST
    @APIResponse(responseCode = "200", description = "Conta corrente registrada com sucesso.")
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao registrar conta corrente.")
    @Operation(summary = "Registrar conta corrente", description = "Cria uma nova conta corrente.")
    public Response registrar(ContaCorrenteEdicaoModel model) {
        try {
            return Response.ok(contaCorrenteService.registrarContaCorrente(model, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao registrar conta corrente.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @PUT
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Conta corrente alterada com sucesso.")
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao alterar conta corrente.")
    @Operation(summary = "Alterar conta corrente", description = "Atualiza os dados de uma conta corrente existente.")
    public Response alterar(@PathParam("id") String id, ContaCorrenteEdicaoModel model) {
        try {
            return Response.ok(contaCorrenteService.alterarContaCorrente(id, model, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao alterar conta corrente.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @PATCH
    @Path("/{id}/ativar")
    @APIResponse(responseCode = "200", description = "Conta corrente ativada com sucesso.")
    @Operation(summary = "Ativar conta corrente", description = "Ativa uma conta corrente pelo seu ID.")
    public Response ativar(@PathParam("id") String id) {
        try {
            return Response.ok(contaCorrenteService.ativarDesativarContaCorrente(id, true, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao ativar conta corrente.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @PATCH
    @Path("/{id}/desativar")
    @APIResponse(responseCode = "200", description = "Conta corrente desativada com sucesso.")
    @Operation(summary = "Desativar conta corrente", description = "Desativa uma conta corrente pelo seu ID.")
    public Response desativar(@PathParam("id") String id) {
        try {
            return Response.ok(contaCorrenteService.ativarDesativarContaCorrente(id, false, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao desativar conta corrente.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Conta corrente excluída com sucesso.")
    @APIResponse(responseCode = "400", description = "Não foi possível excluir a conta corrente.")
    @APIResponse(responseCode = "500", description = "Erro ao excluir conta corrente.")
    @Operation(summary = "Excluir conta corrente", description = "Remove uma conta corrente. Não permite exclusão se houver cartões vinculados.")
    public Response excluir(@PathParam("id") String id) {
        try {
            contaCorrenteService.excluirContaCorrente(id, idUsuarioLogado());
            return Response.ok().build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao excluir conta corrente.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }
}
