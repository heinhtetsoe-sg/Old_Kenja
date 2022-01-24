<?php

require_once('for_php7.php');

require_once('knja033Model.inc');
require_once('knja033Query.inc');

class knja033Controller extends Controller {
    var $ModelClassName = "knja033Model";
    var $ProgramID      = "KNJA033";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja033":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja033Model();       //コントロールマスタの呼び出し
                    $this->callView("knja033Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja033Ctl = new knja033Controller;
?>
