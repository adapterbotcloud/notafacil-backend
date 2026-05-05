-- =============================================================================
-- SEED DATA: Tabelas de Referência IBS/CBS (Reforma Tributária LC 214/2025)
-- =============================================================================
-- Executar manualmente ou configurar spring.sql.init.mode=always

-- =============================================================================
-- CST IBS/CBS - Código de Situação Tributária
-- =============================================================================
INSERT INTO cst_ibs_cbs (codigo, descricao, grupo, habilitado) VALUES
('101', 'Tributação integral', 'TRIBUTADO', true),
('102', 'Redução de alíquota', 'TRIBUTADO', true),
('103', 'Redução de base de cálculo', 'TRIBUTADO', true),
('104', 'Tributação com diferimento total', 'TRIBUTADO', true),
('105', 'Tributação com diferimento parcial', 'TRIBUTADO', true),
('200', 'Alíquota reduzida', 'TRIBUTADO', true),
('201', 'Imunidade', 'IMUNE', true),
('202', 'Imunidade com crédito presumido', 'IMUNE', true),
('301', 'Isenção', 'ISENTO', true),
('302', 'Isenção com crédito presumido', 'ISENTO', true),
('401', 'Não incidência', 'NAO_INCIDENCIA', true),
('501', 'Suspensão', 'SUSPENSAO', true),
('900', 'Outros', 'OUTROS', true)
ON CONFLICT DO NOTHING;

-- =============================================================================
-- INDICADOR DE OPERAÇÃO (6 dígitos)
-- =============================================================================
INSERT INTO indicador_operacao (codigo, descricao, habilitado) VALUES
('010101', 'Prestação de serviço - operação normal - dentro do município', true),
('010102', 'Prestação de serviço - operação normal - fora do município', true),
('010201', 'Prestação de serviço - operação com substituição tributária - dentro do município', true),
('020101', 'Intermediação de serviço - operação normal - dentro do município', true),
('020102', 'Intermediação de serviço - operação normal - fora do município', true),
('030101', 'Estabelecimento do fornecedor - operação normal - dentro do município', true),
('030102', 'Prestação de serviço a consumidor final - operação normal - fora do município', true),
('030201', 'Prestação de serviço a consumidor final - substituição tributária - dentro do município', true),
('040101', 'Exportação de serviço - operação normal', true),
('050101', 'Prestação de serviço ao governo - operação normal', true)
ON CONFLICT DO NOTHING;

-- =============================================================================
-- CLASSIFICAÇÃO TRIBUTÁRIA (6 dígitos)
-- =============================================================================
INSERT INTO classificacao_tributaria (codigo, descricao, grupo, habilitado) VALUES
('410001', 'Serviços de informática e congêneres - desenvolvimento de software', 'TI', true),
('410002', 'Serviços de informática e congêneres - processamento de dados', 'TI', true),
('410003', 'Serviços de informática e congêneres - licenciamento de software', 'TI', true),
('410004', 'Serviços de informática e congêneres - assessoria e consultoria em TI', 'TI', true),
('420001', 'Serviços de telecomunicações', 'TELECOM', true),
('430001', 'Serviços de saúde - consulta médica', 'SAUDE', true),
('430002', 'Serviços de saúde - exames laboratoriais', 'SAUDE', true),
('440001', 'Serviços de educação - ensino regular', 'EDUCACAO', true),
('440002', 'Serviços de educação - ensino técnico e profissionalizante', 'EDUCACAO', true),
('450001', 'Serviços de engenharia e arquitetura - projetos', 'ENGENHARIA', true),
('450002', 'Serviços de engenharia e arquitetura - execução de obras', 'ENGENHARIA', true),
('460001', 'Serviços contábeis e de auditoria', 'CONTABILIDADE', true),
('470001', 'Serviços advocatícios e jurídicos', 'JURIDICO', true),
('480001', 'Serviços de limpeza e conservação', 'LIMPEZA', true),
('490001', 'Serviços de transporte municipal', 'TRANSPORTE', true),
('200028', 'Fornecimento dos serviços de educação', 'EDUCACAO', true),
('500001', 'Serviços financeiros e bancários', 'FINANCEIRO', true)
ON CONFLICT DO NOTHING;

