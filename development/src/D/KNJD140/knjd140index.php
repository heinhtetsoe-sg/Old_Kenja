<?php

require_once('for_php7.php');

require_once('knjd140Model.inc');
require_once('knjd140Query.inc');

class knjd140Controller extends Controller {
    var $ModelClassName = "knjd140Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd140":								//メニュー画面もしくはSUBMITした場合
                case "gakki":								//学期が変わったとき
					$sessionInstance->knjd140Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd140Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd140Ctl = new knjd140Controller;
var_dump($_REQUEST);
?>
