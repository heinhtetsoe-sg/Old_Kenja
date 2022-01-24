<?php

require_once('for_php7.php');

require_once('knjd617uModel.inc');
require_once('knjd617uQuery.inc');

class knjd617uController extends Controller {
    var $ModelClassName = "knjd617uModel";
    var $ProgramID      = "KNJD617U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd617u":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd617uModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd617uForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd617uCtl = new knjd617uController;
?>
