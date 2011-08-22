import javax.jcr.SimpleCredentials;

import org.crsh.jcr.JCR;
import org.crsh.jcr.command.ContainerOpt;
import org.crsh.jcr.command.UserNameOpt;
import org.crsh.jcr.command.PasswordOpt
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Required;

import org.exoplatform.social.extras.migraiton.MigrationTool;
import org.exoplatform.social.extras.migraiton.MigrationException
import org.exoplatform.social.extras.migraiton.reader.NodeReader
import org.exoplatform.social.extras.migraiton.io.WriterContext
import org.exoplatform.social.extras.migraiton.writer.NodeWriter;

@Usage("Social migration tool")
class sm extends org.crsh.jcr.command.JCRCommand
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
          m.runIdentities(r, w, m)
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

    MigrationTool migrationTool = new MigrationTool();

    try {
      NodeReader reader = migrationTool.createReader("11x", session)
      NodeWriter writer = migrationTool.createWriter("12x", session)
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
  public Object init() {
    if (migrationContext == null) {
      migrationContext = new WriterContext();
    }
    return "Context initialized."
  }
  
}