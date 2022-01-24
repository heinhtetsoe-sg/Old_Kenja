<?php

require_once('for_php7.php');

require_once('knjb140tModel.inc');
require_once('knjb140tQuery.inc');

class knjb140tController extends Controller {
    var $ModelClassName = "knjb140tModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb140t":								//メニュー画面もしくはSUBMITした場合
                case "gakki":								//学期が変わったとき
					$sessionInstance->knjb140tModel();		//コントロールマスタの呼び出し
                    $this->callView("knjb140tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb140tCtl = new knjb140tController;
var_dump($_REQUEST);
?>
