<?php

require_once('for_php7.php');

require_once('knji090Model.inc');
require_once('knji090Query.inc');

class knji090Controller extends Controller {
    var $ModelClassName = "knji090Model";
    var $ProgramID      = "KNJI090";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knji090":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knji090Model();		//コントロールマスタの呼び出し
                    $this->callView("knji090Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knji090Ctl = new knji090Controller;
var_dump($_REQUEST);
?>
