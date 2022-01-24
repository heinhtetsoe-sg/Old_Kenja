<?php

require_once('for_php7.php');

require_once('knjl016dModel.inc');
require_once('knjl016dQuery.inc');

class knjl016dController extends Controller {
    var $ModelClassName = "knjl016dModel";
    var $ProgramID      = "KNJL016D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                    $this->callView("knjl016dForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl016dCtl = new knjl016dController;
//var_dump($_REQUEST);
?>
