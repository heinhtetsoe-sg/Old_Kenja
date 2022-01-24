<?php

require_once('for_php7.php');

require_once('knjp803Model.inc');
require_once('knjp803Query.inc');

class knjp803Controller extends Controller {
    var $ModelClassName = "knjp803Model";
    var $ProgramID      = "KNJP803";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjp803Model();
                    $this->callView("knjp803Form1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "knjp803":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjp803Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp803Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp803Ctl = new knjp803Controller;
//var_dump($_REQUEST);
?>
