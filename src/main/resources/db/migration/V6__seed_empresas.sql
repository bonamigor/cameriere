INSERT INTO empresas (id, nome, cnpj, email, telefone, ativo, logradouro, cep, cidade, estado, data_criacao, data_atualizacao)
VALUES
(1, 'Panificadora e Lanchonete Kidelicia', '02.895.623/0001-03', NULL, NULL, 1,
 'Rua Pinheiro Chagas, 171, Setor Sudoeste', '74303-035', 'Goiânia', 'GO', NOW(6), NULL),
(2, 'COOPASSEN', '36.070.538/0001-10', NULL, NULL, 1,
 'R. Padre Alcides Spolidoro, S/N, Q. I4 L. 11/12, Dist Ind Santa Edwiges', NULL, 'Senador Canedo', 'GO', NOW(6), NULL),
(3, 'COOPACO', '33.507.873/0001-44', NULL, NULL, 1,
 'Rua 03, QD. 07, LT. 13, Sala 03, Recanto das Emboabas', NULL, 'Aparecida de Goiânia', 'GO', NOW(6), NULL),
(4, 'COMPAF', '29.119.413/0001-71', NULL, NULL, 1,
 'ROD GO-320 KM 10.5 n 100, LT. 11 QD. 03, RES BOA ESPERANCA', NULL, 'Goiatuba', 'GO', NOW(6), NULL),
(5, 'COOPANIRA', '50.702.609/0001-80', NULL, NULL, 1,
 'Rua José Caitano Leal, Quadra 17, Lote 33, Setor Sul', NULL, 'Goianira', 'GO', NOW(6), NULL),
(6, 'MAGNO COMERCIO DE ALIMENTOS LTDA', '52.308.366/0001-26', NULL, NULL, 1,
 'Rua Pinheiro Chagas, Número 16, Vl. Nova Canaã', NULL, 'Goiânia', 'GO', NOW(6), NULL),
(7, 'COMERCIAL BELO HORIZONTE LTDA', '30.291.399/0001-78', NULL, NULL, 1,
 'Av. Peru, S/N, Qd. 08, Lt. 11, Sala 01, Jd. Belo Horizonte', NULL, 'Aparecida de Goiânia', 'GO', NOW(6), NULL),
(8, 'M S E DE ALMEIDA COMERCIO DE ALIMENTOS LTDA', '09.376.170/0001-40', NULL, NULL, 1,
 'RUA GB2, nº 19, QD 09 LT 01, Jd GUANABARA II', '74680-600', 'Goiânia', 'GO', NOW(6), NULL),
(9, 'CL ALIMENTOS LTDA', '35.137.005/0001-45', NULL, NULL, 1,
 'R DAS ORQUIDEAS, 1008, QUADRA122 LOTE 02, PRQ OESTE INDUSTRIAL', '74375-210', 'Goiânia', 'GO', NOW(6), NULL),
(10, 'COOPERAGO', '55.487.408/0001-21', NULL, NULL, 1,
 'R RIO CLARO, CHACARA 21, S/N, LOTE 17, CAMPO DOURADO', '75330-000', 'Aragoiânia', 'GO', NOW(6), NULL),
(11, 'COMPLANG', '63.901.913/0001-07', NULL, NULL, 1,
 'R NATAL BOLENTINO PC - 2356, S/N, QUADRA4 LOTE 11, NOSSA SENHORA DO CARMO', '75470-000', 'Nova Veneza', 'GO', NOW(6), NULL),
(12, 'COOPER-BR', '63.744.054/0001-81', NULL, NULL, 1,
 'AV CARLOS DE PINA, S/N, QUADRA 17 LOTE 02, SETOR CENTRAL', '75165-000', 'Ouro Verde de Goiás', 'GO', NOW(6), NULL),
(13, 'MENDES ALIMENTOS', '26.669.899/0001-23', NULL, NULL, 1,
 'AV C17, N. 92, QD 147, ST SUDOESTE', '74303-280', 'Goiânia', 'GO', NOW(6), NULL)
ON DUPLICATE KEY UPDATE
    nome = VALUES(nome),
    cnpj = VALUES(cnpj),
    ativo = VALUES(ativo),
    logradouro = VALUES(logradouro),
    cep = VALUES(cep),
    cidade = VALUES(cidade),
    estado = VALUES(estado),
    data_atualizacao = NOW(6);

-- Garante sequência do AUTO_INCREMENT após inserts com IDs explícitos
ALTER TABLE empresas AUTO_INCREMENT = 14;
