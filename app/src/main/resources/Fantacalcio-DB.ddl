CREATE DATABASE fantacalcio;
USE fantacalcio;

CREATE TABLE BONUS_MALUS (
    ID_Bonus_Malus INT PRIMARY KEY AUTO_INCREMENT,
    Tipologia VARCHAR(50) NOT NULL,
    Punteggio DECIMAL(4,2) NOT NULL
);

CREATE TABLE CALCIATORE (
    ID_Calciatore INT PRIMARY KEY AUTO_INCREMENT,
    Nome VARCHAR(50) NOT NULL,
    Cognome VARCHAR(50) NOT NULL,
    Costo INT NOT NULL,
    Ruolo ENUM('P','D','C','A') NOT NULL,
    Infortunato BOOLEAN DEFAULT FALSE,
    Squalificato BOOLEAN DEFAULT FALSE,
    ID_Squadra INT NOT NULL
);

CREATE TABLE CLASSIFICA (
    ID_Classifica INT PRIMARY KEY AUTO_INCREMENT,
    Punti INT DEFAULT 0,
    Vittorie INT DEFAULT 0,
    Pareggi INT DEFAULT 0,
    Sconfitte INT DEFAULT 0,
    Punteggio_totale DECIMAL(6,2) DEFAULT 0,
    Punteggio_subito DECIMAL(6,2) DEFAULT 0
);

CREATE TABLE COMPOSIZIONE (
    ID_Calciatore INT NOT NULL,
    ID_Squadra_Fantacalcio INT NOT NULL,
    PRIMARY KEY (ID_Calciatore, ID_Squadra_Fantacalcio)
);

CREATE TABLE FASCIA_APPARTENENZA (
    ID_Calciatore INT NOT NULL,
    ID_Fascia INT NOT NULL,
    PRIMARY KEY (ID_Calciatore, ID_Fascia)
);

CREATE TABLE FASCIA_GIOCATORE (
    ID_Fascia INT PRIMARY KEY AUTO_INCREMENT,
    Nome_fascia VARCHAR(20) NOT NULL,
    Prob_gol_attaccante DECIMAL(3,2) DEFAULT 0.15,
    Prob_gol_centrocampista DECIMAL(3,2) DEFAULT 0.08,
    Prob_gol_difensore DECIMAL(3,2) DEFAULT 0.03,
    Prob_assist DECIMAL(3,2) DEFAULT 0.12,
    Prob_ammonizione DECIMAL(3,2) DEFAULT 0.20,
    Prob_espulsione DECIMAL(3,2) DEFAULT 0.02,
    Prob_imbattibilita DECIMAL(3,2) DEFAULT 0.25,
    Voto_base_standard DECIMAL(3,2) DEFAULT 0.00
);

CREATE TABLE FORMANO (
    ID_Calciatore INT NOT NULL,
    ID_Formazione INT NOT NULL,
    Panchina ENUM('SI','NO') NOT NULL DEFAULT 'SI',
    PRIMARY KEY (ID_Formazione, ID_Calciatore)
);

CREATE TABLE FORMAZIONE (
    ID_Formazione INT PRIMARY KEY AUTO_INCREMENT,
    Modulo VARCHAR(10) NOT NULL,
    ID_Squadra_Fantacalcio INT NOT NULL,
    Punteggio DECIMAL(4,2) DEFAULT 0,
    Numero_Giornata INT NOT NULL
);

CREATE TABLE GIORNATA (
    Numero INT PRIMARY KEY
);

CREATE TABLE LEGA (
     ID_Lega INT PRIMARY KEY AUTO_INCREMENT,
     Nome VARCHAR(100) NOT NULL,
     Codice_accesso VARCHAR(10),
     Stato ENUM('CREATA','IN_CORSO','TERMINATA') DEFAULT 'CREATA',
     ID_Campionato INT,
     ID_Utente INT NOT NULL
);

CREATE TABLE PUNTEGGIO (
    Numero_Giornata INT NOT NULL,
    ID_Calciatore INT NOT NULL,
    ID_Bonus_Malus INT NOT NULL,
    Quantità INT DEFAULT 1,
    PRIMARY KEY (Numero_Giornata, ID_Bonus_Malus, ID_Calciatore)
);

CREATE TABLE SCONTRO (
    ID_Scontro INT PRIMARY KEY AUTO_INCREMENT,
    ID_Formazione1 INT NOT NULL,
    ID_Formazione2 INT NOT NULL,
    Risultato VARCHAR(15) NOT NULL,
    Stato ENUM('PROGRAMMATO','IN_CORSO','COMPLETATO') DEFAULT 'PROGRAMMATO',
    Data_inizio DATETIME,
    Numero_Giornata INT NOT NULL,
    ID_Lega INT NOT NULL
);

CREATE TABLE SQUADRA (
     ID_Squadra INT PRIMARY KEY AUTO_INCREMENT,
     Nome VARCHAR(50) NOT NULL,
     ID_Campionato INT NOT NULL,
     ID_Calciatore INT NOT NULL
);

