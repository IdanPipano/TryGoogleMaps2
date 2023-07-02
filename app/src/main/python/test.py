import numpy as np

def main():
    A = np.eye(3)
    return np.mean(A)


def will_it_work():
    A = np.eye(4)
    return np.mean(A)

def matrix_mean(A):
    return np.mean(A)