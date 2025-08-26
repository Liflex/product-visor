package ru.dmitartur.context;

/**
 * Хранит контекст изменений в текущем потоке выполнения.
 * Используется для прокидывания originMarket/source в места фиксации изменений без явной передачи параметров.
 */
public final class ChangeContextHolder {

	public static final class ChangeContext {
		public final String originMarket; // "ozon", "wildberries", ...
		public final String sourceSystem; // "KAFKA", "REST_API", ...
		public final String sourceId;     // posting_number, user_id, ...

		public ChangeContext(String originMarket, String sourceSystem, String sourceId) {
			this.originMarket = originMarket;
			this.sourceSystem = sourceSystem;
			this.sourceId = sourceId;
		}
	}

	private static final ThreadLocal<ChangeContext> CTX = new ThreadLocal<>();

	public static AutoCloseable withContext(ChangeContext ctx) {
		CTX.set(ctx);
		return CTX::remove;
	}

	public static ChangeContext get() {
		return CTX.get();
	}
}


