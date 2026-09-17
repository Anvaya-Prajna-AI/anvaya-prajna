-- Anvaya-Prajna-AI Database Migration V2
-- Add review metadata columns to explanation table

ALTER TABLE explanation ADD COLUMN IF NOT EXISTS reviewed_by VARCHAR(255);
ALTER TABLE explanation ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP WITH TIME ZONE;
