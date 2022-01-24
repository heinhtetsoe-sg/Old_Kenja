<?php

require_once('for_php7.php');

require_once('knjm400Model.inc');
require_once('knjm400Query.inc');

class knjm400Controller extends Controller {
    var $ModelClassName = "knjm400Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "":
					$sessionInstance->knjm400Model();		//コントロールマスタの呼び出し
                    $this->callView("knjm400Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjm400Ctl = new knjm400Controller;
var_dump($_REQUEST);
?>
