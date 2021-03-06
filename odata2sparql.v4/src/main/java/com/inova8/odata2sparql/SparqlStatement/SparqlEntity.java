package com.inova8.odata2sparql.SparqlStatement;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.TreeMap;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inova8.odata2sparql.Constants.RdfConstants;
import com.inova8.odata2sparql.RdfConnector.openrdf.RdfNode;
import com.inova8.odata2sparql.RdfModel.RdfModel;
import com.inova8.odata2sparql.RdfModel.RdfModel.RdfEntityType;
import com.inova8.odata2sparql.RdfModel.RdfModel.RdfPrefixes;
import com.inova8.odata2sparql.RdfModel.RdfModel.RdfPrimaryKey;
import com.inova8.odata2sparql.Utils.RdfNodeComparator;

public class SparqlEntity extends Entity {
	private final Logger log = LoggerFactory.getLogger(RdfModel.class);
	private final TreeMap<RdfNode, Object> datatypeProperties = new TreeMap<RdfNode, Object>(new RdfNodeComparator());
	private HashSet<SparqlEntity> matching = new HashSet<SparqlEntity>();
	private final RdfNode subjectNode;
	private final String subject;
	private final RdfPrefixes rdfPrefixes;
	private RdfEntityType rdfEntityType;
	private boolean isExpandedEntity = false;
	private boolean isTargetEntity = false;

	SparqlEntity(RdfNode subjectNode, RdfPrefixes rdfPrefixes) {
		super();
		this.subjectNode = subjectNode;
		this.rdfPrefixes = rdfPrefixes;
		this.subject = this.rdfPrefixes.toQName(subjectNode, RdfConstants.QNAME_SEPARATOR);
		this.addProperty(new Property(null, RdfConstants.SUBJECT, ValueType.PRIMITIVE,
				SparqlEntity.URLEncodeEntityKey(this.subject)));
	}

	@Override
	public URI getId() {
		try {
			if (this.getEntityType()!=null) {
			if (this.getEntityType().isOperation()) {
				String id = rdfEntityType.getEDMEntitySetName() + "(";
				boolean first = true;
				for (RdfPrimaryKey keyProperty : rdfEntityType.getPrimaryKeys()) {
					if (!first)
						id = id + ",";
					Property propertyValue = this.getProperty(keyProperty.getPrimaryKeyName());
					id = id + propertyValue.getName() + "='"
							+ propertyValue.getValue().toString()
									.replace(RdfConstants.QNAME_SEPARATOR, RdfConstants.QNAME_SEPARATOR_ENCODED)
									.replace(":", RdfConstants.QNAME_SEPARATOR_ENCODED)
									// added to handle literals that might contain illegal characters
									.replace(" ", RdfConstants.QNAME_SEPARATOR_ENCODED)
							+ "'";
					first = false;
				}
				id = id + ")";
				return new URI(id);
			} else {
				return new URI(this.getEntityType().getEDMEntitySetName() + "('"
						+ subject.replace(RdfConstants.QNAME_SEPARATOR, RdfConstants.QNAME_SEPARATOR_ENCODED) + "')");
			}
			}else {
				return new URI(this.subject);
			}
		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create id for entity: " + this.subject,
					e);
		}
	}

	public static String URLDecodeEntityKey(String encodedEntityKey) {

		String decodedEntityKey = encodedEntityKey;
		decodedEntityKey = encodedEntityKey.replace("@", "/");
		decodedEntityKey = encodedEntityKey.replace("%25", "%");
		decodedEntityKey = encodedEntityKey.replace("%3A", ":");
		return decodedEntityKey;
	}

	public static String URLEncodeEntityKey(String entityKey) {
		String encodedEntityKey = entityKey;
		encodedEntityKey = encodedEntityKey.replace("/", "@");
		return encodedEntityKey;
	}

	public String getSubject() {
		return subject;
	}

	public RdfNode getSubjectNode() {
		return subjectNode;
	}

	public TreeMap<RdfNode, Object> getDatatypeProperties() {
		return datatypeProperties;
	}

	public RdfEntityType getEntityType() {
		return rdfEntityType;
	}

	public void setEntityType(RdfEntityType rdfEntityType) {
		this.rdfEntityType = rdfEntityType;
	}

	public void assertTargetEntityType(RdfEntityType rdfEntityType) {
		this.setTargetEntity(true);
		if(this.getEntityType()!=null &&  rdfEntityType != this.getEntityType()) {
			//This means that the same entity has been used in an expanded query but when it is off a different type
			log.warn("Assert target type: " + rdfEntityType.getEDMEntityTypeName() + " of: " + this.getId());
		}
		this.rdfEntityType = rdfEntityType;
	}
	public void assertEntityType(RdfEntityType rdfEntityType) {
		if(!this.isTargetEntity()) {
			//Don't overwrite assertion of targetEntity
			this.rdfEntityType = rdfEntityType;
		}
	}
	public boolean isExpandedEntity() {
		return isExpandedEntity;
	}

	public void setExpandedEntity(boolean isExpandedEntity) {
		this.isExpandedEntity = isExpandedEntity;
	}

	public boolean isTargetEntity() {
		return isTargetEntity;
	}

	public void setTargetEntity(boolean isTargetEntity) {
		this.isTargetEntity = isTargetEntity;
	}

	Boolean containsProperty(final String name) {
		Boolean result = false;
		for (Property property : this.getProperties()) {
			if (name.equals(property.getName())) {
				result = true;
				break;
			}
		}
		return result;
	}

	public void addMatching(SparqlEntity matchingEntity) {
		if (!this.equals(matchingEntity))
			matching.add(matchingEntity);
	}

	public HashSet<SparqlEntity> getMatching() {
		return matching;
	}

}
