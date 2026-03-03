-- ================================================================
--  OCEAN VIEW RESORT  --  MS SQL Server Database Schema
--  Compatible: SQL Server 2016+ / Azure SQL
--  Run: sqlcmd -S localhost -i schema.sql  (as sa or dbo)
-- ================================================================

USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'OceanViewResort')
    CREATE DATABASE OceanViewResort
    COLLATE SQL_Latin1_General_CP1_CI_AS;
GO

USE OceanViewResort;
GO

-- ----------------------------------------------------------------
-- Clean reinstall guard
-- ----------------------------------------------------------------
IF OBJECT_ID('dbo.Reservations','U') IS NOT NULL DROP TABLE dbo.Reservations;
IF OBJECT_ID('dbo.Rooms',       'U') IS NOT NULL DROP TABLE dbo.Rooms;
IF OBJECT_ID('dbo.Users',       'U') IS NOT NULL DROP TABLE dbo.Users;
GO

-- ================================================================
-- TABLE: Users  (Staff + Admin accounts)
-- ================================================================
CREATE TABLE dbo.Users (
    id        INT            IDENTITY(1,1)  NOT NULL CONSTRAINT PK_Users PRIMARY KEY,
    fullName  NVARCHAR(100)  NOT NULL,
    username  NVARCHAR(50)   NOT NULL CONSTRAINT UQ_Users_Username UNIQUE,
    password  NVARCHAR(255)  NOT NULL,   -- SHA-256 hex string
    email     NVARCHAR(100)  NULL,
    role      NVARCHAR(20)   NOT NULL    CONSTRAINT DF_Users_Role   DEFAULT 'staff'
              CONSTRAINT CK_Users_Role   CHECK (role   IN ('admin','staff')),
    status    NVARCHAR(20)   NOT NULL    CONSTRAINT DF_Users_Status DEFAULT 'active'
              CONSTRAINT CK_Users_Status CHECK (status IN ('active','inactive')),
    createdAt DATETIME       NOT NULL    CONSTRAINT DF_Users_CreatedAt DEFAULT GETDATE()
);
GO

-- ================================================================
-- TABLE: Rooms
-- ================================================================
CREATE TABLE dbo.Rooms (
    id           NVARCHAR(10)   NOT NULL CONSTRAINT PK_Rooms PRIMARY KEY,
    roomNumber   NVARCHAR(10)   NOT NULL,
    roomType     NVARCHAR(60)   NOT NULL,
    ratePerNight DECIMAL(12,2)  NOT NULL CONSTRAINT CK_Rooms_Rate CHECK (ratePerNight > 0),
    status       NVARCHAR(20)   NOT NULL CONSTRAINT DF_Rooms_Status DEFAULT 'available'
                 CONSTRAINT CK_Rooms_Status CHECK (status IN ('available','reserved','occupied','maintenance')),
    description  NVARCHAR(500)  NULL
);
GO

-- ================================================================
-- TABLE: Reservations
-- ================================================================
CREATE TABLE dbo.Reservations (
    id           INT            IDENTITY(1,1) NOT NULL CONSTRAINT PK_Reservations PRIMARY KEY,
    resNo        NVARCHAR(30)   NOT NULL CONSTRAINT UQ_Res_ResNo UNIQUE,
    guestName    NVARCHAR(100)  NOT NULL,
    guestEmail   NVARCHAR(100)  NULL,
    guestContact NVARCHAR(50)   NULL,
    roomId       NVARCHAR(10)   NOT NULL CONSTRAINT FK_Res_Room REFERENCES dbo.Rooms(id),
    roomType     NVARCHAR(60)   NOT NULL,
    checkIn      DATE           NOT NULL,
    checkOut     DATE           NOT NULL,
    nights       INT            NOT NULL CONSTRAINT CK_Res_Nights CHECK (nights > 0),
    ratePerNight DECIMAL(12,2)  NOT NULL,
    subtotal     DECIMAL(12,2)  NOT NULL,
    tax          DECIMAL(12,2)  NOT NULL,
    total        DECIMAL(12,2)  NOT NULL,
    status       NVARCHAR(20)   NOT NULL CONSTRAINT DF_Res_Status DEFAULT 'confirmed'
                 CONSTRAINT CK_Res_Status CHECK (status IN ('confirmed','checked_in','checked_out','cancelled')),
    createdBy    INT            NULL CONSTRAINT FK_Res_User REFERENCES dbo.Users(id),
    createdAt    DATETIME       NOT NULL CONSTRAINT DF_Res_CreatedAt DEFAULT GETDATE()
);
GO

-- ================================================================
-- INDEXES
-- ================================================================
CREATE NONCLUSTERED INDEX IX_Res_ResNo    ON dbo.Reservations(resNo);
CREATE NONCLUSTERED INDEX IX_Res_Status   ON dbo.Reservations(status);
CREATE NONCLUSTERED INDEX IX_Res_RoomId   ON dbo.Reservations(roomId);
CREATE NONCLUSTERED INDEX IX_Res_CheckIn  ON dbo.Reservations(checkIn, checkOut);
CREATE NONCLUSTERED INDEX IX_Usr_Username ON dbo.Users(username);
GO

