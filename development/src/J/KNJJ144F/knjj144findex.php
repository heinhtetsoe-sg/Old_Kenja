<?php

require_once('for_php7.php');

require_once('knjj144fModel.inc');
require_once('knjj144fQuery.inc');

class knjj144fController extends Controller {
    var $ModelClassName = "knjj144fModel";
    var $ProgramID      = "KNJJ144F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjj144fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjj144fForm1");
                    exit;
                case "knjj144f":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjj144fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjj144fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj144fCtl = new knjj144fController;
//var_dump($_REQUEST);
?>
