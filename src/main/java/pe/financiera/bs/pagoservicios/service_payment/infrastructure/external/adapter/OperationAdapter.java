package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ExternalServiceException;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ModelRestClientException;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationResponse;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Transaction;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.OperationPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.V2OperationRestClient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2OperationCreateRequestDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2OperationResponseDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2OperationUpdateRequestDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2ResultTransactionResponseDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2TransactionCreateRequestDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2TransactionDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2TransactionUpdateDto;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationAdapter implements OperationPort {

	private static final String TRANSACTION_TYPE_CASH_OUT = "CASH_OUT";
	private static final String PLATFORM_IBK = "IBK";

	private final V2OperationRestClient operationRestClient;

	@Override
	public Transaction getTransactionByOperationNumber(String operationNumber) {
		log.info("Fetching transaction for operation number: {}", operationNumber);
		try {
			String search = String.format(
					"( suppliedId==%s or externalId==%s ) and type==%s and platform==%s and enabled==true",
					operationNumber, operationNumber, TRANSACTION_TYPE_CASH_OUT, PLATFORM_IBK);
			Response<V2ResultTransactionResponseDto> response = operationRestClient.searchTransactionByFilter(search)
					.execute();
			if (!response.isSuccessful() || response.body() == null) {
				return null;
			}
			if (response.body().getResult() == null || response.body().getResult().isEmpty()) {
				return null;
			}
			V2TransactionDto dto = response.body().getResult().getFirst();
			return Transaction.builder().transactionId(dto.getTransactionId()).operationId(dto.getOperationId())
					.status(dto.getStatus()).createdUser(dto.getCreatedUser()).amount(dto.getAmount()).build();
		} catch (IOException e) {
			throw new ExternalServiceException(String.format("Error calling Operation API: %s", e.getMessage()));
		}
	}

	@Override
	public OperationResponse getOperationById(String operationId) {
		log.info("Fetching operation for operation id: {}", operationId);
		try {
			Response<V2OperationResponseDto> response = operationRestClient.getOperation(operationId).execute();
			if (!response.isSuccessful() || response.body() == null) {
				return null;
			}
			return toDomain(response.body());
		} catch (IOException e) {
            throw new ExternalServiceException(String.format("Error calling Operation API: %s", e.getMessage()));
		}
	}

	@Override
	public void updateTransactionStatus(String transactionId, String status, String userId) {
		log.info("Updating transaction {} to status {}", transactionId, status);

        V2TransactionUpdateDto request = V2TransactionUpdateDto.builder().status(status).lastModifiedUser(userId)
				.build();
		try {
			operationRestClient.updateTransaction(transactionId, request).execute();
		} catch (IOException e) {
			log.error("Error updating transaction: {}", e.getMessage(), e);
		}
	}

	@Override
	public void updateOperationStatus(String operationId, String status, String userId) {
		log.info("Updating operation {} to status {}", operationId, status);
		V2OperationUpdateRequestDto request = V2OperationUpdateRequestDto.builder().status(status)
				.lastModifiedUser(userId).build();
		try {
			operationRestClient.updateOperation(operationId, request).execute();
		} catch (IOException e) {
			log.error("Error updating operation: {}", e.getMessage(), e);
		}
	}

	@Override
	public OperationResponse createOperation(String codigoInterno, String accountId, BigDecimal amount, String type,
			String destination, String originName, String destinationName, String description, String detail,
			String additionalData) {

		log.info("Creating operation for user {} account {}", codigoInterno, accountId);

		V2OperationCreateRequestDto request = V2OperationCreateRequestDto.builder().originAccountId(accountId)
				.destinationAccountId(accountId).amount(amount).tipAmount(BigDecimal.ZERO).type(type)
				.description(description).detail(detail).additionalData(additionalData).operationReferenceId(accountId)
				.externalReferenceId(accountId).originName(originName).destinationName(destinationName)
				.status("IN_PROGRESS").createdUser(codigoInterno).enabled(true).build();
		try {
			Response<V2OperationResponseDto> response = operationRestClient.createOperation(request).execute();

			if (!response.isSuccessful()) {

				throw new ModelRestClientException(HttpStatusCode.valueOf(response.code()),
						"Error creating operation");
			}

			if (response.body() == null || response.body().getOperationId() == null) {
				throw new ExternalServiceException("External Operation response null or missing operationId");
			}

			return toDomain(response.body());
		} catch (IOException e) {
			throw new ExternalServiceException("External Operation service error");
		}
	}

	private OperationResponse toDomain(V2OperationResponseDto dto) {
		return OperationResponse.builder().operationId(dto.getOperationId()).operationNumber(dto.getOperationNumber())
				.originAccountId(dto.getOriginAccountId()).destinationAccountId(dto.getDestinationAccountId())
				.amount(dto.getAmount()).tipAmount(dto.getTipAmount()).type(dto.getType())
				.description(dto.getDescription()).detail(dto.getDetail()).additionalData(dto.getAdditionalData())
				.operationReferenceId(dto.getOperationReferenceId()).externalReferenceId(dto.getExternalReferenceId())
				.operationSequential(dto.getOperationSequential()).originName(dto.getOriginName())
				.destinationName(dto.getDestinationName()).enabled(dto.getEnabled()).status(dto.getStatus())
				.createdDate(dto.getCreatedDate()).lastModifiedDate(dto.getLastModifiedDate())
				.createdUser(dto.getCreatedUser()).lastModifiedUser(dto.getLastModifiedUser()).build();
	}

	@Override
	public Transaction createTransaction(String operationId, String userId, String type, String platform,
			BigDecimal amount, String operationNumber) {

		log.info("Creating transaction for operation {} user {}", operationId, userId);

		V2TransactionCreateRequestDto request = V2TransactionCreateRequestDto.builder().operationId(operationId)
				.type(type).platform(platform).suppliedId(operationNumber).externalId(operationNumber)
				.status("IN_PROGRESS").createdUser(userId).enabled(true).amount(amount).build();
		try {
			Response<V2TransactionDto> response = operationRestClient.createTransaction(request).execute();

			if (!response.isSuccessful()) {
				throw new ModelRestClientException(HttpStatusCode.valueOf(response.code()),
						"Error creating transaction");
			}

			if (response.body() == null || response.body().getOperationId() == null) {
				throw new ExternalServiceException("External Transaction response null or missing operationId");
			}

			V2TransactionDto dto = response.body();
			return Transaction.builder().transactionId(dto.getTransactionId()).operationId(dto.getOperationId())
					.status(dto.getStatus()).createdUser(dto.getCreatedUser()).amount(dto.getAmount()).build();
		} catch (IOException e) {
			throw new ExternalServiceException(String.format("External Operation service error: %s", e.getMessage()));
		}
	}
}
