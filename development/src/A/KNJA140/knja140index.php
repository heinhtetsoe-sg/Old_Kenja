<?php

require_once('for_php7.php');

require_once('knja140Model.inc');
require_once('knja140Query.inc');

class knja140Controller extends Controller {
    var $ModelClassName = "knja140Model";
    var $ProgramID      = "KNJA140";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja140":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja140Model();		//コントロールマスタの呼び出し
                    $this->callView("knja140Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knja140Ctl = new knja140Controller;
var_dump($_REQUEST);
?>
