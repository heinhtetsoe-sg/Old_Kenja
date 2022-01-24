<?php

require_once('for_php7.php');

require_once('knjb262Model.inc');
require_once('knjb262Query.inc');

class knjb262Controller extends Controller {
    var $ModelClassName = "knjb262Model";
    var $ProgramID      = "KNJB262";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb262":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb262Model();   //コントロールマスタの呼び出し
                    $this->callView("knjb262Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb262Ctl = new knjb262Controller;
?>
