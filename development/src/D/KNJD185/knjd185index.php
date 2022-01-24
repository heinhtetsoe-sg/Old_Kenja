<?php

require_once('for_php7.php');

require_once('knjd185Model.inc');
require_once('knjd185Query.inc');

class knjd185Controller extends Controller {
    var $ModelClassName = "knjd185Model";
    var $ProgramID      = "KNJD185";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd185":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd185Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd185Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd185Ctl = new knjd185Controller;
?>
