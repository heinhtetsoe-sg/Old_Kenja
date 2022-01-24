<?php

require_once('for_php7.php');

require_once('knjp310Model.inc');
require_once('knjp310Query.inc');

class knjp310Controller extends Controller {
    var $ModelClassName = "knjp310Model";
    var $ProgramID      = "KNJp310";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp310":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjp310Model();		//コントロールマスタの呼び出し
                    $this->callView("knjp310Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjp310Ctl = new knjp310Controller;
var_dump($_REQUEST);
?>
