CREATE TABLE IF NOT EXISTS client.user_company (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    company_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'OWNER',
    CONSTRAINT fk_user_company_user FOREIGN KEY (user_id) REFERENCES client."user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_company_company FOREIGN KEY (company_id) REFERENCES client.company(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_company UNIQUE (user_id, company_id)
);

CREATE INDEX IF NOT EXISTS idx_user_company_user ON client.user_company(user_id);
CREATE INDEX IF NOT EXISTS idx_user_company_company ON client.user_company(company_id);




