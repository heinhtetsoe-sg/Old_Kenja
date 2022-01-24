<?php

require_once('for_php7.php');

require_once('knjb252Model.inc');
require_once('knjb252Query.inc');

class knjb252Controller extends Controller {
    var $ModelClassName = "knjb252Model";
    var $ProgramID      = "KNJB252";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb252":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb252Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb252Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb252Ctl = new knjb252Controller;
?>
