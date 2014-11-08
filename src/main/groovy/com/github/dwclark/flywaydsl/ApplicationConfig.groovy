package com.github.dwclark.flywaydsl;

import org.apache.commons.cli.Option;

public class ApplicationConfig {
    
    public static final String PROMPT = '<prompt>';
    public static final String CONF = 'conf';
    public static final String RESOURCE = 'application.properties';
    public static final String STAGES = 'stages';
    public static final String ENVIRONMENTS = 'environments';
    public static final String CALLBACKS = 'callbacks';
    public static final String SCHEMAS = 'schemas';
    public static final String SCRIPT = 'script';

    final CliBuilder cli;

    //db.properties
    final String url;
    final String user;
    final String password;
    final String env;

    //command line
    final String action;
    final boolean help;

    //pre config
    final List<String> environments;
    final List<String> stages;
    final List<String> schemas;
    final List<String> callbacks;
    final String script;

    public ApplicationConfig(String[] args) {
        Properties preConfig = loadPreConfig();
        this.environments = toList(preConfig, ENVIRONMENTS);
        this.stages = toList(preConfig, STAGES);
        this.schemas = toList(preConfig, SCHEMAS);
        this.callbacks = toList(preConfig, CALLBACKS);
        this.script = preConfig[SCRIPT];

        this.cli = cliBuilder(args, script, stages);
        def accessor = cli.parse(args);
        this.help = accessor.h;
        if(this.help) {
            return;
        }
        else if(accessor.f) {
            Properties dbProperties = loadDbProperties(accessor.f);
            this.url = dbProperties.containsKey('url') ? dbProperties['url'] : null;
            this.user = dbProperties.containsKey('user') ? dbProperties['user'] : null;
            this.password = dbProperties.containsKey('password') ? dbProperties['password'] : null;
            this.env = dbProperties.containsKey('env') ? dbProperties['env'] : null;
            def extra = accessor.arguments();
            this.action = (extra && extra.size() > 0) ? extra[0] : null;
        }
        else {
            help = true;
        }
    }

    public static Properties loadDbProperties(String fileName) {
        Properties ret = new Properties();
        new File(fileName).withReader { reader -> ret.load(reader); };
        Console console = System.console();

        if(ret['user'] == PROMPT) {
            console.printf('Enter Database User: ');
            ret['user'] = console.readLine();
        }

        if(ret['password'] == PROMPT) {
            console.printf('Enter Database Password: ');
            ret['password'] = new String(console.readPassword());
        }
        
        return ret;
    }

    public static Properties loadPreConfig() {
        Properties ret = new Properties();
        String path = "${CONF}/${RESOURCE}";
        ApplicationConfig.classLoader.getResourceAsStream(path).withReader(MigrationStage.ENCODING) { reader -> ret.load(reader); };
        return ret;
    }

    public static List<String> toList(Properties props, String key) {
        if(props.containsKey(key)) {
            String val = props[key];
            List<String> ret = [];
            val.split(',').each { ret.add(it); }
            return ret;
        }
        else {
            return Collections.emptyList();
        }
    }

    public static String joinWithDifferentLast(List list, String others, String last) {
        String start = list.take( list.size() - 1 ).join( others )
        def end = list.drop( list.size() - 1 )[ 0 ]
        if(start) {
            [ start, last, end ].join()
        }
        else {
            end as String ?: ''
        }
    }

    public static String usage(String script, List<String> stages) {
        return "${script} -[hf] <" + joinWithDifferentLast(stages, ', ', ' or ') + ">";
    }
    
    public static CliBuilder cliBuilder(String[] args, String script, List<String> stages) {
        CliBuilder cliBuilder = new CliBuilder(usage: usage(script, stages));
        cliBuilder.h(longOpt: 'help', "Show usage information");
        cliBuilder.f(longOpt: 'file', "Properties file with connection information", args: 1, type: String, required: false);
        return cliBuilder;
    }
}