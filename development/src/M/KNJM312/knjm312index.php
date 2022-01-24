<?php

require_once('for_php7.php');

require_once('knjm312Model.inc');
require_once('knjm312Query.inc');

class knjm312Controller extends Controller {
    var $ModelClassName = "knjm312Model";
    var $ProgramID      = "KNJM312";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjm312");
                    break 1;
                case "":
                case "knjm312":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm312Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm312Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm312Ctl = new knjm312Controller;
var_dump($_REQUEST);
?>
