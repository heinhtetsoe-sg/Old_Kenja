<?php

require_once('for_php7.php');

require_once('knjh303Model.inc');
require_once('knjh303Query.inc');

class knjh303Controller extends Controller {
    var $ModelClassName = "knjh303Model";
    var $ProgramID      = "KNJH303";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh303":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjh303Model();		//コントロールマスタの呼び出し
                    $this->callView("knjh303Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh303Ctl = new knjh303Controller;
?>
