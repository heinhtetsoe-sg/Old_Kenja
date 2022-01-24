<?php

require_once('for_php7.php');

require_once('knjl021qModel.inc');
require_once('knjl021qQuery.inc');

class knjl021qController extends Controller {
    var $ModelClassName = "knjl021qModel";
    var $ProgramID      = "KNJL021Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "keisan":
                    $this->callView("knjl021qForm1");
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
$knjl021qCtl = new knjl021qController;
?>
