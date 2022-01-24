<?php

require_once('for_php7.php');

require_once('knje391nModel.inc');
require_once('knje391nQuery.inc');

class knje391nController extends Controller {
    var $ModelClassName = "knje391nModel";
    var $ProgramID      = "KNJE391N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                case "knje391n":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje391nModel();       //コントロールマスタの呼び出し
                    $this->callView("knje391nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje391nCtl = new knje391nController;
//var_dump($_REQUEST);
?>
