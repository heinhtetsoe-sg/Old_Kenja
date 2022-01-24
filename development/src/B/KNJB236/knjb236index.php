<?php

require_once('for_php7.php');

require_once('knjb236Model.inc');
require_once('knjb236Query.inc');

class knjb236Controller extends Controller {
    var $ModelClassName = "knjb236Model";
    var $ProgramID      = "KNJB236";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb236":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb236Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb236Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb236Ctl = new knjb236Controller;
?>
