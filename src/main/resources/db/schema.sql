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
('MAPPA_CIELO', 'Mappa del Cielo', 'Una pergamena cosmica trovata nella stiva.', '/images/chapter1/items/map.png'),
('NEBBIA_NIFLHEIM', 'Nebbia di Niflheim', 'Una fiala sigillata contenente fumo denso e gelido.', '/images/chapter1/items/fog.png'),
('PIUMA_FOLGORE', 'Piuma di Folgore', 'Una piuma dorata carica di elettricità incastrata tra le carrucole.', '/images/chapter1/items/feather.png'),
('POLLINE_YGGDRASIL', 'Polline di Yggdrasil', 'Polvere dorata fluttuante nell''aria.', '/images/chapter1/items/pollen.png'),
('RUGIADA_ASGARD', 'Rugiada delle Nuvole di Asgard', 'Acqua fluttuante condensata sulla polena drago.', '/images/chapter1/items/dew.png'),
('CHIAVE_DRAGO', 'Chiave del Drago', 'Una pesante chiave d''ottone forgiata a forma di testa di drago.', '/images/chapter1/items/key.png'),
('SOFFIO_HRAESVELGR', 'Soffio di Hræsvelgr', 'Una folata d''aria gelida e tempestosa strappata all''aquila gigante.', '/images/chapter1/items/breath.png');

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
-- Flag indipendente per lo scontro con Ymir. Aggiunta non distruttiva.
ALTER TABLE save_slots ADD COLUMN IF NOT EXISTS is_ymir_pacified BOOLEAN DEFAULT FALSE;
-- Nuovi progressi del capitolo 3: tutte le colonne sono aggiunte senza
-- cancellare o sovrascrivere slot, inventari o salvataggi già presenti.
ALTER TABLE save_slots ADD COLUMN IF NOT EXISTS ymir_rune_seed INT DEFAULT 0;
ALTER TABLE save_slots ADD COLUMN IF NOT EXISTS gunnar_wisdom BOOLEAN DEFAULT FALSE;
-- Migrazione non distruttiva: persiste la prima tappa della conversazione di Gunnar.
ALTER TABLE save_slots ADD COLUMN IF NOT EXISTS gunnar_intro_heard BOOLEAN DEFAULT FALSE;
ALTER TABLE save_slots ADD COLUMN IF NOT EXISTS ymir_stele_read BOOLEAN DEFAULT FALSE;
ALTER TABLE save_slots ADD COLUMN IF NOT EXISTS ymir_old_fashioned_created BOOLEAN DEFAULT FALSE;
-- Epilogo del capitolo IV: migrazione non distruttiva.
ALTER TABLE save_slots ADD COLUMN IF NOT EXISTS ragnarok_flambe_created BOOLEAN DEFAULT FALSE;

-- Catalogo oggetti del Capitolo 2.
MERGE INTO items KEY(id) VALUES
('DIARIO_CAPITANO', 'Diario del Capitano', 'Un diario con il codice per decifrare le strategie di Rán.', '/images/chapter2/items/captain_journal.png'),
('CORALLO_RAN', 'Corallo di Rán', 'Un ramo di corallo aspro rosso sangue incastrato nelle rocce.', '/images/chapter2/items/coral.png'),
('INCHIOSTRO_KRAKEN', 'Inchiostro di Kraken', 'Una boccetta di liquido buio e denso depositata nell''anfratto.', '/images/chapter2/items/ink.png'),
('SALE_JORMUNGANDR', 'Sale di Jörmungandr', 'Cristalli lucenti di squama salmastra staccati dalle pareti.', '/images/chapter2/items/salt.png'),
('ACQUA_LETE', 'Acqua del Lete', 'Una linfa trasparente e stranamente immobile.', '/images/chapter2/items/lete.png'),
('SCHIUMA_AEGIR', 'Schiuma di Ægir', 'La birra sottomarina lucente estratta dalle reti della dea Rán.', '/images/chapter2/items/foam.png');

-- Oggetti del Capitolo 3. MERGE conserva gli slot preesistenti e rende
-- riconoscibili dal DB gli oggetti raccolti durante il nuovo capitolo.
MERGE INTO items KEY(id) VALUES
('MUSCHIO_MIDGARD', 'Muschio di Midgard', 'Un lichene brillante che emana un profumo amarognolo di terra pulita.', '/images/chapter3/items/moss.png'),
('RADICE_NIDHOGG', 'Radice Nera di Nidhogg', 'Una radice nerastra e pulsante, attraversata da vene cremisi.', '/images/chapter3/items/nidhogg.png'),
('FRAMMENTO_YGGDRASIL', 'Frammento di Yggdrasil', 'Una scheggia di legno millenario che emette una debole luce dorata.', '/images/chapter3/items/fragment_yggdrasil.png'),
('POLVERE_OSSA_GIGANTE', 'Polvere di Ossa di Gigante', 'Una polvere biancastra di ossa antiche, straordinariamente pesante.', '/images/chapter3/items/bone.png'),
('LACRIMA_YMIR', 'Lacrima di Ymir', 'Una lacrima cristallina del gigante primordiale, intrisa della memoria della terra.', '/images/chapter3/items/ymircry.png');

-- Inventario associato a ciascun salvataggio
CREATE TABLE IF NOT EXISTS save_inventory (
    slot_id INT NOT NULL,
    item_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (slot_id, item_id),
    FOREIGN KEY (slot_id) REFERENCES save_slots(slot_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id)
);

-- Muspelheim: ingredienti veri e due esche del Ragnarok Flambe.
MERGE INTO items KEY(id) VALUES
('BRACE_MUSPELHEIM', 'Brace di Muspelheim', 'L''innesco inestinguibile strappato alla spada di Surtur.', '/images/chapter4/items/ember.png'),
('CENERE_FENRIR', 'Cenere di Fenrir', 'Polvere nera che assorbe la magia.', '/images/chapter4/items/fenrir_ash.png'),
('MAGMA_SVARTALFAHEIMR', 'Magma Filtrato di Svartalfaheimr', 'Liquore lavico incandescente.', '/images/chapter4/items/magma.png'),
('ZOLFO_LOKI', 'Zolfo Ingannatore di Loki', 'Polvere dorata che fa evaporare la miscela.', '/images/chapter4/items/sulfur.png'),
('LACRIMA_HEL', 'Lacrima di Ghiaccio di Hel', 'Cristallo gelido incompatibile con il magma.', '/images/chapter4/items/ice_tear.png');
