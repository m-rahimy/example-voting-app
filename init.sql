DROP TABLE IF EXISTS voting; 
DROP TABLE IF EXISTS people; 
DROP TABLE IF EXISTS candidate; 

CREATE TABLE IF NOT EXISTS  people (  
	id VARCHAR(36) PRIMARY KEY NOT NULL,  
	full_name VARCHAR(50)  DEFAULT NULL,  
	user_agent VARCHAR(255) DEFAULT NULL,  
	remote_address VARCHAR(20) DEFAULT NULL,  
	remote_port VARCHAR(4) DEFAULT NULL,  
	first_visit VARCHAR(20) DEFAULT NULL,  
	last_visit VARCHAR(20) DEFAULT NULL) ; 

CREATE TABLE IF NOT EXISTS  voting ( 
	id SERIAL PRIMARY KEY  NOT NULL,  
	voter_id VARCHAR(36)  NOT NULL,  
	candidate_id INT  NOT NULL,  
	first_voted VARCHAR(20) DEFAULT NULL,  
	last_change VARCHAR(20) DEFAULT NULL,  
	changes_count INT DEFAULT 1) ; 

CREATE TABLE IF NOT EXISTS candidate (  
	id SERIAL PRIMARY KEY  NOT NULL,  
	name VARCHAR(20)  NOT NULL,  
	opponent_id INT  NOT NULL) ; 

DO $$BEGIN  BEGIN    
	ALTER TABLE voting       
		ADD CONSTRAINT voting_ibfk_1 FOREIGN KEY (voter_id) REFERENCES people (id); 
  EXCEPTION    
	WHEN duplicate_object
  THEN 
	RAISE NOTICE 'Table constraint voting_ibfk_1 already exists'; 
  END; 
END $$; 

DO $$BEGIN  BEGIN    
	ALTER TABLE voting       
		ADD CONSTRAINT voting_ibfk_2 FOREIGN KEY (candidate_id) REFERENCES candidate (id); 
  EXCEPTION    
	WHEN duplicate_object 
		THEN RAISE NOTICE 'Table constraint voting_ibfk_2 already exists'; 
  END; 
END $$; 

DO $$BEGIN  BEGIN    
	ALTER TABLE candidate 
		ADD CONSTRAINT candidate_ibfk_1 FOREIGN KEY (opponent_id) REFERENCES candidate (id); 
  EXCEPTION   
	WHEN duplicate_object
		THEN RAISE NOTICE 'Table constraint voting_ibfk_2 already exists'; 
  END; 
END $$; 

CREATE OR REPLACE FUNCTION fetch_candidates() 
	RETURNS TABLE(op1ID INTEGER, op1Name VARCHAR(20), op2ID INTEGER, op2Name VARCHAR(20)) AS $$ 
    BEGIN      
	RETURN QUERY            
		SELECT         
			candidate.id AS op1ID,      
			candidate.name AS op1Name,            
			A.id AS op2ID,   
			A.name AS op2Name           
		FROM              
			candidate A           
		JOIN candidate           
			ON candidate.id = A.opponent_id        
		WHERE candidate.id < A.id; 
   END; 
$$ LANGUAGE plpgsql; 

CREATE OR REPLACE FUNCTION fetch_votes_for_voter(id TEXT) 
	RETURNS TABLE(voteID INTEGER, voterID VARCHAR(36), voterName VARCHAR(36), candidateID INTEGER, firstVoted VARCHAR(20), lastChange VARCHAR(20) , changesCount INTEGER) AS $$ 
    BEGIN      
	RETURN QUERY            
		SELECT         
			voting.id AS voteID,      
			people.id AS voterID,            
                        people.full_name AS voterName,
			candidate_id AS candidateID,   
			first_voted AS firstVoted,           
			last_change AS lastChange,           
			changes_count AS changesCount           
		FROM              
			voting 
                JOIN candidate ON voting.candidate_id=candidate.id
		JOIN people ON voter_id = people.id
		WHERE candidate.id = $1; 
   END; 
$$ LANGUAGE plpgsql; 

INSERT INTO candidate (name, opponent_id) values ('iOS','1'); 
INSERT INTO candidate (name, opponent_id) values ('Android','1'); 
UPDATE candidate SET opponent_id=2 WHERE id=1; 

INSERT INTO candidate (name, opponent_id) values ('Cats','3'); 
INSERT INTO candidate (name, opponent_id) values ('Dogs','3'); 
UPDATE candidate SET opponent_id=4 WHERE id=3; 

INSERT INTO candidate (name, opponent_id) values ('Pepsi','5'); 
INSERT INTO candidate (name, opponent_id) values ('Coca Cola','5'); 
UPDATE candidate SET opponent_id=6 WHERE id=5; 

INSERT INTO candidate (name, opponent_id) values ('Blue Pen','7'); 
INSERT INTO candidate (name, opponent_id) values ('Black Pen','7'); 
UPDATE candidate SET opponent_id=8 WHERE id=7; 

INSERT INTO candidate (name, opponent_id) values ('Regular Pen','9'); 
INSERT INTO candidate (name, opponent_id) values ('Fountain Pen','9'); 
UPDATE candidate SET opponent_id=10 WHERE id=9; 

INSERT INTO candidate (name, opponent_id) values ('Pencil','11'); 
INSERT INTO candidate (name, opponent_id) values ('Mechanical Pencil','11'); 
UPDATE candidate SET opponent_id=12 WHERE id=11; 

