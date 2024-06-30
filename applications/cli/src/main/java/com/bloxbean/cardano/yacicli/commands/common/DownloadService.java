package com.bloxbean.cardano.yacicli.commands.common;

import com.bloxbean.cardano.yacicli.localcluster.ClusterConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.bloxbean.cardano.yacicli.util.ConsoleWriter.*;

@Component
@RequiredArgsConstructor
public class DownloadService {
    private final static String NODE_DOWNLOAD_URL = "https://github.com/IntersectMBO/cardano-node/releases/download";
    private final static String YACI_STORE_DOWNLOAD_URL = "https://github.com/bloxbean/yaci-store/releases/download";
    private final static String OGMIOS_DOWNLOAD_URL = "https://github.com/CardanoSolutions/ogmios/releases/download";
    private final static String KUPO_DOWNLOAD_URL = "https://github.com/CardanoSolutions/kupo/releases/download";

    private final ClusterConfig clusterConfig;

    @Value("${node.version:#{null}}")
    private String nodeVersion;

    @Value("${node.url:#{null}}")
    private String nodeUrl;

    @Value("${yaci.store.version:#{null}}")
    private String yaciStoreVersion;

    @Value("${yaci.store.url:#{null}}")
    private String yaciStoreUrl;

    @Value("${ogmios.version:#{null}}")
    private String ogmiosVersion;

    @Value("${ogmios.url:#{null}}")
    private String ogmiosUrl;

    @Value("${kupo.version:#{null}}")
    private String kupoVersion;

    @Value("${kupo.url:#{null}}")
    private String kupoUrl;

    public boolean downloadNode(boolean overwrite) {
        String downloadPath = resolveNodeDownloadPath();

        if ( downloadPath == null) {
            writeLn(error("Download URL for cardano-node is not set. Please set the download URL in application.properties"));
            return false;
        }

        Path cardanoNode = Path.of(clusterConfig.getYaciCliHome(), "bin", "cardano-node");
        Path cardanoCLI = Path.of(clusterConfig.getYaciCliHome(), "bin", "cardano-cli");
        Path cardanoSubmitApi = Path.of(clusterConfig.getYaciCliHome(), "bin", "cardano-submit-api");

        if (cardanoNode.toFile().exists()) {
            if (!overwrite) {
                writeLn(info("cardano-node already exists in %s", cardanoNode.toFile().getAbsolutePath()));
                writeLn(info("Use --overwrite to overwrite the existing cardano-node"));
                return false;
            }
        }

        String targetDir = clusterConfig.getYaciCliHome();
        var downloadedFile = download("cardano-node", downloadPath, targetDir, "cardano-node.tar.gz");
        if (downloadedFile != null) {
            try {
                extractTarGz(downloadedFile.toFile().getAbsolutePath(), clusterConfig.getYaciCliHome());
                setExecutablePermission(cardanoNode.toFile().getAbsolutePath());
                setExecutablePermission(cardanoCLI.toFile().getAbsolutePath());
                setExecutablePermission(cardanoSubmitApi.toFile().getAbsolutePath());
                return true;
            } catch (IOException e) {
                writeLn(error("Error extracting cardano-node" + e.getMessage()));
            }
        } else {
            writeLn(error("Download failed for cardano-node"));
        }

        return false;
    }

    public boolean downloadYaciStore(boolean overwrite) {
        String downloadPath = resolveYaciStoreDownloadPath();

        if ( downloadPath == null) {
            writeLn(error("Download URL for yaci-store is not set. Please set the download URL in application.properties"));
            return false;
        }

        Path yaciStoreJar = Path.of(clusterConfig.getYaciStoreBinPath(), "yaci-store.jar");

        if (yaciStoreJar.toFile().exists()) {
            if (!overwrite) {
                writeLn(info("YaciStore Jar already exists in %s", yaciStoreJar.toFile().getAbsolutePath()));
                writeLn(info("Use --overwrite to overwrite the existing yaci-store"));
                return false;
            }
        }

        var downloadedFile = download("yaci-store", downloadPath, clusterConfig.getYaciStoreBinPath(), "yaci-store.jar");
        if (downloadedFile != null) {
            return true;
        } else {
            writeLn(error("Download failed for yaci-store"));
        }

        return false;
    }

