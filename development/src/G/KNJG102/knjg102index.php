<?php

require_once('for_php7.php');

require_once('knjg102Model.inc');
require_once('knjg102Query.inc');

class knjg102Controller extends Controller {
    var $ModelClassName = "knjg102Model";
    var $ProgramID      = "KNJG102";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg102":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjg102Model();      //コントロールマスタの呼び出し
                    $this->callView("knjg102Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg102Ctl = new knjg102Controller;
?>
