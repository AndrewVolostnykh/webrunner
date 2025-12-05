package andrew_volostnykh.webrunner.service.persistence.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class ChainRequestDefinition
	extends AbstractRequestDefinition {

	private List<String> steps = new ArrayList<>();

	public ChainRequestDefinition(
		String name
	) {
		super(
			name,
			null,
			"",
			"",
			new HashMap<>(),
			""
		);
	}

	@Override
	public AbstractRequestDefinition deepCopy() {
		ChainRequestDefinition copy = new ChainRequestDefinition(name);
		copy.setSteps(new ArrayList<>(steps));
		return copy;
	}

	@Override
	public RequestType getType() {
		return RequestType.CHAIN_REQUEST;
	}
}


