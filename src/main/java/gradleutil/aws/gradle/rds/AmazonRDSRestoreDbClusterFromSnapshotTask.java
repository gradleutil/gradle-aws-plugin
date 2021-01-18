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
package gradleutil.aws.gradle.rds;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DBCluster;
import com.amazonaws.services.rds.model.RestoreDBClusterFromSnapshotRequest;
import com.amazonaws.services.rds.model.Tag;

public class AmazonRDSRestoreDbClusterFromSnapshotTask extends ConventionTask { // NOPMD
	
	@Getter
	@Setter
	private String dbClusterIdentifier;
	
	@Getter
	@Setter
	private String snapshotIdentifier;
	
	@Getter
	@Setter
	private String engine;
	
	@Getter
	@Setter
	private String dbSubnetGroupName;
	
	@Getter
	@Setter
	private String kmsKeyId;
	
	@Getter
	@Setter
	private String optionGroupName;
	
	@Getter
	@Setter
	private int port;
	
	@Getter
	@Setter
	private Map<String, String> tags;
	
	@Getter
	@Setter
	private List<String> vpcSecurityGroupIds;
	
	@Getter
	private DBCluster dbCluster;
	
	
	public AmazonRDSRestoreDbClusterFromSnapshotTask() {
		setDescription("Restore DbCluster From Snapshot.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void restoreDbClusterToPointInTime() { // NOPMD
		String dbClusterIdentifier = getDbClusterIdentifier();
		
		if (dbClusterIdentifier == null) {
			throw new GradleException("dbClusterIdentifier is not specified");
		}
		
		AmazonRDSPluginExtension ext = getProject().getExtensions().getByType(AmazonRDSPluginExtension.class);
		AmazonRDS rds = ext.getClient();
		
		RestoreDBClusterFromSnapshotRequest request = new RestoreDBClusterFromSnapshotRequest()
			.withDBClusterIdentifier(dbClusterIdentifier)
			.withSnapshotIdentifier(getSnapshotIdentifier())
			.withEngine(getEngine())
			.withDBSubnetGroupName(getDbSubnetGroupName())
			.withKmsKeyId(getKmsKeyId())
			.withVpcSecurityGroupIds(getVpcSecurityGroupIds())
			.withTags(getTags().entrySet().stream()
				.map(it -> new Tag()
					.withKey(it.getKey().toString())
					.withValue(it.getValue().toString()))
				.collect(Collectors.toList()));
		if (getPort() != 0) {
			request.setPort(getPort());
		}
		dbCluster = rds.restoreDBClusterFromSnapshot(request);
		getLogger().info("Restored an RDS cluster to point in time: {}", dbCluster.getDBClusterIdentifier());
		
	}
}
