<?php

require_once('for_php7.php');

require_once('knjxexp_guiModel.inc');
require_once('knjxexp_guiQuery.inc');

class knjxexp_guiController extends Controller {
    var $ModelClassName = "knjxexp_guiModel";
    var $ProgramID      = "KNJXEXP_GUI";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "right":
                    $this->callView("knjxSearch");
                    break 2;
                case "list":
                case "search":
                    $this->callView("knjxexp_guiForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("list");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxexp_guiCtl = new knjxexp_guiController;
//var_dump($_REQUEST);
?>
