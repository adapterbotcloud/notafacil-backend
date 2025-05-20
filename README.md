# NotaFacil

NotaFacil é uma aplicação Spring Boot para emissão de Notas Fiscais de Serviço Eletrônicas (NFS-e) para a cidade de Fortaleza, Ceará, Brasil.

## Descrição

Esta aplicação fornece uma API REST que se comunica com o serviço SOAP da Prefeitura de Fortaleza para emissão de NFS-e. Ela permite o envio de lotes de RPS (Recibo Provisório de Serviço) para conversão em NFS-e, incluindo a assinatura digital dos documentos XML conforme exigido pela legislação.

## Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.4.5**
- **Spring Web** (API REST)
- **Spring Web Services** (Cliente SOAP)
- **XML Digital Signatures** (Assinatura digital de XML)
- **JAXB/JAXWS** (Processamento de XML e geração de classes a partir de XSD/WSDL)
- **MapStruct** (Mapeamento de objetos)
- **SpringDoc OpenAPI** (Documentação da API)
- **Docker** (Containerização)
- **Maven** (Gerenciamento de dependências e build)

## Estrutura do Projeto

```
notafacil/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── notafacil/
│   │   │           ├── config/         # Configurações do Spring
│   │   │           ├── controller/     # Controladores REST
│   │   │           ├── dto/            # Objetos de transferência de dados
│   │   │           ├── mapping/        # Mapeadores MapStruct
│   │   │           ├── service/        # Serviços de negócio
│   │   │           ├── wswrapper/      # Wrappers para serviços SOAP
│   │   │           └── NotaFacilApplication.java  # Classe principal
│   │   └── resources/
│   │       ├── certs/                  # Certificados digitais
│   │       ├── wsdl/                   # Arquivos WSDL
│   │       ├── xsd/                    # Esquemas XSD
│   │       └── application.yml         # Configuração da aplicação
└── pom.xml                             # Configuração do Maven
```

## Configuração

A aplicação é configurada através do arquivo `application.yml`. As principais configurações são:

```yaml
nfse:
  service:
    url: ${NFSE_SERVICE_URL:http://isshomo.sefin.fortaleza.ce.gov.br/grpfor-iss/ServiceGinfesImplService}

xmlsign:
  keystore:
    path: certs/keystore.p12
    password: senha123
    alias: testesign
    keyPassword: senha123
```

### Certificado Digital

Para a assinatura digital dos XMLs, é necessário configurar um certificado digital válido:

1. Coloque seu arquivo PKCS#12 (.p12 ou .pfx) no diretório `src/main/resources/certs/`
2. Configure o caminho, senha e alias no arquivo `application.yml`

## Instalação e Execução

### Pré-requisitos

- Java 21
- Maven 3.8+
- Docker (opcional)

### Compilação

```bash
mvn clean package
```

### Execução Local

```bash
java -jar target/notafacil-1.0.0.jar
```

### Execução com Docker

```bash
# Construir a imagem
docker build -t notafacil .

# Executar o container
docker run -p 8080:8080 notafacil
```

## Uso da API

A API expõe um endpoint REST para envio de lotes de RPS:

### Enviar Lote de RPS

```
POST /api/v1/nfse/recepcionar-lote-rps
```

Exemplo de payload:

```json
{
  "loteRps": {
    "id": 8171,
    "numeroLote": "1234",
    "cnpj": "01234567891234",
    "inscricaoMunicipal": "123456",
    "quantidadeRps": 5,
    "listaRps": [
      {
        "infRps": {
          "identificacaoRps": { "numero": "2", "serie": "A", "tipo": 1 },
          "dataEmissao": "2025-05-01T08:15:00",
          "naturezaOperacao": 1,
          "regimeEspecialTributacao": 1,
          "optanteSimplesNacional": 2,
          "incentivadorCultural": 2,
          "status": 1,
          "servico": {
            "valores": {
              "valorServicos": 500.00,
              "valorDeducoes": 0.00,
              "valorPis": 0.00,
              "valorCofins": 0.00,
              "valorInss": 0.00,
              "valorIr": 0.00,
              "valorCsll": 0.00,
              "issRetido": 2,
              "valorIss": 25.00,
              "valorIssRetido": 0.00,
              "outrasRetencoes": 0.00,
              "baseCalculo": 500.00,
              "aliquota": 0.05,
              "valorLiquidoNfse": 500.00,
              "descontoIncondicionado": 0.00,
              "descontoCondicionado": 0.00
            },
            "itemListaServico": "1.02",
            "codigoTributacaoMunicipio": "010101010",
            "discriminacao": "Serviço de consultoria",
            "codigoMunicipio": "2304400"
          },
          "prestador": {
            "cnpj": "01234567891234",
            "inscricaoMunicipal": "123456"
          },
          "tomador": {
            "identificacaoTomador": {
              "cnpj": "98765432101234",
              "inscricaoMunicipal": "654321"
            },
            "razaoSocial": "Cliente Exemplo 1",
            "endereco": {
              "endereco": "Av. Exemplo 1",
              "numero": "100",
              "bairro": "Centro",
              "codigoMunicipio": "2304400",
              "uf": "CE",
              "cep": "60000001"
            },
            "contato": {
              "telefone": "85900000001",
              "email": "cliente1@exemplo.com"
            }
          }
        }
      }
    ]
  }
}
```

## Documentação da API

A documentação completa da API está disponível através do Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

## Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo LICENSE para detalhes.
# notaFacil
