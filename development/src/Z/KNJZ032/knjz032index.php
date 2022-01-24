<?php

require_once('for_php7.php');

require_once('knjz032Model.inc');
require_once('knjz032Query.inc');

class knjz032Controller extends Controller {
    var $ModelClassName = "knjz032Model";
    var $ProgramID      = "KNJZ032";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "hanei":
                case "clear":
                    $this->callView("knjz032Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjz032Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz032Ctl = new knjz032Controller;
?>
