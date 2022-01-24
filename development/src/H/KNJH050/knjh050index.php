<?php

require_once('for_php7.php');

require_once('knjh050Model.inc');
require_once('knjh050Query.inc');

class knjh050Controller extends Controller {
    var $ModelClassName = "knjh050Model";
    var $ProgramID        = "KNJH050";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh050":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjh050Model();		//コントロールマスタの呼び出し
                    $this->callView("knjh050Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjh050Ctl = new knjh050Controller;
var_dump($_REQUEST);
?>
