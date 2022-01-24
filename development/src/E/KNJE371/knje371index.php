<?php

require_once('for_php7.php');

require_once('knje371Model.inc');
require_once('knje371Query.inc');

class knje371Controller extends Controller {
    var $ModelClassName = "knje371Model";
    var $ProgramID      = "KNJE371";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje371":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje371Model();        //コントロールマスタの呼び出し
                    $this->callView("knje371Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knje371Ctl = new knje371Controller;
var_dump($_REQUEST);
?>
