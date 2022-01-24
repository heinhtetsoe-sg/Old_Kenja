<?php

require_once('for_php7.php');

require_once('knjm310dModel.inc');
require_once('knjm310dQuery.inc');

class knjm310dController extends Controller {
    var $ModelClassName = "knjm310dModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm310d":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm310dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm310dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm310dCtl = new knjm310dController;
//var_dump($_REQUEST);
?>
