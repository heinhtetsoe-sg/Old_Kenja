<?php

require_once('for_php7.php');

require_once('knjc200Model.inc');
require_once('knjc200Query.inc');

class knjc200Controller extends Controller {
    var $ModelClassName = "knjc200Model";
    var $ProgramID      = "KNJC200";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjc200":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjc200Model();		//コントロールマスタの呼び出し
                    $this->callView("knjc200Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjc200Ctl = new knjc200Controller;
//var_dump($_REQUEST);
?>
