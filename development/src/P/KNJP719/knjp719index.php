<?php

require_once('for_php7.php');

require_once('knjp719Model.inc');
require_once('knjp719Query.inc');

class knjp719Controller extends Controller {
    var $ModelClassName = "knjp719Model";
    var $ProgramID      = "KNJP719";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjp719Model();
                    $this->callView("knjp719Form1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "knjp719":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjp719Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp719Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp719Ctl = new knjp719Controller;
//var_dump($_REQUEST);
?>
