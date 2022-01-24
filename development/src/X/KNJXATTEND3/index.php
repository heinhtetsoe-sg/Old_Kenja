<?php

require_once('for_php7.php');

require_once('knjxattend3Model.inc');
require_once('knjxattend3Query.inc');

class knjxattend3Controller extends Controller {
    var $ModelClassName = "knjxattend3Model";
    var $ProgramID      = "KNJXATTEND3";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update2":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("detail");
                    break 1;
                case "":
                case "detail":        //詳細
                case "form1" :        //詳細
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    //$sessionInstance->getDetailModel();
                    $this->callView("knjxattend3Form1");
                    break 2;
                case "reset":
                   $sessionInstance->setCmd("detail");
                   break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxattend3Ctl = new knjxattend3Controller;
?>
