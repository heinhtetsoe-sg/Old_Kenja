<?php

require_once('for_php7.php');

require_once('knjl073mModel.inc');
require_once('knjl073mQuery.inc');

class knjl073mController extends Controller {
    var $ModelClassName = "knjl073mModel";
    var $ProgramID      = "KNJL073M"; //学校マスタメンテの権限

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "chenge":
                case "reset":
                    $this->callView("knjl073mForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
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
$knjl073mCtl = new knjl073mController;
//var_dump($_REQUEST);
?>
