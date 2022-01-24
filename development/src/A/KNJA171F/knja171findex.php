<?php

require_once('for_php7.php');

require_once('knja171fModel.inc');
require_once('knja171fQuery.inc');

class knja171fController extends Controller {
    var $ModelClassName = "knja171fModel";
    var $ProgramID      = "KNJA171F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja171f":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja171fModel();       //コントロールマスタの呼び出し
                    $this->callView("knja171fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja171fCtl = new knja171fController;
?>