    public boolean downloadOgmios(boolean overwrite) {
        if (!SystemUtils.IS_OS_LINUX) {
            writeLn(error("Ogmios is supported only on linux. Skipping!!!"));
            return false;
        }

        String downloadPath = resolveOgmiosDownloadPath();

        if ( downloadPath == null) {
            writeLn(error("Download URL for ogmios is not set. Please set the download URL in application.properties"));
            return false;
        }

        Path ogmiosExec = Path.of(clusterConfig.getOgmiosHome(), "bin", "ogmios");

        if (ogmiosExec.toFile().exists()) {
            if (!overwrite) {
                writeLn(info("ogmios already exists in %s", ogmiosExec.toFile().getAbsolutePath()));
                writeLn(info("Use --overwrite to overwrite the existing ogmios"));
                return false;
            }
        }

        String targetDir = clusterConfig.getOgmiosHome();
        var downloadedFile = download("ogmios", downloadPath, targetDir, "ogmios.zip");
        if (downloadedFile != null) {
            try {
                extractZip(downloadedFile.toFile().getAbsolutePath(), clusterConfig.getOgmiosHome());
                setExecutablePermission(ogmiosExec.toFile().getAbsolutePath());
                return true;
            } catch (IOException e) {
                writeLn(error("Error extracting ogmios" + e.getMessage()));
            }
        } else {
            writeLn(error("Download failed for ogmios"));
        }

        return false;
    }

    public boolean downloadKupo(boolean overwrite) {
        String downloadPath = resolveKupoDownloadPath();

        if ( downloadPath == null) {
            writeLn(error("Download URL for Kupo is not set. Please set the download URL in application.properties"));
            return false;
        }

        Path kupoExec = Path.of(clusterConfig.getKupoHome(), "bin", "kupo");

        if (kupoExec.toFile().exists()) {
            if (!overwrite) {
                writeLn(info("Kupo already exists in %s", kupoExec.toFile().getAbsolutePath()));
                writeLn(info("Use --overwrite to overwrite the existing kupo"));
                return false;
            }
        }

        String targetDir = clusterConfig.getKupoHome();
        var downloadedFile = download("kupo", downloadPath, targetDir, "kupo.tar.gz");
        if (downloadedFile != null) {
            try {
                extractTarGz(downloadedFile.toFile().getAbsolutePath(), clusterConfig.getKupoHome());
                setExecutablePermission(kupoExec.toFile().getAbsolutePath());
                return true;
            } catch (IOException e) {
                writeLn(error("Error extracting kupo" + e.getMessage()));
            }
        } else {
            writeLn(error("Download failed for kupo"));
        }

        return false;
    }

    private void setExecutablePermission(String path) {
        File file = new File(path);
        if (!file.exists()) return;

        Paths.get(path).toFile().setExecutable(true);
    }

