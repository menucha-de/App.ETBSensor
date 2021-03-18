package havis.app.etb.sensor.rest.async;

import havis.app.etb.sensor.Configuration;
import havis.app.etb.sensor.HistoryEntry;
import havis.app.etb.sensor.URI;
import havis.transport.Subscriber;

import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;
import org.fusesource.restygwt.client.TextCallback;

@Path("../rest/webui/etb/sensor")
public interface ETBSensorServiceAsync extends RestService {
	@GET
	@Path("configuration")
	void getConfiguration(MethodCallback<Configuration> callback);

	@PUT
	@Path("configuration")
	void setConfiguration(Configuration configuration, MethodCallback<Void> callback);

	@PUT
	@Path("subscriber")
	public void updateSubscriber(Subscriber subscriber, MethodCallback<Void> callback);

	@POST
	@Path("subscriber/{type}")
	public void addSubscriber(Subscriber subscriber, @PathParam("type")String type, TextCallback callback);

	@GET
	@Path("timestamp")
	void getTimestamp(MethodCallback<Long> callback);

	@GET
	@Path("history/{since}")
	void getHistorySince(@PathParam("since") Long since, MethodCallback<List<HistoryEntry>> callback);

	@GET
	@Path("parse-uri")
	void parseUri(@QueryParam("uri") String uri, MethodCallback<URI> callback);

	@GET
	@Path("create-uri")
	void createUri(@QueryParam("scheme") String scheme, @QueryParam("host") String host, @QueryParam("port") String port,
			@QueryParam("username") String username, @QueryParam("password") String password, @QueryParam("path") String path,
			@QueryParam("query") String query, @QueryParam("topic") String topic, @QueryParam("clientId") String clientId, @QueryParam("qos") String qos,
			MethodCallback<URI> callback);

	@DELETE
	@Path("history")
	void clearHistory(MethodCallback<Void> callback);

	@POST
	@Path("validate-config")
	void validateConfiguration(Map<String, String> configData, MethodCallback<Map<String, String>> methodCallback);

	@GET
	@Path("transportTypes")
	void getTransportTypes(MethodCallback<List<String>> result);
}
