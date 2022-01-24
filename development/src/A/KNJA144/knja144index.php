<?php

require_once('for_php7.php');

require_once('knja144Model.inc');
require_once('knja144Query.inc');

class knja144Controller extends Controller {
    var $ModelClassName = "knja144Model";
    var $ProgramID      = "KNJA144";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja144":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja144Model();		//コントロールマスタの呼び出し
                    $this->callView("knja144Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knja144Ctl = new knja144Controller;
//var_dump($_REQUEST);
?>
