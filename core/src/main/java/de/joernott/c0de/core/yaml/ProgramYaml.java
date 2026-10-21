package de.joernott.c0de.core.yaml;

import de.joernott.c0de.core.line.Sheet;
import java.util.Iterator;
import java.util.Map;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

/**
 * Programs as YAML files (docs/concept/17-yaml-format.md), the format of the c0de disk: the export of a sheet
 * and the layout of a program from a file on a sheet.
 */
public final class ProgramYaml {
    private ProgramYaml() {}

    /**
     * The program on a sheet as YAML.
     *
     * @throws IllegalArgumentException if the program is not structurally valid; the message names the line
     */
    public static String export(Sheet sheet) {
        DumpSettings settings = DumpSettings.builder()
                .setDefaultFlowStyle(FlowStyle.BLOCK)
                .setIndent(2)
                .setIndicatorIndent(2)
                .setIndentWithIndicator(true)
                .build();
        return new Dump(settings).dumpToString(YamlExport.export(sheet));
    }

    /**
     * Lays out the program of a YAML file on a sheet.
     *
     * @throws IllegalArgumentException if the file is not a valid program or doesn't fit on a sheet
     */
    public static Sheet layout(String yaml) {
        return YamlLayout.layout(root(yaml));
    }

    /**
     * The name of the program in a YAML file.
     *
     * @throws IllegalArgumentException if the file is not a program
     */
    public static String programName(String yaml) {
        Object name = root(yaml).get("PROGRAM");
        if (name == null) {
            throw new IllegalArgumentException("The file has no PROGRAM");
        }
        return String.valueOf(name);
    }

    private static Map<?, ?> root(String yaml) {
        try {
            Iterator<Object> documents = new Load(LoadSettings.builder().build())
                    .loadAllFromString(yaml)
                    .iterator();
            if (documents.hasNext() && documents.next() instanceof Map<?, ?> root) {
                return root;
            }
        } catch (YamlEngineException e) {
            throw new IllegalArgumentException("The file is not valid YAML: "
                    + (e.getMessage() == null
                            ? ""
                            : e.getMessage().lines().findFirst().orElse("")));
        }
        throw new IllegalArgumentException("The file is not a c0de program");
    }
}
