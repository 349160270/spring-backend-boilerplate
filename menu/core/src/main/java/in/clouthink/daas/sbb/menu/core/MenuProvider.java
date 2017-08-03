package in.clouthink.daas.sbb.menu.core;

import in.clouthink.daas.sbb.rbac.model.Resource;
import in.clouthink.daas.sbb.rbac.model.ResourceWithChildren;
import in.clouthink.daas.sbb.rbac.model.Resources;
import in.clouthink.daas.sbb.rbac.spi.ResourceProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dz
 */
public class MenuProvider implements ResourceProvider, InitializingBean {

	private static final Log logger = LogFactory.getLog(MenuProvider.class);

	static final String PROVIDER_NAME = MenuProvider.class.getName();

	@Autowired(required = false)
	private List<MenuPlugin> menuPluginList = new ArrayList<>();

	private List<ResourceWithChildren> resourceList = new ArrayList<>();

	@Override
	public String getName() {
		return PROVIDER_NAME;
	}

	@Override
	public List<ResourceWithChildren> listResources() {
		return resourceList;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String,Resource> resourceMap = new HashMap<>();

		menuPluginList.stream()
					  .filter(Menus::isExtendFromRoot)
					  .map(plugin -> plugin.getMenu())
					  .sorted(Menus.MENU_SORTER)
					  .forEach(menu -> {
						  ResourceWithChildren resourceWithChildren = Resources.convert(menu);

						  resourceList.add(resourceWithChildren);

						  if (menu.getExtensionPoint() != null) {
							  resourceMap.put(menu.getExtensionPoint().getId(), resourceWithChildren);
						  }
					  });

		//only process the two level menu struct in current version
		menuPluginList.stream().filter(Menus::isNotExtendFromRoot).sorted(Menus.PLUGIN_SORTER).forEach(plugin -> {
			if (StringUtils.isEmpty(plugin.getExtensionId())) {
				throw new MenuException("The detached plugin found, please specify the extension point id. ");
			}

			Resource resource = resourceMap.get(plugin.getExtensionId());
			if (resource == null) {
				throw new MenuException(String.format(
						"The resource of extension point id[%s] not found , please make sure the extension point is existed",
						plugin.getExtensionId()));
			}

			if (resource instanceof ResourceWithChildren) {
				((ResourceWithChildren) resource).getChildren().add(Resources.convert(plugin.getMenu()));
			}
			else {
				logger.warn(String.format(
						"The target resource[extensionPointId=%s] does not support children. Can't add the resource[code=%s] to target",
						plugin.getExtensionId(),
						resource.getCode()));
			}
		});

	}

}