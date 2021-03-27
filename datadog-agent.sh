#!/usr/bin/env bash
API_KEY=`cat api-key.txt`
docker run -d --name dd-agent -v /var/run/docker.sock:/var/run/docker.sock:ro -v /proc/:/host/proc/:ro -v /sys/fs/cgroup/:/host/sys/fs/cgroup:ro -e DD_LOG_LEVEL=trace -e DD_DOGSTATSD_NON_LOCAL_TRAFFIC=true -e DD_API_KEY=$API_KEY -e DD_SITE="datadoghq.eu"  -p 8125:8125/udp -p 8126:8126 gcr.io/datadoghq/agent:7