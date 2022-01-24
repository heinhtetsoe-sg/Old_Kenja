<?php

require_once('for_php7.php');

require_once('knjd658aModel.inc');
require_once('knjd658aQuery.inc');

class knjd658aController extends Controller {
    var $ModelClassName = "knjd658aModel";
    var $ProgramID      = "KNJD658A";
    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                    $this->callView("knjd658aForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
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
$knjd658aCtl = new knjd658aController;
//var_dump($_REQUEST);
?>
