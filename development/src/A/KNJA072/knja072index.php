<?php

require_once('for_php7.php');

require_once('knja072Model.inc');
require_once('knja072Query.inc');

class knja072Controller extends Controller {
    var $ModelClassName = "knja072Model";
    var $ProgramID      = "KNJA072";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja072":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja072Model();       //コントロールマスタの呼び出し
                    $this->callView("knja072Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja072Ctl = new knja072Controller;
?>
