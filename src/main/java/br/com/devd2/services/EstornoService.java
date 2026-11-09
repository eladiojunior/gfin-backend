package br.com.devd2.services;

import static br.com.devd2.helper.Identificadores.oid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Decimal128;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.HistoricoEstornoModel;

@ApplicationScoped
public class EstornoService {
    @Inject MongoClient mongo;
    @ConfigProperty(name = "quarkus.mongodb.database") String banco;
    private MongoCollection<Document> colecao(boolean despesa) { return mongo.getDatabase(banco).getCollection(despesa ? "tb_despesa_mensal" : "tb_receita_mensal"); }
    private String estado(boolean despesa) { return despesa ? "hasDespesaLiquidada" : "isReceitaLiquidada"; }
    private Bson dono(String id, String usuario) { return Filters.and(Filters.eq("_id", oid(id)), Filters.eq("idUsuario", usuario)); }

    public void liquidarDespesa(String id, String usuario, String forma, String vinculo, java.math.BigDecimal desconto, java.math.BigDecimal juros, java.math.BigDecimal valorOriginal) throws ServiceException {
        var alteracoes = Updates.combine(Updates.set("hasDespesaLiquidada", true), Updates.set("versaoLiquidacao", new org.bson.types.ObjectId().toHexString()), Updates.set("dataHoraLiquidacaoDespesa", new Date()),
            Updates.set("idFormaLancamento", forma), Updates.set("codigoVinculoFormaLiquidacao", vinculo),
            Updates.set("valorDescontoLiquidacaoDespesa", new Decimal128(desconto)), Updates.set("valorMultaJurosLiquidacaoDespesa", new Decimal128(juros)),
            Updates.set("valorTotalLiquidacaoDespesa", new Decimal128(valorOriginal.subtract(desconto).add(juros))));
        var filtro = Filters.and(dono(id, usuario), Filters.ne("hasDespesaLiquidada", true), Filters.eq("valorDespesa", new Decimal128(valorOriginal)));
        if (colecao(true).updateOne(filtro, alteracoes).getModifiedCount() != 1) throw new ServiceException("Lançamento alterado ou já liquidado; consulte o estado atual.");
    }
    public void liquidarReceita(String id, String usuario, String forma, java.math.BigDecimal total) throws ServiceException {
        var filtro = Filters.and(dono(id, usuario), Filters.ne("isReceitaLiquidada", true));
        var alteracoes = Updates.combine(Updates.set("isReceitaLiquidada", true), Updates.set("versaoLiquidacao", new org.bson.types.ObjectId().toHexString()), Updates.set("dataHoraLiquidacaoReceita", new Date()),
            Updates.set("idFormaLancamento", forma), Updates.set("valorTotalLiquidacaoReceita", new Decimal128(total)));
        if (colecao(false).updateOne(filtro, alteracoes).getModifiedCount() != 1) throw new ServiceException("Lançamento alterado ou já liquidado; consulte o estado atual.");
    }

    private Bson historicoIgual(java.util.List<?> historico) {
        if (historico == null || historico.isEmpty()) return Filters.or(Filters.exists("historicoEstornos", false), Filters.eq("historicoEstornos", null), Filters.size("historicoEstornos", 0));
        return Filters.size("historicoEstornos", historico.size());
    }
    public void atualizarDespesa(br.com.devd2.entites.DespesaMensalEntity entidade) throws ServiceException {
        Bson filtro = Filters.and(dono(entidade.id.toHexString(), entidade.idUsuario), Filters.ne("hasDespesaLiquidada", true), historicoIgual(entidade.historicoEstornos));
        if (br.com.devd2.entites.DespesaMensalEntity.<br.com.devd2.entites.DespesaMensalEntity>mongoCollection().replaceOne(filtro, entidade).getMatchedCount() != 1)
            throw new ServiceException("Lançamento alterado por outra operação; consulte o estado atual.");
    }
    public void atualizarReceita(br.com.devd2.entites.ReceitaMensalEntity entidade) throws ServiceException {
        Bson filtro = Filters.and(dono(entidade.id.toHexString(), entidade.idUsuario), Filters.ne("isReceitaLiquidada", true), historicoIgual(entidade.historicoEstornos));
        if (br.com.devd2.entites.ReceitaMensalEntity.<br.com.devd2.entites.ReceitaMensalEntity>mongoCollection().replaceOne(filtro, entidade).getMatchedCount() != 1)
            throw new ServiceException("Lançamento alterado por outra operação; consulte o estado atual.");
    }
    public void excluirPendente(String id, String usuario, boolean despesa) throws ServiceException {
        Bson filtro = Filters.and(dono(id, usuario), Filters.ne(estado(despesa), true), historicoIgual(null));
        if (colecao(despesa).deleteOne(filtro).getDeletedCount() != 1)
            throw new ServiceException("Lançamento alterado, liquidado ou com histórico de estornos; exclusão não permitida.");
    }

