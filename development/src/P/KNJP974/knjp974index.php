<?php

require_once('for_php7.php');

require_once('knjp974Model.inc');
require_once('knjp974Query.inc');

class knjp974Controller extends Controller {
    var $ModelClassName = "knjp974Model";
    var $ProgramID      = "KNJP974";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp974":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp974Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp974Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp974Ctl = new knjp974Controller;
?>
