<?php

require_once('for_php7.php');

require_once('knjl611a_1Model.inc');
require_once('knjl611a_1Query.inc');

class knjl611a_1Controller extends Controller {
    var $ModelClassName = "knjl611a_1Model";
    var $ProgramID      = "KNJL611A_1";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjl611a_1Form1");
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
$knjl611a_1Ctl = new knjl611a_1Controller;
?>
