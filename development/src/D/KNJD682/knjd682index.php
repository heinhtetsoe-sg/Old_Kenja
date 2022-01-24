<?php

require_once('for_php7.php');

require_once('knjd682Model.inc');
require_once('knjd682Query.inc');

class knjd682Controller extends Controller {
    var $ModelClassName = "knjd682Model";
    var $ProgramID      = "KNJD682";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd682":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd682Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd682Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd682Ctl = new knjd682Controller;
?>
