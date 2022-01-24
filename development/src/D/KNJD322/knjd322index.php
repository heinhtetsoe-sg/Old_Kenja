<?php

require_once('for_php7.php');

require_once('knjd322Model.inc');
require_once('knjd322Query.inc');

class knjd322Controller extends Controller {
    var $ModelClassName = "knjd322Model";
    var $ProgramID      = "KNJD322";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd322":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd322Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd322Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd322Ctl = new knjd322Controller;
var_dump($_REQUEST);
?>
