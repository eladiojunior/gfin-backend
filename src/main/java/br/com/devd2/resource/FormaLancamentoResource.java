package br.com.devd2.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.devd2.helper.ResultApiHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.FormaLancamentoModel;
import br.com.devd2.services.FormaLancamentoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
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

@Path("/formas-lancamento")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("usuario")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Formas de lançamento", description = "Operações de gerenciamento das formas de lançamento.")
public class FormaLancamentoResource {

    private final Logger log = LoggerFactory.getLogger(FormaLancamentoResource.class);

    @Inject
    FormaLancamentoService formaLancamentoService;

    @Inject
    JsonWebToken jwt;

    private String idUsuarioLogado() {
        return jwt.getSubject();
    }

    @GET
    @APIResponse(responseCode = "200", description = "Lista as formas de lançamento.")
    @Operation(summary = "Listar formas de lançamento", description = "Lista as formas do usuário autenticado.")
    public Response listar() {
        try {
            return Response.ok(formaLancamentoService.listar(idUsuarioLogado())).build();
        } catch (Exception erro) {
            log.error("Erro ao listar formas de lançamento.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @GET
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Forma de lançamento recuperada com sucesso.")
    @APIResponse(responseCode = "400", description = "Forma de lançamento não encontrada ou identificador inválido.")
    @Operation(summary = "Obter forma de lançamento", description = "Recupera uma forma do usuário autenticado pelo ID.")
    public Response obter(@PathParam("id") String id) {
        try {
            return Response.ok(formaLancamentoService.model(formaLancamentoService.obter(id, idUsuarioLogado()))).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao obter forma de lançamento.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @POST
    @APIResponse(responseCode = "200", description = "Forma de lançamento registrada com sucesso.")
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @Operation(summary = "Registrar forma de lançamento", description = "Cria uma forma para o usuário autenticado.")
    public Response criar(FormaLancamentoModel model) {
        try {
            return Response.ok(formaLancamentoService.criar(model, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao registrar forma de lançamento.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @PUT
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Forma de lançamento alterada com sucesso.")
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @Operation(summary = "Alterar forma de lançamento", description = "Atualiza uma forma do usuário autenticado.")
    public Response alterar(@PathParam("id") String id, FormaLancamentoModel model) {
        try {
            return Response.ok(formaLancamentoService.alterar(id, model, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao alterar forma de lançamento.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Forma de lançamento excluída ou inativada com sucesso.")
    @APIResponse(responseCode = "400", description = "Forma de lançamento não encontrada ou identificador inválido.")
    @Operation(summary = "Excluir forma de lançamento", description = "Remove ou inativa uma forma do usuário autenticado.")
    public Response excluir(@PathParam("id") String id) {
        try {
            formaLancamentoService.excluir(id, idUsuarioLogado());
            return Response.ok().build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao excluir forma de lançamento.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }
}
