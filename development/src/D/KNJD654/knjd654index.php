<?php

require_once('for_php7.php');

require_once('knjd654Model.inc');
require_once('knjd654Query.inc');

class knjd654Controller extends Controller {
    var $ModelClassName = "knjd654Model";
    var $ProgramID      = "KNJD654";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd654":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd654Model();   //コントロールマスタの呼び出し
                    $this->callView("knjd654Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd654Ctl = new knjd654Controller;
//var_dump($_REQUEST);
?>
