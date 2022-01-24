<?php

require_once('for_php7.php');

require_once('knjb1300Model.inc');
require_once('knjb1300Query.inc');

class knjb1300Controller extends Controller {
    var $ModelClassName = "knjb1300Model";
    var $ProgramID      = "KNJB1300";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb1300":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb1300Model();   //コントロールマスタの呼び出し
                    $this->callView("knjb1300Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb1300Ctl = new knjb1300Controller;
?>
