.DEFAULT_GOAL := help
.PHONY: help
help:
	@awk 'BEGIN {FS = ":.*##"} /^([a-zA-Z0-9_-]|\/)+:.*?##/ { printf "  \033[34m%-15s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ IoT Simulator

iot-simulator/start: ## Start IoT simulator
	python3 examples/iot-simulator/iot_simulator.py
