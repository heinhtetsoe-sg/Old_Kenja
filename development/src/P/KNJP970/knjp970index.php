<?php

require_once('for_php7.php');

require_once('knjp970Model.inc');
require_once('knjp970Query.inc');

class knjp970Controller extends Controller {
    var $ModelClassName = "knjp970Model";
    var $ProgramID      = "KNJP970";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp970":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp970Model();      //コントロールマスタの呼び出し
                    $this->callView("knjp970Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp970Ctl = new knjp970Controller;
?>
