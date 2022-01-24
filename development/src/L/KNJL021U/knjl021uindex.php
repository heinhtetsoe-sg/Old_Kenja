<?php

require_once('for_php7.php');

require_once('knjl021uModel.inc');
require_once('knjl021uQuery.inc');

class knjl021uController extends Controller {
    var $ModelClassName = "knjl021uModel";
    var $ProgramID      = "KNJL021U";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "keisan":
                    $this->callView("knjl021uForm1");
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
$knjl021uCtl = new knjl021uController;
?>
