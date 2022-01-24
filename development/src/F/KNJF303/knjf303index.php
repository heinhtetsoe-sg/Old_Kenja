<?php

require_once('for_php7.php');

require_once('knjf303Model.inc');
require_once('knjf303Query.inc');

class knjf303Controller extends Controller {
    var $ModelClassName = "knjf303Model";
    var $ProgramID      = "KNJF303";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "back":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjf303Model();       //コントロールマスタの呼び出し
                    $this->callView("knjf303Form1");
                    exit;
                case "main":
                case "change":
                case "reset":
                    $this->callView("knjf303Form1");
                    break 2;
                case "houkoku":
                    $sessionInstance->getUpdateEdboardModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("change");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf303Ctl = new knjf303Controller;
?>
