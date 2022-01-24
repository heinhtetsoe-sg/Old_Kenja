<?php

require_once('for_php7.php');

require_once('knjc020a_1Model.inc');
require_once('knjc020a_1Query.inc');

class knjc020a_1Controller extends Controller {
    var $ModelClassName = "knjc020a_1Model";
    var $ProgramID      = "KNJC020A_1";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "jmp":
                    $this->callView("knjc020a_1Form1");
                   break 2;
                case "execute":
                case "confirm":
                case "allupdate":
                case "update":
                    //$this->checkAuth(DEF_UPDATE_RESTRICT);
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
$knjc020a_1Ctl = new knjc020a_1Controller;
//var_dump($_REQUEST);
?>
