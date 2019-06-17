#!/bin/bash

# Get the current branch name
branchName=$(git rev-parse --abbrev-ref HEAD)

# Line 18 replacement
#line_18="      branch: \[master\]"
#
#sed -i "18s~.*~${line_18}~" ./.drone.yml

# Line 28 replacement
line_28="      branch: \[master, refs/tags/\*\]"

sed -i "28s~.*~${line_28}~" ./.drone.yml

# Line 42 replacement
line_42="      branch: [master]"

sed -i "42s~.*~${line_42}~" ./.drone.yml

# Line 78 replacement
line_78="      branch: [master]"

sed -i "78s~.*~${line_78}~" ./.drone.yml
