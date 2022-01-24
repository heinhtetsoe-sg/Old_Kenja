<?php

require_once('for_php7.php');

require_once('knjm300Model.inc');
require_once('knjm300Query.inc');

class knjm300Controller extends Controller {
    var $ModelClassName = "knjm300Model";
    var $ProgramID      = "KNJm300";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm300":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm300Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm300Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjm300Model();
                    $this->callView("knjm300Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm300Ctl = new knjm300Controller;
var_dump($_REQUEST);
?>
