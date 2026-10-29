package io.github.kriolos.opos.quarkus;

import io.quarkus.runtime.annotations.RegisterResources;
import java.lang.reflect.Method;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeJNIAccess;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

/**
 * GraalVM Native Image Feature managing JNI registration and runtime class
 * initialization for hardware communication peripherals (nrjavaserial RXTX and
 * usb4java libusb).
 *
 * @author KriolOS
 */
@RegisterResources(globs = {
    "META-INF/usbdevices.xml",
    "javax.usb.properties",
    "native/**",
    "org/usb4java/**"
})
public final class HardwareJNIFeature implements Feature {

    private static final String[] JNI_HARDWARE_CLASSES = {
        // nrjavaserial (RXTX) JNI callbacks
        "gnu.io.RXTXPort",
        "gnu.io.RXTXVersion",
        "gnu.io.RXTXCommDriver",
        "gnu.io.CommPortIdentifier",
        "gnu.io.NativeResourceException",
        "gnu.io.SerialPortEvent",
        "gnu.io.UnsupportedCommOperationException",
        // usb4java (libusb) JNI callbacks and descriptors
        "org.usb4java.LibUsb",
        "org.usb4java.Loader",
        "org.usb4java.BufferUtils",
        "org.usb4java.Context",
        "org.usb4java.Device",
        "org.usb4java.DeviceHandle",
        "org.usb4java.DeviceList",
        "org.usb4java.DeviceDescriptor",
        "org.usb4java.ConfigDescriptor",
        "org.usb4java.Interface",
        "org.usb4java.InterfaceDescriptor",
        "org.usb4java.EndpointDescriptor",
        "org.usb4java.BosDescriptor",
        "org.usb4java.BosDevCapabilityDescriptor",
        "org.usb4java.SsEndpointCompanionDescriptor",
        "org.usb4java.SsUsbDeviceCapabilityDescriptor",
        "org.usb4java.Usb20ExtensionDescriptor",
        "org.usb4java.ContainerIdDescriptor",
        "org.usb4java.Transfer",
        "org.usb4java.IsoPacketDescriptor",
        "org.usb4java.HotplugCallbackHandle"
    };

    public HardwareJNIFeature() {
        // Required default constructor for GraalVM Feature SPI
    }

    @Override
    public String getDescription() {
        return "Hardware peripheral JNI access and runtime initialization (serial and USB)";
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        // Defer hardware native libraries so embedded .so/.dll extraction
        // and System.load() execute at runtime rather than image build time.
        RuntimeClassInitialization.initializeAtRunTime(
                "gnu.io",
                "org.usb4java",
                "javax.usb"
        );

        // Register hardware JNI & reflection classes, methods, and fields for native callbacks
        int registeredJniCount = 0;
        for (String className : JNI_HARDWARE_CLASSES) {
            Class<?> clazz = access.findClassByName(className);
            if (clazz != null) {
                registeredJniCount++;
                RuntimeReflection.register(clazz);
                RuntimeReflection.register(clazz.getDeclaredConstructors());
                RuntimeReflection.register(clazz.getDeclaredFields());
                RuntimeJNIAccess.register(clazz);
                RuntimeJNIAccess.register(clazz.getDeclaredConstructors());
                RuntimeJNIAccess.register(clazz.getDeclaredFields());
                for (Method method : clazz.getDeclaredMethods()) {
                    // Skip Windows-only JNA WinReg method in RXTXCommDriver when JNA WinReg is absent
                    if ("windowsGetSerialPortsFromRegistry".equals(method.getName())) {
                        continue;
                    }
                    RuntimeReflection.register(method);
                    RuntimeJNIAccess.register(method);
                }
            }
        }
        System.out.printf("[HardwareJNIFeature] Initialized %d native hardware JNI & reflection targets (nrjavaserial, usb4java).%n", registeredJniCount);
    }
}
