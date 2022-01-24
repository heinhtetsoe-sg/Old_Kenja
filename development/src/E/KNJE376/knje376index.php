<?php

require_once('for_php7.php');

require_once('knje376Model.inc');
require_once('knje376Query.inc');

class knje376Controller extends Controller {
    var $ModelClassName = "knje376Model";
    var $ProgramID      = "KNJE376";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knje376Model();        //コントロールマスタの呼び出し
                    $this->callView("knje376Form1");
                    exit;
                case "change_grade":
                case "knje376":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knje376Model();        //コントロールマスタの呼び出し
                    $this->callView("knje376Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knje376Ctl = new knje376Controller;
//var_dump($_REQUEST);
?>
