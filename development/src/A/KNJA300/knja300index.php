<?php

require_once('for_php7.php');

require_once('knja300Model.inc');
require_once('knja300Query.inc');

class knja300Controller extends Controller {
    var $ModelClassName = "knja300Model";
    var $ProgramID      = "knja300";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja300":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja300Model();		//コントロールマスタの呼び出し
                    $this->callView("knja300Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knja300Ctl = new knja300Controller;
//var_dump($_REQUEST);
?>
