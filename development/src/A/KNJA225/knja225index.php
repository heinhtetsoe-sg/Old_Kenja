<?php

require_once('for_php7.php');

require_once('knja225Model.inc');
require_once('knja225Query.inc');

class knja225Controller extends Controller {
    var $ModelClassName = "knja225Model";
    var $ProgramID      = "KNJA225";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja225":                       //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja225Model(); //コントロールマスタの呼び出し
                    $this->callView("knja225Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja225Ctl = new knja225Controller;
//var_dump($_REQUEST);
?>
