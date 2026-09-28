package com.example.root.ffttest2;

import java.util.LinkedList;

/**
 * MoEManager implements a simple rule‑based Mixture‑of‑Experts buffer.
 * It keeps a buffer of the last BUFFER_SIZE results (true = success, false = failure).
 * The policy is:
 *   - Record every transmission result via {@link #recordResult(boolean)}.
 *   - {@link #shouldSend()} returns false (veto) if any entry in the buffer is false.
 *   - {@link #getStatusString()} provides a short status for UI display.
 * Future work can replace this with a learned model; the API remains the same.
 */
public class MoEManager {
    // Buffer size is defined in Constants
    private static final int MAX_SIZE = Constants.BUFFER_SIZE;
    private static final LinkedList<Boolean> results = new LinkedList<>();
    // Medium of the last failed transmission. When a transmission fails, a one-shot
    // "veto" is armed for that medium. The next routing decision that is about to use
    // the same medium consumes the veto and switches to the other one. It is only
    // armed again by a subsequent failure, preventing repeated flips.
    private static Constants.CommMedium lastFailedMedium = null;

    /**
     * Record the result of the latest transmission.
     * @param success true if transmission succeeded, false otherwise.
     */
    public static void recordResult(boolean success) {
        recordResult(success, null);
    }

    /**
     * Record the result of the latest transmission together with the medium used.
     * @param success true if transmission succeeded, false otherwise.
     * @param medium  the medium that was used for this transmission.
     */
    public static void recordResult(boolean success, Constants.CommMedium medium) {
        results.addLast(success);
        if (results.size() > MAX_SIZE) {
            results.removeFirst();
        }
        if (success) {
            lastFailedMedium = null;
        } else if (medium != null) {
            lastFailedMedium = medium;
        }
    }

    /**
     * Consume a pending veto for the given medium, if any. Returns true exactly once per
     * failed transmission and only when the router is about to use the medium that failed,
     * so it flips the medium a single time and then waits for a success before another flip.
     * @param medium the medium the router is about to use.
     * @return true if a veto was pending for this medium and has now been consumed.
     */
    public static boolean consumeVeto(Constants.CommMedium medium) {
        if (medium != null && medium == lastFailedMedium) {
            lastFailedMedium = null;
            return true;
        }
        return false;
    }

    /**
     * Determine whether the system should proceed with sending.
     * @return true if all buffered results are true (no veto), false otherwise.
     */
    public static boolean shouldSend() {
        // If any entry is false, veto.
        for (Boolean r : results) {
            if (!r) {
                return false;
            }
        }
        return true;
    }

    /**
     * Provide a concise status string for the UI.
     * Example: "Buffer: 7/10 – OK" or "Buffer: 10/10 – VETO".
     */
    public static String getStatusString() {
        int filled = results.size();
        boolean ok = shouldSend();
        return "Buffer: " + filled + "/" + MAX_SIZE + " – " + (ok ? "OK" : "VETO");
    }
}
