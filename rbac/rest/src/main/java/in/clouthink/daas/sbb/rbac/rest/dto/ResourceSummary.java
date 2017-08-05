package in.clouthink.daas.sbb.rbac.rest.dto;

import in.clouthink.daas.sbb.rbac.model.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 */
public class ResourceSummary {

	public static ResourceSummary from(Resource resource) {
		//always convert by default
		return from(resource, res -> true);
	}

	public static ResourceSummary from(Resource resource, Predicate<Resource> predicate) {
		if (resource == null) {
			return null;
		}
		if (!predicate.test(resource)) {
			return null;
		}
		ResourceSummary result = new ResourceSummary();
		convert(resource, result, predicate);
		return result;
	}

	private static void convert(Resource resource, ResourceSummary target, Predicate<Resource> predicate) {
		target.setVirtual(resource.isVirtual());
		target.setCode(resource.getCode());
		target.setName(resource.getName());
		target.setType(resource.getType());
	}

	private boolean virtual;

	private String code;

	private String name;

	private String type;

	private Map<String,Object> metadata = new HashMap<>();

	public boolean isVirtual() {
		return virtual;
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String,Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String,Object> metadata) {
		this.metadata = metadata;
	}
}
