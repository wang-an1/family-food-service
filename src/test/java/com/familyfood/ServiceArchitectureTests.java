package com.familyfood;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceArchitectureTests {
    private static final Path MAIN_SOURCE = Paths.get("src/main/java/com/familyfood");
    private static final Pattern SPRING_MANAGED = Pattern.compile(
            "@(Service|Component|RestController|RestControllerAdvice|Configuration)\\b");
    private static final Pattern PUBLIC_CLASS = Pattern.compile("public\\s+(?:class|record)\\s+(\\w+)");
    private static final Pattern IMPL_IMPORT = Pattern.compile(
            "(?m)^import\\s+com\\.familyfood\\..*\\.service\\.impl\\.");

    @Test
    void serviceContractsStayAsInterfacesAndImplementationsStayInImplPackages() throws IOException {
        List<String> failures = new ArrayList<>();
        for (Path file : javaFiles()) {
            String normalized = normalize(file);
            String source = Files.readString(file);
            if (normalized.contains("/service/impl/")) {
                if (!source.contains("@Service")) {
                    failures.add(normalized + " is missing @Service");
                }
                if (!file.getFileName().toString().endsWith("Impl.java")) {
                    failures.add(normalized + " must use *Impl.java naming");
                }
                continue;
            }
            if (normalized.contains("/service/") && !source.contains("public interface ")) {
                failures.add(normalized + " must be a service interface");
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    @Test
    void mainCodeDoesNotImportServiceImplementations() throws IOException {
        List<String> failures = new ArrayList<>();
        for (Path file : javaFiles()) {
            String normalized = normalize(file);
            if (normalized.contains("/service/impl/")) {
                continue;
            }
            if (IMPL_IMPORT.matcher(Files.readString(file)).find()) {
                failures.add(normalized + " imports service.impl");
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    @Test
    void springManagedConstructorsUseExplicitInjectionAnnotation() throws IOException {
        List<String> failures = new ArrayList<>();
        for (Path file : javaFiles()) {
            String source = Files.readString(file);
            if (!SPRING_MANAGED.matcher(source).find()) {
                continue;
            }
            Matcher classMatcher = PUBLIC_CLASS.matcher(source);
            if (!classMatcher.find()) {
                continue;
            }
            String className = classMatcher.group(1);
            List<String> lines = source.lines().toList();
            for (int i = 0; i < lines.size(); i++) {
                if (!lines.get(i).matches("\\s*public\\s+" + className + "\\(.*")) {
                    continue;
                }
                int previous = i - 1;
                while (previous >= 0 && lines.get(previous).isBlank()) {
                    previous--;
                }
                if (previous < 0 || !lines.get(previous).matches("\\s*@(Autowired|Resource)(\\b|\\().*")) {
                    failures.add(normalize(file) + ":" + (i + 1) + " constructor must use @Autowired or @Resource");
                }
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    private static List<Path> javaFiles() throws IOException {
        try (var stream = Files.walk(MAIN_SOURCE)) {
            return stream.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }

    private static String normalize(Path path) {
        return MAIN_SOURCE.relativize(path).toString().replace('\\', '/');
    }
}
