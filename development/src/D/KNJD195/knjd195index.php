<?php

require_once('for_php7.php');

require_once('knjd195Model.inc');
require_once('knjd195Query.inc');

class knjd195Controller extends Controller {
    var $ModelClassName = "knjd195Model";
    var $ProgramID      = "KNJD195";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_grade":
                case "knjd195":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd195Model();        //コントロールマスタの呼び出し
                    $this->callView("knjd195Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd195Ctl = new knjd195Controller;
//var_dump($_REQUEST);
?>
