<?php

require_once('for_php7.php');

require_once('knjd192gModel.inc');
require_once('knjd192gQuery.inc');

class knjd192gController extends Controller {
    var $ModelClassName = "knjd192gModel";
    var $ProgramID      = "KNJD192G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd192g":            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd192gModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd192gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd192gCtl = new knjd192gController;
?>
