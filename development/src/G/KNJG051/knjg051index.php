<?php

require_once('for_php7.php');

require_once('knjg051Model.inc');
require_once('knjg051Query.inc');

class knjg051Controller extends Controller {
    var $ModelClassName = "knjg051Model";
    var $ProgramID      = "knjg051";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg051":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjg051Model();		//コントロールマスタの呼び出し
                    $this->callView("knjg051Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjg051Ctl = new knjg051Controller;
var_dump($_REQUEST);
?>
