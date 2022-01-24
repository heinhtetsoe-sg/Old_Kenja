<?php

require_once('for_php7.php');

require_once('knjl021pModel.inc');
require_once('knjl021pQuery.inc');

class knjl021pController extends Controller {
    var $ModelClassName = "knjl021pModel";
    var $ProgramID      = "KNJL021P";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "keisan":
                    $this->callView("knjl021pForm1");
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
$knjl021pCtl = new knjl021pController;
?>
