project 'DeployStrategies', {

  environment 'BlueGreen Production_1', {
    description = ''
    environmentEnabled = '1'
    projectName = 'DeployStrategies'
    reservationRequired = '0'

    environmentTier 'BackEnd', {
      resourceName = [
        'Prod1_bg',
      ]
    }
  }

  environment 'BlueGreen Production_2', {
    environmentEnabled = '1'
    reservationRequired = '0'

    environmentTier 'BackEnd', {
      resourceName = [
        'Prod2_bg',
      ]
    }
  }

  environment 'DEV', {
    description = ''
    environmentEnabled = '1'
    reservationRequired = '0'

    environmentTier 'BackEnd', {
      resourceName = [
        'DEV',
      ]
    }
  }

  environment 'Prod1', {
    description = ''
    environmentEnabled = '1'
    reservationRequired = '0'

    environmentTier 'Backend', {
      resourceName = [
        'Prod1_canary',
      ]
    }
  }

  environment 'Prod2', {
    description = ''
    environmentEnabled = '1'
    reservationRequired = '0'

    environmentTier 'BackEnd', {
      resourceName = [
        'Prod2_canary',
      ]
    }
  }

  environment 'QA', {
    environmentEnabled = '1'
    reservationRequired = '0'

    environmentTier 'BackEnd', {
      resourceName = [
        'QA',
      ]
    }
  }

  environment 'Rolling Deploy Production', {
    description = ''
    environmentEnabled = '1'
    reservationRequired = '0'
    rollingDeployEnabled = '1'
    rollingDeployType = 'phase'

    rollingDeployPhase 'Production', {
      orderIndex = '1'
      rollingDeployPhaseType = 'tagged'
    }

    environmentTier 'Production', {
      resourceName = [
        'Prod1',
        'Prod2',
      ]
      resourcePhaseMapping = ['Prod1': 'Production', 'Prod2': 'Production']
    }
  }

  procedure 'checkDeployment', {
    description = ''
    jobNameTemplate = ''
    resourceName = ''
    timeLimit = ''
    timeLimitUnits = 'minutes'
    workspaceName = ''

    step 'query', {
      description = ''
      alwaysRun = '0'
      broadcast = '0'
      command = '''echo $[url]

while curl $[url] --max-time 1 ; ret=$? ; [ $ret -ne 0 ];do
    echo "No response..."
done

echo $ret'''
      condition = ''
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = ''
      parallel = '0'
      postProcessor = ''
      precondition = ''
      releaseMode = 'none'
      resourceName = ''
      shell = 'sh'
      timeLimit = ''
      timeLimitUnits = 'minutes'
      workingDirectory = ''
      workspaceName = ''
    }
  }

  procedure 'CheckResults', {
    description = ''
    jobNameTemplate = ''
    resourceName = ''
    timeLimit = ''
    timeLimitUnits = 'minutes'
    workspaceName = ''

    step 'query', {
      description = ''
      alwaysRun = '0'
      broadcast = '0'
      command = '''echo \'Results of load Balancer\'

for i in 1 2 3 4 5
do
   curl localhost
done

'''
      condition = ''
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = ''
      parallel = '0'
      postProcessor = ''
      precondition = ''
      releaseMode = 'none'
      resourceName = ''
      shell = ''
      timeLimit = ''
      timeLimitUnits = 'minutes'
      workingDirectory = ''
      workspaceName = ''
    }
  }

  procedure 'disableCanaryMode', {
    description = ''
    jobNameTemplate = ''
    resourceName = ''
    timeLimit = ''
    timeLimitUnits = 'minutes'
    workspaceName = ''
  }

  procedure 'UpdateLoadBalancer', {
    description = ''
    jobNameTemplate = ''
    resourceName = ''
    timeLimit = ''
    timeLimitUnits = 'minutes'
    workspaceName = ''

    step 'UpdateNginx', {
      description = ''
      alwaysRun = '0'
      broadcast = '0'
      command = '''use strict;
use ElectricCommander;

my $ec = new ElectricCommander();
my $strategy= "$[/myProject/deployStrategy]";

my $env = "$[/myProject/targetEnv]";

print " $env $strategy \\n";  

if ( $env eq \'Prod2\' &&  $strategy eq \'Blue/Green\') {
   exec "rm -f /etc/nginx/sites-available/default";
   exec "ln -s ~/deployment-strategies/load-balancer/traffic_100_prodB.conf /etc/nginx/sites-available/default"; 
   print "Reload Nginx file traffic_100_prodB.conf \\n"; 
} 


if ( $env eq \'Prod1\' &&  $strategy eq \'Blue/Green\') {
   #exec "rm -f /etc/nginx/sites-available/default";
   #exec "ln -s ~/deployment-strategies/load-balancer/traffic_100_prodA.conf /etc/nginx/sites-available/default"; 
   print "Reload Nginx file traffic_100_prodA.conf \\n"; 
} 

if ( $env eq \'Prod1\' &&  $strategy eq \'Canary\') {
   #exec "rm -f /etc/nginx/sites-available/default";
   #exec "ln -s ~/deployment-strategies/load-balancer/traffic_30_70_prodA_B.conf /etc/nginx/sites-available/default"; 
   print "Reload Nginx file traffic_30_70_prodA_B.conf \\n";  
}

if ( $env eq \'Prod2\' &&  $strategy eq \'Canary\') {
   #exec "rm -f /etc/nginx/sites-available/default";
   #exec "ln -s ~/deployment-strategies/load-balancer/traffic_70_30_prodA_B.conf /etc/nginx/sites-available/default"; 
   print "Reload Nginx file traffic_70_30_prodA_B.conf \\n"; 
} 

print "Reloaded successfully  \\n";
print "traffic to $env environment is now completed";


'''
      condition = ''
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = ''
      parallel = '0'
      postProcessor = ''
      precondition = ''
      releaseMode = 'none'
      resourceName = 'autotests_basicTraining_DEV'
      shell = 'ec-perl'
      timeLimit = ''
      timeLimitUnits = 'minutes'
      workingDirectory = ''
      workspaceName = ''
    }
  }

  procedure 'updateTargetEnv', {
    description = ''
    jobNameTemplate = ''
    resourceName = ''
    timeLimit = ''
    timeLimitUnits = 'minutes'
    workspaceName = ''

    step 'update', {
      description = ''
      alwaysRun = '0'
      broadcast = '0'
      command = '''use strict;
use ElectricCommander;

my $ec = new ElectricCommander();

my $current = "$[/myProject/TargetEnv]";


if ($current eq "Production-A") {
  $ec->setProperty("/myProject/TargetEnv", "Production-B");
}
if ($current eq "Production-B") {
  $ec->setProperty("/myProject/TargetEnv", "Production-A");
}'''
      condition = ''
      errorHandling = 'failProcedure'
      exclusiveMode = 'none'
      logFileName = ''
      parallel = '0'
      postProcessor = ''
      precondition = ''
      releaseMode = 'none'
      resourceName = 'LoadBalancer'
      shell = 'ec-perl'
      timeLimit = ''
      timeLimitUnits = 'minutes'
      workingDirectory = ''
      workspaceName = ''
    }
  }

  application 'app-v1-SpringBoot', {
    description = ''

    applicationTier 'Backend', {
      applicationName = 'app-v1-SpringBoot'
      component 'app-v1', {
        applicationName = 'app-v1-SpringBoot'
        pluginKey = 'EC-Artifact'
        reference = '0'

        process 'deploy_artifact', {
          processType = 'DEPLOY'
          smartUndeployEnabled = '0'
          timeLimitUnits = 'minutes'

          processStep 'deploy', {
            actualParameter = [
              'commandToRun': '''echo Using /tmp/spring-boot-0.0.1-SNAPSHOT.jar.

java -Dserver.port=8070  -jar /tmp/spring-boot-0.0.1-SNAPSHOT.jar > backend.log 2>&1 & ''',
              'shellToUse': 'sh',
            ]
            alwaysRun = '0'
            dependencyJoinType = 'and'
            errorHandling = 'failProcedure'
            processStepType = 'command'
            subprocedure = 'RunCommand'
            subproject = '/plugins/EC-Core/project'
            useUtilityResource = '0'
          }
        }

        process 'retrieve_artifact', {
          processType = 'OTHER'
          timeLimitUnits = 'minutes'

          processStep 'retrieve_artifact', {
            actualParameter = [
              'artifactName': '$[/myComponent/ec_content_details/artifactName]',
              'artifactVersionLocationProperty': '$[/myComponent/ec_content_details/artifactVersionLocationProperty]',
              'filterList': '$[/myComponent/ec_content_details/filterList]',
              'overwrite': '$[/myComponent/ec_content_details/overwrite]',
              'retrieveToDirectory': '$[/myComponent/ec_content_details/retrieveToDirectory]',
              'versionRange': '$[/myJob/ec_app-v1-version]',
            ]
            alwaysRun = '0'
            dependencyJoinType = 'and'
            errorHandling = 'failProcedure'
            processStepType = 'component'
            subprocedure = 'Retrieve'
            subproject = '/plugins/EC-Artifact/project'
            useUtilityResource = '0'
          }
        }

        // Custom properties

        property 'ec_content_details', {

          // Custom properties

          property 'artifactName', value: 'com.example:spring-boot', {
            expandable = '1'
            suppressValueTracking = '0'
          }
          artifactVersionLocationProperty = '/myJob/retrievedArtifactVersions/$[assignedResourceName]'
          filterList = ''

          property 'overwrite', value: 'update', {
            expandable = '1'
            suppressValueTracking = '0'
          }
          pluginProcedure = 'Retrieve'

          property 'pluginProjectName', value: 'EC-Artifact', {
            expandable = '1'
            suppressValueTracking = '0'
          }
          retrieveToDirectory = '/tmp'

          property 'versionRange', value: '0.0.1-SNAPSHOT', {
            expandable = '1'
            suppressValueTracking = '0'
          }
        }
      }
    }

    process 'app_v1_deploy', {
      applicationName = 'app-v1-SpringBoot'
      exclusiveEnvironment = '0'
      processType = 'OTHER'
      timeLimitUnits = 'minutes'

      formalParameter 'ec_app-v1-run', defaultValue: '1', {
        expansionDeferred = '1'
        required = '0'
        type = 'checkbox'
      }

      formalParameter 'ec_app-v1-version', defaultValue: '$[/projects/DeployStrategies/applications/app-v1-SpringBoot/components/app-v1/ec_content_details/versionRange]', {
        expansionDeferred = '1'
        required = '0'
        type = 'entry'
      }

      formalParameter 'ec_enforceDependencies', defaultValue: '0', {
        expansionDeferred = '1'
        required = '0'
        type = 'checkbox'
      }

      formalParameter 'ec_smartDeployOption', defaultValue: '1', {
        expansionDeferred = '1'
        required = '0'
        type = 'checkbox'
      }

      formalParameter 'ec_stageArtifacts', defaultValue: '0', {
        expansionDeferred = '1'
        required = '0'
        type = 'checkbox'
      }

      processStep 'retrieve', {
        alwaysRun = '0'
        applicationTierName = 'Backend'
        dependencyJoinType = 'and'
        errorHandling = 'abortJob'
        processStepType = 'process'
        subcomponent = 'app-v1'
        subcomponentApplicationName = 'app-v1-SpringBoot'
        subcomponentProcess = 'retrieve_artifact'
        useUtilityResource = '0'

        // Custom properties

        property 'ec_deploy', {

          // Custom properties
          ec_notifierStatus = '0'
        }
      }

      processStep 'deploy', {
        alwaysRun = '0'
        applicationTierName = 'Backend'
        dependencyJoinType = 'and'
        errorHandling = 'abortJob'
        processStepType = 'process'
        subcomponent = 'app-v1'
        subcomponentApplicationName = 'app-v1-SpringBoot'
        subcomponentProcess = 'deploy_artifact'
        useUtilityResource = '0'

        // Custom properties

        property 'ec_deploy', {

          // Custom properties
          ec_notifierStatus = '0'
        }
      }

      processDependency 'retrieve', targetProcessStepName: 'deploy', {
        branchType = 'ALWAYS'
      }

      // Custom properties

      property 'ec_deploy', {

        // Custom properties
        ec_notifierStatus = '0'
      }
    }

    tierMap '1ce529b9-9b63-11eb-88f3-feaab62b41f3', {
      applicationName = 'app-v1-SpringBoot'
      environmentName = 'DEV'
      environmentProjectName = 'DeployStrategies'
      tierMapping '1d0c39dc-9b63-11eb-a7ee-feaab62b41f3', {
        applicationTierName = 'Backend'
        environmentTierName = 'BackEnd'
        tierMapName = '1ce529b9-9b63-11eb-88f3-feaab62b41f3'
      }
    }

    tierMap '22dd99f4-9930-11eb-a451-feaab62b41f3', {
      applicationName = 'app-v1-SpringBoot'
      environmentName = 'BlueGreen Production_1'
      environmentProjectName = 'DeployStrategies'
      tierMapping '230545f6-9930-11eb-9872-feaab62b41f3', {
        applicationTierName = 'Backend'
        environmentTierName = 'BackEnd'
        tierMapName = '22dd99f4-9930-11eb-a451-feaab62b41f3'
      }
    }

    tierMap '46474d50-9930-11eb-8a9e-feaab62b41f3', {
      applicationName = 'app-v1-SpringBoot'
      environmentName = 'Prod1'
      environmentProjectName = 'DeployStrategies'
      tierMapping '466de868-9930-11eb-a4bf-feaab62b41f3', {
        applicationTierName = 'Backend'
        environmentTierName = 'Backend'
        tierMapName = '46474d50-9930-11eb-8a9e-feaab62b41f3'
      }
    }

    tierMap '4e2d96a3-9b63-11eb-86ed-feaab62b41f3', {
      applicationName = 'app-v1-SpringBoot'
      environmentName = 'QA'
      environmentProjectName = 'DeployStrategies'
      tierMapping '4e54a672-9b63-11eb-88f3-feaab62b41f3', {
        applicationTierName = 'Backend'
        environmentTierName = 'BackEnd'
        tierMapName = '4e2d96a3-9b63-11eb-86ed-feaab62b41f3'
      }
    }

    tierMap 'b3fbc961-992e-11eb-a4bf-feaab62b41f3', {
      applicationName = 'app-v1-SpringBoot'
      environmentName = 'Rolling Deploy Production'
      environmentProjectName = 'DeployStrategies'
      tierMapping 'b427460c-992e-11eb-8a9e-feaab62b41f3', {
        applicationTierName = 'Backend'
        environmentTierName = 'Production'
        tierMapName = 'b3fbc961-992e-11eb-a4bf-feaab62b41f3'
      }
    }

    // Custom properties

    property 'ec_deploy', {

      // Custom properties
      ec_notifierStatus = '0'
    }

    property 'jobCounter', value: '8', {
      expandable = '1'
      suppressValueTracking = '1'
    }
  }

  pipeline 'Canary Deployment', {
    description = ''
    disableMultipleActiveRuns = '0'
    disableRestart = '0'
    enabled = '1'
    overrideWorkspace = '0'
    skipStageMode = 'ENABLED'

    formalParameter 'ec_stagesToRun', {
      expansionDeferred = '1'
      required = '0'
    }

    stage 'Dev', {
      description = ''
      colorCode = '#00adee'
      completionType = 'auto'
      pipelineName = 'Canary Deployment'
      waitForPlannedStartDate = '0'

      gate 'PRE'

      gate 'POST'

      task 'Deploy', {
        actualParameter = [
          'ec_enforceDependencies': '1',
        ]
        advancedMode = '0'
        allowOutOfOrderRun = '0'
        alwaysRun = '0'
        enabled = '1'
        errorHandling = 'stopOnError'
        insertRollingDeployManualStep = '0'
        resourceName = ''
        skippable = '0'
        subproject = 'DeployStrategies'
        taskProcessType = 'APPLICATION'
        taskType = 'PROCESS'
        useApproverAcl = '0'
        waitForPlannedStartDate = '0'
      }
    }

    stage 'QA', {
      description = ''
      colorCode = '#ff7f0e'
      completionType = 'auto'
      pipelineName = 'Canary Deployment'
      waitForPlannedStartDate = '0'

      gate 'PRE'

      gate 'POST'

      task 'Deploy', {
        actualParameter = [
          'ec_enforceDependencies': '1',
        ]
        advancedMode = '0'
        allowOutOfOrderRun = '0'
        alwaysRun = '0'
        enabled = '1'
        errorHandling = 'stopOnError'
        insertRollingDeployManualStep = '0'
        resourceName = ''
        skippable = '0'
        subproject = 'DeployStrategies'
        taskProcessType = 'APPLICATION'
        taskType = 'PROCESS'
        useApproverAcl = '0'
        waitForPlannedStartDate = '0'
      }
    }

    stage 'Prod', {
      description = ''
      colorCode = '#2ca02c'
      completionType = 'auto'
      pipelineName = 'Canary Deployment'
      waitForPlannedStartDate = '0'

      gate 'PRE'

      gate 'POST'

      task 'Deployer', {
        description = ''
        actualParameter = [
          'ec_enforceDependencies': '1',
          'ec_smartDeployOption': '1',
          'ec_stageArtifacts': '1',
        ]
        advancedMode = '0'
        allowOutOfOrderRun = '0'
        alwaysRun = '0'
        enabled = '1'
        environmentName = 'Prod1'
        environmentProjectName = 'DeployStrategies'
        errorHandling = 'stopOnError'
        insertRollingDeployManualStep = '0'
        resourceName = ''
        rollingDeployEnabled = '0'
        skippable = '0'
        subapplication = 'app-v1-SpringBoot'
        subprocess = 'app_v1_deploy'
        subproject = 'DeployStrategies'
        taskProcessType = 'APPLICATION'
        taskType = 'PROCESS'
        useApproverAcl = '0'
        waitForPlannedStartDate = '0'
      }

      task 'Smoke Test', {
        description = ''
        advancedMode = '0'
        allowOutOfOrderRun = '0'
        alwaysRun = '0'
        enabled = '1'
        errorHandling = 'stopOnError'
        insertRollingDeployManualStep = '0'
        resourceName = ''
        skippable = '0'
        subprocedure = 'checkDeployment'
        subproject = 'DeployStrategies'
        taskType = 'PROCEDURE'
        useApproverAcl = '0'
        waitForPlannedStartDate = '0'
      }

      task 'Activate Canary State', {
        description = ''
        advancedMode = '0'
        allowOutOfOrderRun = '0'
        allowSkip = '0'
        alwaysRun = '0'
        disableFailure = '0'
        enabled = '1'
        errorHandling = 'stopOnError'
        insertRollingDeployManualStep = '0'
        instruction = ''
        notificationEnabled = '1'
        notificationTemplate = 'ec_default_pipeline_manual_task_notification_template'
        resourceName = ''
        skippable = '0'
        subproject = 'DeployStrategies'
        taskType = 'MANUAL'
        useApproverAcl = '0'
        waitForPlannedStartDate = '0'
        approver = [
          'admin',
        ]
      }

      task 'Divert subset of traffic to enable canary testing', {
        description = ''
        advancedMode = '0'
        allowOutOfOrderRun = '0'
        alwaysRun = '0'
        enabled = '1'
        errorHandling = 'stopOnError'
        insertRollingDeployManualStep = '0'
        resourceName = ''
        skippable = '0'
        subprocedure = 'UpdateLoadBalancer'
        subproject = 'DeployStrategies'
        taskType = 'PROCEDURE'
        useApproverAcl = '0'
        waitForPlannedStartDate = '0'
      }

      task 'Canary Tests ', {
        description = ''
        advancedMode = '0'
        allowOutOfOrderRun = '0'
        alwaysRun = '0'
        enabled = '1'
        errorHandling = 'stopOnError'
        insertRollingDeployManualStep = '0'
        resourceName = ''
        skippable = '0'
        subprocedure = 'CheckResults'
        subproject = 'DeployStrategies'
        taskType = 'PROCEDURE'
        useApproverAcl = '0'
        waitForPlannedStartDate = '0'
      }

      task 'Validate new release', {
        description = ''
        advancedMode = '0'
        allowOutOfOrderRun = '0'
        allowSkip = '0'
        alwaysRun = '0'
        disableFailure = '0'
        enabled = '1'
        errorHandling = 'stopOnError'
        insertRollingDeployManualStep = '0'
        instruction = ''
        notificationEnabled = '1'
        notificationTemplate = 'ec_default_pipeline_manual_task_notification_template'
        resourceName = ''
        skippable = '0'
        subproject = 'DeployStrategies'
        taskType = 'MANUAL'
        useApproverAcl = '0'
        waitForPlannedStartDate = '0'
        approver = [
          'admin',
        ]
      }

      task 'Deactivate Canary State', {
        description = ''
        advancedMode = '0'
        allowOutOfOrderRun = '0'
        alwaysRun = '0'
        enabled = '1'
        errorHandling = 'stopOnError'
        insertRollingDeployManualStep = '0'
        resourceName = ''
        skippable = '0'
        subprocedure = 'disableCanaryMode'
        subproject = 'DeployStrategies'
        taskType = 'PROCEDURE'
        useApproverAcl = '0'
        waitForPlannedStartDate = '0'
      }
    }
  }

  release 'B/G Deployment', {
    description = ''
    disableMultipleActiveRuns = '0'
    plannedEndDate = '2021-05-02'
    plannedStartDate = '2021-05-01'

    pipeline 'Canary Deployment', {
      description = ''
      disableMultipleActiveRuns = '0'
      disableRestart = '0'
      enabled = '1'
      overrideWorkspace = '0'
      releaseName = 'B/G Deployment'
      skipStageMode = 'ENABLED'
      templatePipelineName = 'Canary Deployment'
      templatePipelineProjectName = 'DeployStrategies'

      formalParameter 'ec_stagesToRun', {
        expansionDeferred = '1'
        required = '0'
      }

      stage 'Dev', {
        description = ''
        colorCode = '#00adee'
        completionType = 'auto'
        pipelineName = 'Canary Deployment'
        waitForPlannedStartDate = '0'

        gate 'PRE'

        gate 'POST'

        task 'Deployer', {
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          alwaysRun = '0'
          deployerRunType = 'serial'
          enabled = '1'
          errorHandling = 'stopOnError'
          insertRollingDeployManualStep = '0'
          resourceName = ''
          skippable = '0'
          subproject = 'DeployStrategies'
          taskType = 'DEPLOYER'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
        }
      }

      stage 'QA', {
        description = ''
        colorCode = '#ff7f0e'
        completionType = 'auto'
        pipelineName = 'Canary Deployment'
        waitForPlannedStartDate = '0'

        gate 'PRE'

        gate 'POST'

        task 'Deployer', {
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          alwaysRun = '0'
          deployerRunType = 'serial'
          enabled = '1'
          errorHandling = 'stopOnError'
          insertRollingDeployManualStep = '0'
          resourceName = ''
          skippable = '0'
          subproject = 'DeployStrategies'
          taskType = 'DEPLOYER'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
        }
      }

      stage 'Prod', {
        description = ''
        colorCode = '#2ca02c'
        completionType = 'auto'
        pipelineName = 'Canary Deployment'
        waitForPlannedStartDate = '0'

        gate 'PRE'

        gate 'POST'

        task 'Deployer', {
          description = ''
          actualParameter = [
            'ec_enforceDependencies': '1',
            'ec_smartDeployOption': '1',
            'ec_stageArtifacts': '1',
          ]
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          alwaysRun = '0'
          enabled = '1'
          environmentName = 'Prod1'
          environmentProjectName = 'DeployStrategies'
          errorHandling = 'stopOnError'
          insertRollingDeployManualStep = '0'
          resourceName = ''
          rollingDeployEnabled = '0'
          skippable = '0'
          subapplication = 'app-v1-SpringBoot'
          subprocess = 'app_v1_deploy'
          subproject = 'DeployStrategies'
          taskProcessType = 'APPLICATION'
          taskType = 'PROCESS'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
        }

        task 'Smoke Test', {
          description = ''
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          alwaysRun = '0'
          enabled = '1'
          errorHandling = 'stopOnError'
          insertRollingDeployManualStep = '0'
          resourceName = ''
          skippable = '0'
          subprocedure = 'checkDeployment'
          subproject = 'DeployStrategies'
          taskType = 'PROCEDURE'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
        }

        task 'Switch BLUE and GREEN', {
          description = ''
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          allowSkip = '0'
          alwaysRun = '0'
          disableFailure = '0'
          enabled = '1'
          errorHandling = 'continueOnError'
          insertRollingDeployManualStep = '0'
          instruction = ''
          notificationEnabled = '1'
          notificationTemplate = 'ec_default_pipeline_manual_task_notification_template'
          resourceName = ''
          skippable = '0'
          subproject = 'DeployStrategies'
          taskType = 'MANUAL'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
          approver = [
            'admin',
          ]
        }

        task 'Divert subset of traffic to enable canary testing', {
          description = ''
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          alwaysRun = '0'
          enabled = '1'
          errorHandling = 'stopOnError'
          insertRollingDeployManualStep = '0'
          resourceName = ''
          skippable = '0'
          subprocedure = 'UpdateLoadBalancer'
          subproject = 'DeployStrategies'
          taskType = 'PROCEDURE'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
        }

        task 'Update current and new environment references ', {
          description = ''
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          alwaysRun = '0'
          enabled = '1'
          errorHandling = 'continueOnError'
          insertRollingDeployManualStep = '0'
          resourceName = ''
          skippable = '0'
          subprocedure = 'updateTargetEnv'
          subproject = 'DeployStrategies'
          taskType = 'PROCEDURE'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
        }
      }

      // Custom properties

      property 'ec_counters', {

        // Custom properties
        pipelineCounter = '2'
      }
    }

    deployerApplication 'app-v1-SpringBoot', {
      enforceDependencies = '1'
      errorHandling = 'stopOnError'
      orderIndex = '1'
      processName = 'app_v1_deploy'
      smartDeploy = '1'
      stageArtifacts = '1'

      deployerConfiguration '5f5defbf-9935-11eb-9872-feaab62b41f3', {
        environmentName = 'Rolling Deploy Production'
        environmentTemplateProjectName = 'null'
        insertRollingDeployManualStep = '0'
        processName = 'app_v1_deploy'
        skipDeploy = '0'
        stageName = 'Prod'
      }

      deployerConfiguration 'dc5ee998-9b62-11eb-a2f8-feaab62b41f3', {
        deployerTaskName = 'Deployer'
        environmentName = 'DEV'
        insertRollingDeployManualStep = '0'
        processName = 'app_v1_deploy'
        skipDeploy = '0'
        stageName = 'Dev'
      }

      deployerConfiguration 'dc6be266-9b62-11eb-9d8c-feaab62b41f3', {
        deployerTaskName = 'Deployer'
        environmentName = 'QA'
        environmentTemplateProjectName = 'null'
        insertRollingDeployManualStep = '0'
        processName = 'app_v1_deploy'
        skipDeploy = '0'
        stageName = 'QA'
      }
    }
  }

  release 'Canary Deployment-v2', {
    description = ''
    disableMultipleActiveRuns = '0'
    plannedEndDate = '2021-05-02'
    plannedStartDate = '2021-05-01'

    pipeline 'Canary Deployment', {
      description = ''
      disableMultipleActiveRuns = '0'
      disableRestart = '0'
      enabled = '1'
      overrideWorkspace = '0'
      releaseName = 'Canary Deployment-v2'
      skipStageMode = 'ENABLED'
      templatePipelineName = 'Canary Deployment'
      templatePipelineProjectName = 'DeployStrategies'

      formalParameter 'ec_stagesToRun', {
        expansionDeferred = '1'
        required = '0'
      }

      stage 'Dev', {
        description = ''
        colorCode = '#00adee'
        completionType = 'auto'
        pipelineName = 'Canary Deployment'
        waitForPlannedStartDate = '0'

        gate 'PRE'

        gate 'POST'

        task 'Deployer', {
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          alwaysRun = '0'
          deployerRunType = 'serial'
          enabled = '1'
          errorHandling = 'stopOnError'
          insertRollingDeployManualStep = '0'
          resourceName = ''
          skippable = '0'
          subproject = 'DeployStrategies'
          taskType = 'DEPLOYER'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
        }
      }

      stage 'QA', {
        description = ''
        colorCode = '#ff7f0e'
        completionType = 'auto'
        pipelineName = 'Canary Deployment'
        waitForPlannedStartDate = '0'

        gate 'PRE'

        gate 'POST'

        task 'Deployer', {
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          alwaysRun = '0'
          deployerRunType = 'serial'
          enabled = '1'
          errorHandling = 'stopOnError'
          insertRollingDeployManualStep = '0'
          resourceName = ''
          skippable = '0'
          subproject = 'DeployStrategies'
          taskType = 'DEPLOYER'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
        }
      }

      stage 'Prod', {
        description = ''
        colorCode = '#2ca02c'
        completionType = 'auto'
        pipelineName = 'Canary Deployment'
        waitForPlannedStartDate = '0'

        gate 'PRE'

        gate 'POST'

        task 'Deployer', {
          description = ''
          actualParameter = [
            'ec_enforceDependencies': '1',
            'ec_smartDeployOption': '1',
            'ec_stageArtifacts': '1',
          ]
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          alwaysRun = '0'
          enabled = '1'
          environmentName = '$[/projects/DeployStrategies/targetEnv]'
          environmentProjectName = 'DeployStrategies'
          errorHandling = 'stopOnError'
          insertRollingDeployManualStep = '0'
          resourceName = ''
          rollingDeployEnabled = '0'
          skippable = '0'
          subapplication = 'app-v1-SpringBoot'
          subprocess = 'app_v1_deploy'
          subproject = 'DeployStrategies'
          taskProcessType = 'APPLICATION'
          taskType = 'PROCESS'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
        }

        task 'Smoke Test', {
          description = ''
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          alwaysRun = '0'
          enabled = '1'
          errorHandling = 'continueOnError'
          insertRollingDeployManualStep = '0'
          resourceName = ''
          skippable = '0'
          subprocedure = 'checkDeployment'
          subproject = 'DeployStrategies'
          taskType = 'PROCEDURE'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
        }

        task 'Activate Canary State', {
          description = ''
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          allowSkip = '0'
          alwaysRun = '0'
          disableFailure = '0'
          enabled = '1'
          errorHandling = 'stopOnError'
          insertRollingDeployManualStep = '0'
          instruction = ''
          notificationEnabled = '1'
          notificationTemplate = 'ec_default_pipeline_manual_task_notification_template'
          resourceName = ''
          skippable = '0'
          subproject = 'DeployStrategies'
          taskType = 'MANUAL'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
          approver = [
            'admin',
          ]
        }

        task 'Divert subset of traffic to enable canary testing', {
          description = ''
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          alwaysRun = '0'
          enabled = '1'
          errorHandling = 'stopOnError'
          insertRollingDeployManualStep = '0'
          resourceName = ''
          skippable = '0'
          subprocedure = 'UpdateLoadBalancer'
          subproject = 'DeployStrategies'
          taskType = 'PROCEDURE'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
        }

        task 'Canary Tests ', {
          description = ''
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          alwaysRun = '0'
          enabled = '1'
          errorHandling = 'stopOnError'
          insertRollingDeployManualStep = '0'
          resourceName = ''
          skippable = '0'
          subprocedure = 'CheckResults'
          subproject = 'DeployStrategies'
          taskType = 'PROCEDURE'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
        }

        task 'Validate new release', {
          description = ''
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          allowSkip = '0'
          alwaysRun = '0'
          disableFailure = '0'
          enabled = '1'
          errorHandling = 'stopOnError'
          insertRollingDeployManualStep = '0'
          instruction = ''
          notificationEnabled = '1'
          notificationTemplate = 'ec_default_pipeline_manual_task_notification_template'
          resourceName = ''
          skippable = '0'
          subproject = 'DeployStrategies'
          taskType = 'MANUAL'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
          approver = [
            'admin',
          ]
        }

        task 'Deactivate Canary State', {
          description = ''
          advancedMode = '0'
          allowOutOfOrderRun = '0'
          alwaysRun = '0'
          condition = '$[/javascript myStageRuntime.tasks["Validate new release"].completed]'
          enabled = '1'
          errorHandling = 'stopOnError'
          insertRollingDeployManualStep = '0'
          resourceName = ''
          skippable = '0'
          subprocedure = 'disableCanaryMode'
          subproject = 'DeployStrategies'
          taskType = 'PROCEDURE'
          useApproverAcl = '0'
          waitForPlannedStartDate = '0'
        }
      }

      // Custom properties

      property 'ec_counters', {

        // Custom properties
        pipelineCounter = '6'
      }
    }

    deployerApplication 'app-v1-SpringBoot', {
      enforceDependencies = '1'
      errorHandling = 'stopOnError'
      orderIndex = '1'
      processName = 'app_v1_deploy'
      smartDeploy = '1'
      stageArtifacts = '1'

      deployerConfiguration '5f5defbf-9935-11eb-9872-feaab62b41f3', {
        environmentName = 'Rolling Deploy Production'
        environmentTemplateProjectName = 'null'
        insertRollingDeployManualStep = '0'
        processName = 'app_v1_deploy'
        skipDeploy = '0'
        stageName = 'Prod'
      }

      deployerConfiguration 'dc5ee998-9b62-11eb-a2f8-feaab62b41f3', {
        deployerTaskName = 'Deployer'
        environmentName = 'DEV'
        insertRollingDeployManualStep = '0'
        processName = 'app_v1_deploy'
        skipDeploy = '0'
        stageName = 'Dev'
      }

      deployerConfiguration 'dc6be266-9b62-11eb-9d8c-feaab62b41f3', {
        deployerTaskName = 'Deployer'
        environmentName = 'QA'
        environmentTemplateProjectName = 'null'
        insertRollingDeployManualStep = '0'
        processName = 'app_v1_deploy'
        skipDeploy = '0'
        stageName = 'QA'
      }
    }
  }

  // Custom properties

  property 'deployStrategy', value: 'Canary', {
    description = '''Canary -default
Blue/Green
RollingDeployment'''
    expandable = '1'
    suppressValueTracking = '0'
  }

  property 'targetEnv', value: 'Prod1', {
    description = '''Prod1 default
Prod2
Prod3 '''
    expandable = '1'
    suppressValueTracking = '0'
  }
}