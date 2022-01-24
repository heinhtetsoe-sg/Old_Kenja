<?php

require_once('for_php7.php');

require_once('knjh441Model.inc');
require_once('knjh441Query.inc');

class knjh441Controller extends Controller {
    var $ModelClassName = "knjh441Model";
    var $ProgramID      = "KNJH441";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh441":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjh441Model();        //コントロールマスタの呼び出し
                    $this->callView("knjh441Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh441Ctl = new knjh441Controller;
//var_dump($_REQUEST);
?>
