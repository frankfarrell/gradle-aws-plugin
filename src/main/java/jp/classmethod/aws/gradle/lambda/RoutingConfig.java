/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.classmethod.aws.gradle.lambda;

import java.util.Collections;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import com.amazonaws.services.lambda.model.AliasRoutingConfiguration;

/**
 * Created by frankfarrell on 16/01/2018.
 *
 * This a bit different than the aws api.
 */
public class RoutingConfig {
	
	@Input
	@Getter
	@Setter
	private Double additionalVersionWeight;
	
	@Input
	@Optional
	@Getter
	@Setter
	private Boolean usePreviousVersion;
	
	@Input
	@Optional
	@Getter
	@Setter
	private Boolean useNextVersion;
	
	@Input
	@Optional
	@Getter
	@Setter
	private String additionalVersion;
	
	
	public RoutingConfig() {
	}
	
	public AliasRoutingConfiguration getAliasRoutingConfiguration(final String functionName,
			final String functionVersion) {
		if (getAdditionalVersionWeight() == null) {
			throw new GradleException("Additional Version Weight for routing config is required");
		}
		if (getAdditionalVersion() == null
				&& getUsePreviousVersion() == null
				&& getUseNextVersion() == null) {
			throw new GradleException("Exactly one of AdditionalVersion, UsePreviousVersion, "
					+ "UseNextVersion for routing config is required");
		}
		
		final Double additionalVersionWeight = getAdditionalVersionWeight();
		
		final AliasRoutingConfiguration aliasRoutingConfiguration = new AliasRoutingConfiguration();
		
		if (getAdditionalVersion() != null) {
			aliasRoutingConfiguration.withAdditionalVersionWeights(
					Collections.singletonMap(getAdditionalVersion(), additionalVersionWeight));
		} else if (getUsePreviousVersion() != null && getUsePreviousVersion()) {
			
			try {
				final Long functionVersionAsLong = Long.valueOf(functionVersion);
				final Long prevVersion = getPreviousVersion(functionName, functionVersionAsLong);
				aliasRoutingConfiguration.withAdditionalVersionWeights(
						Collections.singletonMap(prevVersion.toString(), additionalVersionWeight));
			} catch (final NumberFormatException e) {
				throw new GradleException("functionVersion must be a number if usePreviousVersion is true");
			}
			
		} else if (getUseNextVersion() != null && getUseNextVersion()) {
			try {
				final Long functionVersionAsLong = Long.valueOf(functionVersion);
				final Long nextVersion = getNextVersion(functionVersionAsLong);
				aliasRoutingConfiguration.withAdditionalVersionWeights(
						Collections.singletonMap(nextVersion.toString(), additionalVersionWeight));
			} catch (final NumberFormatException e) {
				throw new GradleException("functionVersion must be a number if useNextVersion is true");
			}
		}
		return aliasRoutingConfiguration;
	}
	
	private Long getNextVersion(final Long functionVersion) {
		return functionVersion + 1;
	}
	
	private Long getPreviousVersion(final String functionName,
			final Long functionVersion) {
		if (functionVersion <= 1L) {
			throw new GradleException("There is no older version for "
					+ functionName);
		} else {
			return functionVersion - 1;
		}
	}
}