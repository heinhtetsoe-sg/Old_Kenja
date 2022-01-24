<?php

require_once('for_php7.php');

require_once('knjl110nModel.inc');
require_once('knjl110nQuery.inc');

class knjl110nController extends Controller {
    var $ModelClassName = "knjl110nModel";
    var $ProgramID      = "KNJL110N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl110nForm1");
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
$knjl110nCtl = new knjl110nController;
?>
