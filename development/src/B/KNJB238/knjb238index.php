<?php

require_once('for_php7.php');

require_once('knjb238Model.inc');
require_once('knjb238Query.inc');

class knjb238Controller extends Controller {
    var $ModelClassName = "knjb238Model";
    var $ProgramID      = "KNJB238";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb238":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb238Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb238Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb238Ctl = new knjb238Controller;
?>
