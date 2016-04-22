package br.com.javamagazine.paginacaojm.managedbean;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import br.com.javamagazine.paginacaojm.domain.Cliente;
import br.com.javamagazine.paginacaojm.ejb.ClienteEJB;

@Named
@RequestScoped
public class ClienteBeanSemPaginacao implements Serializable {
	
	private static final long serialVersionUID = -4105606132447888240L;

	@EJB
	private ClienteEJB clienteEJB;
	
	private List<Cliente> listaClientes;
	private List<Cliente> listaClientesFiltrados;

	public String listarSemPaginacao() {
		listaClientes = clienteEJB.listaClientes();
		
		return "listarClientesSemPaginacao";
	}
	
	public List<Cliente> getListaClientes() {
		return listaClientes;
	}

	public void setListaClientes(List<Cliente> listaClientes) {
		this.listaClientes = listaClientes;
	}

	public List<Cliente> getListaClientesFiltrados() {
		return listaClientesFiltrados;
	}

	public void setListaClientesFiltrados(List<Cliente> listaClientesFiltrados) {
		this.listaClientesFiltrados = listaClientesFiltrados;
	}
}
