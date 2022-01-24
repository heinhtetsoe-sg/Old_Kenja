<?php

require_once('for_php7.php');

require_once('knjh112cModel.inc');
require_once('knjh112cQuery.inc');

class knjh112cController extends Controller {
    var $ModelClassName = "knjh112cModel";
    var $ProgramID      = "KNJH112C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "changeSeme":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh112cModel();
                    $this->callView("knjh112cForm1");
                    exit;
                case "knjh112c":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh112cModel();       //コントロールマスタの呼び出し
                    $this->callView("knjh112cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjh112cCtl = new knjh112cController;
//var_dump($_REQUEST);
?>
