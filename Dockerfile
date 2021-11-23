FROM 113355358853.dkr.ecr.eu-west-1.amazonaws.com/pla/tools/amazoncorretto-fbis:2.0.1

ENV http_proxy 'http://proxy.local:8080'
ENV https_proxy 'http://proxy.local:8080'
ENV no_proxy 'localhost, 127.0.0.1, 169.254.169.254, .iptho.co.uk, .ipttools.io, .ipttools.info, .svc.cluster.local, 10.200.0.1, 10.200.0.10'

ENV USER user_pttg_ip_audit
ENV USER_ID 1001
ENV GROUP group_pttg_ip_audit
ENV NAME pttg-ip-audit

ARG VERSION
RUN mkdir /app

RUN addgroup ${GROUP} && \
    adduser -D ${USER} -g ${GROUP} -u ${USER_ID} && \
    chown -R ${USER}:${GROUP} /app

COPY src/main/resources/rds-combined-ca-bundle.pem /app

COPY run.sh /app
RUN chmod a+x /app/run.sh
COPY ./build/libs/${NAME}-${VERSION}.jar /app/

USER ${USER_ID}
ENTRYPOINT /app/run.sh