-- =============================================================================
-- NBS - Nomenclatura Brasileira de Serviços (amostra)
-- =============================================================================
INSERT INTO nbs (codigo, descricao, secao, habilitado) VALUES
('108011100', 'Serviços de desenvolvimento de software sob encomenda', 'Seção 1 - TI', true),
('108011200', 'Serviços de licenciamento de software', 'Seção 1 - TI', true),
('108012100', 'Serviços de consultoria em tecnologia da informação', 'Seção 1 - TI', true),
('108012200', 'Serviços de suporte técnico em TI', 'Seção 1 - TI', true),
('108013100', 'Serviços de hospedagem e infraestrutura de TI (cloud)', 'Seção 1 - TI', true),
('108020100', 'Serviços de telecomunicações fixas', 'Seção 1 - Telecom', true),
('108020200', 'Serviços de telecomunicações móveis', 'Seção 1 - Telecom', true),
('102011100', 'Serviços de construção de edifícios', 'Seção 1 - Construção', true),
('102012100', 'Serviços de engenharia e projetos', 'Seção 1 - Construção', true),
('105011100', 'Serviços de saúde humana - consulta', 'Seção 1 - Saúde', true),
('105011200', 'Serviços de saúde humana - exames', 'Seção 1 - Saúde', true),
('106011100', 'Serviços de educação fundamental e médio', 'Seção 1 - Educação', true),
('106012100', 'Serviços de educação superior', 'Seção 1 - Educação', true),
('107011100', 'Serviços contábeis', 'Seção 1 - Profissional', true),
('107012100', 'Serviços advocatícios', 'Seção 1 - Profissional', true),
('103011100', 'Serviços de limpeza e conservação predial', 'Seção 1 - Facilidades', true),
('104011100', 'Serviços de transporte municipal de passageiros', 'Seção 1 - Transporte', true),
('122011200', 'Serviços de pré-escola', 'Seção 1 - Educação', true),
('8511200', 'Serviços de educação infantil - pré-escola', 'Seção 1 - Educação', true)
ON CONFLICT DO NOTHING;

-- =============================================================================
-- CORRELAÇÃO LC 116 → NBS → IndOp → ClassTrib
-- =============================================================================
INSERT INTO correlacao_lc116_nbs (item_lista_servico, descricao_lc116, nbs_codigo, indicador_operacao_codigo, classificacao_tributaria_codigo, cst_padrao, habilitado) VALUES
('01.01', 'Análise e desenvolvimento de sistemas', '108011100', '030102', '410001', '101', true),
('01.02', 'Programação', '108011100', '030102', '410001', '101', true),
('01.03', 'Processamento de dados e congêneres', '108011200', '030102', '410002', '101', true),
('01.04', 'Elaboração de programas de computadores', '108011100', '030102', '410003', '101', true),
('01.05', 'Licenciamento ou cessão de direito de uso de programas de computação', '108011200', '030102', '410003', '101', true),
('01.06', 'Assessoria e consultoria em informática', '108012100', '030102', '410004', '101', true),
('01.07', 'Suporte técnico em informática', '108012200', '030102', '410004', '101', true),
('01.08', 'Planejamento, confecção, manutenção e atualização de páginas eletrônicas', '108011100', '030102', '410001', '101', true),
('01.09', 'Disponibilização de conteúdos digitais e aplicativos em geral', '108013100', '030102', '410003', '101', true),
('07.02', 'Execução de obra de engenharia', '102011100', '010101', '450002', '101', true),
('07.03', 'Elaboração de projetos de engenharia', '102012100', '030102', '450001', '101', true),
('04.01', 'Medicina e biomedicina - consultas', '105011100', '030102', '430001', '102', true),
('04.02', 'Análises clínicas e exames', '105011200', '030102', '430002', '102', true),
('08.01', 'Ensino regular pré-escolar, fundamental, médio e superior', '106011100', '030102', '440001', '301', true),
('08.02', 'Instrução, treinamento e ensino técnico', '106012100', '030102', '440002', '101', true),
('17.01', 'Assessoria ou consultoria contábil', '107011100', '030102', '460001', '101', true),
('17.14', 'Advocacia', '107012100', '030102', '470001', '101', true),
('07.10', 'Limpeza, manutenção e conservação', '103011100', '010101', '480001', '101', true),
('16.01', 'Serviços de transporte coletivo municipal', '104011100', '010101', '490001', '101', true)
ON CONFLICT DO NOTHING;
