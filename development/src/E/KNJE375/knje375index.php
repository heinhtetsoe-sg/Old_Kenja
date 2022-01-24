<?php

require_once('for_php7.php');

require_once('knje375Model.inc');
require_once('knje375Query.inc');

class knje375Controller extends Controller {
    var $ModelClassName = "knje375Model";
    var $ProgramID      = "KNJE375";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje375":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje375Model();       //コントロールマスタの呼び出し
                    $this->callView("knje375Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje375Ctl = new knje375Controller;
?>