CREATE TABLE SQUADRA_FANTACALCIO (
     ID_Squadra_Fantacalcio INT PRIMARY KEY AUTO_INCREMENT,
     ID_Classifica INT NOT NULL,
     Nome VARCHAR(50) NOT NULL,
     Budget_totale INT DEFAULT 1000,
     Budget_rimanente INT DEFAULT 1000,
     Data_creazione DATETIME DEFAULT CURRENT_TIMESTAMP,
     Data_ultima_modifica DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     Completata BOOLEAN DEFAULT FALSE,
     ID_Utente INT NOT NULL,
     ID_Lega INT NOT NULL
);

CREATE TABLE TIPO_CAMPIONATO (
     ID_Campionato INT PRIMARY KEY AUTO_INCREMENT,
     Nome VARCHAR(50) NOT NULL,
     Anno YEAR NOT NULL
);

CREATE TABLE UTENTE (
     ID_Utente INT PRIMARY KEY AUTO_INCREMENT,
     Nome VARCHAR(50) NOT NULL,
     Cognome VARCHAR(50) NOT NULL,
     Nickname VARCHAR(30) NOT NULL,
     Email VARCHAR(100) NOT NULL,
     Numero VARCHAR(15),
     Data_di_nascita DATE NOT NULL,
     Password VARCHAR(255) NOT NULL
);

CREATE TABLE VOTO_GIORNATA (
    Numero_Giornata INT NOT NULL,
    ID_Calciatore INT NOT NULL,
    Voto_base DECIMAL(4,2) NOT NULL,
    PRIMARY KEY (Numero_Giornata, ID_Calciatore)
);

ALTER TABLE CALCIATORE ADD CONSTRAINT FKAPPARTENENZA_SQUADRA
     FOREIGN KEY (ID_Squadra)
     REFERENCES SQUADRA (ID_Squadra);

ALTER TABLE COMPOSIZIONE ADD CONSTRAINT FKCOM_SQU
     FOREIGN KEY (ID_Squadra_Fantacalcio)
     REFERENCES SQUADRA_FANTACALCIO (ID_Squadra_Fantacalcio);

ALTER TABLE COMPOSIZIONE ADD CONSTRAINT FKCOM_CAL
     FOREIGN KEY (ID_Calciatore)
     REFERENCES CALCIATORE (ID_Calciatore);

ALTER TABLE FASCIA_APPARTENENZA ADD CONSTRAINT FKFAS_FAS
     FOREIGN KEY (ID_Fascia)
     REFERENCES FASCIA_GIOCATORE (ID_Fascia);

ALTER TABLE FASCIA_APPARTENENZA ADD CONSTRAINT FKFAS_CAL
     FOREIGN KEY (ID_Calciatore)
     REFERENCES CALCIATORE (ID_Calciatore);

ALTER TABLE FORMANO ADD CONSTRAINT FKFOR_FOR
     FOREIGN KEY (ID_Formazione)
     REFERENCES FORMAZIONE (ID_Formazione);

ALTER TABLE FORMANO ADD CONSTRAINT FKFOR_CAL
     FOREIGN KEY (ID_Calciatore)
     REFERENCES CALCIATORE (ID_Calciatore);

ALTER TABLE FORMAZIONE ADD CONSTRAINT FKSCHIERAMENTO
     FOREIGN KEY (ID_Squadra_Fantacalcio)
     REFERENCES SQUADRA_FANTACALCIO (ID_Squadra_Fantacalcio);

ALTER TABLE FORMAZIONE ADD CONSTRAINT FKSCHIERATA_IN
     FOREIGN KEY (Numero_Giornata)
     REFERENCES GIORNATA (Numero);

ALTER TABLE LEGA ADD CONSTRAINT FKCONTIENE
     FOREIGN KEY (ID_Campionato)
     REFERENCES TIPO_CAMPIONATO (ID_Campionato);

ALTER TABLE LEGA ADD CONSTRAINT FKADMIN
     FOREIGN KEY (ID_Utente)
     REFERENCES UTENTE (ID_Utente);

ALTER TABLE PUNTEGGIO ADD CONSTRAINT FKPUN_BON
     FOREIGN KEY (ID_Bonus_Malus)
     REFERENCES BONUS_MALUS (ID_Bonus_Malus);

ALTER TABLE PUNTEGGIO ADD CONSTRAINT FKPUN_CAL
     FOREIGN KEY (ID_Calciatore)
     REFERENCES CALCIATORE (ID_Calciatore);

ALTER TABLE PUNTEGGIO ADD CONSTRAINT FKPUN_GIO
     FOREIGN KEY (Numero_Giornata)
     REFERENCES GIORNATA (Numero);

ALTER TABLE SCONTRO ADD CONSTRAINT FKAVVENGONO
     FOREIGN KEY (Numero_Giornata)
     REFERENCES GIORNATA (Numero);

ALTER TABLE SCONTRO ADD CONSTRAINT FKORGANIZZA
     FOREIGN KEY (ID_Lega)
     REFERENCES LEGA (ID_Lega);

ALTER TABLE SCONTRO ADD CONSTRAINT FKSQUADRA_1_FK
     FOREIGN KEY (ID_Formazione1)
     REFERENCES FORMAZIONE (ID_Formazione);

