<?php

require_once('for_php7.php');

require_once('knjd172kModel.inc');
require_once('knjd172kQuery.inc');

class knjd172kController extends Controller {
    var $ModelClassName = "knjd172kModel";
    var $ProgramID      = "KNJD172K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd172k":                             //メニュー画面もしくはSUBMITした場合
                case "clickcheng":                          //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd172kModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd172kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd172kCtl = new knjd172kController;
//var_dump($_REQUEST);
?>
