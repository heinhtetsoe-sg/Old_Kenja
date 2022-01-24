<?php

require_once('for_php7.php');

require_once('knje368Model.inc');
require_once('knje368Query.inc');

class knje368Controller extends Controller {
    var $ModelClassName = "knje368Model";
    var $ProgramID      = "KNJE368";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje368":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje368Model();       //コントロールマスタの呼び出し
                    $this->callView("knje368Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje368Ctl = new knje368Controller;
?>
