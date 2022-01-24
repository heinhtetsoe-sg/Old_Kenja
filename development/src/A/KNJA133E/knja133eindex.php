<?php

require_once('for_php7.php');

require_once('knja133eModel.inc');
require_once('knja133eQuery.inc');

class knja133eController extends Controller {
    var $ModelClassName = "knja133eModel";
    var $ProgramID      = "KNJA133E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja133e":                            //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knja133eModel();      //コントロールマスタの呼び出し
                    $this->callView("knja133eForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja133eModel();      //コントロールマスタの呼び出し
                    $this->callView("knja133eForm1");
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
$knja133eCtl = new knja133eController;
?>
