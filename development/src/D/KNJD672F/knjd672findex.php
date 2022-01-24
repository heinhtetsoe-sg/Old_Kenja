<?php

require_once('for_php7.php');

require_once('knjd672fModel.inc');
require_once('knjd672fQuery.inc');

class knjd672fController extends Controller {
    var $ModelClassName = "knjd672fModel";
    var $ProgramID      = "KNJD672F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd672f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd672fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd672fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd672fCtl = new knjd672fController;
?>
