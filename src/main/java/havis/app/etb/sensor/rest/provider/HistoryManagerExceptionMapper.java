package havis.app.etb.sensor.rest.provider;

import havis.app.etb.sensor.HistoryManagerException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class HistoryManagerExceptionMapper implements ExceptionMapper<HistoryManagerException> {

	@Override
	public Response toResponse(HistoryManagerException ex) {
		return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
	}

}
