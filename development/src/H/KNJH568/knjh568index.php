<?php

require_once('for_php7.php');

require_once('knjh568Model.inc');
require_once('knjh568Query.inc');

class knjh568Controller extends Controller {
    var $ModelClassName = "knjh568Model";
    var $ProgramID      = "KNJH568";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh568":                             //メニュー画面もしくはSUBMITした場合
                case "chgGrade":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh568Model();       //コントロールマスタの呼び出し
                    $this->callView("knjh568Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh568Ctl = new knjh568Controller;
//var_dump($_REQUEST);
?>
