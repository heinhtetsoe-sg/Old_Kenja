<?php

require_once('for_php7.php');

require_once('knja172Model.inc');
require_once('knja172Query.inc');

class knja172Controller extends Controller {
    var $ModelClassName = "knja172Model";
    var $ProgramID      = "KNJA172";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja172":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja172Model();       //コントロールマスタの呼び出し
                    $this->callView("knja172Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja172Ctl = new knja172Controller;
?>
