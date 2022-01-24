<?php

require_once('for_php7.php');

require_once('knjf032Model.inc');
require_once('knjf032Query.inc');

class knjf032Controller extends Controller {
    var $ModelClassName = "knjf032Model";
    var $ProgramID      = "KNJF032";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf032":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjf032Model();       //コントロールマスタの呼び出し
                    $this->callView("knjf032Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf032Ctl = new knjf032Controller;
?>
