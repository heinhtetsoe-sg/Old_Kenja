<?php

require_once('for_php7.php');

require_once('knjd105jModel.inc');
require_once('knjd105jQuery.inc');

class knjd105jController extends Controller {
    var $ModelClassName = "knjd105jModel";
    var $ProgramID      = "KNJD105J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd105jModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd105jForm1");
                    exit;
                case "knjd105j":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd105jModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd105jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd105jCtl = new knjd105jController;
//var_dump($_REQUEST);
?>
