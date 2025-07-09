psql -U postgres -c "DROP DATABASE biosteel WITH (FORCE);"
psql -U postgres -c "CREATE DATABASE biosteel;"
psql -U postgres
CREATE ROLE biosteel WITH LOGIN PASSWORD 'Mobi.biosteel.1';
GRANT ALL PRIVILEGES ON DATABASE biosteel TO biosteel;
sudo -u postgres psql
GRANT ALL PRIVILEGES ON DATABASE biosteel TO biosteel;
\c biosteel
GRANT ALL ON SCHEMA public TO biosteel;
GRANT ALL ON ALL TABLES IN SCHEMA public TO biosteel;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO biosteel;
GRANT ALL ON SCHEMA biosteel TO biosteel;
GRANT ALL ON ALL TABLES IN SCHEMA biosteel TO biosteel;
GRANT ALL ON ALL SEQUENCES IN SCHEMA biosteel TO biosteel;
\q

