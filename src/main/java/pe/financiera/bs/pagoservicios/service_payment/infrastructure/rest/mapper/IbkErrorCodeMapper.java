package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.mapper;

import java.util.HashMap;
import java.util.Map;

public class IbkErrorCodeMapper {

	private IbkErrorCodeMapper() {
	}

	private static final Map<String, Integer> ERROR_CODE_MAP = new HashMap<>();

	static {
		ERROR_CODE_MAP.put("07.01.07", 340);
		ERROR_CODE_MAP.put("07.01.11", 341);
		ERROR_CODE_MAP.put("07.01.12", 342);
		ERROR_CODE_MAP.put("07.01.13", 343);
		ERROR_CODE_MAP.put("07.01.16", 344);
		ERROR_CODE_MAP.put("07.01.17", 345);
		ERROR_CODE_MAP.put("07.01.19", 346);
		ERROR_CODE_MAP.put("07.01.20", 347);
		ERROR_CODE_MAP.put("07.01.21", 348);
		ERROR_CODE_MAP.put("07.01.24", 349);
		ERROR_CODE_MAP.put("07.01.25", 350);
		ERROR_CODE_MAP.put("07.02.03", 351);
		ERROR_CODE_MAP.put("07.02.13", 352);
		ERROR_CODE_MAP.put("07.01.01", 353);
		ERROR_CODE_MAP.put("07.01.02", 354);
		ERROR_CODE_MAP.put("07.01.06", 355);
		ERROR_CODE_MAP.put("07.01.10", 356);
		ERROR_CODE_MAP.put("07.02.22", 357);
		ERROR_CODE_MAP.put("01.03.01", 358);
		ERROR_CODE_MAP.put("07.01.03", 359);
		ERROR_CODE_MAP.put("07.02.04", 360);
		ERROR_CODE_MAP.put("07.02.12", 361);
		ERROR_CODE_MAP.put("07.01.09", 362);
		ERROR_CODE_MAP.put("07.01.18", 363);
		ERROR_CODE_MAP.put("07.02.11", 364);
		ERROR_CODE_MAP.put("07.02.14", 365);
		ERROR_CODE_MAP.put("07.02.15", 366);
		ERROR_CODE_MAP.put("07.02.16", 367);
		ERROR_CODE_MAP.put("07.02.17", 368);
		ERROR_CODE_MAP.put("07.02.18", 369);
		ERROR_CODE_MAP.put("07.02.19", 370);
		ERROR_CODE_MAP.put("07.02.20", 371);
		ERROR_CODE_MAP.put("07.02.21", 372);
		ERROR_CODE_MAP.put("01.01.01", 373);
	}

	public static int mapCode(String code) {
		return ERROR_CODE_MAP.getOrDefault(code, 399);
	}
}
