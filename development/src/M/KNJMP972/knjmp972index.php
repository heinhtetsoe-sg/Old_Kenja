<?php

require_once('for_php7.php');

require_once('knjmp972Model.inc');
require_once('knjmp972Query.inc');

class knjmp972Controller extends Controller {
    var $ModelClassName = "knjmp972Model";
    var $ProgramID      = "KNJMP972";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp972":                            //メニュー画面もしくはSUBMITした場合
                case "read":
                    $sessionInstance->knjmp972Model();      //コントロールマスタの呼び出し
                    $this->callView("knjmp972Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp972Ctl = new knjmp972Controller;
?>
