<?php

require_once('for_php7.php');

require_once('knjh100Model.inc');
require_once('knjh100Query.inc');

class knjh100Controller extends Controller {
    var $ModelClassName = "knjh100Model";
    var $ProgramID        = "KNJH100";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh100":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh100Model();       //コントロールマスタの呼び出し
                    $this->callView("knjh100Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjh100Ctl = new knjh100Controller;
var_dump($_REQUEST);
?>
