-- Schema per il database H2 di Alchemist's Hangover

-- Catalogo Oggetti
CREATE TABLE IF NOT EXISTS items (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    icon_path VARCHAR(255)
);

-- Popolamento catalogo oggetti base (se non già presenti)
MERGE INTO items KEY(id) VALUES
('MAPPA_CIELO', 'Mappa del Cielo', 'Una pergamena cosmica trovata nella stiva.', '/images/items/item_map.png'),
('NEBBIA_NIFLHEIM', 'Nebbia di Niflheim', 'Una fiala sigillata contenente fumo denso e gelido.', '/images/items/item_fog.png'),
('PIUMA_FOLGORE', 'Piuma di Folgore', 'Una piuma dorata carica di elettricità incastrata tra le carrucole.', '/images/items/item_feather.png'),
('POLLINE_YGGDRASIL', 'Polline di Yggdrasil', 'Polvere dorata fluttuante nell''aria.', '/images/items/item_pollen.png'),
('RUGIADA_ASGARD', 'Rugiada delle Nuvole di Asgard', 'Acqua fluttuante condensata sulla polena drago.', '/images/items/item_dew.png'),
('CHIAVE_DRAGO', 'Chiave del Drago', 'Una pesante chiave d''ottone forgiata a forma di testa di drago.', '/images/items/item_key.png'),
('SOFFIO_HRAESVELGR', 'Soffio di Hræsvelgr', 'Una folata d''aria gelida e tempestosa strappata all''aquila gigante.', '/images/items/item_breath.png');

-- Slot di Salvataggio
CREATE TABLE IF NOT EXISTS save_slots (
    slot_id INT PRIMARY KEY,
    slot_name VARCHAR(100) NOT NULL,
    current_chapter INT DEFAULT 1,
    current_room INT DEFAULT 1,
    current_health INT DEFAULT 3,
    max_health INT DEFAULT 3,
    has_given_map BOOLEAN DEFAULT FALSE,
    has_dragon_key BOOLEAN DEFAULT FALSE,
    is_dragon_unlocked BOOLEAN DEFAULT FALSE,
    is_boss_defeated BOOLEAN DEFAULT FALSE,
    last_saved TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Migrazione non distruttiva per database creati con lo schema precedente.
ALTER TABLE save_slots ADD COLUMN IF NOT EXISTS current_chapter INT DEFAULT 1;

-- Catalogo oggetti del Capitolo 2.
MERGE INTO items KEY(id) VALUES
('DIARIO_CAPITANO', 'Diario del Capitano', 'Un diario con il codice per decifrare le strategie di Rán.', '/images/items/item_journl.png'),
('CORALLO_RAN', 'Corallo di Rán', 'Un ramo di corallo aspro rosso sangue incastrato nelle rocce.', '/images/items/item_coral.png'),
('INCHIOSTRO_KRAKEN', 'Inchiostro di Kraken', 'Una boccetta di liquido buio e denso depositata nell''anfratto.', '/images/items/item_ink.png'),
('SALE_JORMUNGANDR', 'Sale di Jörmungandr', 'Cristalli lucenti di squama salmastra staccati dalle pareti.', '/images/items/item_salt.png'),
('ACQUA_LETE', 'Acqua del Lete', 'Una linfa trasparente e stranamente immobile.', '/images/items/item_lete.png'),
('SCHIUMA_AEGIR', 'Schiuma di Ægir', 'La birra sottomarina lucente estratta dalle reti della dea Rán.', '/images/items/item_foam.png');

-- Inventario associato a ciascun salvataggio
CREATE TABLE IF NOT EXISTS save_inventory (
    slot_id INT NOT NULL,
    item_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (slot_id, item_id),
    FOREIGN KEY (slot_id) REFERENCES save_slots(slot_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id)
);
