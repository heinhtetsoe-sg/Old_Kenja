<?php

require_once('for_php7.php');

require_once('knjp392Model.inc');
require_once('knjp392Query.inc');

class knjp392Controller extends Controller {
    var $ModelClassName = "knjp392Model";
    var $ProgramID      = "KNJp392";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp392":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjp392Model();		//コントロールマスタの呼び出し
                    $this->callView("knjp392Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjp392Ctl = new knjp392Controller;
var_dump($_REQUEST);
?>
