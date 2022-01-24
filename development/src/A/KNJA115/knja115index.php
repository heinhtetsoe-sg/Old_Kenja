<?php

require_once('for_php7.php');

require_once('knja115Model.inc');
require_once('knja115Query.inc');

class knja115Controller extends Controller {
    var $ModelClassName = "knja115Model";
    var $ProgramID      = "KNJA115";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knja115Model();
                    $this->callView("knja115Form1");
                    exit;
                case "knja115":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knja115Model();       //コントロールマスタの呼び出し
                    $this->callView("knja115Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja115Ctl = new knja115Controller;
//var_dump($_REQUEST);
?>
