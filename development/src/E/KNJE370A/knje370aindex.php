<?php

require_once('for_php7.php');

require_once('knje370aModel.inc');
require_once('knje370aQuery.inc');

class knje370aController extends Controller {
    var $ModelClassName = "knje370aModel";
    var $ProgramID      = "KNJE370A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje370a":                                //メニュー画面もしくはSUBMITした場合
                case "changeYear":
                case "changeGradeHr":
                    $sessionInstance->knje370aModel();        //コントロールマスタの呼び出し
                    $this->callView("knje370aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje370aCtl = new knje370aController;
//var_dump($_REQUEST);
?>
