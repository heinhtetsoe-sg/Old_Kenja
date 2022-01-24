<?php

require_once('for_php7.php');

require_once('knjd615rModel.inc');
require_once('knjd615rQuery.inc');

class knjd615rController extends Controller {
    var $ModelClassName = "knjd615rModel";
    var $ProgramID      = "KNJD615R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd615rModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615rForm1");
                    exit;
                case "knjd615rChseme":
                case "knjd615r":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd615rModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd615rCtl = new knjd615rController;
//var_dump($_REQUEST);
?>
