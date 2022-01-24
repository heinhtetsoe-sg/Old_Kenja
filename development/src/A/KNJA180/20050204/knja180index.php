<?php

require_once('for_php7.php');

require_once('knja180Model.inc');
require_once('knja180Query.inc');

class knja180Controller extends Controller {
    var $ModelClassName = "knja180Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja180":
					$sessionInstance->knja180Model();		//コントロールマスタの呼び出し
                    $this->callView("knja180Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knja180Ctl = new knja180Controller;
var_dump($_REQUEST);
?>
