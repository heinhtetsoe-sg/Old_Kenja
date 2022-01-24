<?php

require_once('for_php7.php');

require_once('knjg101Model.inc');
require_once('knjg101Query.inc');

class knjg101Controller extends Controller {
    var $ModelClassName = "knjg101Model";
    var $ProgramID      = "KNJG101";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg101":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjg101Model();       //コントロールマスタの呼び出し
                    $this->callView("knjg101Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg101Ctl = new knjg101Controller;
?>
