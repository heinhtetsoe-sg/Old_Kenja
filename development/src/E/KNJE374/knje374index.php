<?php

require_once('for_php7.php');

require_once('knje374Model.inc');
require_once('knje374Query.inc');

class knje374Controller extends Controller {
    var $ModelClassName = "knje374Model";
    var $ProgramID      = "KNJE374";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje374":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje374Model();       //コントロールマスタの呼び出し
                    $this->callView("knje374Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje374Ctl = new knje374Controller;
?>
