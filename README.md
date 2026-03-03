# Jenkins Infisical Plugin

This plugin adds a build wrapper to set environment variables from [Infisical](https://infisical.com), the open-source secrets manager. Secrets are generally masked in the build log, so you can't accidentally print them.

## Infisical Authentication

Authenticating with Infisical is done through the use of [Machine Identities](https://infisical.com/docs/documentation/platform/identities/machine-identities).
Currently the Jenkins plugin supports [Universal Auth](https://infisical.com/docs/documentation/platform/identities/universal-auth)  and [LDAP Auth](https://infisical.com/docs/documentation/platform/identities/ldap-auth) for authentication. More methods will be added soon.

### How does Universal Auth work?
To use Universal Auth, you'll need to create a new Credential _(Infisical Universal Auth Credential)_. The credential should contain your Universal Auth client ID, and your Universal Auth client secret.
Please [read more here](https://infisical.com/docs/documentation/platform/identities/universal-auth) on how to setup a Machine Identity to use universal auth.


### Creating a Universal Auth credential

Creating a universal auth credential inside Jenkins is very straight forward.

Simply navigate to `Dashboard -> Manage Jenkins -> Credentials -> System -> Global credentials (unrestricted)`.

Press the `Add Credentials` button and select `Infisical Universal Auth Credential` in the `Kind` field.

The `ID` and `Description` field doesn't matter much in this case, as they won't be read anywhere. The description field will be displayed as the credential name during the plugin configuration.

![Infisical Universal Auth Credential](docs/images/universal-auth-credential.png)

### How does LDAP Auth work?
To use LDAP Auth, you'll need to create a new Credential _(Infisical LDAP Auth Credential)_.  The credential should contain your IdentityId, LDAP username, and LDAP password.
Please [read more here](https://infisical.com/docs/documentation/platform/identities/ldap-auth) on how to setup a Machine Identity to use LDAP authentication.


### Creating an LDAP credential

Simply navigate to `Dashboard -> Manage Jenkins -> Credentials -> System -> Global credentials (unrestricted)`.

Press the `Add Credentials` button and select `Infisical LDAP Credential` in the `Kind` field.

The `ID` and `Description` field doesn't matter much in this case, as they won't be read anywhere. The description field will be displayed as the credential name during the plugin configuration.

## Plugin Usage
### Configuration

Configuration takes place on a job-level basis.

Inside your job, you simply tick the `Infisical Plugin` checkbox under "Build Environment". After enabling the plugin, you'll see a new section appear where you'll have to configure the plugin.

![Plugin enabled](docs/images/plugin-checked.png)

You'll be prompted with 4 options to fill:
* Infisical URL
    * This defaults to https://app.infisical.com. This field is only relevant if you're running a managed or self-hosted instance. If you are using Infisical Cloud, leave this as-is, otherwise enter the URL of your Infisical instance.
* Infisical Credential
    * This is where you select your Infisical credential to use for authentication. In the step above [Creating a Universal Auth credential](#creating-a-universal-auth-credential), you can read on how to configure the credential. Simply select the credential you have created for this field.
* Infisical Project Slug
    * This is the slug of the project you wish to fetch secrets from. You can find this in your project settings on Infisical by clicking "Copy project slug".
* Environment Slug
    * This is the slug of the environment to fetch secrets from. In most cases it's either `dev`, `staging`, or `prod`. You can however create custom environments in Infisical. If you are using custom environments, you need to enter the slug of the custom environment you wish to fetch secrets from.
  
That's it! Now you're ready to select which secrets you want to fetch into Jenkins.
By clicking the `Add an Infisical secret` in the Jenkins UI like seen in the screenshot below.

![Add Infisical secret](/docs/images/add-infisical-secret.png)

You need to select which secrets that should be pulled into Jenkins.
You start by specifying a [folder path from Infisical](https://infisical.com/docs/documentation/platform/folder#comparing-folders). The root path is simply `/`. You also need to select wether or not you want to [include imports](https://infisical.com/docs/documentation/platform/secret-reference#secret-imports). Now you can add secrets the secret keys that you want to pull from Infisical into Jenkins. If you want to add multiple secrets, press the "Add key/value pair".

If you wish to pull secrets from multiple paths, you can press the "Add an Infisical secret" button at the bottom, and configure a new set of secrets to pull.


## Pipeline usage


### Generating pipeline block

Using the Infisical Plugin in a Jenkins pipeline is very straight forward. To generate a block to use the Infisical Plugin in a Pipeline, simply to go `{JENKINS_URL}/jenkins/job/{JOB_ID}/pipeline-syntax/`.

You can find a direct link on the Pipeline configuration page in the very bottom of the page, see image below.

![Pipeline Syntax Highlight](docs/images/pipeline-syntax-highlight.png)

On the Snippet Generator page, simply configure the Infisical Plugin like it's documented in the [Configuration documentation](#configuration) step.

Once you have filled out the configuration, press `Generate Pipeline Script`, and it will generate a block you can use in your pipeline.

![Pipeline Configuration](docs/images/pipeline-configuration.png)

### Using Infisical in a Pipeline

Using the generated block in a pipeline is very straight forward. There's a few approaches on how to implement the block in a Pipeline script.
Here's an example of using the generated block in a pipeline script. Make sure to replace the placeholder values with your own values.

The script is formatted for clarity. All these fields will be pre-filled for you if you use the `Snippet Generator` like described in the [step above](#generating-pipeline-block).
```groovy
node {
    withInfisical(
        configuration: [
            infisicalCredentialId: 'YOUR_CREDENTIAL_ID',
            infisicalEnvironmentSlug: 'PROJECT_ENV_SLUG', 
            infisicalProjectSlug: 'PROJECT_SLUG', 
            infisicalUrl: 'https://app.infisical.com' // Change this to your Infisical instance URL if you aren't using Infisical Cloud.
        ], 
        infisicalSecrets: [
            infisicalSecret(
                includeImports: true, 
                path: '/', 
                secretValues: [
                    [infisicalKey: 'DATABASE_URL'],
                    [infisicalKey: "API_URL"],
                    [infisicalKey: 'THIS_KEY_MIGHT_NOT_EXIST', isRequired: false],
                ]
            )
        ]
    ) {
        // Code runs here
        sh "printenv"
    }     
}
```

## Configuration as Code

The Jenkins Configuration as Code plugin ([JCasC](https://github.com/casz/configuration-as-code-plugin)) allows you to configure Jenkins via a yaml file.

Using this plugin, you can reference Infisical secrets in your JCasC yaml files.

### Prerequisite: 
Install 'Configuration as Code' Plugin on your Jenkins instance.

Refer to [Installing a new plugin in Jenkins](https://jenkins.io/doc/book/managing/plugins/#installing-a-plugin).

### Infisical Vault Plugin as a Secret Source for JCasC

The JCasC plugin allows you to configure Infisical Jenkins credentials in the yaml configuration file and also allows you to reference secrets throughout the yaml file via string interpolation.

### JCasC config for Jenkins Credentials

JCasC can be used to create the Universal Auth and LDAP Authentication credentials which are used to look up secrets from Infisical.

The secrets can be defined in the yaml file like so:

```yaml
credentials:
  system:
    domainCredentials:
      - credentials:
          - infisicalLdapCredential:
              description: "Infisical LDAP Credential"
              id: "infisical-ldap-credential"
              identityId: "jenkins_identity_id"
              password: "secret123!"
              username: "infisical_username"
              scope: SYSTEM
          - infisicalUniversalAuthCredential:
              description: "Infisical Universal Auth Credential"
              id: "infisical-universal-auth-credential"
              machineIdentityClientId: "machine_identity_client_id"
              machineIdentityClientSecret: "machine_identity_client_secret"
              scope: SYSTEM
```

### JCasC Infisical string interpolation in yaml file

Note that the above example contains secret values stored in clear text.  This can be avoided by using string interpolation to replace the secret values from Infisical at load time.

JCasC will need to access Infisical in order to perform the lookups for string interpolation.
The connection information is provided via the following environment variables which must be present during Jenkins startup.

General Variables:
* CASC_INFISICAL_FILE: (Optional) Location of file which contains the environment variables (alternate way of providing variable values)
* CASC_INFISICAL_URL: (Optional) URL for connecting to Infisical.  Defaults to https://app.infisical.com
* CASC_INFISICAL_INCLUDE_IMPORTS: (Optional) Should imported secrets be processed? (defaults to FALSE)
* CASC_INFISICAL_ENVIRONMENT_SLUG: Environment slug to pull secrets for.
* CASC_INFISICAL_PROJECT_SLUG:  Project slug containing secrets.
* CASC_INFISICAL_RECURSIVE:  (Optional) Should secrets from subfolders of CASC_INFISICAL_PATHS be retrieved? (defaults to FALSE)
* CASC_INFISICAL_PATHS:  Comma delimited ist of paths to search for secrets in.  Only secrets in these folders will be retrieved for lookup.

Universal Authentication Variables:
* CASC_INFISICAL_MACHINE_IDENTITY_CLIENT_ID: Machine Identity Client Id used to authenticated with Infisical
* CASC_INFISICAL_MACHINE_IDENTITY_CLIENT_SECRET: Machine Identity Client Secret used

LDAP Authentication Variables:
* CASC_INFISICAL_LDAP_IDENTITY_ID: Identity ID for the machine id
* CASC_INFISICAL_LDAP_USER: LDAP username associated with the machine id
* CASC_INFISICAL_LDAP_PW: Password for the LDAP user

Optional CASC_INFISICAL_FILE can be used to pass secrets via file instead of environment variables.  The file should be formatted like so:

```properties
CASC_INFISICAL_URL=https://app.infisical.com
CASC_INFISICAL_ENVIRONMENT_SLUG=prod
CASC_INFISICAL_PROJECT_SLUG=myproject
CASC_INFISICAL_PATHS=/secrets/jenkins,/secrets/otherpath
CASC_INFISICAL_LDAP_IDENTITY_ID=70ee8db9-8e15-4022-92c1-0d46232ebf82
CASC_INFISICAL_LDAP_USER=username
CASC_INFISICAL_LDAP_PW=secret123!
```

### Referencing Infisical secrets in JCasC yaml

To use string interpolation to replace secrets in the JCasC yaml file reference them with ${} notation.

In this example the secret values will be pulled from Infisical as long as the environment variables are configured:
```yaml
credentials:
  system:
    domainCredentials:
      - credentials:
          - usernamePassword:
              description: "Database Credentials"
              id: "database-credential"
              password: "${DATABASE_PASSWORD}"
              username: "${DATABASE_USERNAME}"
              scope: GLOBAL 
```

In the above example the "password" value would be pulled from Infisical in secrets named DATABASE_PASSWORD and DATABASE_USERNAME.