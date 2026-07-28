# Feedback Flow

Sistema serverless na AWS para coleta e classificação automática de feedbacks de clientes.

## O que faz

Recebe feedbacks via API REST com descrição e nota de 1 a 10, classifica a urgência (positiva, neutra ou crítica), armazena no DynamoDB e dispara e-mail de alerta para feedbacks críticos. Toda sexta às 10h UTC, envia um relatório semanal automático.

## Stack

Java 21 • AWS SDK v2 • Jackson • SLF4J/Logback • Maven • Serverless Framework


## Como executar
```bash
# Pré-requisitos: Java 21, Maven, AWS CLI, Serverless Framework

# Configurar credenciais AWS
aws configure

# Compilar
mvn clean package

# Deploy
serverless deploy

# Testar
curl -X POST https://eh2pxbyvi5.execute-api.us-east-1.amazonaws.com/dev/feedback \
  -H "Content-Type: application/json" \
  -d '{"description":"Great service!","rating":9}'

# Remover infraestrutura
serverless remove
