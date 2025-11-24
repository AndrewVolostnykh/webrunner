package andrew_volostnykh.webrunner.utils;

public class Exceptions {

	public interface CheckedConsumer<T> {
		void accept(T t) throws Exception;
	}

	public interface CheckedSupplier<T> {
		T get() throws Exception;
	}

	public static <T> T muteException(CheckedSupplier<T> func) {
		try {
			return func.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
