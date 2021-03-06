package com.inova8.odata2sparql.uri;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;

import com.inova8.odata2sparql.Exception.OData2SparqlException;
import com.inova8.odata2sparql.RdfEdmProvider.RdfEdmProvider;
import com.inova8.odata2sparql.RdfModel.RdfModel.RdfEntityType;
import com.inova8.odata2sparql.SparqlStatement.SparqlEntity;

public class RdfResourceEntitySet extends RdfResourcePart {
	RdfEdmProvider rdfEdmProvider;
	EdmEntitySet edmEntitySet;
	RdfEntityType rdfEntityType;
	List<UriParameter> keyPredicates;

	public RdfResourceEntitySet(RdfEdmProvider rdfEdmProvider, UriResourceEntitySet uriResourceEntitySet) {
		super.setUriResourceKind(UriResourceKind.entitySet);
		this.rdfEdmProvider = rdfEdmProvider;
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		keyPredicates = uriResourceEntitySet.getKeyPredicates();
		rdfEntityType = rdfEdmProvider.getRdfEntityTypefromEdmEntitySet(edmEntitySet);
		this.edmEntitySet = edmEntitySet;
	}
	public RdfResourceEntitySet(RdfEdmProvider rdfEdmProvider, EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates) {
		super.setUriResourceKind(UriResourceKind.entitySet);
		this.rdfEdmProvider = rdfEdmProvider;
		this.keyPredicates = keyPredicates;
		rdfEntityType = rdfEdmProvider.getRdfEntityTypefromEdmEntitySet(edmEntitySet);
		this.edmEntitySet = edmEntitySet;
	}
	public EdmEntitySet getEdmEntitySet() {
		return edmEntitySet;
	}

	public RdfEntityType getRdfEntityType() {
		return rdfEntityType;
	}

	public List<UriParameter> getKeyPredicates() {
		return keyPredicates;
	}

	public String getDecodedKey() throws OData2SparqlException {
		if (keyPredicates.size() > 1) {
			String pathVariable = "";
			for (UriParameter entityKey : keyPredicates) {
				String decodedEntityKey = SparqlEntity.URLDecodeEntityKey(entityKey.getText());
				String expandedKey = rdfEdmProvider.getRdfModel().getRdfPrefixes()
						.expandPrefix(decodedEntityKey.substring(1, decodedEntityKey.length() - 1));
				pathVariable = entityKey.getReferencedProperty() + "=<" + expandedKey + ">,";
			}
			return pathVariable.substring(0, pathVariable.length() - 1);
		} else if (!keyPredicates.isEmpty()) {
			String decodedEntityKey = SparqlEntity.URLDecodeEntityKey(keyPredicates.get(0).getText());
			String expandedKey = rdfEdmProvider.getRdfModel().getRdfPrefixes()
					.expandPrefix(decodedEntityKey.substring(1, decodedEntityKey.length() - 1));
			return "<" + expandedKey + ">";
		} else {
			return null;
		}
	}

	public String getLocalKey() {
		if (keyPredicates.size() > 1) {
			return null;
		} else if (!keyPredicates.isEmpty()) {
			try {
				String key = keyPredicates.get(0).getText();
				key = key.substring(1, key.length() - 1);
				return  "'" +  UriUtils.encodeUri( key) + "'";
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public String getSubjectId() {
		if (keyPredicates.size() > 1) {
			return null;
		} else if (!keyPredicates.isEmpty()) {
			String key = keyPredicates.get(0).getText();
			key = key.substring(1, key.length() - 1);
			try {
				return key =UriUtils.encodeUri(key);
			} catch (UnsupportedEncodingException e) {
				return null;
			}
			//key = UriUtils.encodeQName(key);
			//return key.substring(1, key.length() - 1);
		} else {
			return null;
		}
	}

	public String geEntityString() {

		return getRdfEntityType().getEntityTypeName() + "(" + getLocalKey() + ")";
	}

	public boolean hasKey() {
		return !keyPredicates.isEmpty();
	}
	public String getEntitySetName() {
		
		return  this.edmEntitySet.getName();
	}
}
