<?php

require_once('for_php7.php');

require_once('knjl041oModel.inc');
require_once('knjl041oQuery.inc');

class knjl041oController extends Controller {
    var $ModelClassName = "knjl041oModel";
    var $ProgramID      = "KNJL041O";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                    $this->callView("knjl041oForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
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
$knjl041oCtl = new knjl041oController;
?>
