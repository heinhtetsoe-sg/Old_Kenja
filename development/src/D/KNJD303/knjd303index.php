<?php

require_once('for_php7.php');

require_once('knjd303Model.inc');
require_once('knjd303Query.inc');

class knjd303Controller extends Controller {
    var $ModelClassName = "knjd303Model";
    var $ProgramID      = "KNJD303";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd303":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd303Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd303Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd303Ctl = new knjd303Controller;
var_dump($_REQUEST);
?>
