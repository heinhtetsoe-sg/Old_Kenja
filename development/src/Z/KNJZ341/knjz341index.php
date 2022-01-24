<?php

require_once('for_php7.php');

require_once('knjz341Model.inc');
require_once('knjz341Query.inc');

class knjz341Controller extends Controller {
    var $ModelClassName = "knjz341Model";
    var $ProgramID      = "KNJZ341";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjz341":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjz341Model();		//コントロールマスタの呼び出し
                    $this->callView("knjz341Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjz341Ctl = new knjz341Controller;
var_dump($_REQUEST);
?>
