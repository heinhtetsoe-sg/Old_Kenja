<?php

require_once('for_php7.php');

require_once('knjp375Model.inc');
require_once('knjp375Query.inc');

class knjp375Controller extends Controller {
    var $ModelClassName = "knjp375Model";
    var $ProgramID      = "KNJP375";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp375":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp375Model();      //コントロールマスタの呼び出し
                    $this->callView("knjp375Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp375Ctl = new knjp375Controller;
?>
