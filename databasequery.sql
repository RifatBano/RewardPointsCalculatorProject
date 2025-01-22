There are four tables in the RewardPointCalculator project:
1)customer
2)customer_transaction
3)reward_points
4)blacklisted_token
Postgres query:
1)customer
CREATE TABLE customer (id SERIAL PRIMARY KEY,first_name VARCHAR(255) NOT NULL,last_name VARCHAR(255) NOT NULL,email VARCHAR(255) NOT NULL UNIQUE,password VARCHAR(255) NOT NULL);
2)customer_transaction
CREATE TABLE customer_transaction (id SERIAL PRIMARY KEY,customer_id BIGINT NOT NULL,amount DOUBLE PRECISION NOT NULL,spent_details TEXT,date DATE NOT NULL,CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE);
3)reward_points
CREATE TABLE reward_points (id SERIAL PRIMARY KEY,customer_id BIGINT NOT NULL,points INT NOT NULL,month INT NOT NULL CHECK (month >= 1 AND month <= 12),year INT NOT NULL CHECK (year >= 1900 AND year <= 9999),CONSTRAINT fk_customer_reward FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE);
4)blacklisted_token
CREATE TABLE blacklisted_token (
    id SERIAL PRIMARY KEY,           
    token VARCHAR(255) NOT NULL,    
     username VARCHAR(255) NOT NULL,  
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
);

