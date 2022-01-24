<?php

require_once('for_php7.php');

require_once('knjp967Model.inc');
require_once('knjp967Query.inc');

class knjp967Controller extends Controller {
    var $ModelClassName = "knjp967Model";
    var $ProgramID      = "KNJP967";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp967":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp967Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp967Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp967Ctl = new knjp967Controller;
?>
