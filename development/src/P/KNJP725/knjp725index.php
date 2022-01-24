<?php

require_once('for_php7.php');

require_once('knjp725Model.inc');
require_once('knjp725Query.inc');

class knjp725Controller extends Controller {
    var $ModelClassName = "knjp725Model";
    var $ProgramID      = "KNJP725";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp725":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjp725Model();        //コントロールマスタの呼び出し
                    $this->callView("knjp725Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp725Ctl = new knjp725Controller;
//var_dump($_REQUEST);
?>
