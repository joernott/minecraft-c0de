package de.joernott.c0de.dimension;

import de.joernott.c0de.C0de;
import de.joernott.c0de.core.config.C0deConfig;
import de.joernott.c0de.core.sheet.DimensionTypeJson;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.IoSupplier;

/**
 * A data pack generated from the configuration. It contains the dimension type of the c0de dimension,
 * whose height depends on the setting {@code maxBuildHeight}. The pack is always enabled.
 */
public final class SettingsPack {
    public static final String ID = "c0de:settings";

    private SettingsPack() {}

    /** The source that provides the pack to the pack repository of the server. */
    public static RepositorySource source() {
        return consumer -> {
            Pack pack = create();
            if (pack != null) {
                consumer.accept(pack);
            }
        };
    }

    private static Pack create() {
        int maxBuildHeight = C0de.config().get(C0deConfig.MAX_BUILD_HEIGHT);
        //? if >=26.1 {
        /*String dimensionType = DimensionTypeJson.createWithAttributes(maxBuildHeight, true);
        *///?} elif >=1.21.11 {
        /*String dimensionType = DimensionTypeJson.createWithAttributes(maxBuildHeight);
        *///?} else {
        String dimensionType = DimensionTypeJson.create(maxBuildHeight);
        //?}
        Map<String, byte[]> files = Map.of(DimensionTypeJson.PATH, dimensionType.getBytes(StandardCharsets.UTF_8));
        PackLocationInfo location =
                new PackLocationInfo(ID, Component.literal("c0de settings"), PackSource.BUILT_IN, Optional.empty());
        Pack.ResourcesSupplier resources = new Pack.ResourcesSupplier() {
            //? if >=26.3 {
            /*@Override
            public net.minecraft.server.packs.PackMetadataResources openMetadata(PackLocationInfo info) {
                return new InMemoryPackResources(info, files);
            }

            @Override
            public java.util.stream.Stream<net.minecraft.server.packs.PackResources> openResources(
                    PackLocationInfo info, Pack.Metadata metadata) {
                return java.util.stream.Stream.of(new InMemoryPackResources(info, files));
            }
            *///?} else {
            @Override
            public net.minecraft.server.packs.PackResources openPrimary(PackLocationInfo info) {
                return new InMemoryPackResources(info, files);
            }

            @Override
            public net.minecraft.server.packs.PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                return new InMemoryPackResources(info, files);
            }
            //?}
        };
        return Pack.readMetaAndCreate(
                location, resources, PackType.SERVER_DATA, new PackSelectionConfig(true, Pack.Position.TOP, false));
    }

    /** Pack resources whose files are kept in memory. Paths start with the directory of the pack type, e.g. "data/". */
    //? if >=26.3 {
    /*private static final class InMemoryPackResources
            extends net.minecraft.server.packs.AbstractPackMetadataResources
            implements net.minecraft.server.packs.PackResources {
    *///?} else {
    private static final class InMemoryPackResources extends net.minecraft.server.packs.AbstractPackResources {
    //?}
        private final Map<String, byte[]> files;

        InMemoryPackResources(PackLocationInfo location, Map<String, byte[]> files) {
            super(location);
            this.files = files;
        }

        @Override
        public IoSupplier<InputStream> getRootResource(String... elements) {
            if (String.join("/", elements).equals("pack.mcmeta")) {
                // Since 1.21.9, packs declare the range of formats they support
                //? if >=1.21.9 {
                /*int major = SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).major();
                String formats = "\"min_format\":" + major + ",\"max_format\":" + major;
                *///?} else {
                String formats = "\"pack_format\":" + SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA);
                //?}
                String json = "{\"pack\":{" + formats + ",\"description\":\"Settings of the c0de mod\"}}";
                return () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        }

        @Override
        public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
            byte[] content = files.get(type.getDirectory() + "/" + location.getNamespace() + "/" + location.getPath());
            return content == null ? null : () -> new ByteArrayInputStream(content);
        }

        @Override
        public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
            String namespaceDirectory = type.getDirectory() + "/" + namespace + "/";
            String prefix = namespaceDirectory + path + "/";
            files.forEach((file, content) -> {
                if (file.startsWith(prefix)) {
                    output.accept(
                            ResourceLocation.fromNamespaceAndPath(namespace, file.substring(namespaceDirectory.length())),
                            () -> new ByteArrayInputStream(content));
                }
            });
        }

        @Override
        public Set<String> getNamespaces(PackType type) {
            Set<String> namespaces = new HashSet<>();
            for (String file : files.keySet()) {
                String[] parts = file.split("/");
                if (parts.length > 2 && parts[0].equals(type.getDirectory())) {
                    namespaces.add(parts[1]);
                }
            }
            return namespaces;
        }

        @Override
        public void close() {}
    }
}
