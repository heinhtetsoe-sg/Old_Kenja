<?php

require_once('for_php7.php');

require_once('knjp500Model.inc');
require_once('knjp500Query.inc');

class knjp500Controller extends Controller {
    var $ModelClassName = "knjp500Model";
    var $ProgramID      = "KNJp500";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp500":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjp500Model();		//コントロールマスタの呼び出し
                    $this->callView("knjp500Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjp500Ctl = new knjp500Controller;
var_dump($_REQUEST);
?>
