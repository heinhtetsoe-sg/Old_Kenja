<?php
require_once('knje140Model.inc');
require_once('knje140Query.inc');

class knje140Controller extends Controller {
    var $ModelClassName = "knje140Model";
    var $ProgramID      = "KNJe140";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje140":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje140Model();       //コントロールマスタの呼び出し
                    $this->callView("knje140Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knje140Ctl = new knje140Controller;
var_dump($_REQUEST);
?>
