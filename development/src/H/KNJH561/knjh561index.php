<?php

require_once('for_php7.php');

require_once('knjh561Model.inc');
require_once('knjh561Query.inc');

class knjh561Controller extends Controller {
    var $ModelClassName = "knjh561Model";
    var $ProgramID      = "KNJH561";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh561":                             //メニュー画面もしくはSUBMITした場合
                case "chgGrade":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh561Model();       //コントロールマスタの呼び出し
                    $this->callView("knjh561Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh561Ctl = new knjh561Controller;
//var_dump($_REQUEST);
?>
