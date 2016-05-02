package br.com.javamagazine.paginacaojm.paginacao;
 
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import br.com.javamagazine.paginacaojm.domain.Cliente;
import br.com.javamagazine.paginacaojm.ejb.ClienteEJB;
 
public class LazyClienteDataModel extends LazyDataModel<Cliente> {
     
	private static final long serialVersionUID = 6519078577912436151L;
	
	private List<Cliente> listaPaginadaDeClientes;
	
	private ClienteEJB clienteEJB;
	
	public LazyClienteDataModel(ClienteEJB clienteEJB) {
		this.clienteEJB = clienteEJB;
	}
     
    @Override
    public Cliente getRowData(String rowKey) {
        for (Cliente cliente : listaPaginadaDeClientes) {
            if (cliente.getId().equals(rowKey)) {
                return cliente;
            }
        }
 
        return null;
    }
 
    @Override
    public Object getRowKey(Cliente cliente) {
        return cliente.getId();
    }
 
    @Override
    public List<Cliente> load(int primeiroRegistro, int qtdRegistrosPagina, String campoOrdenacao, SortOrder tipoOrdenacao, Map<String, Object> filtros) {
    	try {
    		/*
	    	 * Guardo a quantidade de registros retornada passando o poss�vel filtro 
	    	 * aplicado, sem utilizar a ordena��o porque n�o precisa
	    	 */
			this.setRowCount(clienteEJB.retornaQuantidadeDeClientes(filtros).intValue());
	    	
	    	/*
	    	 * Retorno a lista de clientes devolvida de acordo com o poss�vel filtro aplicado 
	    	 * e utilizando um eventual crit�rio de ordena��o especificado pelo usu�rio
	    	 */
	    	return listaPaginadaDeClientes = clienteEJB.listaClientesPorDemanda(filtros, campoOrdenacao, tipoOrdenacao, qtdRegistrosPagina, primeiroRegistro);
    	} catch (NoSuchFieldException|SecurityException e) {
			e.printStackTrace();
			return null;
		}
    }
}