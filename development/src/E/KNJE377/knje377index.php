<?php

require_once('for_php7.php');

require_once('knje377Model.inc');
require_once('knje377Query.inc');

class knje377Controller extends Controller {
    var $ModelClassName = "knje377Model";
    var $ProgramID      = "KNJE377";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje377":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje377Model();      //コントロールマスタの呼び出し
                    $this->callView("knje377Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje377Ctl = new knje377Controller;
?>
