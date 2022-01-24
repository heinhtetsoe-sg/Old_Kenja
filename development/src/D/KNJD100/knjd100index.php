<?php

require_once('for_php7.php');

require_once('knjd100Model.inc');
require_once('knjd100Query.inc');

class knjd100Controller extends Controller {
    var $ModelClassName = "knjd100Model";
    var $ProgramID      = "KNJD100";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd100":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd100Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd100Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd100Model();
                    $this->callView("knjd100Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd100Ctl = new knjd100Controller;
var_dump($_REQUEST);
?>
