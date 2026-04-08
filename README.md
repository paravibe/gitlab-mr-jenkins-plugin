## Introduction

GitLab Merge Requests builder allows to trigger a job on different merge request actions.

## Variables
```
gitlabMergeRequestTitle
gitlabMergeRequestDescription
gitlabMergeRequestUrl
gitlabMergeRequestId
gitlabMergeRequestIid
gitlabMergeRequestSourceBranch
gitlabMergeRequestTargetBranch
gitlabMergeRequestProjectId
gitlabMergeRequestProjectName
gitlabMergeRequestProjectWebUrl
gitlabMergeRequestLastCommitId
gitlabMergeRequestSshUrl
gitlabMergeRequestWebUrl
gitlabMergeRequestLabels
gitlabMergeRequestUserName
gitlabMergeRequestUserUsername
gitlabMergeRequestAuthorUsername
gitlabJobPipelineName
```

## Develop

`docker exec -it mvn mvn hpi:run -Dhost=0.0.0.0` - run local instance of Jenkins with plugin
http://localhost:8080/jenkins/

`docker exec -it mvn mvn install` - build and run tests
