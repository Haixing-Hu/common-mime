#!/usr/bin/python

#
#     Copyright (c) 2022 - 2024.
#     Haixing Hu, Qubit Co. Ltd.
#
#     All rights reserved.
#

from math import sin, pi


def f(x):
    return sin(x ** 2)


def integrate_f(a, b, N):
    s = 0
    dx = (b - a) / N
    for i in range(N):
        s += f(a + i * dx)
    return s * dx


if __name__ == '__main__':
    print(integrate_f(0, pi, 1000))
