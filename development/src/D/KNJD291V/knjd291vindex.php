<?php

require_once('for_php7.php');

require_once('knjd291vModel.inc');
require_once('knjd291vQuery.inc');

class knjd291vController extends Controller {
    var $ModelClassName = "knjd291vModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd291v":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd291vModel();		//コントロールマスタの呼び出し
                    $this->callView("knjd291vForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd291vCtl = new knjd291vController;
var_dump($_REQUEST);
?>
