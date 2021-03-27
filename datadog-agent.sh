#!/usr/bin/env bash
value=`cat api-key.txt`
docker run -d --name dd-agent -v /var/run/docker.sock:/var/run/docker.sock:ro -v /proc/:/host/proc/:ro -v /sys/fs/cgroup/:/host/sys/fs/cgroup:ro -e DD_API_KEY=$API_KEY -e DD_SITE="datadoghq.eu" gcr.io/datadoghq/agent:7