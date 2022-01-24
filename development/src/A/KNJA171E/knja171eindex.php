<?php

require_once('for_php7.php');

require_once('knja171eModel.inc');
require_once('knja171eQuery.inc');

class knja171eController extends Controller {
    var $ModelClassName = "knja171eModel";
    var $ProgramID      = "KNJA171E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja171e":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja171eModel();       //コントロールマスタの呼び出し
                    $this->callView("knja171eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja171eCtl = new knja171eController;
?>
