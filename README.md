# Prerequisites
```
java
maven
docker
```
# Installation
Follow these steps to install and run the project:
1. Initialize Docker:
Build and run Docker container for backend **infrastructure**
```shell
docker compose -p PET --env-file ./docker/local.env -f ./docker/docker-compose.local.yml up --build -d
```
If Docker container is already installed, you can use the following command to start the containers instead:
```shell
docker compose -p PET --env-file ./docker/local.env -f ./docker/docker-compose.local.yml start
```
2. Modify initialize sql file:
- This file will auto run [./docker/scripts/init.sql](./docker/scripts/init.sql) for first run of database container
- If you modify it, and want to apply new change please follow this step:
  - modify init.sql
  - stop database container
  - delete volumes folder [./docker/volumes](./docker/volumes)
  - start database container again
3. ... (update in future)