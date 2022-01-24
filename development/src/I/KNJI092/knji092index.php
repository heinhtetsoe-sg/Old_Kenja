<?php

require_once('for_php7.php');

require_once('knji092Model.inc');
require_once('knji092Query.inc');

class knji092Controller extends Controller {
    var $ModelClassName = "knji092Model";
    var $ProgramID      = "KNJI092";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knji092":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knji092Model();       //コントロールマスタの呼び出し
                    $this->callView("knji092Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knji092Ctl = new knji092Controller;
var_dump($_REQUEST);
?>
