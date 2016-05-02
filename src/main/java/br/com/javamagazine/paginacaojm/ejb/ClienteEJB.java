package br.com.javamagazine.paginacaojm.ejb;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.primefaces.model.SortOrder;

import br.com.javamagazine.paginacaojm.domain.Cliente;

@Stateless
public class ClienteEJB {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@SuppressWarnings("unchecked")
	public List<Cliente> listaClientes() {
		// CriteriaBuilder � uma fabrica de defini��es de consultas
		CriteriaBuilder cBuilder = entityManager.getCriteriaBuilder();
		// A partir da CriteriaQuery podemos informar de qual tabela v�m os dados, adicionar cl�usulas where, etc
		CriteriaQuery<Cliente> cQuery = cBuilder.createQuery(Cliente.class);
		
		cQuery.from(Cliente.class);
		// A partir de uma Query podemos setar a quantidade m�xima de registros a serem retornados
		Query query = entityManager.createQuery(cQuery);
		
		return (List<Cliente>) query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Cliente> listaClientesPorDemanda(Map<String, Object> filtros, String campoOrdenacao, SortOrder tipoOrdenacao, int qtdRegistrosPorPagina, int registroInicial) throws NoSuchFieldException, SecurityException {
		CriteriaBuilder cBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Cliente> cQuery = cBuilder.createQuery(Cliente.class);
        // Atrav�s do Root<T> � poss�vel obter as colunas, fazer joins, usar a clausula "in" e outros
        Root<Cliente> cliente = cQuery.from(Cliente.class);
        
        // se algum filtro foi aplicado no datatable, adiciona as cl�usulas no where e os par�metros na consulta
    	if (!filtros.isEmpty()) {
    		adicionaClausulasEValoresNoWhere(filtros, cQuery, cBuilder, cliente);
		}
    	
    	// se foi aplicado algum crit�rio de ordena��o no datatable
    	if (campoOrdenacao != null) {
    		cQuery.orderBy(tipoOrdenacao == SortOrder.ASCENDING ? cBuilder.asc(cliente.get(campoOrdenacao)) : cBuilder.desc(cliente.get(campoOrdenacao)));
    	} else { // sen�o, ordena pelo nome
    		cQuery.orderBy(cBuilder.asc(cliente.get("nome")));
    	}
    	
    	Query query = entityManager.createQuery(cQuery);
    	// determina a quantidade de registros que deve ser retornada (qtd de linhas por p�gina do datatable)
		query.setMaxResults(qtdRegistrosPorPagina);
		/*
		 *  Determina a partir de que registro deve retornar, por conta da p�gina que o usu�rio 
		 *  selecionou no DataTable. Se est� na p�gina 2 e tem 10 registros por p�gina, registroInicial
		 *  ser� 10.
		 */
		query.setFirstResult(registroInicial);

        return (List<Cliente>) query.getResultList();
	}

	public Long retornaQuantidadeDeClientes(Map<String, Object> filtros) throws NoSuchFieldException, SecurityException {
		CriteriaBuilder cBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cQuery = cBuilder.createQuery(Long.class);
        Root<Cliente> cliente = cQuery.from(Cliente.class);
        
        // se algum filtro foi aplicado no datatable, adiciona as cl�usulas no where e os par�metros na consulta
    	if (!filtros.isEmpty()) {
    		adicionaClausulasEValoresNoWhere(filtros, cQuery, cBuilder, cliente);
		}
        
        cQuery.select(cBuilder.count(cliente));
        Query query = entityManager.createQuery(cQuery);
        
		return (Long) query.getSingleResult();
	}
	
	private void adicionaClausulasEValoresNoWhere(Map<String, Object> filtros, CriteriaQuery<?> cQuery, CriteriaBuilder cBuilder, Root<Cliente> cliente) {
		for (Iterator<String> it = filtros.keySet().iterator(); it.hasNext();) {
			String propriedadeFiltrada = it.next();
			
			if (cliente.get(propriedadeFiltrada).getJavaType().equals(String.class)) {
				/*
				 *  Se estiver filtrando um campo do tipo String, transforma o valor pesquisado e o 
				 *  valor do banco para mai�sculo para ignorar o case do campo e adiciona um par�metro 
				 *  com o nome do campo pesquisado
				 */
				
				Path<String> path = cliente.get(propriedadeFiltrada);
	    	    cQuery.where(cBuilder.like(cBuilder.upper(path), ((String) "%" + filtros.get(propriedadeFiltrada)).toUpperCase() + "%"));
			} else {
				/*
				 *  Se o campo n�o for String, adiciona uma cl�usula para verificar se o valor do 
				 *  campo no banco � igual ao valor passado adicionando um par�metro com o nome 
				 *  do campo pesquisado
				 */
				Path<Object> path = cliente.get(propriedadeFiltrada);
				cQuery.where(cBuilder.equal(path, filtros.get(propriedadeFiltrada)));
			}
		}
	}
	
}
