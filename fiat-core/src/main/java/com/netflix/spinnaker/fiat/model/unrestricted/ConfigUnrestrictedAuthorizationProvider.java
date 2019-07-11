/*
 * Copyright 2019 Armory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.fiat.model.unrestricted;

import com.netflix.spinnaker.fiat.model.Authorization;
import com.netflix.spinnaker.fiat.model.resources.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConfigUnrestrictedAuthorizationProvider implements UnrestrictedAuthorizationProvider {
  private Set<Authorization> unrestrictedAppAccess;
  private Set<Authorization> unrestrictedAccountAccess;
  private Set<Authorization> unrestrictedBuildServiceAccess;

  @Autowired
  public ConfigUnrestrictedAuthorizationProvider(
      @Value("${fiat.unrestricted-access.applications:READ,WRITE,EXECUTE}")
          List<String> unrestrictedAppAccess,
      @Value("${fiat.unrestricted-access.accounts:READ,WRITE}")
          List<String> unrestrictedAccountAccess,
      @Value("${fiat.unrestricted-access.build-services:READ,WRITE,EXECUTE}")
          List<String> unrestrictedBuildServiceAccess) {
    this.unrestrictedAccountAccess =
        Collections.unmodifiableSet(
            unrestrictedAccountAccess.stream()
                .map(s -> Authorization.valueOf(s))
                .collect(Collectors.toSet()));
    this.unrestrictedAppAccess =
        Collections.unmodifiableSet(
            unrestrictedAppAccess.stream()
                .map(s -> Authorization.valueOf(s))
                .collect(Collectors.toSet()));
    ;
    this.unrestrictedBuildServiceAccess =
        Collections.unmodifiableSet(
            unrestrictedBuildServiceAccess.stream()
                .map(s -> Authorization.valueOf(s))
                .collect(Collectors.toSet()));
    ;
  }

  @PostConstruct
  public void install() {
    log.info(
        "Authorizations for unrestricted resources set to: app={}, account={}, build services={}",
        unrestrictedAppAccess,
        unrestrictedAccountAccess,
        unrestrictedBuildServiceAccess);
    UnrestrictedAuthorizations.setProvider(this);
  }

  protected Set<Authorization> getUnrestrictedAuthorizations(Resource.AccessControlled resource) {
    log.debug("Checking resource {} unrestricted authorizations", resource);
    switch (resource.getResourceType()) {
      case ACCOUNT:
        return unrestrictedAccountAccess;
      case APPLICATION:
        return unrestrictedAppAccess;
      case BUILD_SERVICE:
        return unrestrictedBuildServiceAccess;
      default:
        return Authorization.ALL;
    }
  }

  @Override
  public Supplier<Set<Authorization>> getUnrestrictedAuthorizationsSupplier(
      Resource.AccessControlled resource) {
    return () -> getUnrestrictedAuthorizations(resource);
  }
}
