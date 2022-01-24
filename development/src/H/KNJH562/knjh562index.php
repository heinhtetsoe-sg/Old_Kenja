<?php

require_once('for_php7.php');

require_once('knjh562Model.inc');
require_once('knjh562Query.inc');

class knjh562Controller extends Controller {
    var $ModelClassName = "knjh562Model";
    var $ProgramID      = "KNJH562";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh562":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh562Model();       //コントロールマスタの呼び出し
                    $this->callView("knjh562Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh562Ctl = new knjh562Controller;
//var_dump($_REQUEST);
?>
