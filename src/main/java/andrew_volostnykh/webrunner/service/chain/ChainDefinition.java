package andrew_volostnykh.webrunner.service.chain;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ChainDefinition {

	private String name;
	private Map<String, Object> globalVars = new HashMap<>();
	private String beforeChainJs;
	private String afterChainJs;
	private List<ChainStep> steps = new ArrayList<>();
}
