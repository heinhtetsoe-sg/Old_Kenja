<?php

require_once('for_php7.php');

require_once('knjz235nModel.inc');
require_once('knjz235nQuery.inc');

class knjz235nController extends Controller {
    var $ModelClassName = "knjz235nModel";
    var $ProgramID      = "KNJZ235N";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "reset":
                case "changeKind":
                case "edit":
                    if($sessionInstance->pattern == 'D154N'){
                        $this->callView("knjz235nForm2");
                    } else {
                        $this->callView("knjz235nForm1");
                    }
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":    //前年度コピー
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz235nCtl = new knjz235nController;
?>
