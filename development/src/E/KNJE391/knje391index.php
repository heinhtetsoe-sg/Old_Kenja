<?php

require_once('for_php7.php');

require_once('knje391Model.inc');
require_once('knje391Query.inc');

class knje391Controller extends Controller {
    var $ModelClassName = "knje391Model";
    var $ProgramID      = "KNJE391";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                case "knje391":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje391Model();       //コントロールマスタの呼び出し
                    $this->callView("knje391Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje391Ctl = new knje391Controller;
//var_dump($_REQUEST);
?>
