package pe.financiera.bs.pagoservicios.service_payment.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizacionCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationResponse;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationType;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Transaction;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.CompleteBillPaymentUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.OperationPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ProductoPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.TrxOhPayPort;

import static pe.financiera.bs.pagoservicios.service_payment.application.service.constant.ServicesConstant.BLOQ_N;
import static pe.financiera.bs.pagoservicios.service_payment.application.service.constant.ServicesConstant.DEB_FISICO;
import static pe.financiera.bs.pagoservicios.service_payment.application.service.constant.ServicesConstant.DEB_VIRTUAL;
import static pe.financiera.bs.pagoservicios.service_payment.application.service.constant.ServicesConstant.PRINCIPAL;


@Slf4j
@RequiredArgsConstructor
public class CompleteBillPaymentInteractor implements CompleteBillPaymentUseCase {


    private final TrxOhPayPort trxOhPayPort;

    private final OperationPort operationPort;

    private final ProductoPort productoPort;

    @Override
    public void execute(CompleteBillPaymentCommand command) {
        log.info("V2 Interactor: Processing post-payment for operation {}", command.operationNumber());

        ResolvedOperation resolved = resolveOperation(command);
        if (resolved == null) {
            return;
        }

        if (resolved.userId == null) {
            log.error("User id could not be resolved for operation {}", resolved.operation.getOperationId());
            return;
        }

        if (command.success()) {
            handleSuccess(resolved);
        } else {
            handleFailure(command, resolved);
        }

        //TODO publicar topico


    }

    private ResolvedOperation resolveOperation(CompleteBillPaymentCommand command) {

        Transaction transaction = operationPort.getTransactionByOperationNumber(command.operationNumber());
        if (transaction == null) {
            log.error("Transaction not found for operation number {}", command.operationNumber());
            return null;
        }
        String operationId = transaction.getOperationId();
        String transactionId = transaction.getTransactionId();

        OperationResponse operation = operationPort.getOperationById(operationId);
        if (operation == null) {
            log.error("Operation not found for operation id {}", operationId);
            return null;
        }

        String userId = command.userId();
        if (userId == null) {
            userId = operation.getCreatedUser();
            log.info("User id missing on command, falling back to operation createdUser {}", userId);
        }

        return new ResolvedOperation(operation, transactionId, userId);
    }

    private void handleSuccess(ResolvedOperation resolved) {
        OperationResponse operation = resolved.operation;
        OperationType operationType = OperationType.fromString(operation.getType());
        log.info("Handle Success: Updating statuses for operation {} (type {})", operation.getOperationId(), operationType);

        operationPort.updateTransactionStatus(resolved.transactionId, "COMPLETED", resolved.userId);
        operationPort.updateOperationStatus(operation.getOperationId(), "COMPLETED", resolved.userId);

    }

    private void handleFailure(CompleteBillPaymentCommand command, ResolvedOperation resolved) {
        OperationResponse operation = resolved.operation;
        log.warn("Handle Failure: Processing failed payment for operation {}", operation.getOperationId());

        ProductoCommand productoCommand = ProductoCommand.builder().codInterno(command.userId())
            .tipoTarjeta(PRINCIPAL).codBloqueoCuenta(BLOQ_N).codBloqueoTarjeta(BLOQ_N).bloqueoCuentaCondicion(1)
            .codProducto(DEB_FISICO).codProducto(DEB_VIRTUAL).build();

        ProductoResult productoResult = productoPort.buscarProducto(productoCommand);

        AutorizacionCommand autorizacionCommand = AutorizacionCommand.builder()
            .customerUid(productoResult.uidcliente()).accountUid(productoResult.uidcuenta())
            .operationNumber(command.operationNumber()).amount(command.amount())
            .customerDocumentType(String.valueOf(productoResult.tipoDocumento()))
            .customerDocumentNumber(productoResult.numeroDocumento())
            .customerFullName(productoResult.nombreEmbozado()).operationType(OperationType.RECHARGE)
            .build();

        trxOhPayPort.reversar(autorizacionCommand);

        operationPort.updateTransactionStatus(resolved.transactionId, "NOT_PROCESSED", resolved.userId);
        operationPort.updateOperationStatus(operation.getOperationId(), "NOT_PROCESSED", resolved.userId);

    }



    private static final class ResolvedOperation {
        private final OperationResponse operation;
        private final String transactionId;
        private final String userId;

        private ResolvedOperation(OperationResponse operation, String transactionId, String userId) {
            this.operation = operation;
            this.transactionId = transactionId;
            this.userId = userId;
        }
    }
}
