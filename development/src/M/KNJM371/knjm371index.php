<?php

require_once('for_php7.php');

require_once('knjm371Model.inc');
require_once('knjm371Query.inc');

class knjm371Controller extends Controller {
    var $ModelClassName = "knjm371Model";
    var $ProgramID      = "KNJM371";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm371":                            //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knjm371Model();      //コントロールマスタの呼び出し
                    $this->callView("knjm371Form1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm371Model();      //コントロールマスタの呼び出し
                    $this->callView("knjm371Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm371Ctl = new knjm371Controller;
?>
