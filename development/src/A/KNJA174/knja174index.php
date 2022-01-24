<?php

require_once('for_php7.php');

require_once('knja174Model.inc');
require_once('knja174Query.inc');

class knja174Controller extends Controller {
    var $ModelClassName = "knja174Model";
    var $ProgramID      = "KNJA174";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja174":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja174Model();        //コントロールマスタの呼び出し
                    $this->callView("knja174Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja174Ctl = new knja174Controller;
//var_dump($_REQUEST);
?>
