package br.com.devd2.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.devd2.helper.ResultApiHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.CartaoCreditoEdicaoModel;
import br.com.devd2.services.CartaoCreditoService;
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

@Path("/cartoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("usuario")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cartões", description = "Operações de gerenciamento de cartões de crédito/débito.")
public class CartaoCreditoResource {

    private final Logger log = LoggerFactory.getLogger(CartaoCreditoResource.class);

    @Inject
    CartaoCreditoService cartaoCreditoService;

    @Inject
    JsonWebToken jwt;

    private String idUsuarioLogado() {
        return jwt.getSubject();
    }

    @GET
    @APIResponse(responseCode = "200", description = "Lista os cartões.")
    @Operation(summary = "Listar cartões", description = "Lista cartões do usuário autenticado.")
    public Response listar() {
        try {
            return Response.ok(cartaoCreditoService.listarCartoes(idUsuarioLogado())).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @GET
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Cartão recuperado com sucesso.")
    @APIResponse(responseCode = "400", description = "Cartão não encontrado.")
    @Operation(summary = "Obter cartão", description = "Recupera um cartão pelo seu ID.")
    public Response obter(@PathParam("id") String id) {
        try {
            return Response.ok(cartaoCreditoService.obterCartao(id, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @POST
    @APIResponse(responseCode = "200", description = "Cartão registrado com sucesso.")
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao registrar cartão.")
    @Operation(summary = "Registrar cartão", description = "Cria um novo cartão. A conta corrente é opcional (cartões de alimentação/benefício não precisam de conta).")
    public Response registrar(CartaoCreditoEdicaoModel model) {
        try {
            return Response.ok(cartaoCreditoService.registrarCartao(model, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao registrar cartão.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @PUT
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Cartão alterado com sucesso.")
    @APIResponse(responseCode = "400", description = "Regra de negócio não atendida.")
    @APIResponse(responseCode = "500", description = "Erro ao alterar cartão.")
    @Operation(summary = "Alterar cartão", description = "Atualiza os dados de um cartão existente.")
    public Response alterar(@PathParam("id") String id, CartaoCreditoEdicaoModel model) {
        try {
            return Response.ok(cartaoCreditoService.alterarCartao(id, model, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao alterar cartão.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @PATCH
    @Path("/{id}/situacao/{codigoSituacao}")
    @APIResponse(responseCode = "200", description = "Situação do cartão alterada com sucesso.")
    @APIResponse(responseCode = "400", description = "Situação inválida.")
    @Operation(summary = "Alterar situação do cartão", description = "Altera a situação do cartão: 1=Ativo, 2=Bloqueado, 3=Cancelado.")
    public Response alterarSituacao(@PathParam("id") String id, @PathParam("codigoSituacao") int codigoSituacao) {
        try {
            return Response.ok(cartaoCreditoService.alterarSituacaoCartao(id, codigoSituacao, idUsuarioLogado())).build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao alterar situação do cartão.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Cartão excluído com sucesso.")
    @APIResponse(responseCode = "400", description = "Cartão não encontrado.")
    @APIResponse(responseCode = "500", description = "Erro ao excluir cartão.")
    @Operation(summary = "Excluir cartão", description = "Remove um cartão pelo seu ID.")
    public Response excluir(@PathParam("id") String id) {
        try {
            cartaoCreditoService.excluirCartao(id, idUsuarioLogado());
            return Response.ok().build();
        } catch (ServiceException regra) {
            return Response.status(Status.BAD_REQUEST).entity(ResultApiHelper.toResponseRegra(regra)).build();
        } catch (jakarta.ws.rs.BadRequestException erro) {
            return Response.status(Status.BAD_REQUEST).entity("Identificador invalido").build();
        } catch (Exception erro) {
            log.error("Erro ao excluir cartão.", erro);
            return Response.serverError().entity(ResultApiHelper.toResponseErro(erro)).build();
        }
    }
}
