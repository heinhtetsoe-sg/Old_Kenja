<?php

require_once('for_php7.php');

require_once('knji093Model.inc');
require_once('knji093Query.inc');

class knji093Controller extends Controller {
    var $ModelClassName = "knji093Model";
    var $ProgramID      = "KNJI093";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "changeSchKind":
                case "knji093":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knji093Model();      //コントロールマスタの呼び出し
                    $this->callView("knji093Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knji093Ctl = new knji093Controller;
?>
