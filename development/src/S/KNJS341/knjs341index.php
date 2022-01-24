<?php

require_once('for_php7.php');

require_once('knjs341Model.inc');
require_once('knjs341Query.inc');

class knjs341Controller extends Controller {
    var $ModelClassName = "knjs341Model";
    var $ProgramID      = "KNJS341";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main";
                    $this->callView("knjs341Form1");
                    break 2;
                case "knjs341":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjs341Model();        //コントロールマスタの呼び出し
                    $this->callView("knjs341Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs341Ctl = new knjs341Controller;
?>
