<?php

require_once('for_php7.php');

require_once('knjg103Model.inc');
require_once('knjg103Query.inc');

class knjg103Controller extends Controller {
    var $ModelClassName = "knjg103Model";
    var $ProgramID      = "KNJG103";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg103":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjg103Model();       //コントロールマスタの呼び出し
                    $this->callView("knjg103Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg103Ctl = new knjg103Controller;
?>
