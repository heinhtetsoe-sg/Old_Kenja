<?php

require_once('for_php7.php');

require_once('knjl376qModel.inc');
require_once('knjl376qQuery.inc');

class knjl376qController extends Controller {
    var $ModelClassName = "knjl376qModel";
    var $ProgramID      = "KNJL376Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "search":
                    $this->callView("knjl376qForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("search");
                    break 1;
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
$knjl376qCtl = new knjl376qController;
?>
