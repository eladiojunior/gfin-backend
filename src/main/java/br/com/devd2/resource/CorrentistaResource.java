package br.com.devd2.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.devd2.helper.ResultApiHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.CorrentistaEdicaoModel;
import br.com.devd2.services.CorrentistaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/correntistas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("usuario")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Correntistas", description = "Operações de gerenciamento da agenda de correntistas (beneficiários/favorecidos).")
public class CorrentistaResource {

    private final Logger log = LoggerFactory.getLogger(CorrentistaResource.class);

    @Inject
    CorrentistaService correntistaService;

    @Inject
    JsonWebToken jwt;

    private String idUsuarioLogado() {
        return jwt.getSubject();
    }

    @GET
    @APIResponse(responseCode = "200", description = "Lista os correntistas.")
    @Operation(summary = "Listar correntistas", description = "Lista correntistas do usuário autenticado.")
    public Response listar() {
        try {
            return Response.ok(correntistaService.listarCorrentistas(idUsuarioLogado())).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @GET
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Correntista recuperado com sucesso.")
    @APIResponse(responseCode = "400", description = "Correntista não encontrado.")
    @Operation(summary = "Obter correntista", description = "Recupera um correntista pelo seu ID.")
    public Response obter(@PathParam("id") String id) {
        try {
            return Response.ok(correntistaService.obterCorrentista(id, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @POST
    @APIResponse(responseCode = "200", description = "Correntista registrado com sucesso.")
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao registrar correntista.")
    @Operation(summary = "Registrar correntista", description = "Adiciona um novo correntista à agenda.")
    public Response registrar(CorrentistaEdicaoModel model) {
        try {
            return Response.ok(correntistaService.registrarCorrentista(model, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao registrar correntista.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @PUT
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Correntista alterado com sucesso.")
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao alterar correntista.")
    @Operation(summary = "Alterar correntista", description = "Atualiza os dados de um correntista existente.")
    public Response alterar(@PathParam("id") String id, CorrentistaEdicaoModel model) {
        try {
            return Response.ok(correntistaService.alterarCorrentista(id, model, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao alterar correntista.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Correntista excluído com sucesso.")
    @APIResponse(responseCode = "400", description = "Correntista não encontrado.")
    @APIResponse(responseCode = "500", description = "Erro ao excluir correntista.")
    @Operation(summary = "Excluir correntista", description = "Remove um correntista da agenda.")
    public Response excluir(@PathParam("id") String id) {
        try {
            correntistaService.excluirCorrentista(id, idUsuarioLogado());
            return Response.ok().build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao excluir correntista.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }
}
