<?php

require_once('for_php7.php');

require_once('knjd320Model.inc');
require_once('knjd320Query.inc');

class knjd320Controller extends Controller {
    var $ModelClassName = "knjd320Model";
    var $ProgramID      = "KNJD320";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd320":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd320Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd320Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd320Ctl = new knjd320Controller;
var_dump($_REQUEST);
?>
