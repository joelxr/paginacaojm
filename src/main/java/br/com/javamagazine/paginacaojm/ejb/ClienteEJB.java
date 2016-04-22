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
		// CriteriaBuilder � uma fabrica de defini��es de consultas
		CriteriaBuilder cBuilder = entityManager.getCriteriaBuilder();
		// A partir da CriteriaQuery podemos informar de qual tabela v�m os dados, adicionar cl�usulas where, etc
        CriteriaQuery<Cliente> cQuery = cBuilder.createQuery(Cliente.class);
        // O EntityType servir� para pegarmos informa��es sobre a classe da consulta, como o tipo dos atributos 
        EntityType<Cliente> tipo = entityManager.getMetamodel().entity(Cliente.class);
        // Atrav�s do Root<T> � poss�vel obter as colunas, fazer joins, usar a clausula "in" e outros
        Root<Cliente> cliente = cQuery.from(Cliente.class);
        
        // se algum filtro foi aplicado no datatable, adiciona as cl�usulas no where e os par�metros na consulta
    	if (!filtros.isEmpty()) {
    		adicionaClausulasNoWhere(filtros, cQuery, cBuilder, cliente, tipo);
		}
    	
    	// se foi aplicado algum crit�rio de ordena��o no datatable
    	if (campoOrdenacao != null) {
    		cQuery.orderBy(tipoOrdenacao == SortOrder.ASCENDING ? cBuilder.asc(cliente.get(campoOrdenacao)) : cBuilder.desc(cliente.get(campoOrdenacao)));
    	} else { // sen�o, ordena pelo nome
    		cQuery.orderBy(cBuilder.asc(cliente.get("nome")));
    	}
    	
    	// A partir da TypedQuery podemos passar os valores dos par�metros
    	TypedQuery<Cliente> tQuery = entityManager.createQuery(cQuery);
    	
    	// se algum filtro foi aplicado no datatable, substitui os par�metros pelos valores do filtro
    	if (!filtros.isEmpty()) {
    		substituiParametrosPorValor(filtros, tQuery, tipo);
		}
    	   	
    	// determina a quantidade de registros que deve ser retornada (qtd de linhas por p�gina do datatable)
		tQuery.setMaxResults(qtdRegistrosPorPagina);
		/*
		 *  Determina a partir de que registro deve retornar, por conta da p�gina que o usu�rio 
		 *  selecionou no DataTable. Se est� na p�gina 2 e tem 10 registros por p�gina, registroInicial
		 *  ser� 10.
		 */
		tQuery.setFirstResult(registroInicial);

        return (List<Cliente>) tQuery.getResultList();
	}

	public Long retornaQuantidadeDeClientes(Map<String, Object> filtros) {
		CriteriaBuilder cBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cQuery = cBuilder.createQuery(Long.class);
        EntityType<Cliente> tipo = entityManager.getMetamodel().entity(Cliente.class);
        Root<Cliente> cliente = cQuery.from(Cliente.class);
        
        // se algum filtro foi aplicado no datatable, adiciona as cl�usulas no where e os par�metros na consulta
    	if (!filtros.isEmpty()) {
    		adicionaClausulasNoWhere(filtros, cQuery, cBuilder, cliente, tipo);
		}
        
        cQuery.select(cBuilder.count(cliente));
        
        Query query = entityManager.createQuery(cQuery);
        
        // se algum filtro foi aplicado no datatable, substitui os par�metros pelos valores do filtro
    	if (!filtros.isEmpty()) {
    		substituiParametrosPorValor(filtros, query, tipo);
		}
		
		return (Long) query.getSingleResult();
	}
	
	private void substituiParametrosPorValor(Map<String, Object> filtros, Query query, EntityType<Cliente> tipo) {
		for (Iterator<String> it = filtros.keySet().iterator(); it.hasNext();) {
			String propriedadeFiltrada = it.next();
			// A passagem de par�metros � igual ao do JPQL
			if (tipo.getAttribute(propriedadeFiltrada).getJavaType().equals(String.class)) {
				/*
				 *  Se estiver filtrando um campo do tipo String, pega clientes cujo campo contenha no 
				 *  in�cio, no meio, ou no final o trecho informado no filtro 
				 */
				query.setParameter(propriedadeFiltrada, "%" + filtros.get(propriedadeFiltrada) + "%");
			} else {
				// se o campo n�o for String, adiciona o valor do campo 
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
				 *  valor do banco para mai�sculo para ignorar o case do campo e adiciona um par�metro 
				 *  com o nome do campo pesquisado
				 */
				cQuery.where(cBuilder.like(cBuilder.upper(cliente.get(tipo.getDeclaredSingularAttribute(propriedadeFiltrada, String.class))), cBuilder.upper(cBuilder.parameter(String.class, propriedadeFiltrada))));
			} else {
				/*
				 *  Se o campo n�o for String, adiciona uma cl�usula para verificar se o valor do 
				 *  campo no banco � igual ao valor passado adicionando um par�metro com o nome 
				 *  do campo pesquisado
				 */
				cQuery.where(cBuilder.equal(cliente.get(propriedadeFiltrada), cBuilder.parameter(tipo.getAttribute(propriedadeFiltrada).getJavaType(), propriedadeFiltrada)));
			}
			
		}		
	}
	
}
