<?php

require_once('for_php7.php');

require_once('knja290Model.inc');
require_once('knja290Query.inc');

class knja290Controller extends Controller {
    var $ModelClassName = "knja290Model";
    var $ProgramID      = "KNJA290";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja290":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja290Model();		//コントロールマスタの呼び出し
                    $this->callView("knja290Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knja290Ctl = new knja290Controller;
var_dump($_REQUEST);
?>
