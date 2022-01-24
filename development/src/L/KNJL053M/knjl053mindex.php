<?php

require_once('for_php7.php');

require_once('knjl053mModel.inc');
require_once('knjl053mQuery.inc');

class knjl053mController extends Controller {
    var $ModelClassName = "knjl053mModel";
    var $ProgramID      = "KNJL053M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjl053mForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
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
$knjl053mCtl = new knjl053mController;
?>
