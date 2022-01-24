<?php

require_once('for_php7.php');

require_once('knjc150Model.inc');
require_once('knjc150Query.inc');

class knjc150Controller extends Controller {
    var $ModelClassName = "knjc150Model";
    var $ProgramID      = "KNJC150";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc150":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjc150Model();		//コントロールマスタの呼び出し
                    $this->callView("knjc150Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjc150Ctl = new knjc150Controller;
var_dump($_REQUEST);
?>
