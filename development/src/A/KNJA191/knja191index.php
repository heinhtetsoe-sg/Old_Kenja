<?php

require_once('for_php7.php');

require_once('knja191Model.inc');
require_once('knja191Query.inc');

class knja191Controller extends Controller {
    var $ModelClassName = "knja191Model";
    var $ProgramID      = "KNJA191";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja191":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja191Model();       //コントロールマスタの呼び出し
                    $this->callView("knja191Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja191Ctl = new knja191Controller;
?>
