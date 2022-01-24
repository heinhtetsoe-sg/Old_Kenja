<?php

require_once('for_php7.php');

require_once('knja173Model.inc');
require_once('knja173Query.inc');

class knja173Controller extends Controller {
    var $ModelClassName = "knja173Model";
    var $ProgramID      = "KNJA173";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja173":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja173Model();        //コントロールマスタの呼び出し
                    $this->callView("knja173Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja173Ctl = new knja173Controller;
//var_dump($_REQUEST);
?>
