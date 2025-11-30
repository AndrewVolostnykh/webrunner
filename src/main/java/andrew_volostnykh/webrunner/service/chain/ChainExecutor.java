package andrew_volostnykh.webrunner.service.chain;

import andrew_volostnykh.webrunner.DependenciesContainer;
import andrew_volostnykh.webrunner.service.http.HttpRequestService;
import andrew_volostnykh.webrunner.service.js.JsExecutorService;
import andrew_volostnykh.webrunner.service.test_engine.VarsApplicatorService;

public class ChainExecutor {

	private final HttpRequestService httpService = DependenciesContainer.httpRequestService();
	private final JsExecutorService jsExecutor = DependenciesContainer.jsExecutorService();
	private final VarsApplicatorService varsApplicator = DependenciesContainer.varsApplicator();

//	public CompletableFuture<Void> executeChain(ChainDefinition chain) {
//		return CompletableFuture.runAsync(() -> {
//			Map<String, Object> chainGlobalVars = new HashMap<>(chain.getGlobalVars());
//
//			if (chain.getBeforeChainJs() != null && !chain.getBeforeChainJs().isBlank()) {
//				chainGlobalVars.put("chainGlobal", jsExecutor.executeJsVariables(chain.getBeforeChainJs()));
//			}
//
//			for (ChainStep step : chain.getSteps()) {
//				Map<String, Object> stepContext = new HashMap<>();
//				chainGlobalVars.put(step.getRequestId(), stepContext);
//
//				// TODO: need to add global chain vars!!! not only local
//				Map<String, Object> stepBeforeVars = jsExecutor.executeJsVariables(
//					step.getBeforeJs(),
//					chainGlobalVars
//				);
//
//				if (step.getBeforeJs() != null) {
//					stepContext.put(
//						"before",
//						stepBeforeVars
//					);
//				}
//
//				String body = varsApplicator.applyVariables(step.getRequest().getBody(), stepBeforeVars);
//
//				var resp = muteException(() ->
//											 httpService.sendRequest(
//												 step.getRequest().getMethod(),
//												 step.getRequest().getUrl(),
//												 body,
//												 step.getRequest().getHeaders()
//											 ));
//
//				stepContext.put("response", resp);
//
//				if (step.getAfterJs() != null) {
//					jsExecutor.executeJsVariables(
//						step.getAfterJs(),
//						chainGlobalVars
//					);
//				}
//			}
//
//			if (chain.getAfterChainJs() != null && !chain.getAfterChainJs().isBlank()) {
//				jsExecutor.executeJsVariables(chain.getAfterChainJs(), chainGlobalVars);
//			}
//		});
//	}
}
