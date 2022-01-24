<?php

require_once('for_php7.php');

require_once('knjd192nModel.inc');
require_once('knjd192nQuery.inc');

class knjd192nController extends Controller {
    var $ModelClassName = "knjd192nModel";
    var $ProgramID      = "KNJD192N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd192nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192nForm1");
                    exit;
                case "change_grade":
                case "knjd192n":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd192nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd192nCtl = new knjd192nController;
//var_dump($_REQUEST);
?>
