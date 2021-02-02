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
package net.gradleutil.aws.rds;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.*;

public class AmazonRDSDescribeDBClustersTask extends ConventionTask { // NOPMD
	
	@Getter
	@Setter
	private String dbClusterIdentifier;
	
	@Getter
	@Setter
	private int maxRecords = 50;
	
	@Getter
	@Setter
	private Map<String, List<String>> filters;
	
	@Getter
	private List<DBCluster> dbClusters;
	
	
	public AmazonRDSDescribeDBClustersTask() {
		setDescription("Describe AWS instances.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void describeDBClusters() { // NOPMD
		String dbClusterIdentifier = getDbClusterIdentifier();
		AmazonRDSPluginExtension ext = getProject().getExtensions().getByType(AmazonRDSPluginExtension.class);
		AmazonRDS rds = ext.getClient();
		try {
			DescribeDBClustersRequest request = new DescribeDBClustersRequest()
				.withMaxRecords(getMaxRecords());
			if (getDbClusterIdentifier() != null && getDbClusterIdentifier().length() > 0) {
				request.withDBClusterIdentifier(getDbClusterIdentifier());
			}
			if (getFilters() != null) {
				request.withFilters(getFilters().entrySet().stream()
					.map(it -> new Filter()
						.withName(it.getKey().toString())
						.withValues(it.getValue()))
					.collect(Collectors.toList()));
			}
			DescribeDBClustersResult dir = rds.describeDBClusters(request);
			dbClusters = dir.getDBClusters();
		} catch (AmazonServiceException e) {
			throw new GradleException("Fail to describe instance: " + dbClusterIdentifier, e);
		}
	}
}
