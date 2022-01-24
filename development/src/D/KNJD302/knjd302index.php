<?php

require_once('for_php7.php');

require_once('knjd302Model.inc');
require_once('knjd302Query.inc');

class knjd302Controller extends Controller {
    var $ModelClassName = "knjd302Model";
    var $ProgramID      = "KNJD302";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd302":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd302Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd302Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd302Ctl = new knjd302Controller;
var_dump($_REQUEST);
?>
