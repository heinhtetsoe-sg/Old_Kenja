<?php

require_once('for_php7.php');

require_once('knjm320Model.inc');
require_once('knjm320Query.inc');

class knjm320Controller extends Controller {
    var $ModelClassName = "knjm320Model";
    var $ProgramID      = "KNJm320";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm320":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm320Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm320Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjm320Model();
                    $this->callView("knjm320Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm320Ctl = new knjm320Controller;
var_dump($_REQUEST);
?>
