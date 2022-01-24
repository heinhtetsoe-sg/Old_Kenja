<?php

require_once('for_php7.php');

require_once('knja171uModel.inc');
require_once('knja171uQuery.inc');

class knja171uController extends Controller {
    var $ModelClassName = "knja171uModel";
    var $ProgramID      = "KNJA171U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja171u":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja171uModel();        //コントロールマスタの呼び出し
                    $this->callView("knja171uForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja171uCtl = new knja171uController;
?>
