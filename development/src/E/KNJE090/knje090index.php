<?php

require_once('for_php7.php');

require_once('knje090Model.inc');
require_once('knje090Query.inc');

class knje090Controller extends Controller {
    var $ModelClassName = "knje090Model";
    var $ProgramID      = "KNJE090";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje090":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knje090Model();		//コントロールマスタの呼び出し
                    $this->callView("knje090Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knje090Ctl = new knje090Controller;
var_dump($_REQUEST);
?>
