<?php

require_once('for_php7.php');

require_once('knja310Model.inc');
require_once('knja310Query.inc');

class knja310Controller extends Controller {
    var $ModelClassName = "knja310Model";
    var $ProgramID      = "KNJA310";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja310":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja310Model();		//コントロールマスタの呼び出し
                    $this->callView("knja310Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knja310Ctl = new knja310Controller;
//var_dump($_REQUEST);
?>
