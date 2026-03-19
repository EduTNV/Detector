🛡️ Sistema de Detecção de Fraude em Cartões de Crédito
Este projeto é um sistema completo de monitoramento e validação de transações financeiras em tempo real. Desenvolvido como parte de uma UC Dual, a solução foi apresentada e validada por especialistas do time de Tecnologia da Informação do Bradesco, simulando um ambiente real de segurança bancária.

📄 Artigo e Documentação Oficial
Para entender a fundamentação teórica, os diagramas de arquitetura e os resultados detalhados, acesse o artigo completo do projeto:
👉 Leia o Artigo Oficial do Projeto (PDF)

🔍 Como funciona a Detecção? (O Diferencial Técnico)
Diferente de sistemas simples de cadastro, este motor de busca utiliza três camadas de validação lógica para classificar uma transação como COMPLETED ou PENDING_CONFIRMATION:

Análise de Desvio de Gasto: O sistema calcula a média de gastos do usuário. Se uma nova compra for superior a 3x (Fator de Desvio) essa média, a transação é marcada para confirmação manual.

Inteligência Geográfica (Spatial Data): Utilizando a biblioteca JTS (GeometryFactory), o sistema calcula a distância em KM entre a localização da transação e a posição atual do usuário. Compras realizadas a mais de 25km de distância geram um alerta de fraude.

Janela de Horário Habitual: O motor verifica se a transação ocorre dentro do intervalo de tempo (início/fim) definido no perfil de comportamento do usuário. Fora desse horário, o sistema bloqueia a operação preventivamente.

🚀 Tecnologias Utilizadas
Backend: Java 21 e Spring Boot 3+.

Segurança: Spring Security com autenticação JWT (JSON Web Token) e criptografia BCrypt.

Banco de Dados: PostgreSQL (com suporte a dados geográficos).

Frontend: React.js para o Dashboard do Usuário e Simulador de Compras.

Geolocalização: JTS (Java Topology Suite) para cálculos espaciais precisos.

🧱 Estrutura do Backend
A arquitetura foi desenhada para ser modular e escalável:

controller/: Endpoints REST para usuários, cartões e transações.

service/: Onde reside a inteligência do "Motor de Fraude".

dto/: Objetos de transferência de dados para garantir a segurança das entidades.

exception/: Tratamento customizado de erros, como a FraudDetectedException.

🔐 Funcionalidades Principais
Fluxo de Aprovação em Duas Etapas: Transações suspeitas não são negadas de imediato; elas ficam pendentes no Dashboard para que o usuário possa Confirmar ou Negar via interface.

Aprendizado de Padrão: A cada transação legítima concluída, o sistema atualiza a média de gasto e os padrões de comportamento do usuário.

Dashboard em Tempo Real: Visualização de histórico e status das transações.

👨‍💻 Autores
Projeto desenvolvido em colaboração por:

Eduardo Travassos (EduTNV)

Alexandre do Valle

Vinicius Airam

🇺🇸 Project Overview (English)
This is a full-stack Fraud Detection System validated by IT professionals from Bradesco. It uses a Java/Spring Boot engine to analyze credit card transactions in real-time based on three criteria: Spending Deviation (3x limit), Geographical Distance (25km limit using Spatial Data), and Habitual Time Windows. Suspicious activities trigger a PENDING state, requiring manual user confirmation through a React.js dashboard.
