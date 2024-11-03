package graalvm.features;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.impl.RuntimeClassInitializationSupport;

import java.security.Security;

public class BouncyCastleFeature implements Feature {

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        System.out.println("[BouncyCastleFeature] Adding BouncyCastle support");
        RuntimeClassInitialization.initializeAtBuildTime("org.bouncycastle");
        RuntimeClassInitializationSupport rci = ImageSingletons.lookup(RuntimeClassInitializationSupport.class);
        rci.rerunInitialization("org.bouncycastle.jcajce.provider.drbg.DRBG$Default", "");
        rci.rerunInitialization("org.bouncycastle.jcajce.provider.drbg.DRBG$NonceAndIV", "");
        Security.addProvider(new BouncyCastleProvider());
    }

}
