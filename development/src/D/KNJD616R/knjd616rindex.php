<?php

require_once('for_php7.php');

require_once('knjd616rModel.inc');
require_once('knjd616rQuery.inc');

class knjd616rController extends Controller {
    var $ModelClassName = "knjd616rModel";
    var $ProgramID      = "KNJD616R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd616rModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd616rForm1");
                    exit;
                case "knjd616rChseme":
                case "knjd616r":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd616rModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd616rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd616rCtl = new knjd616rController;
//var_dump($_REQUEST);
?>
