package br.com.javamagazine.paginacaojm.managedbean;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;

import br.com.javamagazine.paginacaojm.domain.Cliente;
import br.com.javamagazine.paginacaojm.ejb.ClienteEJB;
import br.com.javamagazine.paginacaojm.paginacao.LazyClienteDataModel;

@Named
@SessionScoped
public class ClienteBeanComPaginacao implements Serializable {
	
	private static final long serialVersionUID = -5491025504258538323L;

	@EJB
	private ClienteEJB clienteEJB;
	
	private LazyClienteDataModel listaClientesLazyModel;
	
	@PostConstruct
	public void inicializaDataModel() {
		listaClientesLazyModel = new LazyClienteDataModel(clienteEJB);
	}
	
	public String listarComPaginacao() {
		
		return "listarClientesComPaginacao";
	}

	public LazyDataModel<Cliente> getListaClientesLazyModel() {
        return listaClientesLazyModel;
    }
}
