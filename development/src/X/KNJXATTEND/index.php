<?php

require_once('for_php7.php');

require_once('knjxattendModel.inc');
require_once('knjxattendQuery.inc');

class knjxattendController extends Controller {
    var $ModelClassName = "knjxattendModel";
    var $ProgramID      = "KNJXATTEND";

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
                    $this->callView("knjxattendForm1");
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
$knjxattendCtl = new knjxattendController;
?>
