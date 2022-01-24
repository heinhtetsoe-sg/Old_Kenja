<?php

require_once('for_php7.php');

require_once('knjm130Model.inc');
require_once('knjm130Query.inc');

class knjm130Controller extends Controller {
    var $ModelClassName = "knjm130Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
					$sessionInstance->knjm130Model();		//コントロールマスタの呼び出し
                    $this->callView("knjm130Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjm130Ctl = new knjm130Controller;
//var_dump($_REQUEST);
?>
