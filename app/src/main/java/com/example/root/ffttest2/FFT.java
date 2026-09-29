package com.example.root.ffttest2;

/**
 * Minimal in-place radix-2 FFT, used by the MoE noise analyzer.
 *
 * Only what the sensor pipeline needs: a forward complex transform with a
 * standard Hann window applied beforehand by the caller. Input length must be a
 * power of two; the noise analyzer sizes its window to guarantee that.
 */
final class FFT {

    private FFT() {}

    /**
     * In-place complex FFT.
     *
     * @param re real component, length must be a power of two
     * @param im imaginary component, same length as {@code re}
     */
    static void fft(double[] re, double[] im) {
        int n = re.length;
        if (n < 2 || im.length != n) {
            throw new IllegalArgumentException("FFT input lengths must match and be >= 2");
        }
        if ((n & (n - 1)) != 0) {
            // Fail loudly. Silently returning here would leave the caller reading
            // untransformed samples as if they were spectral magnitudes.
            throw new IllegalArgumentException("FFT length must be a power of two, got " + n);
        }

        // Bit-reversal permutation.
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >> 1;
            for (; (j & bit) != 0; bit >>= 1) {
                j ^= bit;
            }
            j ^= bit;
            if (i < j) {
                double tr = re[i]; re[i] = re[j]; re[j] = tr;
                double ti = im[i]; im[i] = im[j]; im[j] = ti;
            }
        }

        // Iterative Cooley-Tukey.
        for (int len = 2; len <= n; len <<= 1) {
            double ang = -2.0 * Math.PI / len;
            double wReal = Math.cos(ang);
            double wImag = Math.sin(ang);
            for (int i = 0; i < n; i += len) {
                double curReal = 1.0;
                double curImag = 0.0;
                for (int k = 0; k < len / 2; k++) {
                    int a = i + k;
                    int b = i + k + len / 2;
                    double tr = re[b] * curReal - im[b] * curImag;
                    double ti = re[b] * curImag + im[b] * curReal;
                    re[b] = re[a] - tr;
                    im[b] = im[a] - ti;
                    re[a] += tr;
                    im[a] += ti;
                    double nextReal = curReal * wReal - curImag * wImag;
                    curImag = curReal * wImag + curImag * wReal;
                    curReal = nextReal;
                }
            }
        }
    }

    /**
     * Apply a Hann window in place. Call before {@link #fft} to limit spectral
     * leakage from the abrupt buffer edges.
     */
    static void hann(double[] x) {
        int n = x.length;
        for (int i = 0; i < n; i++) {
            x[i] *= 0.5 * (1.0 - Math.cos(2.0 * Math.PI * i / (n - 1)));
        }
    }
}
