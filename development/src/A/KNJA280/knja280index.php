<?php

require_once('for_php7.php');

require_once('knja280Model.inc');
require_once('knja280Query.inc');

class knja280Controller extends Controller {
    var $ModelClassName = "knja280Model";
    var $ProgramID      = "KNJA280";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja280":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja280Model();		//コントロールマスタの呼び出し
                    $this->callView("knja280Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knja280Ctl = new knja280Controller;
var_dump($_REQUEST);
?>
