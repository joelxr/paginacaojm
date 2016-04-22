package br.com.javamagazine.paginacaojm.ejb;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.primefaces.model.SortOrder;

import br.com.javamagazine.paginacaojm.domain.Cliente;

@Stateless
public class ClienteEJB {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@SuppressWarnings("unchecked")
	public List<Cliente> listaClientes() {
		Query query = entityManager.createQuery("Select c from Cliente c order by c.cpf");
	
		return query.getResultList();
	}
	
	public List<Cliente> listaClientesPorDemanda(Map<String, Object> filtros, String campoOrdenacao, SortOrder tipoOrdenacao, int qtdRegistrosPorPagina, int registroInicial) {
		// CriteriaBuilder é uma fabrica de definições de consultas
		CriteriaBuilder cBuilder = entityManager.getCriteriaBuilder();
		// A partir da CriteriaQuery podemos informar de qual tabela vêm os dados, adicionar cláusulas where, etc
        CriteriaQuery<Cliente> cQuery = cBuilder.createQuery(Cliente.class);
        // O EntityType servirá para pegarmos informações sobre a classe da consulta, como o tipo dos atributos 
        EntityType<Cliente> tipo = entityManager.getMetamodel().entity(Cliente.class);
        // Através do Root<T> é possível obter as colunas, fazer joins, usar a clausula "in" e outros
        Root<Cliente> cliente = cQuery.from(Cliente.class);
        
        // se algum filtro foi aplicado no datatable, adiciona as cláusulas no where e os parâmetros na consulta
    	if (!filtros.isEmpty()) {
    		adicionaClausulasNoWhere(filtros, cQuery, cBuilder, cliente, tipo);
		}
    	
    	// se foi aplicado algum critério de ordenação no datatable
    	if (campoOrdenacao != null) {
    		cQuery.orderBy(tipoOrdenacao == SortOrder.ASCENDING ? cBuilder.asc(cliente.get(campoOrdenacao)) : cBuilder.desc(cliente.get(campoOrdenacao)));
    	} else { // senão, ordena pelo nome
    		cQuery.orderBy(cBuilder.asc(cliente.get("nome")));
    	}
    	
    	// A partir da TypedQuery podemos passar os valores dos parâmetros
    	TypedQuery<Cliente> tQuery = entityManager.createQuery(cQuery);
    	
    	// se algum filtro foi aplicado no datatable, substitui os parâmetros pelos valores do filtro
    	if (!filtros.isEmpty()) {
    		substituiParametrosPorValor(filtros, tQuery, tipo);
		}
    	   	
    	// determina a quantidade de registros que deve ser retornada (qtd de linhas por página do datatable)
		tQuery.setMaxResults(qtdRegistrosPorPagina);
		/*
		 *  Determina a partir de que registro deve retornar, por conta da página que o usuário 
		 *  selecionou no DataTable. Se está na página 2 e tem 10 registros por página, registroInicial
		 *  será 10.
		 */
		tQuery.setFirstResult(registroInicial);

        return (List<Cliente>) tQuery.getResultList();
	}

	public Long retornaQuantidadeDeClientes(Map<String, Object> filtros) {
		CriteriaBuilder cBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cQuery = cBuilder.createQuery(Long.class);
        EntityType<Cliente> tipo = entityManager.getMetamodel().entity(Cliente.class);
        Root<Cliente> cliente = cQuery.from(Cliente.class);
        
        // se algum filtro foi aplicado no datatable, adiciona as cláusulas no where e os parâmetros na consulta
    	if (!filtros.isEmpty()) {
    		adicionaClausulasNoWhere(filtros, cQuery, cBuilder, cliente, tipo);
		}
        
        cQuery.select(cBuilder.count(cliente));
        
        Query query = entityManager.createQuery(cQuery);
        
        // se algum filtro foi aplicado no datatable, substitui os parâmetros pelos valores do filtro
    	if (!filtros.isEmpty()) {
    		substituiParametrosPorValor(filtros, query, tipo);
		}
		
		return (Long) query.getSingleResult();
	}
	
	private void substituiParametrosPorValor(Map<String, Object> filtros, Query query, EntityType<Cliente> tipo) {
		for (Iterator<String> it = filtros.keySet().iterator(); it.hasNext();) {
			String propriedadeFiltrada = it.next();
			// A passagem de parâmetros é igual ao do JPQL
			if (tipo.getAttribute(propriedadeFiltrada).getJavaType().equals(String.class)) {
				/*
				 *  Se estiver filtrando um campo do tipo String, pega clientes cujo campo contenha no 
				 *  início, no meio, ou no final o trecho informado no filtro 
				 */
				query.setParameter(propriedadeFiltrada, "%" + filtros.get(propriedadeFiltrada) + "%");
			} else {
				// se o campo não for String, adiciona o valor do campo 
				query.setParameter(propriedadeFiltrada, filtros.get(propriedadeFiltrada));
			}
		}
	}

	private void adicionaClausulasNoWhere(Map<String, Object> filtros, CriteriaQuery<?> cQuery, CriteriaBuilder cBuilder, Root<Cliente> cliente, EntityType<Cliente> tipo) {
		for (Iterator<String> it = filtros.keySet().iterator(); it.hasNext();) {
			String propriedadeFiltrada = it.next();
			
			if (tipo.getAttribute(propriedadeFiltrada).getJavaType().equals(String.class)) {
				/*
				 *  Se estiver filtrando um campo do tipo String, transforma o valor pesquisado e o 
				 *  valor do banco para maiúsculo para ignorar o case do campo e adiciona um parâmetro 
				 *  com o nome do campo pesquisado
				 */
				cQuery.where(cBuilder.like(cBuilder.upper(cliente.get(tipo.getDeclaredSingularAttribute(propriedadeFiltrada, String.class))), cBuilder.upper(cBuilder.parameter(String.class, propriedadeFiltrada))));
			} else {
				/*
				 *  Se o campo não for String, adiciona uma cláusula para verificar se o valor do 
				 *  campo no banco é igual ao valor passado adicionando um parâmetro com o nome 
				 *  do campo pesquisado
				 */
				cQuery.where(cBuilder.equal(cliente.get(propriedadeFiltrada), cBuilder.parameter(tipo.getAttribute(propriedadeFiltrada).getJavaType(), propriedadeFiltrada)));
			}
			
		}		
	}
	
}
