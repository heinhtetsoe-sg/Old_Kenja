<?php

require_once('for_php7.php');

require_once('knja240Model.inc');
require_once('knja240Query.inc');

class knja240Controller extends Controller {
    var $ModelClassName = "knja240Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja240":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja240Model();		//コントロールマスタの呼び出し
                    $this->callView("knja240Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knja240Ctl = new knja240Controller;
var_dump($_REQUEST);
?>
