<?php

require_once('for_php7.php');

require_once('knjd301aModel.inc');
require_once('knjd301aQuery.inc');

class knjd301aController extends Controller {
    var $ModelClassName = "knjd301aModel";
    var $ProgramID      = "KNJD301A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd301aModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd301aForm1");
                    exit;
                case "knjd301a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd301aModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd301aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd301aCtl = new knjd301aController;
//var_dump($_REQUEST);
?>
