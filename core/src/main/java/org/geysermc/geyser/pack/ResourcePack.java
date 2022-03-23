/*
 * Copyright (c) 2019-2022 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.geyser.pack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.geysermc.geyser.GeyserImpl;
import org.geysermc.geyser.text.MinecraftLocale;
import org.geysermc.geyser.util.FileUtils;
import org.geysermc.geyser.text.GeyserLocale;
import xyz.redsmarty.resourcepackconverter.api.ConversionAPI;
import xyz.redsmarty.resourcepackconverter.utils.InvalidResourcePackException;
import xyz.redsmarty.resourcepackconverter.utils.type.ConversionOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This represents a resource pack and all the data relevant to it
 */
public class ResourcePack {
    /**
     * Stores converted java packs SHA-1 hashes
     */
    public static final List<String> JAVA_HASHES = new ArrayList<>();

    /**
     * The list of loaded resource packs
     */
    public static final Map<String, ResourcePack> PACKS = new HashMap<>();

    /**
     * The size of each chunk to use when sending the resource packs to clients in bytes
     */
    public static final int CHUNK_SIZE = 102400;

    private byte[] sha256;
    private String sha1;
    private File file;
    private ResourcePackManifest manifest;
    private ResourcePackManifest.Version version;

    /**
     * Loop through the packs directory and locate valid resource pack files
     */
    public static void loadPacks() {
        Path packsDirectory = GeyserImpl.getInstance().getBootstrap().getConfigFolder().resolve("packs");
        if (!packsDirectory.toFile().exists()) {
            try {
                Files.createDirectories(packsDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        File cacheFile = GeyserImpl.getInstance().getBootstrap().getConfigFolder().resolve("packs/cache.json").toFile();
        if (cacheFile.exists()) {
            try {
                JsonNode root = GeyserImpl.JSON_MAPPER.readTree(cacheFile);
                JsonNode hashes = root.get("converted_pack_hashes");
                if (hashes.isArray()) {
                    for (JsonNode hash : hashes) {
                        if (hash.isTextual()) {
                            JAVA_HASHES.add(hash.asText());
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File bedrockPacks = packsDirectory.resolve("bedrock").toFile();

        if (bedrockPacks.exists()) {
            for (File bedrockPack: bedrockPacks.listFiles((file, name) -> name.endsWith(".zip") || name.endsWith(".mcpack"))) {
                loadBedrockPack(bedrockPack);
            }
        } else {
            bedrockPacks.mkdirs();
        }
        File javaPacks = packsDirectory.resolve("java").toFile();

        if (javaPacks.exists()) {
            for (File javaPack: javaPacks.listFiles((file, name) -> name.endsWith(".zip"))) {
                try {
                    String hash = MinecraftLocale.byteArrayToHexString(FileUtils.calculateSHA1(javaPack)).toUpperCase();
                    File bedrockFile = convert(new FileInputStream(javaPack), hash);
                    // We already have this pack in cache, we do not need to convert it
                    if (bedrockFile == null) continue;
                    loadBedrockPack(bedrockFile);
                } catch (IOException exception) {
                    GeyserLocale.getLocaleStringLog("geyser.resource_pack.conversion_failed", (Object) exception.getStackTrace());
                } catch (InvalidResourcePackException exception) {
                    GeyserLocale.getLocaleStringLog("geyser.resource_pack.conversion_failed", exception.getReason());
                }
            }
        } else {
            javaPacks.mkdirs();
        }
    }

    public static void loadBedrockPack(File file) {
        ResourcePack pack = new ResourcePack();

        pack.sha256 = FileUtils.calculateSHA256(file);
        pack.sha1 = MinecraftLocale.byteArrayToHexString(FileUtils.calculateSHA1(file));

        Stream<? extends ZipEntry> stream = null;
        try {
            ZipFile zip = new ZipFile(file);

            stream = zip.stream();
            stream.forEach((x) -> {
                if (x.getName().contains("manifest.json")) {
                    try {
                        ResourcePackManifest manifest = FileUtils.loadJson(zip.getInputStream(x), ResourcePackManifest.class);
                        // Sometimes a pack_manifest file is present and not in a valid format,
                        // but a manifest file is, so we null check through that one
                        if (manifest.getHeader().getUuid() != null) {
                            pack.file = file;
                            pack.manifest = manifest;
                            pack.version = ResourcePackManifest.Version.fromArray(manifest.getHeader().getVersion());

                            PACKS.put(pack.getManifest().getHeader().getUuid().toString(), pack);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            GeyserImpl.getInstance().getLogger().error(GeyserLocale.getLocaleStringLog("geyser.resource_pack.broken", file.getName()));
            e.printStackTrace();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public static void saveCache() {
        File cacheFile = GeyserImpl.getInstance().getBootstrap().getConfigFolder().resolve("packs/cache.json").toFile();

        ObjectNode root = GeyserImpl.JSON_MAPPER.createObjectNode();
        ArrayNode convertedPackHashes = root.putArray("converted_pack_hashes");
        for (String hash : JAVA_HASHES) {
            convertedPackHashes.add(hash);
        }
        try {
            GeyserImpl.JSON_MAPPER.writeValue(cacheFile, root);
        } catch (IOException e) {
            GeyserLocale.getLocaleStringLog("geyser.resource_pack.saving_cache_failed");
            e.printStackTrace();
        }
    }

    public byte[] getSha256() {
        return sha256;
    }

    public String getSha1() {
        return sha1;
    }

    public File getFile() {
        return file;
    }

    public ResourcePackManifest getManifest() {
        return manifest;
    }

    public ResourcePackManifest.Version getVersion() {
        return version;
    }

    /**
     * Converts the java resource pack to bedrock resource pack
     * @param pack Pack to convert
     * @param hash SHA1 hash of the pack
     * @throws IOException If URL is invalid/Error with writing files
     */
    public static File convert(InputStream pack, String hash) throws IOException, InvalidResourcePackException {
        hash = hash.toUpperCase();
        if (ResourcePack.JAVA_HASHES.contains(hash) || !GeyserImpl.getInstance().getConfig().isConvertResourcePack()) return null;
        File packsDirectory = GeyserImpl.getInstance().getBootstrap().getConfigFolder().resolve("packs/bedrock").toFile();
        if (!packsDirectory.exists()) packsDirectory.mkdirs();

        File bedrockResourcePackFile = new File(packsDirectory, hash + ".mcpack");

        File logFile = new File(GeyserImpl.getInstance().getBootstrap().getConfigFolder().resolve("packs").toFile(), "resource_pack_conversion.log");
        StringBuilder logs = new StringBuilder(new String(FileUtils.readAllBytes(logFile)));
        logs.append("**************************************************************************").append("\n");
        logs.append(String.format("Starting conversion of resource pack with hash %s", hash)).append("\n");
        logs.append("**************************************************************************").append("\n\n");

        String customModelDataMappings = ConversionAPI.getInstance().convert(pack, bedrockResourcePackFile, new ConversionOptions(hash, UUID.randomUUID(), new int[]{1, 0, 0}, hash, "geysermc", message -> {
            if (GeyserImpl.getInstance().getConfig().isDebugResourcePack()) {
                logs.append(message).append("\n");
            }
        })).getGeneratedMappings().getJson();

        ResourcePack.JAVA_HASHES.add(hash);
        FileUtils.writeFile(new File(GeyserImpl.getInstance().getBootstrap().getConfigFolder().resolve("packs/mappings").toFile(), hash + "_custom_model_data_mappings.json"), customModelDataMappings);
        logs.append("\n\n");
        if (GeyserImpl.getInstance().getConfig().isDebugResourcePack()) {
            FileUtils.appendFile(logFile, logs.toString());
        }

        return bedrockResourcePackFile;
    }
}
