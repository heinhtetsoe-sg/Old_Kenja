<?php

require_once('for_php7.php');

require_once('knjd234Model.inc');
require_once('knjd234Query.inc');

class knjd234Controller extends Controller {
    var $ModelClassName = "knjd234Model";
    var $ProgramID      = "KNJD234";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd234":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd234Model();        //コントロールマスタの呼び出し
                    $this->callView("knjd234Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd234Ctl = new knjd234Controller;
var_dump($_REQUEST);
?>
