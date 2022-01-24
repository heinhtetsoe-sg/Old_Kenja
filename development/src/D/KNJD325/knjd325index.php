<?php

require_once('for_php7.php');

require_once('knjd325Model.inc');
require_once('knjd325Query.inc');

class knjd325Controller extends Controller {
    var $ModelClassName = "knjd325Model";
    var $ProgramID      = "KNJD325";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd325":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd325Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd325Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd325Ctl = new knjd325Controller;
var_dump($_REQUEST);
?>
