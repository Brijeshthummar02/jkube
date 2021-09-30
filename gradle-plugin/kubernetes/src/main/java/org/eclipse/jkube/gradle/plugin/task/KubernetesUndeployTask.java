/**
 * Copyright (c) 2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at:
 *
 *     https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.jkube.gradle.plugin.task;

import org.eclipse.jkube.gradle.plugin.KubernetesExtension;
import org.eclipse.jkube.kit.common.util.ResourceUtil;
import org.eclipse.jkube.kit.config.resource.ResourceConfig;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KubernetesUndeployTask extends AbstractJKubeTask {

  @Inject
  public KubernetesUndeployTask(Class<? extends KubernetesExtension> extensionClass) {
    super(extensionClass);
    setDescription("Undeploys (deletes) the kubernetes resources generated by the current project.");
  }

  @Override
  public void run() {
    try {
      ResourceConfig resources = ResourceConfig.toBuilder(kubernetesExtension.resources)
        .namespace(Optional.ofNullable(kubernetesExtension.getNamespaceOrDefault())
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .orElse(null))
        .build();
      final File environmentResourceDir = ResourceUtil.getFinalResourceDir(resolveResourceSourceDirectory(), kubernetesExtension.getResourceEnvironmentOrDefault());
      final String fallbackNamespace = Optional.ofNullable(kubernetesExtension.resources)
        .map(ResourceConfig::getNamespace).orElse(clusterAccess.getNamespace());
      jKubeServiceHub.getUndeployService()
        .undeploy(fallbackNamespace, environmentResourceDir, resources, findManifestsToUndeploy().toArray(new File[0]));
    } catch (IOException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

  protected File findResources() {
    return kubernetesExtension.getKubernetesManifestOrDefault();
  }

  protected List<File> findManifestsToUndeploy() {
    final List<File> ret = new ArrayList<>();
    ret.add(findResources());
    return ret;
  }
}