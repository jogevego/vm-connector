package com.example;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.JSchException;
import java.security.Security;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Provider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

class CustomURLClassLoader extends URLClassLoader {

  public CustomURLClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }

  @Override
  public void addURL(URL url) {
    super.addURL(url);
  }
}

public class SshTunnelDemo {

    private static final String FIPS_BC_PROVIDER =
        "org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider";
    private static final String REGULAR_BC_PROVIDER =
      "org.bouncycastle.jce.provider.BouncyCastleProvider";
    private static final Set<String> PROVIDERS_TO_REMOVE =
        new HashSet<>(Arrays.asList("BC", "BCFIPS"));
     private static final CustomURLClassLoader CLASS_LOADER =
      new CustomURLClassLoader(new URL[] {}, ClassLoader.getSystemClassLoader());

      private static boolean loadCryptoProviderAtFirstPlace(String canonicalClassName) {
        return retrieveClassFromCurrentClassloader(canonicalClassName)
            .map(
                provider -> {
                    if (Security.getProviders().length > 1) {
                    Provider firstProvider = Security.getProviders()[0];
                    if (firstProvider != null && provider.isAssignableFrom(firstProvider.getClass())) {
                        System.out.println("Provider already initialized and at first position");
                        return true;
                    }
                    }
                    PROVIDERS_TO_REMOVE.forEach(Security::removeProvider);
                    Provider prov;
                    try {
                    prov = (Provider) provider.getDeclaredConstructor(null).newInstance();
                    } catch (ExceptionInInitializerError
                        | IllegalAccessException
                        | InstantiationException
                        | NoSuchMethodException
                        | InvocationTargetException e) {
                    System.err.println("Exception instancing provider: " + e);
                    return false;
                    }

                    if (1 != Security.insertProviderAt(prov, 1)) {
                    System.err.println("Error adding provider at position 1");
                    return false;
                    }
                    System.out.println("Provider initialized");
                    return true;
                })
            .orElse(false);
        }

        public static Optional<Class<?>> retrieveClassFromCurrentClassloader(String classCanonicalName) {
        try {
            return Optional.of(Class.forName(classCanonicalName, true, CLASS_LOADER));
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Could not find provider class: " + cnfe);
            System.out.println("It the mentioned class is not provider-related, check the target language level.");
            return Optional.empty();
        }
    }
    public static void main(String[] args) throws Exception {

        // Add BouncyCastleFipsProvider at the first position
        if (!loadCryptoProviderAtFirstPlace(FIPS_BC_PROVIDER)) {
            System.err.println("Failed to load BouncyCastleFipsProvider");
            return;
        }

        String user = ""; // your SSH username
        String host = ""; // your SSH server
        int port = 22;
        String privateKey = System.getProperty("user.home") + "/.ssh/id_ed25519";

        try {
            JSch.setLogger(new com.jcraft.jsch.Logger() {
                public boolean isEnabled(int level) { return true; }
                public void log(int level, String message) {
                    System.out.println("JSch Log [level " + level + "]: " + message);
                }
            });
            JSch jsch = new JSch();
            jsch.addIdentity(privateKey);

            Session session = jsch.getSession(user, host, port);
            session.setConfig("StrictHostKeyChecking", "no"); // for demo only

            System.out.println("Connecting to " + host + "...");
            session.connect(10000); // 10 seconds timeout

            System.out.println("Connected!");
            session.disconnect();
        } catch (JSchException e) {
            System.err.println("SSH connection failed: " + e.getMessage());
        }
    }
}