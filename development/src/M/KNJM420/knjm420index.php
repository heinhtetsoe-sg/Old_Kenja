<?php

require_once('for_php7.php');

require_once('knjm420Model.inc');
require_once('knjm420Query.inc');

class knjm420Controller extends Controller {
    var $ModelClassName = "knjm420Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
					$sessionInstance->knjm420Model();		//コントロールマスタの呼び出し
                    $this->callView("knjm420Form1");
                    exit;
                case "dsub":
                    $this->callView("knjm420Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjm420Ctl = new knjm420Controller;
//var_dump($_REQUEST);
?>
