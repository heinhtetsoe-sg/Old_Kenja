<?php

require_once('for_php7.php');

require_once('knjb100Model.inc');
require_once('knjb100Query.inc');

class knjb100Controller extends Controller {
    var $ModelClassName = "knjb100Model";
    var $ProgramID      = "KNJB100";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb100":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb100Model();   //コントロールマスタの呼び出し
                    $this->callView("knjb100Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb100Ctl = new knjb100Controller;
?>
