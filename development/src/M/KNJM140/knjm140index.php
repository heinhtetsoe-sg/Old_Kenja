<?php

require_once('for_php7.php');

require_once('knjm140Model.inc');
require_once('knjm140Query.inc');

class knjm140Controller extends Controller {
    var $ModelClassName = "knjm140Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
					$sessionInstance->knjm140Model();		//コントロールマスタの呼び出し
                    $this->callView("knjm140Form1");
                    exit;
                case "dsub":
                    $this->callView("knjm140Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjm140Ctl = new knjm140Controller;
//var_dump($_REQUEST);
?>
