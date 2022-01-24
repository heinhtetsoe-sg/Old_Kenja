<?php

require_once('for_php7.php');

require_once('knja240aModel.inc');
require_once('knja240aQuery.inc');

class knja240aController extends Controller {
    var $ModelClassName = "knja240aModel";
    var $ProgramID      = "KNJA240A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja240a":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja240aModel();      //コントロールマスタの呼び出し
                    $this->callView("knja240aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja240aCtl = new knja240aController;
//var_dump($_REQUEST);
?>
