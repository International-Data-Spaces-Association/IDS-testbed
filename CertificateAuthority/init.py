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
    open('data/.5660383bb674636060c3e2279e5a2139', 'w').close()


def check_init():
    if not os.path.isfile('data/.5660383bb674636060c3e2279e5a2139'):
        print("PKI structure is not initialized")
        exit(1)