<?php

require_once('for_php7.php');

require_once('knja133fModel.inc');
require_once('knja133fQuery.inc');

class knja133fController extends Controller {
    var $ModelClassName = "knja133fModel";
    var $ProgramID      = "KNJA133F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja133f":                            //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knja133fModel();      //コントロールマスタの呼び出し
                    $this->callView("knja133fForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja133fModel();      //コントロールマスタの呼び出し
                    $this->callView("knja133fForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("print");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja133fCtl = new knja133fController;
?>
