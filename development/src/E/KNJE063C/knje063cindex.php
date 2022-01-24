<?php

require_once('for_php7.php');

require_once('knje063cModel.inc');
require_once('knje063cQuery.inc');

class knje063cController extends Controller {
    var $ModelClassName = "knje063cModel";
    var $ProgramID      = "KNJE063C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje063c":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje063cModel();      //コントロールマスタの呼び出し
                    $this->callView("knje063cForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knje063c");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje063cCtl = new knje063cController;
?>
