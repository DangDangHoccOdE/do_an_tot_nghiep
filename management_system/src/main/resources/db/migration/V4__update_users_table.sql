-- Thêm cột cv (TEXT)
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS cv TEXT;

-- Thêm cột it_role (VARCHAR, map EnumType.STRING)
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS it_role VARCHAR(50);
