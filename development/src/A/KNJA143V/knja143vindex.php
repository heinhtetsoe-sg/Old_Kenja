<?php

require_once('for_php7.php');

require_once('knja143vModel.inc');
require_once('knja143vQuery.inc');

class knja143vController extends Controller {
    var $ModelClassName = "knja143vModel";
    var $ProgramID      = "KNJA143V";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja143v":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja143vModel();        //コントロールマスタの呼び出し
                    $this->callView("knja143vForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja143vCtl = new knja143vController;
//var_dump($_REQUEST);
?>
