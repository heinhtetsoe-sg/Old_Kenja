<?php

require_once('for_php7.php');

require_once('knjl082qModel.inc');
require_once('knjl082qQuery.inc');

class knjl082qController extends Controller {
    var $ModelClassName = "knjl082qModel";
    var $ProgramID      = "KNJL082Q";
    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "main":
                case "clear";
                    $this->callView("knjl082qForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl082qCtl = new knjl082qController;
?>
