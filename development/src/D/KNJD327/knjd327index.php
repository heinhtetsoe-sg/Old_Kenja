<?php

require_once('for_php7.php');

require_once('knjd327Model.inc');
require_once('knjd327Query.inc');

class knjd327Controller extends Controller {
    var $ModelClassName = "knjd327Model";
    var $ProgramID      = "KNJD327";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd327":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd327Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd327Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd327Ctl = new knjd327Controller;
var_dump($_REQUEST);
?>
