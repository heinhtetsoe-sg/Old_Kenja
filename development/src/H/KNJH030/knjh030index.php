<?php

require_once('for_php7.php');

require_once('knjh030Model.inc');
require_once('knjh030Query.inc');

class knjh030Controller extends Controller {
    var $ModelClassName = "knjh030Model";
    var $ProgramID        = "KNJH030";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh030":								//メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
					$sessionInstance->knjh030Model();		//コントロールマスタの呼び出し
                    $this->callView("knjh030Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjh030Ctl = new knjh030Controller;
//var_dump($_REQUEST);
?>
