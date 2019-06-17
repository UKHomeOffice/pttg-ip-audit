#!/bin/bash

# Get the current branch name
branchName=$(git rev-parse --abbrev-ref HEAD)

# Line 18 replacement
#line_18_1="      branch: \[master\]"
#line_18_2="      branch: [master, ${branchName}]"
#
#sed -i "18s~${line_18_1}~${line_18_2}~" ./.drone.yml

# Line 28 replacement
line_28_1="      branch: \[master, refs/tags/\*\]"
line_28_2="      branch: [master, refs/tags/*, ${branchName}]"

sed -i "28s~${line_28_1}~${line_28_2}~" ./.drone.yml


# Line 42 replacement
line_42_1="      branch: \[master\]"
line_42_2="      branch: [master, ${branchName}]"

sed -i "42s~${line_42_1}~${line_42_2}~" ./.drone.yml

# Line 78 replacement
line_78_1="      branch: \[master\]"
line_78_2="      branch: [master, ${branchName}]"

sed -i "78s~${line_78_1}~${line_78_2}~" ./.drone.yml
