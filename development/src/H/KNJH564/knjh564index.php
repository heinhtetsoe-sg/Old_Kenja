<?php

require_once('for_php7.php');

require_once('knjh564Model.inc');
require_once('knjh564Query.inc');

class knjh564Controller extends Controller {
    var $ModelClassName = "knjh564Model";
    var $ProgramID      = "KNJH564";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh564":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh564Model();        //コントロールマスタの呼び出し
                    $this->callView("knjh564Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh564Ctl = new knjh564Controller;
//var_dump($_REQUEST);
?>
