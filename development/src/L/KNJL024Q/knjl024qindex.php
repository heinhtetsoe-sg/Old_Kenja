<?php

require_once('for_php7.php');

require_once('knjl024qModel.inc');
require_once('knjl024qQuery.inc');

class knjl024qController extends Controller {
    var $ModelClassName = "knjl024qModel";
    var $ProgramID      = "KNJL024Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "keisan":
                    $this->callView("knjl024qForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reset":
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl024qCtl = new knjl024qController;
?>
