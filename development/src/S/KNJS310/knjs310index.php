<?php

require_once('for_php7.php');

require_once('knjs310Model.inc');
require_once('knjs310Query.inc');

class knjs310Controller extends Controller {
    var $ModelClassName = "knjs310Model";
    var $ProgramID      = "KNJS310";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjs310":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjs310Model();       //コントロールマスタの呼び出し
                    $this->callView("knjs310Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs310Ctl = new knjs310Controller;
?>
