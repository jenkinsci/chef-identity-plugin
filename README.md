# Chef Identity Jenkins Plugin

Allows you to use the Jenkins admin to define a user.pem key and contents of a knife.rb file and save it as an Identity for communicating with a Chef Server.  Then in any build job, exposes a checkbox to use a Chef Identity.  If checked, select an Identity and a `.chef` folder will be created in the workspace with the user.pem key and knife.rb files, allowing knife (or possibly other) commands to execute against a Chef server and have the auth credentials needed to execute those commands.

This DOES NOT install a Chef client on your Jenkins server, that needs to be done separately.

## REQUIREMENTS:
The minimum Jenkins version we're building for is LTS 1.554.3.
Get your bearings with <https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial>

## Install plugin

**Step 1**: Build .hpi file

```
mvn clean -DskipTest package
```

**Step 2**: In jenkins server

- Go to **Manage Jenkins** -> **Manage Plugins** -> **Advanced** -> **Upload Plugin**

- Select file chef-identity.hpi and upload to jenkins server

## Chef Identity Setting

Go to **Manage Jenkins** -> **Configure System** -> **Chef Identity Management**

- Identity Name

- user.pem key: Key pair download from AWS

- knife.rb file: Default setting below

```
log_level                :info
log_location             STDOUT
cookbook_path            [File.expand_path('../../cookbooks' , __FILE__), File.expand_path('../../site-cookbooks' , __FILE__)]
local_mode               true
knife[:use_sudo] = true
```

- Path cookbook: Path of cookbook git repository

- Run list

- Remote host

- Remote account: In AWS, Ubuntu OS default account is ubuntu, Amazone OS default is ec2-user.

## TO DO
* Officially track these on Waffle.io once in Jenkins repo? Or Jenkin's Jira?
* Localize
* Setup FormValidation to remind people key and knife fields aren't optional
* Ensure the `client_key` has a value of `'./user.pem'` in the knife.rb file
* -OR- overwrite it for the user to ensure the expected value is there?
* Button to validate key + knife file on Config screen?  Will require external libraries/tools
* Use FileCallable for File actions?

# LICENSE and AUTHOR:

Author:: Tyler Fitch <tfitch@getchef.com>

The MIT License (MIT)

Copyright (c) 2014 Tyler Fitch

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.