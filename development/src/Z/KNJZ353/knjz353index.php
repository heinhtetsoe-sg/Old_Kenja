<?php

require_once('for_php7.php');

require_once('knjz353Model.inc');
require_once('knjz353Query.inc');

class knjz353Controller extends Controller {
    var $ModelClassName = "knjz353Model";
    var $ProgramID      = "KNJZ353";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "update":         //出欠表示項目
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                case "change";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz353Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz353Ctl = new knjz353Controller;
?>
