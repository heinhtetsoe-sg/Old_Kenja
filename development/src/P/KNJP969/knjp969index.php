<?php

require_once('for_php7.php');

require_once('knjp969Model.inc');
require_once('knjp969Query.inc');

class knjp969Controller extends Controller {
    var $ModelClassName = "knjp969Model";
    var $ProgramID      = "KNJP969";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp969":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp969Model();      //コントロールマスタの呼び出し
                    $this->callView("knjp969Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp969Ctl = new knjp969Controller;
?>
