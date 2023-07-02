import numpy as np
import scipy

def main():
    A = np.eye(3)
    return np.mean(A)


def will_it_work():
    A = np.eye(4)
    return np.mean(A)

def matrix_mean(A):
    return np.mean(A)

def str_matrix_sum(A, head):
    sum = head
    for a in A:
        if a != None:
            sum += np.sum([int(s) for s in a.split(',')])
    return sum


def parse_data(data_array, start=0):
  """
  The data array is a cyclic array: it has all the data in a cycle and an index
  to represent where the data starts. Now we will change it into vectors of
  accelerations and time.
  """
  length = len(data_array)
  observation = []
  for i in range(length):
    observation.append(data_array[(i + start) % length])
  T = np.zeros(length)
  X = np.zeros(length)
  Y = np.zeros(length)
  Z = np.zeros(length)
  for i, value in enumerate(observation):
    value = value.replace("\r\n", "")
    values = value.split(",")
    print(values)
    T[i] = float(values[3])
    X[i] = float(values[0])
    Y[i] = float(values[1])
    Z[i] = float(values[2])
  return T, X, Y, Z