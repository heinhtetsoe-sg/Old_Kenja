<?php

require_once('for_php7.php');

require_once('knjd659nModel.inc');
require_once('knjd659nQuery.inc');

class knjd659nController extends Controller {
    var $ModelClassName = "knjd659nModel";
    var $ProgramID      = "KNJD659N";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd659nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd659nForm1");
                    exit;
                case "change_grade":
                case "knjd659n":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd659nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd659nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd659nCtl = new knjd659nController;
//var_dump($_REQUEST);
?>
