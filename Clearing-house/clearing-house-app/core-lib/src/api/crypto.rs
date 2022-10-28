use num_bigint::BigUint;
use ring::signature::KeyPair;
use biscuit::jwk::{AlgorithmParameters, JWKSet, CommonParameters};
use biscuit::Empty;

pub fn get_jwks(key_path: &str) -> Option<JWKSet<Empty>>{
    let keypair = biscuit::jws::Secret::rsa_keypair_from_file(key_path).unwrap();

    if let biscuit::jws::Secret::RsaKeyPair(a) = keypair{
        let pk_modulus = BigUint::from_bytes_be(a.as_ref().public_key().modulus().big_endian_without_leading_zero());
        let pk_e = BigUint::from_bytes_be(a.as_ref().public_key().exponent().big_endian_without_leading_zero());

        let params = biscuit::jwk::RSAKeyParameters{
            n: pk_modulus,
            e: pk_e,
            ..Default::default()
        };

        let mut common = CommonParameters::default();
        common.key_id = get_fingerprint(key_path);

        let jwk = biscuit::jwk::JWK::<Empty>{
            common,
            algorithm: AlgorithmParameters::RSA(params),
            additional: Empty::default(),
        };

        let jwks = biscuit::jwk::JWKSet::<Empty>{
            keys: vec!(jwk)
        };
        return Some(jwks)
    }
    None
}

pub fn get_fingerprint(key_path: &str) -> Option<String>{
    let keypair = biscuit::jws::Secret::rsa_keypair_from_file(key_path).unwrap();
    if let biscuit::jws::Secret::RsaKeyPair(a) = keypair {
        let pk_modulus = a.as_ref().public_key().modulus().big_endian_without_leading_zero().to_vec();
        let pk_e = a.as_ref().public_key().exponent().big_endian_without_leading_zero().to_vec();

        let pk = openssh_keys::PublicKey::from_rsa(pk_e, pk_modulus);
        return Some(pk.fingerprint())
    }
    None
}