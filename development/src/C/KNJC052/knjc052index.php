<?php

require_once('for_php7.php');

require_once('knjc052Model.inc');
require_once('knjc052Query.inc');

class knjc052Controller extends Controller {
    var $ModelClassName = "knjc052Model";
    var $ProgramID      = "KNJC052";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc052":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc052Model();       //コントロールマスタの呼び出し
                    $this->callView("knjc052Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc052cCtl = new knjc052Controller;
//var_dump($_REQUEST);
?>