    private Path download(String component, String downloadUrl, String targetDir, String targetFileName) {
        writeLn(infoLabel("Download", "Downloading %s from %s", component, downloadUrl));

        String fileUrl = downloadUrl; // Replace with your file URL

        try {
            URL url = new URL(fileUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            long fileSize = httpConn.getContentLengthLong();

            Path targetPath = Paths.get(targetDir, targetFileName);
            Files.createDirectories(targetPath.getParent());

            try (InputStream inputStream = httpConn.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(targetPath.toFile())) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;
                int percentCompleted;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    percentCompleted = (int) ((totalBytesRead * 100) / fileSize);
                    write("\rDownloading: " + percentCompleted + "%");
                }
                System.out.println();
                writeLn(success("Download complete for %s!", component));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return targetPath;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void extractTarGz(String filePath, String extractDir) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             BufferedInputStream bis = new BufferedInputStream(fis);
             GzipCompressorInputStream gzIn = new GzipCompressorInputStream(bis);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn)) {

            TarArchiveEntry entry;
            while ((entry = tarIn.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                Path entryDestination = Paths.get(extractDir, entry.getName());
                Files.createDirectories(entryDestination.getParent());

                try (OutputStream out = new FileOutputStream(entryDestination.toFile())) {
                    byte[] buffer = new byte[4096];
                    int len;

                    long bytesRead = 0;

                    writeLn(infoLabel("Extracting", "Extracting %s", clusterConfig.getYaciCliHome() + File.separator + entry.getName()));
                    while ((len = tarIn.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        bytesRead += len;
                    }
                }
            }
            System.out.println(); // Move to next line after extraction completes
        }
    }

    public void extractZip(String filePath, String extractDir) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ZipArchiveInputStream zipIn = new ZipArchiveInputStream(bis)) {

            ArchiveEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                Path entryDestination = Paths.get(extractDir, entry.getName());
                Files.createDirectories(entryDestination.getParent());

                try (OutputStream out = new FileOutputStream(entryDestination.toFile())) {
                    byte[] buffer = new byte[4096];
                    int len;

                    long bytesRead = 0;

                    writeLn(infoLabel("Extracting", "Extracting %s", clusterConfig.getYaciCliHome() + File.separator + entry.getName()));
                    while ((len = zipIn.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                        bytesRead += len;
                    }
                }
            }
            System.out.println(); // Move to next line after extraction completes
        }
    }

    private String resolveNodeDownloadPath() {
        if (!StringUtils.isEmpty(nodeUrl)) {
            return nodeUrl;
        }

        if (StringUtils.isEmpty(nodeVersion)) {
            writeLn(error("Node version is not set. Please set the node version (node.version) or node download url (node.url) in application.properties"));
            return null;
        }

        String osPrefix = null;
        if (SystemUtils.IS_OS_MAC) {
            osPrefix = "macos";
        } else if (SystemUtils.IS_OS_LINUX) {
            osPrefix = "linux";
        } else {
            writeLn(error("Unsupported OS : " + System.getProperty("os.name")));
        }

        if (osPrefix == null)
            return null;

        String url = NODE_DOWNLOAD_URL + "/" + nodeVersion + "/cardano-node-" + nodeVersion + "-" + osPrefix + ".tar.gz";
        return url;
    }

    private String resolveYaciStoreDownloadPath() {
        if (!StringUtils.isEmpty(yaciStoreUrl)) {
            return yaciStoreUrl;
        }

        if (StringUtils.isEmpty(yaciStoreVersion)) {
            writeLn(error("YaciStore version is not set. Please set the yaci-store version (yaci.store.version) or yaci-store download url (yaci.store.url) in application.properties"));
            return null;
        }

        String url = YACI_STORE_DOWNLOAD_URL + "/v" + yaciStoreVersion + "/yaci-store-all-" + yaciStoreVersion +".jar";
        return url;
    }

    private String resolveOgmiosDownloadPath() {
        if (!StringUtils.isEmpty(ogmiosUrl)) {
            return ogmiosUrl;
        }

        if (StringUtils.isEmpty(ogmiosVersion)) {
            writeLn(error("Ogmios version is not set. Please set the ogmios version (ogmios.version) or ogmios download url (ogmios.url) in application.properties"));
            return null;
        }

        String arch = System.getProperty("os.arch");
        String cpuArch = null;
        if (arch.startsWith("aarch") || arch.startsWith("arm")) {
            cpuArch = "aarch64";
        } else{
            cpuArch = "x86_64";
        }

        String url = OGMIOS_DOWNLOAD_URL + "/v" + ogmiosVersion + "/ogmios-v" + ogmiosVersion + "-" + cpuArch + "-linux.zip";
        return url;
    }

    private String resolveKupoDownloadPath() {
        if (!StringUtils.isEmpty(kupoUrl)) {
            return kupoUrl;
        }

        if (StringUtils.isEmpty(kupoVersion)) {
            writeLn(error("Kupo version is not set. Please set the kupo version (kupo.version) or kupo download url (kupo.url) in application.properties"));
            return null;
        }

        String osPrefix = null;
        if (SystemUtils.IS_OS_MAC) {
            osPrefix = "Darwin";
        } else if (SystemUtils.IS_OS_LINUX) {
            osPrefix = "Linux";
        } else {
            writeLn(error("Unsupported OS : " + System.getProperty("os.name")));
        }

        String arch = System.getProperty("os.arch");
        String cpuArch = null;
        if (arch.startsWith("aarch") || arch.startsWith("arm")) {
            cpuArch = "arm64";
        } else{
            cpuArch = "amd64";
        }

        String versionParts[] = kupoVersion.split("\\.");
        String trimmedVersionPath = kupoVersion;
        if (versionParts.length == 3)
             trimmedVersionPath = versionParts[0] + "." + versionParts[1];

        String url = KUPO_DOWNLOAD_URL + "/v" + trimmedVersionPath + "/kupo-" + kupoVersion + "-" + cpuArch + "-" + osPrefix + ".tar.gz";
        return url;
    }
}
