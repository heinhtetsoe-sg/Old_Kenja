<?php

require_once('for_php7.php');

require_once('knjl010oModel.inc');
require_once('knjl010oQuery.inc');

class knjl010oController extends Controller {
    var $ModelClassName = "knjl010oModel";
    var $ProgramID      = "KNJL010O";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "read2":
                case "back":
                case "next":
                case "reset":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl010oForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read2");
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
$knjl010oCtl = new knjl010oController;
?>
