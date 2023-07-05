CREATE OR REPLACE FUNCTION "transferCurrency"(
    _from_user_id BIGINT,
    _to_user_id BIGINT,
    _currency VARCHAR,
    _amount NUMERIC
)
RETURNS TABLE (new_from_amount NUMERIC, new_to_amount NUMERIC, updated_rows INT)
LANGUAGE plpgsql AS $$
DECLARE
    v_new_from_amount NUMERIC;
    v_new_to_amount NUMERIC;
BEGIN
    BEGIN
        INSERT INTO user_currency (user_id, currency, amount)
        VALUES (_from_user_id, _currency, 0),
               (_to_user_id, _currency, 0)
        ON CONFLICT (user_id, currency) DO NOTHING;

        UPDATE user_currency
        SET amount = amount - _amount
        WHERE user_id = _from_user_id AND currency = _currency
        RETURNING amount INTO v_new_from_amount;

        IF v_new_from_amount < 0 THEN
            RAISE EXCEPTION 'Amount cannot go below zero';
        END IF;

        UPDATE user_currency
        SET amount = amount + _amount
        WHERE user_id = _to_user_id AND currency = _currency
        RETURNING amount INTO v_new_to_amount;

        new_from_amount := v_new_from_amount;
        new_to_amount := v_new_to_amount;
        GET DIAGNOSTICS updated_rows = ROW_COUNT;
    EXCEPTION
        WHEN OTHERS THEN
            new_from_amount := v_new_from_amount + _amount;
            new_to_amount := v_new_to_amount - _amount;
    END;
    RETURN QUERY SELECT new_from_amount, new_to_amount, updated_rows;
END;
$$;

CREATE OR REPLACE FUNCTION "updateCurrency"(
    _user_id BIGINT,
    _currency VARCHAR,
    _amount NUMERIC
)
RETURNS TABLE (new_amount NUMERIC, updated_rows INT)
LANGUAGE plpgsql AS $$
DECLARE
    v_new_amount NUMERIC;
BEGIN
    BEGIN
        INSERT INTO user_currency (user_id, currency, amount)
        VALUES (_user_id, _currency, 0)
        ON CONFLICT (user_id, currency) DO NOTHING;

        UPDATE user_currency
        SET amount = amount + _amount
        WHERE user_id = _user_id AND currency = _currency
        RETURNING amount INTO v_new_amount;

        IF v_new_amount < 0 THEN
            RAISE EXCEPTION 'Amount cannot go below zero';
        END IF;

        new_amount := v_new_amount;
        GET DIAGNOSTICS updated_rows = ROW_COUNT;
    EXCEPTION
        WHEN OTHERS THEN
            new_amount := v_new_amount - _amount;
    END;
    RETURN QUERY SELECT new_amount, updated_rows;
END;
$$;