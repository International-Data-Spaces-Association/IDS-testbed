from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.asymmetric import rsa, ec
from cryptography.hazmat.primitives.serialization import Encoding, PrivateFormat, NoEncryption

from OpenSSL import crypto

def generate_ecdsa_key(key_curve):
    key_curve = key_curve.lower()
    if ('secp256r1' == key_curve):
        key = ec.generate_private_key(ec.SECP256R1(), default_backend())
    elif ('secp384r1' == key_curve):
        key = ec.generate_private_key(ec.SECP384R1(), default_backend())
    elif ('secp521r1' == key_curve):
        key = ec.generate_private_key(ec.SECP521R1(), default_backend())
    else:
        print('Unsupported key curve: ' + key_curve + '\n')
        return None

    key_pem = key.private_bytes(encoding=Encoding.PEM, format=PrivateFormat.TraditionalOpenSSL, encryption_algorithm=NoEncryption())
    return crypto.load_privatekey(crypto.FILETYPE_PEM, key_pem)
