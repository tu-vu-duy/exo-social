
import org.crsh.jcr.JCR;
import org.crsh.jcr.command.ContainerOpt;
import org.crsh.jcr.command.UserNameOpt;
import org.crsh.jcr.command.PasswordOpt;
import org.crsh.cmdline.annotations.Man;
import org.crsh.cmdline.annotations.Usage;
import org.crsh.cmdline.annotations.Command;
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.annotations.Argument;
import org.crsh.cmdline.annotations.Required;
import org.crsh.command.InvocationContext;

import org.exoplatform.social.extras.migration.MigrationTool;
import org.exoplatform.social.extras.migration.MigrationException;
import org.exoplatform.social.extras.migration.io.WriterContext;

@Usage("Social migration tool")
public class sm extends org.crsh.jcr.command.JCRCommand
{

  @Usage("Run full migration")
  @Command
  public Object runall() throws ScriptException {

    return runCmd(
        {
          m, r, w ->
          m.runAll(r, w, migrationContext)
        }
    );

  }

  @Usage("Run identities migration")
  @Command
  public Object runidentities() throws ScriptException {

    return runCmd(
        {
          m, r, w ->
          m.runIdentities(r, w, migrationContext)
        }
    );

  }

  @Usage("Run profiles migration")
  @Command
  public Object runprofiles() throws ScriptException {

    return runCmd(
        {
          m, r, w ->
          m.runProfiles(r, w, migrationContext)
        }
    );

  }

  @Usage("Run spaces migration")
  @Command
  public Object runspaces() throws ScriptException {

    return runCmd(
        {
          m, r, w ->
          m.runSpaces(r, w, migrationContext)
        }
    );
    
  }

  @Usage("Run relationships migration")
  @Command
  public Object runrelationships() throws ScriptException {

    return runCmd(
        {
          m, r, w ->
          m.runRelationships(r, w, migrationContext)
        }
    );

  }

  @Usage("Run activities migration")
  @Command
  public Object runactivities() throws ScriptException {

    return runCmd(
        {
          m, r, w ->
          m.runActivities(r, w, migrationContext)
        }
    );
  }


  @Usage("Rollback migration")
  @Command
  public Object rollback() throws ScriptException {

    String done = runCmd(
        {
          m, r, w ->
          m.rollback(r, w, migrationContext)
        }
    );
    
    migrationContext = null;

    return done;

  }

  private String runCmd(def call) {

    long start = System.currentTimeMillis();

    if (migrationContext == null) {
      return "Context not initialized, please use 'sm init'.";
    }

    try {
      call(migrationTool, reader, writer);
    }
    catch (MigrationException e) {
      return e.getMessage();
    }

    long time = (System.currentTimeMillis() - start) / 1000;

    return "Operation done in ${time}s";

  }

  @Usage("Initialize")
  @Command
  public Object init(InvocationContext<Void, Void> context, @Option(names=["f","from"]) String from, @Option(names=["t","to"]) String to) {

    migrationTool = new MigrationTool();
    
    if (from == null || to == null) {
      return "You must specify --from (-f) and --to (-t) version";
    }

    reader = migrationTool.createReader(from, to, session)
    if (reader == null) {
      return "Unable to find reader for specified version."
    }

    writer = migrationTool.createWriter(from, to, session)

    if (writer == null) {
      return "Unable to find writer for specified version."
    }

    if (migrationContext == null) {
      migrationContext = new WriterContext(from, to);
    }
    return "Context initialized."
  }
  
}