<?php

require_once('for_php7.php');

require_once('knjb256Model.inc');
require_once('knjb256Query.inc');

class knjb256Controller extends Controller {
    var $ModelClassName = "knjb256Model";
    var $ProgramID      = "KNJB256";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb256":                             //メニュー画面もしくはSUBMITした場合
                case "change":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb256Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb256Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb256Ctl = new knjb256Controller;
?>
