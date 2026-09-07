package pe.financiera.bs.pagoservicios.service_payment.domain.port.out;


import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoResult;


public interface ProductoPort {

    ProductoResult buscarProducto(ProductoCommand productoCommand);
}
