<?php

require_once('for_php7.php');

require_once('knjh440Model.inc');
require_once('knjh440Query.inc');

class knjh440Controller extends Controller {
    var $ModelClassName = "knjh440Model";
    var $ProgramID      = "KNJH440";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "update": //模試
                case "update2"://科目
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
                case "grade";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh440Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh440Ctl = new knjh440Controller;
//var_dump($_REQUEST);
?>
