<?php

require_once('for_php7.php');

require_once('knjd061Model.inc');
require_once('knjd061Query.inc');

class knjd061Controller extends Controller {
    var $ModelClassName = "knjd061Model";
    var $ProgramID      = "KNJD061";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd061":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd061Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd061Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd061Ctl = new knjd061Controller;
var_dump($_REQUEST);
?>
