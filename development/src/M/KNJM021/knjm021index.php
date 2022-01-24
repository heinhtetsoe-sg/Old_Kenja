<?php

require_once('for_php7.php');

require_once('knjm021Model.inc');
require_once('knjm021Query.inc');

class knjm021Controller extends Controller {
    var $ModelClassName = "knjm021Model";
    var $ProgramID      = "KNJm021";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm021":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjm021Model();		//コントロールマスタの呼び出し
                    $this->callView("knjm021Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjm021Model();
                    $this->callView("knjm021Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjm021Ctl = new knjm021Controller;
var_dump($_REQUEST);
?>
