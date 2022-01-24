<?php

require_once('for_php7.php');

require_once('knjd323Model.inc');
require_once('knjd323Query.inc');

class knjd323Controller extends Controller {
    var $ModelClassName = "knjd323Model";
    var $ProgramID      = "KNJD323";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd323":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd323Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd323Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd323Ctl = new knjd323Controller;
var_dump($_REQUEST);
?>
