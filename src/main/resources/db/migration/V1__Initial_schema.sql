CREATE TABLE "USER" (
                      ID BIGINT PRIMARY KEY,
                      NAME VARCHAR(500) NOT NULL,
                      DATE_OF_BIRTH VARCHAR(10) NOT NULL,
                      PASSWORD VARCHAR(500) NOT NULL
);

CREATE TABLE ACCOUNT (
                         ID BIGINT PRIMARY KEY,
                         USER_ID BIGINT NOT NULL UNIQUE,
                         BALANCE DECIMAL(19,2) NOT NULL,
                         INITIAL_DEPOSIT DECIMAL(19,2) NOT NULL,
                         FOREIGN KEY (USER_ID) REFERENCES "USER"(ID)
);

CREATE TABLE EMAIL_DATA (
                            ID BIGINT PRIMARY KEY,
                            USER_ID BIGINT NOT NULL,
                            EMAIL VARCHAR(200) NOT NULL UNIQUE,
                            FOREIGN KEY (USER_ID) REFERENCES "USER"(ID)
);

CREATE TABLE PHONE_DATA (
                            ID BIGINT PRIMARY KEY,
                            USER_ID BIGINT NOT NULL,
                            PHONE VARCHAR(13) NOT NULL UNIQUE,
                            FOREIGN KEY (USER_ID) REFERENCES "USER"(ID)
);