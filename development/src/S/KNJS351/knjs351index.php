<?php

require_once('for_php7.php');

require_once('knjs351Model.inc');
require_once('knjs351Query.inc');

class knjs351Controller extends Controller {
    var $ModelClassName = "knjs351Model";
    var $ProgramID      = "KNJS351";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main";
                    $this->callView("knjs351Form1");
                    break 2;
                case "knjs351":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjs351Model();        //コントロールマスタの呼び出し
                    $this->callView("knjs351Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs351Ctl = new knjs351Controller;
?>
