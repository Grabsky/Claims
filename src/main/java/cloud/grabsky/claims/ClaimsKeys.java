package cloud.grabsky.claims;

import org.bukkit.NamespacedKey;

public class ClaimsKeys {
    public static NamespacedKey CLAIM_LEVEL;

    public ClaimsKeys(final Claims instance) {
        CLAIM_LEVEL = new NamespacedKey(instance, "claimLevel");
    }
}
