<?php

require_once('for_php7.php');

require_once('knjd618uModel.inc');
require_once('knjd618uQuery.inc');

class knjd618uController extends Controller {
    var $ModelClassName = "knjd618uModel";
    var $ProgramID      = "KNJD618U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd618u":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd618uModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd618uForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd618uCtl = new knjd618uController;
?>
