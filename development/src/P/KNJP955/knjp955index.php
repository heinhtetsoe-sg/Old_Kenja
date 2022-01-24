<?php

require_once('for_php7.php');

require_once('knjp955Model.inc');
require_once('knjp955Query.inc');

class knjp955Controller extends Controller {
    var $ModelClassName = "knjp955Model";
    var $ProgramID      = "KNJP955";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp955":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp955Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp955Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp955Ctl = new knjp955Controller;
?>
