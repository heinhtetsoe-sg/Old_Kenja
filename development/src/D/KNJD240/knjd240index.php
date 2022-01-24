<?php

require_once('for_php7.php');

require_once('knjd240Model.inc');
require_once('knjd240Query.inc');

class knjd240Controller extends Controller {
    var $ModelClassName = "knjd240Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd240":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd240Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd240Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd240Ctl = new knjd240Controller;
var_dump($_REQUEST);
?>
