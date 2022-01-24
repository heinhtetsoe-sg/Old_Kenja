<?php

require_once('for_php7.php');

require_once('knja130bModel.inc');
require_once('knja130bQuery.inc');

class knja130bController extends Controller {
    var $ModelClassName = "knja130bModel";
    var $ProgramID      = "KNJA130B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja130b":                            //メニュー画面もしくはSUBMITした場合
                case "print":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja130bModel();        //コントロールマスタの呼び出し
                    $this->callView("knja130bForm1");
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
$knja130bCtl = new knja130bController;
?>
