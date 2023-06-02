CREATE TABLE certificates (
  serial_number            varbinary(128) NOT NULL,
  authority_key_identifier varbinary(128) NOT NULL,
  ca_label                 varbinary(128),
  status                   varbinary(128) NOT NULL,
  reason                   int,
  expiry                   timestamp DEFAULT '0000-00-00 00:00:00',
  revoked_at               timestamp DEFAULT '0000-00-00 00:00:00',
  pem                      varbinary(4096) NOT NULL,
  issued_at                timestamp DEFAULT '0000-00-00 00:00:00',
  not_before               timestamp DEFAULT '0000-00-00 00:00:00',
  metadata                 JSON,
  sans                     JSON,
  common_name              TEXT,
  PRIMARY KEY(serial_number, authority_key_identifier)
);

CREATE TABLE ocsp_responses (
  serial_number            varbinary(128) NOT NULL,
  authority_key_identifier varbinary(128) NOT NULL,
  body                     varbinary(4096) NOT NULL,
  expiry                   timestamp DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY(serial_number, authority_key_identifier)
);
