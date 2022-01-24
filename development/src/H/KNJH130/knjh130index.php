<?php

require_once('for_php7.php');

require_once('knjh130Model.inc');
require_once('knjh130Query.inc');

class knjh130Controller extends Controller {
    var $ModelClassName = "knjh130Model";
    var $ProgramID    = "KNJH130";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh130":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh130Model();       //コントロールマスタの呼び出し
                    $this->callView("knjh130Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjh130Ctl = new knjh130Controller;
var_dump($_REQUEST);
?>
