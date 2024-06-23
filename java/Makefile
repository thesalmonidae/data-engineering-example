.DEFAULT_GOAL := help
.PHONY: help
help:
	@awk 'BEGIN {FS = ":.*##"} /^([a-zA-Z0-9_-]|\/)+:.*?##/ { printf "  \033[34m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Flink

flink/start: ## Start Flink
	/flink-1.19.0/bin/start-cluster.sh

flink/stop: ## Stop Flink
	/flink-1.19.0/bin/stop-cluster.sh

##@ iot2kafka

iot2kafka/start: ## Start iot2Kafka
	cd examples/iot2kafka && \
	mvn camel:run

##@ kafka2postgres
kafka2postgres/start: ## Start kafka2postgres
	cd examples/kafka2postgres && \
	flink run target/kafka2postgres-1.0.0-SNAPSHOT.jar

##@ postgres2app
postgres2app/start: ## Start postgres2app
	cd examples/postgres2app && \
	mvn camel:run