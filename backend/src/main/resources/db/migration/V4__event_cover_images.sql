-- Add optional event cover artwork while keeping existing bulletin posts valid.
ALTER TABLE bulletin_post ADD COLUMN cover_image_url VARCHAR(1000) NULL;
