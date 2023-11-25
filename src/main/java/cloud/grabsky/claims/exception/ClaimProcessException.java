package cloud.grabsky.claims.exception;

import net.kyori.adventure.text.Component;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public final class ClaimProcessException extends RuntimeException {

    @Getter(AccessLevel.PUBLIC)
    private Component errorMessage;

}
