ARG     ECLIPSE_TEMURIN_TAG="17-ubi9-minimal"
FROM    eclipse-temurin:${ECLIPSE_TEMURIN_TAG}

ARG     APACHE_MAVEN_MAJOR_VERSION="3"
ARG     APACHE_MAVEN_VERSION="${APACHE_MAVEN_MAJOR_VERSION}.9.7"
ARG     APACHE_FLINK_VERSION="1.19.0"
RUN     microdnf update && \
        microdnf install -y \
                dnf && \
        dnf update && \
        dnf install -y \
                findutils \
                make && \
        curl -LO https://dlcdn.apache.org/maven/maven-${APACHE_MAVEN_MAJOR_VERSION}/${APACHE_MAVEN_VERSION}/binaries/apache-maven-${APACHE_MAVEN_VERSION}-bin.tar.gz && \
        tar -zxf apache-maven-${APACHE_MAVEN_VERSION}-bin.tar.gz && \
        rm apache-maven-${APACHE_MAVEN_VERSION}-bin.tar.gz && \
        curl -LO https://dlcdn.apache.org/flink/flink-${APACHE_FLINK_VERSION}/flink-${APACHE_FLINK_VERSION}-bin-scala_2.12.tgz && \
        tar -zxf flink-${APACHE_FLINK_VERSION}-bin-scala_2.12.tgz && \
        rm flink-${APACHE_FLINK_VERSION}-bin-scala_2.12.tgz && \
        echo "export PATH=\"/apache-maven-${APACHE_MAVEN_VERSION}/bin:/flink-${APACHE_FLINK_VERSION}/bin:$PATH\"" >> /root/.bashrc
