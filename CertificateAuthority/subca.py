from OpenSSL import crypto

import os

import ec

def create(args):
    if os.path.isfile(f'data/subca/{args.common_name}.crt'):
        print('A sub-CA with that common name already exists, aborting!')
        exit(1)

    try:
        # Load key and certificate of CA
        with open(f'data/ca/{args.CA}.key', 'rb') as f:
            ca_key = crypto.load_privatekey(crypto.FILETYPE_PEM, f.read())
        with open(f'data/ca/{args.CA}.crt', 'rb') as f:
            ca_crt = crypto.load_certificate(crypto.FILETYPE_PEM, f.read())
        
        # Load current serial
        with open(f'data/ca/{args.CA}.serial', 'rt') as f:
            serial = int(f.read())
            
    except FileNotFoundError:
        print("The given CA is invalid or broken!")
        exit(1)
    
    if args.algo == 'rsa':
        k = crypto.PKey()
        k.generate_key(crypto.TYPE_RSA, args.bits)
    else:
        k = ec.generate_ecdsa_key(args.algo)

    cert = crypto.X509()

    cert.set_version(2)

    if args.country_name:
        cert.get_subject().countryName = args.country_name
    if args.state_name:
        cert.get_subject().stateOrProvinceName = args.state_name
    if args.locality_name:
        cert.get_subject().localityName = args.locality_name
    if args.organization_name:
        cert.get_subject().organizationName = args.organization_name
    if args.unit_name:
        cert.get_subject().organizationalUnitName = args.unit_name
    if args.email:
        cert.get_subject().emailAddress = args.email

    cert.get_subject().commonName = args.common_name

    cert.set_serial_number(serial)

    cert.gmtime_adj_notBefore(0)
    cert.gmtime_adj_notAfter(86400 * args.valid_days)

    cert.set_issuer(ca_crt.get_subject())

    cert.set_pubkey(k)

    cert.add_extensions([
            crypto.X509Extension(b'basicConstraints', True, b'CA:TRUE, pathlen:0'),
            crypto.X509Extension(b'keyUsage', True, b'keyCertSign'),
            crypto.X509Extension(b'subjectKeyIdentifier', True, b'hash', subject=cert),
            crypto.X509Extension(b'authorityKeyIdentifier', True, b'keyid, issuer', issuer=ca_crt)
        ])

    cert.sign(ca_key, args.hash)

    cert_enc = crypto.dump_certificate(crypto.FILETYPE_PEM, cert)
    key_enc = crypto.dump_privatekey(crypto.FILETYPE_PEM, k)

    with open(f'data/subca/{args.common_name}.key', 'wb') as f:
        f.write(key_enc)
    with open(f'data/subca/{args.common_name}.crt', 'wb') as f:
        f.write(cert_enc)
    with open(f'data/subca/{args.common_name}.serial', 'wt') as f:
        f.write('1')

    # Save serial from CA
    with open(f'data/ca/{args.CA}.serial', 'wt') as f:
        f.write(str(serial + 1))

def list_subcas():
    names = [os.path.splitext(i)[0] for i in os.listdir('data/subca/')]

    names_sorted = sorted(list(set(names)))

    for i in names_sorted:
        print(i)
