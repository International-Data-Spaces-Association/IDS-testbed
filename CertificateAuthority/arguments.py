import argparse
import sys

def parse():
    # Main parser
    parser = argparse.ArgumentParser(description = 'Manage test PKI', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    subpasers = parser.add_subparsers(title='Available subcommands')

    # init parser
    parser_init = subpasers.add_parser('init', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser_init.set_defaults(command='init')

    # ca parser
    parser_ca = subpasers.add_parser('ca', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser_ca.set_defaults(command='ca')
    subparsers_ca = parser_ca.add_subparsers(title='Available subcommands')

    # ca list parser
    parser_ca_list = subparsers_ca.add_parser('list', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser_ca_list.set_defaults(subcommand='list')

    # ca create parser
    parser_ca_create = subparsers_ca.add_parser('create', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser_ca_create.set_defaults(subcommand='create')
    parser_ca_create.add_argument('--common-name', required=True, help='Name of the new CA')
    parser_ca_create.add_argument('--algo', default='secp521r1', choices=['rsa', 'secp256r1', 'secp384r1', 'secp521r1'], help='Key algorithm to use')
    parser_ca_create.add_argument('--bits', default=4096, type=int, help='Bits of the key')
    parser_ca_create.add_argument('--valid-days', default=3650, type=int, help='Validity time in days')
    parser_ca_create.add_argument('--hash', default='sha512', choices=['sha256', 'sha384', 'sha512'], help='Algorithm used for signing')
    parser_ca_create.add_argument('--country-name', help='Country name')
    parser_ca_create.add_argument('--state-name', help='State of province name')
    parser_ca_create.add_argument('--locality-name', help='Locality name')
    parser_ca_create.add_argument('--organization-name', help='Organization name')
    parser_ca_create.add_argument('--unit-name', help='Organizational unit name')
    parser_ca_create.add_argument('--email', help='Email address')

    # subca parser
    parser_subca = subpasers.add_parser('subca', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser_subca.set_defaults(command='subca')
    subparsers_subca = parser_subca.add_subparsers(title='Available subcommands')

    # subca list parser
    parser_subca_list = subparsers_subca.add_parser('list', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser_subca_list.set_defaults(subcommand='list')

    # subca create parser
    parser_subca_create = subparsers_subca.add_parser('create', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser_subca_create.set_defaults(subcommand='create')
    parser_subca_create.add_argument('--CA', required=True, help='CA to use for signing')
    parser_subca_create.add_argument('--common-name', required=True, help='Name of the new sub-CA')
    parser_subca_create.add_argument('--algo', default='secp384r1', choices=['rsa', 'secp256r1', 'secp384r1', 'secp521r1'], help='Key algorithm to use')
    parser_subca_create.add_argument('--bits', default=4096, type=int, help='Bits of the key')
    parser_subca_create.add_argument('--valid-days', default=365*5, type=int, help='Validity time in days')
    parser_subca_create.add_argument('--hash', default='sha512', choices=['sha256', 'sha384', 'sha512'], help='Algorithm used for signing')
    parser_subca_create.add_argument('--country-name', help='Country name')
    parser_subca_create.add_argument('--state-name', help='State of province name')
    parser_subca_create.add_argument('--locality-name', help='Locality name')
    parser_subca_create.add_argument('--organization-name', help='Organization name')
    parser_subca_create.add_argument('--unit-name', help='Organizational unit name')
    parser_subca_create.add_argument('--email', help='Email address')

    # cert parser
    parser_cert = subpasers.add_parser('cert', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser_cert.set_defaults(command='cert')
    subparsers_cert = parser_cert.add_subparsers(title='Available subcommands')

    # cert list parser
    parser_cert_list = subparsers_cert.add_parser('list', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser_cert_list.set_defaults(subcommand='list')

    # cert create parser
    parser_cert_create = subparsers_cert.add_parser('create', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser_cert_create.set_defaults(subcommand='create')
    parser_cert_create.add_argument('--subCA', required=True, help='sub-CA to use for signing')
    parser_cert_create.add_argument('--common-name', required=True, help='Name of the new device')
    parser_cert_create.add_argument('--algo', default='secp256r1', choices=['rsa', 'secp256r1', 'secp384r1', 'secp521r1'], help='Key algorithm to use')
    parser_cert_create.add_argument('--bits', default=4096, type=int, help='Bits of the key')
    parser_cert_create.add_argument('--valid-days', default=365*3, type=int, help='Validity time in days')
    parser_cert_create.add_argument('--hash', default='sha384', choices=['sha256', 'sha384', 'sha512'], help='Algorithm used for signing')
    parser_cert_create.add_argument('--country-name', help='Country name')
    parser_cert_create.add_argument('--state-name', help='State of province name')
    parser_cert_create.add_argument('--locality-name', help='Locality name')
    parser_cert_create.add_argument('--organization-name', help='Organization name')
    parser_cert_create.add_argument('--unit-name', help='Organizational unit name')
    parser_cert_create.add_argument('--email', help='Email address')
    parser_cert_create.add_argument('--server', action='store_true', help='Certificate is for Server')
    parser_cert_create.add_argument('--client', action='store_true', help='Certificate is for Client')

    # cert sign parser
    parser_cert_create = subparsers_cert.add_parser('sign', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser_cert_create.set_defaults(subcommand='sign')
    parser_cert_create.add_argument('--subCA', required=True, help='sub-CA to use for signing')
    parser_cert_create.add_argument('--common-name', required=True, help='Name of the new device')
    parser_cert_create.add_argument('--key-file', required=True, help='Path to file with public key for the new device in PEM format')
    parser_cert_create.add_argument('--valid-days', default=365*3, type=int, help='Validity time in days')
    parser_cert_create.add_argument('--hash', default='sha384', choices=['sha256', 'sha384', 'sha512'], help='Algorithm used for signing')
    parser_cert_create.add_argument('--country-name', help='Country name')
    parser_cert_create.add_argument('--state-name', help='State of province name')
    parser_cert_create.add_argument('--locality-name', help='Locality name')
    parser_cert_create.add_argument('--organization-name', help='Organization name')
    parser_cert_create.add_argument('--unit-name', help='Organizational unit name')
    parser_cert_create.add_argument('--email', help='Email address')
    parser_cert_create.add_argument('--server', action='store_true', help='Certificate is for Server')
    parser_cert_create.add_argument('--client', action='store_true', help='Certificate is for Client')

    # Print help if empty
    if len(sys.argv) < 2:
        parser.print_help()
        exit(1)

    # parse arguments
    args = parser.parse_args()
    return args
