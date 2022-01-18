import os
import shutil

def init():
    try:
        shutil.rmtree('data/')
    except FileNotFoundError:
        pass

    os.mkdir('data/')
    os.mkdir('data/ca')
    os.mkdir('data/subca')
    os.mkdir('data/cert')

def check_init():
    if not os.path.isdir('data/') or not os.path.isdir('data/ca') or not os.path.isdir('data/subca') or not os.path.isdir('data/cert'):
        print("PKI structure is not initialized")
        exit(1)
