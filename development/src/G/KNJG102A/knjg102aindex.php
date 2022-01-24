<?php

require_once('for_php7.php');

require_once('knjg102aModel.inc');
require_once('knjg102aQuery.inc');

class knjg102aController extends Controller {
    var $ModelClassName = "knjg102aModel";
    var $ProgramID      = "KNJG102A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg102a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjg102aModel();      //コントロールマスタの呼び出し
                    $this->callView("knjg102aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg102aCtl = new knjg102aController;
?>
