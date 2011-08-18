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
import org.exoplatform.social.extras.migraiton.MigrationException;

@Usage("migration commands")
@Man("Migration commande")
class migration extends org.crsh.jcr.command.JCRCommand
{

  @Usage("run migration")
  @Man("run")
  @Command
  public Object run() throws ScriptException {

    MigrationTool migrationTool = new MigrationTool();
    
    try {
      migrationTool.run("11x", "12x", session);
    }
    catch (MigrationException e) {
      return e.getMessage();
    }
  }
}