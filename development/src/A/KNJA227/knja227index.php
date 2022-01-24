<?php

require_once('for_php7.php');

require_once('knja227Model.inc');
require_once('knja227Query.inc');

class knja227Controller extends Controller {
    var $ModelClassName = "knja227Model";
    var $ProgramID      = "KNJA227";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "search":
                case "knja227":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja227Model();       //コントロールマスタの呼び出し
                    $this->callView("knja227Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knja227Ctl = new knja227Controller;
?>
