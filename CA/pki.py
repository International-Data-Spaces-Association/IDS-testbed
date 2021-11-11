#!/usr/bin/env python3

import arguments
import init
import ca
import subca
import cert

if __name__ == '__main__':
    args = arguments.parse()

    if args.command == 'init':
        init.init()
    else:
        init.check_init()

    if args.command == 'ca':
        if args.subcommand == 'create':
            ca.create(args)
        elif args.subcommand == 'list':
            ca.list_cas()
    elif args.command == 'subca':
        if args.subcommand == 'create':
            subca.create(args)
        elif args.subcommand == 'list':
            subca.list_subcas()
    elif args.command == 'cert':
        if args.subcommand == 'create':
            cert.create(args)
        elif args.subcommand == 'sign':
            cert.sign(args)
        elif args.subcommand == 'list':
            cert.list_certs()
