<?php

require_once('for_php7.php');

require_once('knjl110bModel.inc');
require_once('knjl110bQuery.inc');

class knjl110bController extends Controller {
    var $ModelClassName = "knjl110bModel";
    var $ProgramID      = "KNJL110B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl110bForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjl110bCtl = new knjl110bController;
?>
