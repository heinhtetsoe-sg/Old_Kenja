<?php

require_once('for_php7.php');

require_once('knja171gModel.inc');
require_once('knja171gQuery.inc');

class knja171gController extends Controller {
    var $ModelClassName = "knja171gModel";
    var $ProgramID      = "KNJA171G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja171g":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja171gModel();       //コントロールマスタの呼び出し
                    $this->callView("knja171gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja171gCtl = new knja171gController;
?>
