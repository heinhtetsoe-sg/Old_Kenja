<?php

require_once('for_php7.php');

require_once('knjg030Model.inc');
require_once('knjg030Query.inc');

class knjg030Controller extends Controller {
    var $ModelClassName = "knjg030Model";
    var $ProgramID        = "KNJG030";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg030":								//メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
					$sessionInstance->knjg030Model();		//コントロールマスタの呼び出し
                    $this->callView("knjg030Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjg030Ctl = new knjg030Controller;
var_dump($_REQUEST);
?>
