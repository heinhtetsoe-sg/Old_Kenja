<?php

require_once('for_php7.php');

require_once('knje373Model.inc');
require_once('knje373Query.inc');

class knje373Controller extends Controller {
    var $ModelClassName = "knje373Model";
    var $ProgramID      = "KNJE373";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje373":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje373Model();        //コントロールマスタの呼び出し
                    $this->callView("knje373Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knje373Ctl = new knje373Controller;
var_dump($_REQUEST);
?>