ALTER TABLE SCONTRO ADD CONSTRAINT FKSQUADRA_2_FK
     FOREIGN KEY (ID_Formazione2)
     REFERENCES FORMAZIONE (ID_Formazione);

ALTER TABLE SQUADRA ADD CONSTRAINT FKAPPARTENENZA_CAMPIONATO
     FOREIGN KEY (ID_Campionato)
     REFERENCES TIPO_CAMPIONATO (ID_Campionato);

ALTER TABLE SQUADRA_FANTACALCIO ADD CONSTRAINT FKCREA
     FOREIGN KEY (ID_Utente)
     REFERENCES UTENTE (ID_Utente);

ALTER TABLE SQUADRA_FANTACALCIO ADD CONSTRAINT FKPARTECIPA
     FOREIGN KEY (ID_Lega)
     REFERENCES LEGA (ID_Lega);

ALTER TABLE SQUADRA_FANTACALCIO ADD CONSTRAINT FKCLASSIFICAZIONE_FK
     FOREIGN KEY (ID_Classifica)
     REFERENCES CLASSIFICA (ID_Classifica);

ALTER TABLE VOTO_GIORNATA ADD CONSTRAINT FKVOT_CAL
     FOREIGN KEY (ID_Calciatore)
     REFERENCES CALCIATORE (ID_Calciatore);

ALTER TABLE VOTO_GIORNATA ADD CONSTRAINT FKVOT_GIO
     FOREIGN KEY (Numero_Giornata)
     REFERENCES GIORNATA (Numero);

-- (Opzionale ma consigliato) chiave univoca su Tipologia
ALTER TABLE BONUS_MALUS ADD UNIQUE KEY uk_bonusmalus_tipologia (Tipologia);

	INSERT INTO BONUS_MALUS (Tipologia, Punteggio) VALUES
	  ('Gol',               3.00),
	  ('Assist',            1.00),
	  ('Ammonizione',      -0.50),
	  ('Espulsione',       -1.00),
	  ('Porta Imbattuta',   1.00)
	AS new
	ON DUPLICATE KEY UPDATE
	  Punteggio = new.Punteggio;
  
  INSERT INTO UTENTE (Nome, Cognome, Nickname, Email, Numero, Data_di_nascita, Password) VALUES
  ('Mario',   'Rossi',    'team3', 'mario.rossi@example.com',   '3331234567', '1990-05-10', 'team3'),
  ('Luca',    'Bianchi',  'team4',  'luca.bianchi@example.com',  '3339876543', '1992-09-21', 'team4')
AS new
ON DUPLICATE KEY UPDATE
  Nome = new.Nome,
  Cognome = new.Cognome,
  Nickname = new.Nickname,
  Numero = new.Numero,
  Data_di_nascita = new.Data_di_nascita,
  Password = new.Password;

INSERT INTO FASCIA_GIOCATORE
  (ID_Fascia, Nome_fascia, Voto_base_standard,
   Prob_gol_attaccante, Prob_gol_centrocampista, Prob_gol_difensore,
   Prob_assist, Prob_ammonizione, Prob_espulsione, Prob_imbattibilita)
VALUES
(1, 'TOP',       6.8,  0.42, 0.25, 0.15, 0.30, 0.12, 0.01, 0.35),
(2, 'SEMI-TOP',  6.5,  0.30, 0.17, 0.10, 0.22, 0.15, 0.02, 0.30),
(3, 'TITOLARE',  6.2,  0.22, 0.12, 0.05, 0.17, 0.18, 0.03, 0.25),
(4, 'ROTAZIONE', 6.0,  0.15, 0.10, 0.04, 0.12, 0.20, 0.04, 0.22),
(5, 'SCOMMESSA', 5.8,  0.09, 0.05, 0.03, 0.08, 0.24, 0.06, 0.18)
AS new
ON DUPLICATE KEY UPDATE
  Nome_fascia             = new.Nome_fascia,
  Voto_base_standard      = new.Voto_base_standard,
  Prob_gol_attaccante     = new.Prob_gol_attaccante,
  Prob_gol_centrocampista = new.Prob_gol_centrocampista,
  Prob_gol_difensore      = new.Prob_gol_difensore,
  Prob_assist             = new.Prob_assist,
  Prob_ammonizione        = new.Prob_ammonizione,
  Prob_espulsione         = new.Prob_espulsione,
  Prob_imbattibilita      = new.Prob_imbattibilita;

INSERT INTO UTENTE (Nome, Cognome, Nickname, Email, Numero, Data_di_nascita, Password) VALUES
  ('Mario',   'Rossi',    'team1', 'mario.rossi@example.com',   '3331234567', '1990-05-10', 'team1'),
  ('Luca',    'Bianchi',  'team2',  'luca.bianchi@example.com',  '3339876543', '1992-09-21', 'team2')
AS new
ON DUPLICATE KEY UPDATE
  Nome = new.Nome,
  Cognome = new.Cognome,
  Nickname = new.Nickname,
  Numero = new.Numero,
  Data_di_nascita = new.Data_di_nascita,
  Password = new.Password;