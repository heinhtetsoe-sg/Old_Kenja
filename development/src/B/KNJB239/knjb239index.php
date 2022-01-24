<?php

require_once('for_php7.php');

require_once('knjb239Model.inc');
require_once('knjb239Query.inc');

class knjb239Controller extends Controller {
    var $ModelClassName = "knjb239Model";
    var $ProgramID      = "KNJB239";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb239":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb239Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb239Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb239Ctl = new knjb239Controller;
?>
