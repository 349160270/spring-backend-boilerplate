package in.clouthink.daas.sbb.rbac.rest.support.impl;

import in.clouthink.daas.sbb.account.domain.model.ExtRole;
import in.clouthink.daas.sbb.account.domain.model.RoleType;
import in.clouthink.daas.sbb.account.domain.model.SysRole;
import in.clouthink.daas.sbb.account.service.RoleService;
import in.clouthink.daas.sbb.rbac.impl.model.TypedRole;
import in.clouthink.daas.sbb.rbac.impl.service.support.RbacUtils;
import in.clouthink.daas.sbb.rbac.impl.service.support.ResourceRoleRelationshipService;
import in.clouthink.daas.sbb.rbac.model.TypedCode;
import in.clouthink.daas.sbb.rbac.rest.dto.ResourceWithChildren;
import in.clouthink.daas.sbb.rbac.rest.dto.TypedRoleSummary;
import in.clouthink.daas.sbb.rbac.rest.service.ResourceCacheService;
import in.clouthink.daas.sbb.rbac.rest.support.PermissionRestSupport;
import in.clouthink.daas.sbb.rbac.service.PermissionService;
import in.clouthink.daas.sbb.rbac.service.ResourceService;
import in.clouthink.daas.sbb.rbac.support.parser.RoleCodeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PermissionRestSupportImpl implements PermissionRestSupport {

	private RoleCodeParser roleCodeParser = new RoleCodeParser();

	@Autowired
	private RoleService roleService;

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private ResourceCacheService resourceCacheService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private ResourceRoleRelationshipService resourceRoleRelationshipService;

	@Override
	public List<ResourceWithChildren> listGrantedResources(String roleCode) {
		//granted resource codes & action codes
		Map<String,Set<String>> resourceCodes =

				resourceRoleRelationshipService.listAllowedResource(roleCode)
											   .stream()
											   .collect(Collectors.toMap(resource -> resource.getCode(),
																		 resource -> resource.getActions()
																							 .stream()
																							 .map(action -> action.getCode())
																							 .collect(Collectors.toSet())));

		List<ResourceWithChildren> result = resourceCacheService.listResources(false);

		processChildren(result, resourceCodes);

		return result;
	}

	private void processChildren(List<ResourceWithChildren> result, Map<String,Set<String>> resourceCodes) {
		result.stream().forEach(resourceWithChildren -> {
			resourceWithChildren.setGranted(resourceCodes.containsKey(resourceWithChildren.getCode()));
			resourceWithChildren.getActions().stream().forEach(action -> {
				Set<String> actionCodes = resourceCodes.get(resourceWithChildren.getCode());
				action.setGranted(actionCodes != null && actionCodes.contains(action.getCode()));
			});

			processChildren(resourceWithChildren.getChildren(), resourceCodes);
		});
	}

	@Override
	public List<TypedRoleSummary> listGrantedRoles(String code) {
		return resourceRoleRelationshipService.listAllowedRoles(code)
											  .stream()
											  .map(authority -> RbacUtils.convertToTypedRole(authority))
											  .map(role -> TypedRoleSummary.from((TypedRole) role))
											  .collect(Collectors.toList());
	}

	@Override
	public void grantRolesToResource(String code, String[] typedRoleCodes) {
		if (typedRoleCodes == null || typedRoleCodes.length == 0) {
			return;
		}
		for (String typedRoleCode : typedRoleCodes) {
			TypedCode typedCode = roleCodeParser.parse(typedRoleCode);
			if (RoleType.EXT_ROLE.name().equals(typedCode.getType())) {
				ExtRole appRole = roleService.findByCode(typedCode.getCode());
				if (appRole != null) {
					resourceRoleRelationshipService.bindResourceAndRole(code, appRole);
				}
			}
			else if (RoleType.SYS_ROLE.name().equals(typedCode.getType())) {
				SysRole sysRole = SysRole.valueOf(typedCode.getCode());
				if (sysRole != null) {
					resourceRoleRelationshipService.bindResourceAndRole(code, sysRole);
				}
			}
		}
	}

	@Override
	public void revokeRolesFromResource(String code, String[] typedRoleCodes) {
		if (typedRoleCodes == null || typedRoleCodes.length == 0) {
			return;
		}
		for (String typedRoleCode : typedRoleCodes) {
			TypedCode typedCode = roleCodeParser.parse(typedRoleCode);
			if (RoleType.EXT_ROLE.name().equals(typedCode.getType())) {
				ExtRole appRole = roleService.findByCode(typedCode.getCode());
				if (appRole != null) {
					resourceRoleRelationshipService.unbindResourceAndRole(code, appRole);
				}
			}
			else if (RoleType.SYS_ROLE.name().equals(typedCode.getType())) {
				SysRole sysRole = SysRole.valueOf(typedCode.getCode());
				if (sysRole != null) {
					resourceRoleRelationshipService.unbindResourceAndRole(code, sysRole);
				}
			}
		}
	}
}