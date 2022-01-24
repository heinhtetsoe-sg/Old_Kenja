<?php

require_once('for_php7.php');

require_once('knja223bModel.inc');
require_once('knja223bQuery.inc');

class knja223bController extends Controller {
    var $ModelClassName = "knja223bModel";
    var $ProgramID      = "KNJA223B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja223b":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja223bModel();        //コントロールマスタの呼び出し
                    $this->callView("knja223bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja223bCtl = new knja223bController;
//var_dump($_REQUEST);
?>
