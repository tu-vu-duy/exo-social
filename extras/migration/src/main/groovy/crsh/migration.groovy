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

@Usage("migration commands")
@Man("Migration commande")
class migration extends org.crsh.jcr.command.JCRCommand
{

  @Usage("run migration")
  @Man("run")
  @Command
  public Object runidentities() throws ScriptException {

    MigrationTool migrationTool = new MigrationTool();
    
    try {
      NodeReader reader = migrationTool.createReader("11x", session)
      NodeWriter writer = migrationTool.createWriter("12x", session)
      migrationTool.runIdentities(reader, writer, new WriterContext());
    }
    catch (MigrationException e) {
      return e.getMessage();
    }
  }

  @Usage("run migration")
  @Man("run")
  @Command
  public Object genusers() throws ScriptException {

    MigrationTool migrationTool = new MigrationTool();
    NodeWriter writer = migrationTool.createWriter("12x", session)
    writer.generateOrganization();

  }
}