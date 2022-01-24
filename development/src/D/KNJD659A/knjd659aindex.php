<?php

require_once('for_php7.php');

require_once('knjd659aModel.inc');
require_once('knjd659aQuery.inc');

class knjd659aController extends Controller {
    var $ModelClassName = "knjd659aModel";
    var $ProgramID      = "KNJD659A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd659aModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd659aForm1");
                    exit;
                case "change_grade":
                case "knjd659a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd659aModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd659aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd659aCtl = new knjd659aController;
//var_dump($_REQUEST);
?>
