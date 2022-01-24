<?php

require_once('for_php7.php');

require_once('knja171aModel.inc');
require_once('knja171aQuery.inc');

class knja171aController extends Controller {
    var $ModelClassName = "knja171aModel";
    var $ProgramID      = "KNJA171A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja171a":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja171aModel();        //コントロールマスタの呼び出し
                    $this->callView("knja171aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja171aCtl = new knja171aController;
//var_dump($_REQUEST);
?>
