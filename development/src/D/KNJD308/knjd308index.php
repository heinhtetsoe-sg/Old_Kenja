<?php

require_once('for_php7.php');

require_once('knjd308Model.inc');
require_once('knjd308Query.inc');

class knjd308Controller extends Controller {
    var $ModelClassName = "knjd308Model";
    var $ProgramID      = "KNJD308";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd308":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd308Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd308Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd308Ctl = new knjd308Controller;
var_dump($_REQUEST);
?>
