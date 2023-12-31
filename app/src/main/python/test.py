import numpy as np
from scipy import interpolate
from scipy import signal



ata_inverse_file = np.zeros((203, 203))
atb_file = np.zeros(203)


def parse_data(data_array, start=0):
    """
    The data array is a cyclic array: it has all the data in a cycle and an index
    to represent where the data starts. Now we will change it into vectors of
    accelerations and time.
    """
    data_array = np.array(data_array)
    length = len(data_array)
    observation = []
    non_None_values = 0
    for i in range(length):
        next_value = data_array[(start + i) % length]
        if next_value is None:
            continue
        non_None_values += 1
        observation.append(next_value)
    T = np.zeros(non_None_values)
    X = np.zeros(non_None_values)
    Y = np.zeros(non_None_values)
    Z = np.zeros(non_None_values)
    for i, value in enumerate(observation):
        value = value.replace("\r\n", "")
        values = value.split(",")
        #print(values)
        T[i] = float(values[3])
        X[i] = float(values[0])
        Y[i] = float(values[1])
        Z[i] = float(values[2])
    return T, X, Y, Z

def predict(observation, num_samples, head=0, debug=False):
    # Transform the raw data to a feature vector
    if debug:
        transformed_observation = add_features_to_data(observation)
    else:
        transformed_observation = add_features_to_data(parse_data(observation, start=head))
    # Here are some constants
    global ata_inverse_file
    global atb_file
    full_vector_prediction = ata_inverse_file @ atb_file
    average_speed = 4.82803
    unit_change = 3.6
    # Choose dominance of the average speed, now is 0.2
    modifier = num_samples**-0.2
    return full_vector_prediction @ transformed_observation * (1-modifier) * unit_change + modifier * average_speed

def add_features_to_data(observation: tuple):
    # TODO: This unpacking is suspicious, check how data is built. Might work though
    T, X, Y, Z = observation
    # Absolute values of accelerations
    A = acc_norm(X, Y, Z)
    D = np.abs(np.diff(A))
    # Dominant frequency in the accelerations size
    dom_freq = np.array([FFT(A, dt_sample=T[1]-T[0]), FFT(D, dt_sample=T[1]-T[0])])
    # Integral for velocity, using the first 0.5327 seconds as indicator (based on trian data)
    # integral_result = np.array([integrate_accelerations(0.5327 ,X, Y, Z)])
    # The difference of time from start to end
    delta_t = np.array([T[-1] - T[0]])
    # Combine all results
    return np.concatenate((A, dom_freq, delta_t))

def acc_norm(X, Y, Z):
    A = np.sqrt(X**2 + Y**2 + Z**2)
    return A

def FFT(signal, dt_sample=1/10, return_all=False, plot=False):
    # plot the FFT magnitudes
    frequencies = np.fft.fftfreq(len(signal))
    fourier = np.abs(np.fft.fft(signal - np.mean(signal)))
    dominant_frequency = frequencies[np.argmax(fourier)] / dt_sample

    if plot:
        plt.plot(frequencies, fourier)
        plt.ylabel("|FT|")
        plt.xlabel(f"f * {dt_sample}")
        plt.title("FFT")
        plt.show()

    if not return_all:
        return dominant_frequency

    return frequencies, fourier, dominant_frequency

# def integrate_accelerations(threshold, X, Y, Z, dt=0.1):
#     v_x = scipy.integrate.trapezoid(X[0:int(threshold/dt)], dx=dt)
#     v_y = scipy.integrate.trapezoid(Y[0:int(threshold/dt)], dx=dt)
#     v_z = scipy.integrate.trapezoid(Z[0:int(threshold/dt)], dx=dt)
#     v_final = np.linalg.norm([v_x, v_y, v_z])
#     return v_final

def train(observation: np.ndarray, label: float, head=0, debug=False):
    """
    ATA_inverse = the (A.T @ A)^-1 of the old data, saved in firebase
    ATb = the (A.T @ b) of the old data, saved in firebase
    observation = the new observation we just had (ndarray of windowSize strings)
    label = the GPS difference of distances from the start of sample to end (distance in km over the route needed)
    return: the (A.T @ A)^-1 of the new data, the (A.T @ b) of the new data and the full multiplication
    """
    # Transform the raw data to a feature vector
    global ata_inverse_file
    global atb_file
    if debug:
        transformed_observation = add_features_to_data(observation)
    else:
        transformed_observation = add_features_to_data(parse_data(observation, start=head))
    print("parse_data(observation, start=head)")
    print(parse_data(observation, start=head))
    print(transformed_observation)
    print(transformed_observation.shape)
    # Using the sherman-morrison formula, we solve the problem in O(n^2)
    ATA_v = ata_inverse_file @ transformed_observation
    quad = transformed_observation.T @ ata_inverse_file @ transformed_observation
    ATA_inverse_new = ata_inverse_file - np.outer(ATA_v, ATA_v) / quad
    # Also calculate the value of the new ATb
    print(f'{type(atb_file)=}')
    print(f'{type(transformed_observation)=}')
    machpela = label * transformed_observation
    print(f"{type(machpela[0])=}")
    print(f'{type(atb_file[0])=}')
    ATb_new = atb_file + machpela
    print('hi')
    ata_inverse_file = ATA_inverse_new
    atb_file = ATb_new
    return ATA_inverse_new, ATb_new, ATA_inverse_new @ ATb_new

def big_matrix():
    # print(scipy.__version__)
    A, x = np.random.rand(5, 5), np.random.rand(5)
    return A, x, A @ x

def randomMatVec(num_features):
    epsilon = 0.000001
    return epsilon * np.eye(num_features), np.ones(num_features)

def get_matrix(vec, i):
    global ata_inverse_file
    x = np.array(vec)
    ata_inverse_file[int(i)] = x

def get_atb(vec):
    global atb_file
    atb_file = np.array(vec)

