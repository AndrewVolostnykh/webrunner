package andrew_volostnykh.webrunner.utils;

import java.util.List;
import java.util.Map;

public class Maps {

	public static void singularPut(
		Map<String, List<String>> map,
		String key,
		String value
	) {
		if (!map.containsKey(key)) {
			map.put(key, List.of(value));
		} else {
			List<String> list = map.get(key);
			list.add(value);
		}
	}
}
