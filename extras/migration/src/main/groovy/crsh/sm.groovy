
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
          m.runAll(r, w, ctx)
        }
    );

  }

  @Usage("Run identities migration")
  @Command
  public Object runidentities() throws ScriptException {

    //
    if (ctx?.isCompleted(WriterContext.DataType.IDENTITIES)) {
      return "Identities was already migrated";
    }

    //
    return runCmd(
        {
          m, r, w ->
          m.runIdentities(r, w, ctx)
        }
    );

  }

  @Usage("Run profiles migration")
  @Command
  public Object runprofiles() throws ScriptException {

    //
    if (ctx?.isCompleted(WriterContext.DataType.PROFILES)) {
      return "Profiles was already migrated";
    }

    //
    return runCmd(
        {
          m, r, w ->
          m.runProfiles(r, w, ctx)
        }
    );

  }

  @Usage("Run spaces migration")
  @Command
  public Object runspaces() throws ScriptException {

    //
    if (ctx?.isCompleted(WriterContext.DataType.SPACES)) {
      return "Spaces was already migrated";
    }

    //
    return runCmd(
        {
          m, r, w ->
          m.runSpaces(r, w, ctx)
        }
    );
    
  }

  @Usage("Run relationships migration")
  @Command
  public Object runrelationships() throws ScriptException {

    //
    if (ctx?.isCompleted(WriterContext.DataType.RELATIONSHIPS)) {
      return "Relationships was already migrated";
    }

    //
    return runCmd(
        {
          m, r, w ->
          m.runRelationships(r, w, ctx)
        }
    );

  }

  @Usage("Run activities migration")
  @Command
  public Object runactivities() throws ScriptException {

    //
    if (ctx?.isCompleted(WriterContext.DataType.ACTIVITIES)) {
      return "Activities was already migrated";
    }

    //
    return runCmd(
        {
          m, r, w ->
          m.runActivities(r, w, ctx)
        }
    );
  }


  @Usage("Rollback migration")
  @Command
  public Object rollback() throws ScriptException {

    //
    String done = runCmd(
        {
          m, r, w ->
          m.rollback(r, w, ctx)
        }
    );

    //
    ctx = null;
    return done;

  }

  private String runCmd(def call) {

    //
    long start = System.currentTimeMillis();
    if (ctx == null) {
      return "Context not initialized, please use 'sm init' or 'sm restore'.";
    }

    //
    try {
      call(migrationTool, reader, writer);
    }
    catch (MigrationException e) {
      return e.getMessage();
    }

    //
    long time = (System.currentTimeMillis() - start) / 1000;
    return "Operation done in ${time}s";

  }

  @Usage("Initialize context")
  @Command
  public Object init(InvocationContext<Void, Void> context, @Option(names=["f","from"]) String from, @Option(names=["t","to"]) String to) {

    //
    migrationTool = new MigrationTool();

    //
    if (from == null || to == null) {
      return "You must specify --from (-f) and --to (-t) version";
    }

    //
    reader = migrationTool.createReader(from, to, session)
    if (reader == null) {
      return "Unable to find reader for specified version."
    }

    //
    writer = migrationTool.createWriter(from, to, session)
    if (writer == null) {
      return "Unable to find writer for specified version."
    }

    //
    try {
      ctx = new WriterContext(session, from, to);
    }
    catch(MigrationException e) {
      return e.getMessage();
    }

    return "Context initialized."

  }

  @Usage("Restore context")
  @Command
  public Object restore() {

    //
    migrationTool = new MigrationTool();

    //
    try {
      ctx = new WriterContext(session);
      reader = migrationTool.createReader(ctx.getFrom(), ctx.getTo(), session);
      writer = migrationTool.createWriter(ctx.getFrom(), ctx.getTo(), session);
    }
    catch(MigrationException e) {
      return e.getMessage();
    }

    return "Context restored."

  }

  @Usage("Print context status")
  @Command
  public Object status() {

    //
    if (ctx == null) {
      return "Context not initialized, please use 'sm init'.";
    }

    //
    String
    status  = "Migration status :\n";
    status += "-[Versions]-----------------------\n";
    status += " - From version            | " + ctx.getFrom() + "\n";
    status += " - To version              | " + ctx.getTo() + "\n";
    status += "-[Completion]---------------------\n";
    status += " - Identities completed    | " + ctx.isCompleted(WriterContext.DataType.IDENTITIES) + "\n";
    status += " - Spaces completed        | " + ctx.isCompleted(WriterContext.DataType.SPACES) + "\n";
    status += " - Profiles completed      | " + ctx.isCompleted(WriterContext.DataType.PROFILES) + "\n";
    status += " - Relationships completed | " + ctx.isCompleted(WriterContext.DataType.RELATIONSHIPS) + "\n";
    status += " - Activities completed    | " + ctx.isCompleted(WriterContext.DataType.ACTIVITIES) + "\n";
    status += "-[Migrated]-----------------------\n";
    status += " - Identities migrated     | " + ctx.getDone(WriterContext.DataType.IDENTITIES) + "\n";
    status += " - Spaces migrated         | " + ctx.getDone(WriterContext.DataType.SPACES) + "\n";
    status += " - Profiles migrated       | " + ctx.getDone(WriterContext.DataType.PROFILES) + "\n";
    status += " - Relationships migrated  | " + ctx.getDone(WriterContext.DataType.RELATIONSHIPS) + "\n";
    status += " - Activities migrated     | " + ctx.getDone(WriterContext.DataType.ACTIVITIES) + "\n";

    //
    return status;

  }
  
}