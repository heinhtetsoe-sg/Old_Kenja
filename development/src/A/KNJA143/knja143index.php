<?php

require_once('for_php7.php');

require_once('knja143Model.inc');
require_once('knja143Query.inc');

class knja143Controller extends Controller {
    var $ModelClassName = "knja143Model";
    var $ProgramID      = "KNJA143";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja143":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja143Model();		//コントロールマスタの呼び出し
                    $this->callView("knja143Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knja143Ctl = new knja143Controller;
//var_dump($_REQUEST);
?>
