package havis.custom.harting.etb.sensor.rest.async;

import havis.net.rest.shared.data.SerializableValue;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("../rest/ale/config/readercycle")
public interface MiddlewareServiceAsync extends RestService {
	@GET
	@Path("lifetime")
	void getLifetime(MethodCallback<SerializableValue<String>> callback);

}
