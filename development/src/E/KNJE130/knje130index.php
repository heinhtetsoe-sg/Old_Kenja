<?php

require_once('for_php7.php');

require_once('knje130Model.inc');
require_once('knje130Query.inc');

class knje130Controller extends Controller {
    var $ModelClassName = "knje130Model";
    var $ProgramID      = "KNJE130";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje130":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knje130Model();		//コントロールマスタの呼び出し
                    $this->callView("knje130Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knje130Ctl = new knje130Controller;
var_dump($_REQUEST);
?>
