# Rocket Chat Exporter CLI

A simple Java CLI tool to export the messages from a [Rocket Chat](https://rocket.chat/) server. It currently supports _groups_, _channels_ and _direct message_ export.
 As export format this tool only supports the **[Slack CSV](https://slack.com/intl/en-au/help/articles/201748703#) output format** as of now, but adding new output format is quite easy (check out the `ExportFormat` interface).

This tool uses the [RocketChat API](https://rocket.chat/docs/developer-guides/rest-api/groups/) and is useful for situations where the user does not have administrative access to the server.

[![GitHub release](https://img.shields.io/github/release/patrickfav/rocketchat-exporter.svg)](https://github.com/patrickfav/rocketchat-exporter/releases/latest) [![Build Status](https://travis-ci.org/patrickfav/rocketchat-exporter.svg?branch=master)](https://travis-ci.org/patrickfav/rocketchat-exporter) [![Coverage Status](https://coveralls.io/repos/github/patrickfav/rocketchat-exporter/badge.svg?branch=master)](https://coveralls.io/github/patrickfav/rocketchat-exporter?branch=master)


## Quickstart

Provide the tool with your host URL, output file and user name.

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
	(1) channel a
	(2) channel b
	(3) channel c
Select option (1-3):
3
Successfully exported 122 channel messages to 'out\channel_channel-c_20190927013945.csv'
```

### Manpage

    Usage: export [-hV] [--debug] [-o=<file>] -t=<host> -u=<username>
    Exports rocket chat messages from a specific group/channel.
          --debug             Add debug log output to STDOUT.
      -h, --help              Show this help message and exit.
      -o, --outFile=<file>    The file or directory to write the export data to.
                                Will write to current directory with auto generated
                                filename if this arg is omitted.
      -t, --host=<host>       The rocket chat server. E.g. 'https://myserver.com'
      -u, --user=<username>   RocketChat username for authentication.
      -V, --version           Print version information and exit.

### Requirements

* [Java Runtime Environment (JRE) 11](https://adoptopenjdk.net/)

## Download

**[Grab jar from latest Release](https://github.com/patrickfav/rocketchat-exporter/releases/latest)**

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
