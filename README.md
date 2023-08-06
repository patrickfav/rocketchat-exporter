# Rocket Chat Exporter CLI

A simple Java CLI tool to export the messages from a [Rocket Chat](https://rocket.chat/) server. It currently supports _groups_, _channels_ and _direct message_ export. You can either choose a single conversation or export all in a batch. As export format this tool only supports the **[Slack CSV](https://slack.com/intl/en-au/help/articles/201748703#) output format** as of now, but adding new output format is quite easy (check out the `ExportFormat` interface).

This tool uses the [RocketChat API](https://developer.rocket.chat/reference/api/rest-api/endpoints/core-endpoints/groups-endpoints) and is useful for situations where the user does not have administrative access to the server.

[![GitHub release](https://img.shields.io/github/release/patrickfav/rocketchat-exporter.svg)](https://github.com/patrickfav/rocketchat-exporter/releases/latest)
[![Github Actions](https://github.com/patrickfav/rocketchat-exporter/actions/workflows/build.yml/badge.svg)](https://github.com/patrickfav/rocketchat-exporter/actions)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=patrickfav_rocketchat-exporter&metric=coverage)](https://sonarcloud.io/summary/new_code?id=patrickfav_rocketchat-exporter)

## Quickstart

Provide the tool with your host URL, output file and username.

    java -jar .\rocketchat-exporter.jar --host "http://my-rocket-chat.com" -o "./out" -u "fname.lastname@mail.com"

After that the password will be prompted. The tool will print all available channels. 
Choose one and the resulting export will be written to the provided file. Example output:

```
Please enter your RocketChat password: ************
Authentication successful (fname.lastname).

What type do you want to export:
	(1) group
	(2) channel
	(3) direct message
Select option (1-3):
2

Please choose the channel you want to export:
	(1) [ALL]
	(2) channel a
	(3) channel b
	(4) channel c
Select option (1-4):
3
Successfully exported 122 channel messages to 'out\channel_channel-b_20190927013945.csv'
```

### Manpage

    Usage: export [-hV] [--debug] [-m=<maxMessages>] [-o=<file>] -t=<host>
                  -u=<username>
    Exports rocket chat messages from a specific group/channel.
          --debug             Add debug log output to STDOUT.
      -h, --help              Show this help message and exit.
      -m, --maxMsg=<maxMessages>
                              How many messages should be exported.
      -o, --outFile=<file>    The file or directory to write the export data to.
                                Will write to current directory with auto generated
                                filename if this arg is omitted. If you want to
                                export multiple conversations you must pass a
                                directory not a file.
      -t, --host=<host>       The rocket chat server. E.g. 'https://myserver.com'
      -u, --user=<username>   RocketChat username for authentication.
      -V, --version           Print version information and exit.

### Requirements

* [Java Runtime Environment (JRE) 11](https://adoptopenjdk.net/)

## Download

**[Grab jar from the latest Release](https://github.com/patrickfav/rocketchat-exporter/releases/latest)**

## Development

### Build with Maven

Use the Maven wrapper to create a jar including all dependencies

    mvnw clean install

### Checkstyle Config File

This project uses my [`common-parent`](https://github.com/patrickfav/mvn-common-parent) which centralized a lot of
the plugin versions as well as providing the checkstyle config rules. Specifically they are maintained
in [`checkstyle-config`](https://github.com/patrickfav/checkstyle-config). Locally the files will be copied after
you `mvnw install` into your `target` folder and is called
`target/checkstyle-checker.xml`. So if you use a plugin for your IDE, use this file as your local configuration.

# License

Copyright 2019 Patrick Favre-Bulle

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
