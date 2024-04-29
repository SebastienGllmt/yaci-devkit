#!/bin/bash
CMD="docker-compose"
if ! command -v docker-compose &> /dev/null
then
    echo "docker-compose not found, let's try 'docker compose'"
    CMD="docker compose"
fi

$CMD --env-file env --env-file version up -d

exit_status=$?

if [ $exit_status -eq 0 ]; then
    echo "Docker Compose started successfully."
    source ./info.sh
else
    echo "Failed to start Docker Compose services."
fi

exit $exit_status
