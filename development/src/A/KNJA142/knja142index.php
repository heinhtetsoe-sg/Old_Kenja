<?php

require_once('for_php7.php');

require_once('knja142Model.inc');
require_once('knja142Query.inc');

class knja142Controller extends Controller {
    var $ModelClassName = "knja142Model";
    var $ProgramID      = "KNJA142";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja142":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja142Model();		//コントロールマスタの呼び出し
                    $this->callView("knja142Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knja142Ctl = new knja142Controller;
//var_dump($_REQUEST);
?>
