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
package jp.classmethod.aws.gradle.rds;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DBInstanceNotFoundException;
import com.amazonaws.services.rds.model.DBSnapshot;
import com.amazonaws.services.rds.model.DescribeDBSnapshotsRequest;
import com.amazonaws.services.rds.model.DescribeDBSnapshotsResult;

public class AmazonRDSDescribeDbSnapshotsTask extends ConventionTask { // NOPMD
	
	@Getter
	@Setter
	private String dbInstanceIdentifier;
	
	@Getter
	private List<DBSnapshot> dbSnapshots;
	
	
	public AmazonRDSDescribeDbSnapshotsTask() {
		setDescription("Wait RDS instance for specific status.");
		getInputs().property("rerun", LocalDateTime.now(ZoneId.systemDefault()));
		setGroup("AWS");
	}
	
	@TaskAction
	public void waitInstanceForStatus() { // NOPMD
		// to enable conventionMappings feature
		String dbInstanceIdentifier = getDbInstanceIdentifier();
		if (dbInstanceIdentifier == null) {
			throw new GradleException("dbInstanceIdentifier is not specified");
		}
		
		AmazonRDSPluginExtension ext = getProject().getExtensions().getByType(AmazonRDSPluginExtension.class);
		AmazonRDS rds = ext.getClient();
		try {
			DescribeDBSnapshotsResult dir = rds.describeDBSnapshots(new DescribeDBSnapshotsRequest()
				.withDBInstanceIdentifier(dbInstanceIdentifier));
			dbSnapshots = dir.getDBSnapshots();
		} catch (DBInstanceNotFoundException e) {
			throw new GradleException(dbInstanceIdentifier + " does not exist", e);
		} catch (AmazonServiceException e) {
			throw new GradleException("Fail to describe instance: " + dbInstanceIdentifier, e);
		}
		
		dbSnapshots.forEach(dbSnapshot -> getLogger().info("Description of DB snapshot {}.", dbSnapshot));
		
	}
}
