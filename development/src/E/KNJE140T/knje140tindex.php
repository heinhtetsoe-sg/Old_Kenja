<?php

require_once('for_php7.php');

require_once('knje140tModel.inc');
require_once('knje140tQuery.inc');

class knje140tController extends Controller {
    var $ModelClassName = "knje140tModel";
    var $ProgramID      = "KNJE140T";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje140t":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje140tModel();       //コントロールマスタの呼び出し
                    $this->callView("knje140tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje140tCtl = new knje140tController;
var_dump($_REQUEST);
?>
