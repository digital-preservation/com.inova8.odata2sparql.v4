package com.inova8.odata2sparql.SparqlProcessor;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ODataServerError;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.ErrorProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;


public class SparqlErrorProcessor implements ErrorProcessor {
	private OData odata;
	private ServiceMetadata serviceMetadata;
	private final Log log = LogFactory.getLog(SparqlErrorProcessor.class);
	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}

	@Override
	public void processError(ODataRequest request, ODataResponse response, ODataServerError serverError,
			ContentType responseFormat) {
		try {
			ODataSerializer serializer = odata.createSerializer(responseFormat);
			response.setContent(serializer.error(serverError).getContent());
			response.setStatusCode(serverError.getStatusCode());
			response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
			switch(serverError.getStatusCode()){
				case 404: break;
				default:
					//Only log severe errors, not 404 etc
					if(serverError.getException()!=null){
						log.error(request.getMethod() + ": " + request.getRawRequestUri() +" Error: " + serverError.getException().getMessage() +"\nCause: " + serverError.getException().getCause());
					}else{
						log.error(request.getMethod() + ": " + request.getRawRequestUri() +" Error: " + serverError.getException().getMessage());
					}					
			}

		} catch (Exception e) {
			// This should never happen but to be sure we have this catch here to prevent sending a stacktrace to a client.
			String responseContent = "{\"error\":{\"code\":null,\"message\":\"An unexpected exception occurred during error processing\"}}";
			response.setContent(new ByteArrayInputStream(responseContent.getBytes(Charset.forName("utf-8"))));
			response.setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode());
			response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_JSON.toContentTypeString());
		}
	}
}