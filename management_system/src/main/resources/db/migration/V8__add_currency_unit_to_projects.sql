ALTER TABLE projects ADD COLUMN currency_unit VARCHAR(10) DEFAULT 'VND';
CREATE INDEX idx_projects_currency_unit ON projects(currency_unit);
