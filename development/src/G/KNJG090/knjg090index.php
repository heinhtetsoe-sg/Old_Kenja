<?php

require_once('for_php7.php');

require_once('knjg090Model.inc');
require_once('knjg090Query.inc');

class knjg090Controller extends Controller {
    var $ModelClassName = "knjg090Model";
    var $ProgramID      = "KNJG090";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg090":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjg090Model();   //コントロールマスタの呼び出し
                    $this->callView("knjg090Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg090Ctl = new knjg090Controller;
?>
