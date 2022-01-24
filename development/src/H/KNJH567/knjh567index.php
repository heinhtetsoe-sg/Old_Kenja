<?php

require_once('for_php7.php');

require_once('knjh567Model.inc');
require_once('knjh567Query.inc');

class knjh567Controller extends Controller {
    var $ModelClassName = "knjh567Model";
    var $ProgramID      = "KNJH567";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh567":                             //メニュー画面もしくはSUBMITした場合
                case "chgGrade":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh567Model();       //コントロールマスタの呼び出し
                    $this->callView("knjh567Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh567Ctl = new knjh567Controller;
//var_dump($_REQUEST);
?>
