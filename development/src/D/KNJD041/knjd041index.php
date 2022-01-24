<?php

require_once('for_php7.php');

require_once('knjd041Model.inc');
require_once('knjd041Query.inc');

class knjd041Controller extends Controller {
    var $ModelClassName = "knjd041Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd041":								//メニュー画面もしくはSUBMITした場合
                case "gakki":								//学期が変わったとき
					$sessionInstance->knjd041Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd041Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd041Ctl = new knjd041Controller;
var_dump($_REQUEST);
?>