-- ================================================================
-- SEED: Users
-- password "admin123" -> SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a
-- password "staff123" -> SHA-256: 7c4a8d09ca3762af61e59520943dc26494f8941b4f4d75d12e6deae8a7de0e36
-- ================================================================
INSERT INTO dbo.Users (fullName, username, password, email, role, status) VALUES
('Arjun Perera',       'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a','arjun@oceanview.lk', 'admin','active'),
('Nadia Kumarasinghe', 'nadia', '7c4a8d09ca3762af61e59520943dc26494f8941b4f4d75d12e6deae8a7de0e36','nadia@oceanview.lk', 'staff','active'),
('Ravi Fernando',      'ravi',  '7c4a8d09ca3762af61e59520943dc26494f8941b4f4d75d12e6deae8a7de0e36','ravi@oceanview.lk',  'staff','active'),
('Saman Wijesekera',   'saman', '7c4a8d09ca3762af61e59520943dc26494f8941b4f4d75d12e6deae8a7de0e36','saman@oceanview.lk', 'staff','inactive');
GO

-- ================================================================
-- SEED: Rooms
-- ================================================================
INSERT INTO dbo.Rooms (id, roomNumber, roomType, ratePerNight, status, description) VALUES
('R101','101','Deluxe Ocean Suite',    8500.00, 'occupied',    'Spacious 55m2 suite with private balcony and ocean view'),
('R102','102','Premier Ocean View',    6500.00, 'available',   '38m2 room with floor-to-ceiling ocean view windows'),
('R103','103','Garden Standard Room',  4200.00, 'reserved',    'Cozy 28m2 room surrounded by lush tropical gardens'),
('R104','104','Family Retreat Suite', 12000.00, 'maintenance', '72m2 two-bedroom suite with pool access'),
('R105','105','Deluxe Ocean Suite',    8500.00, 'available',   'Spacious 55m2 suite with private balcony and ocean view'),
('R106','106','Premier Ocean View',    6500.00, 'occupied',    '38m2 room with floor-to-ceiling ocean view windows'),
('R107','107','Garden Standard Room',  4200.00, 'available',   'Cozy 28m2 room surrounded by lush tropical gardens'),
('R108','108','Presidential Villa',   28000.00, 'reserved',    '150m2 private villa with infinity pool and private dining'),
('R109','109','Premier Ocean View',    6500.00, 'available',   '38m2 room with floor-to-ceiling ocean view windows'),
('R110','110','Garden Standard Room',  4200.00, 'available',   'Cozy 28m2 room surrounded by lush tropical gardens'),
('R111','111','Deluxe Ocean Suite',    8500.00, 'maintenance', 'Spacious 55m2 suite with private balcony and ocean view'),
('R112','112','Family Retreat Suite', 12000.00, 'available',   '72m2 two-bedroom suite with pool access');
GO

-- ================================================================
-- SEED: Reservations
-- ================================================================
INSERT INTO dbo.Reservations
    (resNo, guestName, guestEmail, guestContact, roomId, roomType,
     checkIn, checkOut, nights, ratePerNight, subtotal, tax, total, status, createdBy)
VALUES
('RES-20250301-001','Sarah Chen',    'sarah.chen@email.com',   '+94 77 123 4567',  'R101','Deluxe Ocean Suite',
 '2025-03-01','2025-03-05',4,  8500.00, 34000.00,  4080.00,  38080.00,'checked_in', 1),
('RES-20250302-001','James Whitmore','jwhitmore@outlook.com',  '+1 555 987 6543',  'R106','Premier Ocean View',
 '2025-03-02','2025-03-06',4,  6500.00, 26000.00,  3120.00,  29120.00,'checked_in', 2),
('RES-20250303-001','Priya Nair',    'priya.nair@gmail.com',   '+91 98765 43210',  'R103','Garden Standard Room',
 '2025-03-04','2025-03-07',3,  4200.00, 12600.00,  1512.00,  14112.00,'confirmed',  2),
('RES-20250303-002','Marco Romano',  'm.romano@hotmail.com',   '+39 06 1234 5678', 'R108','Presidential Villa',
 '2025-03-05','2025-03-10',5, 28000.00,140000.00, 16800.00, 156800.00,'confirmed',  1),
('RES-20250228-001','Emma Okafor',   'emma.okafor@yahoo.com',  '+234 803 000 0001','R105','Deluxe Ocean Suite',
 '2025-02-28','2025-03-03',3,  8500.00, 25500.00,  3060.00,  28560.00,'checked_out',1),
('RES-20250301-002','Liam Hartley',  'l.hartley@domain.co.uk', '+44 20 1234 5678', 'R112','Family Retreat Suite',
 '2025-03-06','2025-03-09',3, 12000.00, 36000.00,  4320.00,  40320.00,'confirmed',  2);
GO

-- ================================================================
-- Verify
-- ================================================================
SELECT 'Users'        AS [Table], COUNT(*) AS [Rows] FROM dbo.Users        UNION ALL
SELECT 'Rooms',                   COUNT(*)           FROM dbo.Rooms         UNION ALL
SELECT 'Reservations',            COUNT(*)           FROM dbo.Reservations;
GO
PRINT '================================================================';
PRINT '  OceanViewResort schema ready.';
PRINT '  Login:  admin / admin123   |   nadia / staff123';
PRINT '================================================================';
GO
