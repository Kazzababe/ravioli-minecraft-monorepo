CREATE TABLE IF NOT EXISTS dialogue_state (
    user_id     UUID                NOT NULL,
    dialogue    VARCHAR (64)        NOT NULL,
    state       INTEGER             NOT NULL,
    UNIQUE (user_id, dialogue)
);