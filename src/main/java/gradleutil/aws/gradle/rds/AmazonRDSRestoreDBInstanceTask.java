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
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.RestoreDBInstanceFromDBSnapshotRequest;
import com.amazonaws.services.rds.model.Tag;

public class AmazonRDSRestoreDBInstanceTask extends ConventionTask {
	
	@Getter
	@Setter
	private String dbName;
	
	@Getter
	@Setter
	private String dbInstanceIdentifier;
	
	@Getter
	@Setter
	private Integer allocatedStorage;
	
	@Getter
	@Setter
	private String dbInstanceClass;
	
	@Getter
	@Setter
	private String engine;
	
	@Getter
	@Setter
	private List<String> vpcSecurityGroupIds;
	
	@Getter
	@Setter
	private String dbSubnetGroupName;
	
	@Getter
	@Setter
	private String preferredMaintenanceWindow;
	
	@Getter
	@Setter
	private String dbParameterGroupName;
	
	@Getter
	@Setter
	private Integer backupRetentionPeriod;
	
	@Getter
	@Setter
	private String preferredBackupWindow;
	
	@Getter
	@Setter
	private Integer port;
	
	@Getter
	@Setter
	private Boolean multiAZ;
	
	@Getter
	@Setter
	private String engineVersion;
	
	@Getter
	@Setter
	private Boolean autoMinorVersionUpgrade;
	
	@Getter
	@Setter
	private String licenseModel;
	
	@Getter
	@Setter
	private Integer iops;
	
	@Getter
	@Setter
	private String optionGroupName;
	
	@Getter
	@Setter
	private Boolean publiclyAccessible;
	
	@Getter
	@Setter
	private String characterSetName;
	
	@Getter
	@Setter
	private String storageType;
	
	@Getter
	@Setter
	private String tdeCredentialArn;
	
	@Getter
	@Setter
	private String tdeCredentialPassword;
	
	@Getter
	@Setter
	private Boolean storageEncrypted;
	
	@Getter
	@Setter
	private String kmsKeyId;
	
	@Getter
	@Setter
	private Boolean copyTagsToSnapshot;
	
	@Getter
	@Setter
	private Integer promotionTier;
	
	@Getter
	@Setter
	private String dbClusterIdentifier;
	
	@Getter
	@Setter
	private String availabilityZone;
	
	@Getter
	@Setter
	private List<String> securityGroups;
	
	@Getter
	private DBInstance dbInstance;
	
	@Getter
	@Setter
	private Map<String, String> tags;
	
	
	public AmazonRDSRestoreDBInstanceTask() {
		setDescription("Restore RDS instance.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void restoreDBInstance() {
		String dbInstanceIdentifier = getDbInstanceIdentifier();
		String dbInstanceClass = getDbInstanceClass();
		String engine = getEngine();
		
		if (dbInstanceClass == null) {
			throw new GradleException("dbInstanceClass is required");
		}
		if (dbInstanceIdentifier == null) {
			throw new GradleException("dbInstanceIdentifier is required");
		}
		if (engine == null) {
			throw new GradleException("engine is required");
		}
		
		AmazonRDSPluginExtension ext = getProject().getExtensions().getByType(AmazonRDSPluginExtension.class);
		AmazonRDS rds = ext.getClient();
		
		RestoreDBInstanceFromDBSnapshotRequest request = new RestoreDBInstanceFromDBSnapshotRequest()
			.withDBName(getDbName())
			.withDBInstanceIdentifier(dbInstanceIdentifier)
			.withDBInstanceClass(dbInstanceClass)
			.withEngine(engine)
			.withVpcSecurityGroupIds(getVpcSecurityGroupIds())
			.withDBSubnetGroupName(getDbSubnetGroupName())
			.withDBParameterGroupName(getDbParameterGroupName())
			.withPort(getPort())
			.withMultiAZ(getMultiAZ())
			.withAutoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
			.withLicenseModel(getLicenseModel())
			.withIops(getIops())
			.withOptionGroupName(getOptionGroupName())
			.withPubliclyAccessible(getPubliclyAccessible())
			.withStorageType(getStorageType())
			.withTdeCredentialArn(getTdeCredentialArn())
			.withTdeCredentialPassword(getTdeCredentialPassword())
			.withCopyTagsToSnapshot(getCopyTagsToSnapshot())
			.withAvailabilityZone(getAvailabilityZone())
			.withTags(getTags().entrySet().stream()
				.map(it -> new Tag()
					.withKey(it.getKey())
					.withValue(it.getValue()))
				.collect(Collectors.toList()));
		
		dbInstance = rds.restoreDBInstanceFromDBSnapshot(request);
		getLogger().info("Restore RDS instance requested: {}", dbInstance.getDBInstanceIdentifier());
	}
}
