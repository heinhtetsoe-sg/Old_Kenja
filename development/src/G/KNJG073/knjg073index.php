<?php

require_once('for_php7.php');

require_once('knjg073Model.inc');
require_once('knjg073Query.inc');

class knjg073Controller extends Controller {
    var $ModelClassName = "knjg073Model";
    var $ProgramID      = "KNJG073";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg073":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjg073Model();       //コントロールマスタの呼び出し
                    $this->callView("knjg073Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg073Ctl = new knjg073Controller;
?>
