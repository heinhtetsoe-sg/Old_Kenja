<?php

require_once('for_php7.php');

require_once('knjg021Model.inc');
require_once('knjg021Query.inc');

class knjg021Controller extends Controller {
    var $ModelClassName = "knjg021Model";
    var $ProgramID      = "knjg021";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg021":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjg021Model();		//コントロールマスタの呼び出し
                    $this->callView("knjg021Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjg021Ctl = new knjg021Controller;
var_dump($_REQUEST);
?>
