<?php

require_once('for_php7.php');

require_once('knjg105Model.inc');
require_once('knjg105Query.inc');

class knjg105Controller extends Controller {
    var $ModelClassName = "knjg105Model";
    var $ProgramID      = "KNJG105";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg105":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjg105Model();       //コントロールマスタの呼び出し
                    $this->callView("knjg105Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg105Ctl = new knjg105Controller;
?>
