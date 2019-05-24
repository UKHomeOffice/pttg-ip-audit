Audit Service
=

[![Docker Repository on Quay](https://quay.io/repository/ukhomeofficedigital/pttg-ip-audit/status "Docker Repository on Quay")](https://quay.io/repository/ukhomeofficedigital/pttg-ip-hmrc)

## Overview

This is the Audit Service. The service accepts the following requests:
* save audit data to a database
* retrieve audit data
* retrieve audit history
* archive audit data
 
## Find Us

* [GitHub]

## Technical Notes

The API is implemented using Spring Boot and exposes a RESTFul interface.

The endpoints are defined in `AuditResource.java`, `AuditHistoryResource.java` and `ArchiveResource.java`.

## Building

### ACP

This service is built by Gradle on [Drone] using [Drone yaml].

### EBSA

This service is built by Gradle on [Jenkins] using the [build Jenkinsfile].

## Infrastructure

### ACP

This service is packaged as a Docker image and stored on [Quay.io]

This service is deployed by [Drone] onto a Kubernetes cluster using its [Kubernetes configuration]

### EBSA

This service is packaged as a Docker image and stored on AWS ECR.

This service is deployed by [Jenkins] onto a Kubernetes cluster using the [deploy Jenkinsfile].

## Running Locally

Check out the project and run the command `./gradlew bootRun` which will install gradle locally, download all dependencies, build the project and run it.

The API should then be available on http://localhost:8083, where:
- port 8083 is defined in `application.properties` with key `server.port`
- the paths for the various endpoints are defined in the various *Resource.java classes
- the expected request body for each endpoint contains a JSON representation of it's *Request.java class

Note that this service runs locally against a HSQL in memory database.  An example of how to run against a docker based postgres database can be found in `AuditEntryJpaJsonbTest.java`.

## Dependencies

When deployed this service depends upon a database created by [pttg-postgres]. 

## Versioning

For the versions available, see the [tags on this repository].

## Authors

See the list of [contributors] who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENCE.md]
file for details.



[contributors]:                     https://github.com/UKHomeOffice/pttg-ip-audit/graphs/contributors
[Quay.io]:                          https://quay.io/repository/ukhomeofficedigital/pttg-ip-audit
[kubernetes configuration]:         https://github.com/UKHomeOffice/kube-pttg-ip-audit
[Drone yaml]:                       .drone.yml
[tags on this repository]:          https://github.com/UKHomeOffice/pttg-ip-audit/tags
[LICENCE.md]:                       LICENCE.md
[GitHub]:                           https://github.com/orgs/UKHomeOffice/teams/pttg
[Drone]:                            https://drone.acp.homeoffice.gov.uk/UKHomeOffice/pttg-ip-audit
[Jenkins]:                          https://eue-pttg-jenkins-dtzo-kops1.service.ops.iptho.co.uk/job/build_eue_api_audit_service/             
[build Jenkinsfile]:                https://bitbucket.ipttools.info/projects/EUE-API/repos/eue-api-shared-services-toolset/browse/Jenkinsfile.pttg_ip_audit
[deploy Jenkinsfile]:               https://eue-pttg-jenkins-dtzo-kops1.service.ops.iptho.co.uk/job/deploy_np_dev_push_eue_api_project_tiller/
