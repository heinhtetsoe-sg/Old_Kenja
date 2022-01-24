<?php

require_once('for_php7.php');

require_once('knjd040Model.inc');
require_once('knjd040Query.inc');

class knjd040Controller extends Controller {
    var $ModelClassName = "knjd040Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd040":                             //メニュー画面もしくはSUBMITした場合
                case "gakki":                               //学期が変わったとき
                    $sessionInstance->knjd040Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd040Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd040Ctl = new knjd040Controller;
var_dump($_REQUEST);
?>
