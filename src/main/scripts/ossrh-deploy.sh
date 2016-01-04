#!/usr/bin/env bash
###
# Open Gateway - Core Components.
# Copyright 2015-2016 GRyCAP (Universitat Politecnica de Valencia)
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 
# This product combines work with different licenses. See the "NOTICE" text
# file for details on the various modules and licenses.
# 
# The "NOTICE" text file is part of the distribution. Any derivative works
# that you distribute must include a readable copy of the "NOTICE" text file.
###

# Author: Erik Torres <etserrano[AT]gmail.com>
# Url:    https://github.com/etorres
# Date:   01/04/2016

###
# Deploy maven artefact in current directory into OSSRH using the Nexus 
# Staging Maven Plugin. See additional instructions at:
#
# http://central.sonatype.org/pages/apache-maven.html
# http://central.sonatype.org/pages/ossrh-guide.html
#
# Note that if autoReleaseAfterClose value is set to true in your POM file, 
# then the project will be also released to the Maven Central Repository.
#
# Recommended use: 1) change to the root directory of your project; and 
# 2) execute this script.
###

read -p "Confirm deployment of current Maven project to OSSRH (yes/no)? "

if [ "$REPLY" == "yes" ] ; then
  ssh-add ~/.ssh/id_rsa
  ssh-add -l
  mvn -Dgrycap.deploy.release=true clean deploy | tee ossrh-deploy.log
  ssh-add -D
else
  echo "Exit without deploy"
fi
