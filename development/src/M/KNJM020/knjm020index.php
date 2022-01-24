<?php

require_once('for_php7.php');

require_once('knjm020Model.inc');
require_once('knjm020Query.inc');

class knjm020Controller extends Controller {
    var $ModelClassName = "knjm020Model";
    var $ProgramID      = "KNJm020";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm020":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjm020Model();		//コントロールマスタの呼び出し
                    $this->callView("knjm020Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjm020Model();
                    $this->callView("knjm020Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjm020Ctl = new knjm020Controller;
var_dump($_REQUEST);
?>