    public void estornar(String id, String usuario, boolean despesa) throws ServiceException {
        var collection = colecao(despesa);
        Document atual = collection.find(dono(id, usuario)).first();
        if (atual == null) throw new ServiceException("Lançamento mensal não encontrado.");
        String estado = estado(despesa);
        if (!Boolean.TRUE.equals(atual.getBoolean(estado))) throw new ServiceException("Lançamento mensal já está pendente.");
        String sufixo = despesa ? "Despesa" : "Receita";
        String data = "dataHoraLiquidacao" + sufixo;
        Document historico = new Document("dataHoraEstorno", new Date())
                .append("dataHoraLiquidacao", atual.get(data))
                .append("idFormaLancamento", atual.get("idFormaLancamento"))
                .append("valorTotalLiquidacao", atual.get("valorTotalLiquidacao" + sufixo));
        if (despesa) historico.append("codigoVinculoFormaLiquidacao", atual.get("codigoVinculoFormaLiquidacao"))
                .append("valorDescontoLiquidacao", atual.get("valorDescontoLiquidacaoDespesa"))
                .append("valorMultaJurosLiquidacao", atual.get("valorMultaJurosLiquidacaoDespesa"));
        List<Bson> alteracoes = new ArrayList<>();
        alteracoes.add(Updates.push("historicoEstornos", historico));
        alteracoes.add(Updates.set(estado, false));
        alteracoes.add(Updates.unset(data));
        alteracoes.add(Updates.unset("valorTotalLiquidacao" + sufixo));
        if (despesa) { alteracoes.add(Updates.unset("valorDescontoLiquidacaoDespesa")); alteracoes.add(Updates.unset("valorMultaJurosLiquidacaoDespesa")); }
        var filtro = Filters.and(dono(id, usuario), Filters.eq(estado, true), Filters.eq(data, atual.get(data)), atual.get("versaoLiquidacao") == null ? Filters.or(Filters.exists("versaoLiquidacao", false), Filters.eq("versaoLiquidacao", null)) : Filters.eq("versaoLiquidacao", atual.get("versaoLiquidacao")));
        if (collection.updateOne(filtro, Updates.combine(alteracoes)).getModifiedCount() != 1)
            throw new ServiceException("Lançamento alterado por outra operação; consulte o estado atual.");
    }
    public List<HistoricoEstornoModel> historico(String id, String usuario, boolean despesa) throws ServiceException {
        Document atual = colecao(despesa).find(dono(id, usuario)).first();
        if (atual == null) throw new ServiceException("Lançamento mensal não encontrado.");
        var resultado = new ArrayList<HistoricoEstornoModel>();
        List<Document> lista = atual.getList("historicoEstornos", Document.class);
        if (lista == null) return resultado;
        for (Document item : lista) {
            var h = new HistoricoEstornoModel();
            h.dataHoraEstorno = data(item.get("dataHoraEstorno"));
            h.dataHoraLiquidacao = data(item.get("dataHoraLiquidacao"));
            h.idFormaLancamento = item.getString("idFormaLancamento");
            h.codigoVinculoFormaLiquidacao = item.getString("codigoVinculoFormaLiquidacao");
            h.valorTotalLiquidacao = valor(item.get("valorTotalLiquidacao"));
            h.valorDescontoLiquidacao = valor(item.get("valorDescontoLiquidacao"));
            h.valorMultaJurosLiquidacao = valor(item.get("valorMultaJurosLiquidacao"));
            resultado.add(h);
        }
        return resultado;
    }
    private String data(Object valor) { return valor instanceof Date d ? d.toInstant().toString() : null; }
    private java.math.BigDecimal valor(Object valor) {
        if (valor instanceof Decimal128 d) return d.bigDecimalValue();
        if (valor instanceof Number n) return new java.math.BigDecimal(n.toString());
        if (valor instanceof String s) return new java.math.BigDecimal(s);
        return null;
    }
}
