<?php

require_once('for_php7.php');

require_once('knjx240Model.inc');
require_once('knjx240Query.inc');

class knjx240Controller extends Controller {
    var $ModelClassName = "knjx240Model";
    var $ProgramID      = "KNJX240";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjx240":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjx240Model();       //コントロールマスタの呼び出し
                    $this->callView("knjx240Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx240Ctl = new knjx240Controller;
//var_dump($_REQUEST);
?>
