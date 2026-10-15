INSERT INTO categorias (id, nombre, icono, descripcion, estado, total_productos, fecha_creacion, fecha_actualizacion) VALUES
('gpu',         'Tarjetas Gráficas (GPU)', 'Layers',          'Tarjetas de video GeForce RTX y AMD Radeon', 'ACTIVA', 24, NOW(), NOW()),
('cpu',         'Procesadores',            'Zap',             'Procesadores Intel Core 13ª/14ª Gen y AMD Ryzen 7000/8000', 'ACTIVA', 30, NOW(), NOW()),
('motherboard', 'Placas Madre',            'Grid',            'Mainboards chipsets Z790, B760, X670, B650', 'ACTIVA', 18, NOW(), NOW()),
('hdd',         'Discos Duros (HDD)',      'Database',        'Discos mecánicos para almacenamiento masivo y NAS', 'ACTIVA', 15, NOW(), NOW()),
('psu',         'Fuentes de Poder',        'BatteryCharging', 'Fuentes 80 Plus Bronze, Gold y Platinum ATX 3.0', 'ACTIVA', 20, NOW(), NOW()),
('usb',         'Memorias USB & Flash',    'Usb',             'Pen drives USB 3.2 y tarjetas MicroSD por paquete máster', 'ACTIVA', 12, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO subcategorias (categoria_id, nombre) VALUES
('ram', 'DDR4 Desktop'), ('ram', 'DDR5 Desktop'), ('ram', 'DDR5 SO-DIMM Laptop'),
('ssd', 'NVMe M.2 PCIe 4.0'), ('ssd', 'NVMe M.2 PCIe 3.0'), ('ssd', 'SATA III 2.5"'),
('gpu', 'GeForce RTX 4000'), ('gpu', 'Radeon RX 7000'), ('gpu', 'Workstation Pro'),
('cpu', 'Intel Core 14ª Gen'), ('cpu', 'Intel Core 13ª Gen'), ('cpu', 'AMD Ryzen AM5'), ('cpu', 'AMD Ryzen AM4'),
('motherboard', 'Chipset Z790/B760'), ('motherboard', 'Chipset X670/B650'), ('motherboard', 'Micro-ATX'), ('motherboard', 'Mini-ITX'),
('hdd', 'HDD Surveillance 24/7'), ('hdd', 'HDD NAS Enterprise'), ('hdd', 'HDD Desktop 3.5"'),
('psu', '80+ Bronze'), ('psu', '80+ Gold Modular'), ('psu', '80+ Platinum ATX 3.0'),
('usb', 'USB 3.2 Gen 1'), ('usb', 'USB Tipo-C'), ('usb', 'Tarjetas MicroSD Clase 10')
ON CONFLICT DO NOTHING;

UPDATE categorias SET total_productos = 45 WHERE id = 'ram';
UPDATE categorias SET total_productos = 38 WHERE id = 'ssd';

