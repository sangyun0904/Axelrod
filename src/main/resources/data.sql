INSERT INTO player (username, created_at, email, name, password, updated_at) VALUES ('test', now(), 'test@gmail.com', 'test user', 'test', now())
ON CONFLICT DO NOTHING;

INSERT INTO account (id, account_num, balance, created_at, updated_at, username) VALUES (0, '000-0000-0000-0000', 9999999999999999999, now(), now(), 'test')
ON CONFLICT DO NOTHING;

INSERT INTO stock (id, market, name, price, sector, ticker, time_stamp, industry) VALUES (0, 'NASDAQ', 'International Business Machines Corporation Common Stock', 223.18, 'Technology', 'IBM', '2025-01-12 15:11:20.53996', 'Computer Manufacturing')
ON CONFLICT DO NOTHING;