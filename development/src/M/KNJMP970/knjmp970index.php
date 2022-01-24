<?php

require_once('for_php7.php');

require_once('knjmp970Model.inc');
require_once('knjmp970Query.inc');

class knjmp970Controller extends Controller {
    var $ModelClassName = "knjmp970Model";
    var $ProgramID      = "KNJMP970";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp970":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp970Model();      //コントロールマスタの呼び出し
                    $this->callView("knjmp970Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp970Ctl = new knjmp970Controller;
?>
