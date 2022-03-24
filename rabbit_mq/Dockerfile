FROM rabbitmq:3.8-management-alpine

COPY --chown=rabbitmq rabbitmq.conf /etc/rabbitmq/
COPY --chown=rabbitmq definitions.json /etc/rabbitmq/

COPY --chown=rabbitmq enabled_plugins /etc/rabbitmq/

ENV RABBITMQ_ERLANG_COOKIE="messages_erlang_cluster_cookie"

