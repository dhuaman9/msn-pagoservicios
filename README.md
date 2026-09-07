# us-bs-service-pay

Microservicio de negocio (BS) de Pago de Servicios. Es la **capa de orquestación**: recibe peticiones del UX, consulta los datos del usuario/cuenta, gestiona las operaciones en base de datos, ejecuta el débito vía `BBR Processor` y delega el pago externo a `us-gateway-interbank`.

## Arquitectura

```
us-ux-service-pay
       │
       ▼ HTTP
us-bs-service-pay
       │
       ├──HTTP──► us-gateway-interbank  (pago Interbank)
       ├──HTTP──► BBR Processor         (débito cuenta)
       ├──HTTP──► Customer API          (datos cliente)
       ├──HTTP──► Operation API         (registro operación)
       ├──DB  ──► MySQL + Flyway        (recipients, services, sync)
       ├──Cache──► Redis                (deduplicación, locks)
       └──PubSub──► GCP                (consumer: bill-completed-v2-subscription)
```

## Endpoints expuestos

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/bs-service-pay/validate` | Lista de empresas (recipients) del dashboard |
| `GET` | `/bs-service-pay/recipient/service` | Listado de servicios de una empresa |
| `POST` | `/bs-service-pay/service/pre-confirmation` | Consulta facturas vía Interbank |
| `POST` | `/bs-service-pay/service/confirmation` | Ejecuta el pago completo |

## Ejecución local

```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Tests

```bash
mvn test
```

---

## Sistema de Logs por Steps — Service Pay BS

> Este documento describe los steps que corresponden únicamente a la capa **BS**. Para ver el flujo completo (UX → BS → GW → EXT) consultar el README de `us-ux-service-pay`.

### Convención de nomenclatura

```
BS_SERVICE_PAY_{OPERACION}_STEP_{N.M}_{DESCRIPCION}
```

Los números de step en BS son decimales (`N.M`) donde `N` es el step del UX que disparó la llamada.

---

### VALIDATE — steps BS

| Orden | Step | Nivel | Clase |
|-------|------|-------|-------|
| 3.1 | `BS_SERVICE_PAY_VALIDATE_STEP_3.1_REQUEST_RECEIVED` | INFO | `RecipientV2Controller` |
| 3.2 | `BS_SERVICE_PAY_VALIDATE_STEP_3.2_GET_LAST_SYNC` | INFO | `FindLastRecipientsInteractor` |
| 3.3 | `BS_SERVICE_PAY_VALIDATE_STEP_3.3_FIND_RECIPIENTS` | INFO | `FindLastRecipientsInteractor` |
| 3.4 | `BS_SERVICE_PAY_VALIDATE_STEP_3.4_GET_HOME_ALERT` | INFO | `FindLastRecipientsInteractor` |
| 3.5 | `BS_SERVICE_PAY_VALIDATE_STEP_3.5_END_COMPLETED` | INFO | `FindLastRecipientsInteractor` |

---

### SERVICES — steps BS

| Orden | Step | Nivel | Clase |
|-------|------|-------|-------|
| 3.1 | `BS_SERVICE_PAY_SERVICES_STEP_3.1_REQUEST_RECEIVED` | INFO | `ServiceV2Controller` |
| 3.2 | `BS_SERVICE_PAY_SERVICES_STEP_3.2_FIND_SERVICES` | INFO | `GetServicesInteractor` |
| 3.2e | `BS_SERVICE_PAY_SERVICES_STEP_3.2_ERROR_EMPTY_SERVICES` | ERROR | `GetServicesInteractor` |
| 3.3 | `BS_SERVICE_PAY_SERVICES_STEP_3.3_END_COMPLETED` | INFO | `GetServicesInteractor` |

---

### PRE_CONFIRMATION — steps BS

| Orden | Step | Nivel | Clase |
|-------|------|-------|-------|
| 3.1 | `BS_SERVICE_PAY_PRE_CONFIRMATION_STEP_3.1_REQUEST_RECEIVED` | INFO | `BillV2Controller` |
| 3.2 | `BS_SERVICE_PAY_PRE_CONFIRMATION_STEP_3.2_CALLING_GW_BILLS` | INFO | `GetBillsInteractor` |
| 3.2e | `BS_SERVICE_PAY_PRE_CONFIRMATION_STEP_3.2_ERROR_GW_BILLS` | ERROR | `BillProviderAdapter` |
| 3.3 | `BS_SERVICE_PAY_PRE_CONFIRMATION_STEP_3.3_END_COMPLETED` | INFO | `GetBillsInteractor` |

---

### CONFIRMATION — steps BS (operación más compleja)

| Orden | Step | Nivel | Clase |
|-------|------|-------|-------|
| 4.1 | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.1_REQUEST_RECEIVED` | INFO | `BillV2Controller` |
| 4.2 | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.2_GET_ACCOUNT_CUSTOMER_CARD` | INFO | `PayServiceInteractor` |
| 4.2e | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.2_ERROR_GET_ACCOUNT` | ERROR | `PayServiceInteractor` |
| 4.3 | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.3_RESOLVE_BILL` | INFO | `PayServiceInteractor` |
| 4.3e | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.3_ERROR_BILL_NOT_FOUND` | ERROR | `PayServiceInteractor` |
| 4.4 | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.4_CREATE_OPERATION` | INFO | `PayServiceInteractor` |
| 4.4e | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.4_ERROR_CREATE_OPERATION` | ERROR | `PayServiceInteractor` |
| 4.5 | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.5_CREATE_TRANSACTION_BBR` | INFO | `PayServiceInteractor` |
| 4.6 | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.6_CALLING_BBR_INCORPORATE` | INFO | `PayServiceInteractor` |
| 4.6e | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.6_ERROR_BBR_INCORPORATE_REST` | ERROR | `BBRProcessorAdapter` |
| 4.6t | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.6_ERROR_BBR_INCORPORATE_TIMEOUT` | ERROR | `BBRProcessorAdapter` |
| 4.7 | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.7_UPDATE_BBR_COMPLETED` | INFO | `PayServiceInteractor` |
| 4.8 | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.8_CREATE_TRANSACTION_IBK` | INFO | `PayServiceInteractor` |
| 4.9 | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.9_CALLING_GW_PAYMENT` | INFO | `PayServiceInteractor` |
| 4.9e | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.9_ERROR_GW_PAYMENT` | ERROR | `ExternalPaymentAdapter` |
| 4.10 | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.10_UPDATE_IBK_WAITING` | INFO | `PayServiceInteractor` |
| 4.11 | `BS_SERVICE_PAY_CONFIRMATION_STEP_4.11_END_COMPLETED` | INFO | `PayServiceInteractor` |

> Para el flujo completo (UX → BS → GW) ver README de `us-ux-service-pay`.
