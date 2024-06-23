.DEFAULT_GOAL := help
.PHONY: help
help:
	@awk 'BEGIN {FS = ":.*##"} /^([a-zA-Z0-9_-]|\/)+:.*?##/ { printf "  \033[34m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Docker

docker/network/create: ## Create Docker network
	docker network create data-toolbox

docker/compose/up: ## Start Docker compose
	docker compose -f compose.yaml up -d

docker/compose/down: ## Stop Docker compose
	docker compose -f compose.yaml down

docker/compose/kafka/up: ## Start Kafka service in Docker compose
	docker compose -f compose.yaml up -d kafka

docker/compose/kafka/down: ## Stop Kafka service in Docker compose
	docker compose -f compose.yaml down kafka

docker/compose/kafka-exporter/up: ## Start Kafka Exporter service in Docker compose
	docker compose -f compose.yaml up -d kafka-exporter

docker/compose/kafka-exporter/down: ## Stop Kafka Exporter service in Docker compose
	docker compose -f compose.yaml down kafka-exporter

docker/compose/postgres/up: ## Start Postgres service in Docker compose
	docker compose -f compose.yaml up -d postgres

docker/compose/postgres/down: ## Stop Postgres service in Docker compose
	docker compose -f compose.yaml down postgres

docker/compose/prometheus/up: ## Start Prometheus service in Docker compose
	docker compose -f compose.yaml up -d prometheus

docker/compose/prometheus/down: ## Stop Prometheus service in Docker compose
	docker compose -f compose.yaml down prometheus

docker/compose/grafana/up: ## Start Grafana service in Docker compose
	docker compose -f compose.yaml up -d grafana

docker/compose/grafana/down: ## Stop Grafana service in Docker compose
	docker compose -f compose.yaml down grafana

docker/compose/superset/up: ## Start Superset service in Docker compose
	docker compose -f compose.yaml up -d superset

docker/compose/superset/down: ## Stop Superset service in Docker compose
	docker compose -f compose.yaml down superset

##@ Resources

resources/create: ## Create resources
	python3 resources/setup.py create

resources/delete: ## Delete resources
	python3 resources/setup.py delete
