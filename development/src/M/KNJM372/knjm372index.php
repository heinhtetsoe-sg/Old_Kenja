<?php

require_once('for_php7.php');

require_once('knjm372Model.inc');
require_once('knjm372Query.inc');

class knjm372Controller extends Controller {
    var $ModelClassName = "knjm372Model";
    var $ProgramID      = "KNJM372";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm372":                            //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knjm372Model();      //コントロールマスタの呼び出し
                    $this->callView("knjm372Form1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm372Model();      //コントロールマスタの呼び出し
                    $this->callView("knjm372Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm372Ctl = new knjm372Controller;
?>
