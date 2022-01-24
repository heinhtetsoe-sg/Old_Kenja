<?php

require_once('for_php7.php');

require_once('knjb243Model.inc');
require_once('knjb243Query.inc');

class knjb243Controller extends Controller {
    var $ModelClassName = "knjb243Model";
    var $ProgramID      = "KNJB243";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb243":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb243Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb243Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb243Ctl = new knjb243Controller;
?>
