<?php

require_once('for_php7.php');

require_once('knjl394qModel.inc');
require_once('knjl394qQuery.inc');

class knjl394qController extends Controller {
    var $ModelClassName = "knjl394qModel";
    var $ProgramID      = "KNJL394Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                    $this->callView("knjl394qForm1");
                    break 2;
                case "exec":
                    $this->callView("knjl394qForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjl394qCtl = new knjl394qController;
?>
