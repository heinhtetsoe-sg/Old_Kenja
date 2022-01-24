<?php

require_once('for_php7.php');

require_once('knjf050Model.inc');
require_once('knjf050Query.inc');

class knjf050Controller extends Controller {
    var $ModelClassName = "knjf050Model";
    var $ProgramID      = "KNJF050";

    function main(){
        $sessionInstance =& Model::getModel($this);
        while( true ){
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf050Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "total":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "reset":
                    $sessionInstance->setCmd("main");
                    $sessionInstance->flg="ok";
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    $sessionInstance->flg="ok";
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf050Ctl = new knjf050Controller;
?>