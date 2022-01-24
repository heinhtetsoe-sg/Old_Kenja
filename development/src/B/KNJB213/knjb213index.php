<?php

require_once('for_php7.php');

require_once('knjb213Model.inc');
require_once('knjb213Query.inc');

class knjb213Controller extends Controller {
    var $ModelClassName = "knjb213Model";
    var $ProgramID      = "KNJB213";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb213":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb213Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb213Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb213Ctl = new knjb213Controller;
?>
