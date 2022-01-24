<?php

require_once('for_php7.php');

require_once('knjz100Model.inc');
require_once('knjz100Query.inc');

class knjz100Controller extends Controller {
    var $ModelClassName = "knjz100Model";
    var $ProgramID      = "KNJZ100";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjz100":                //メニュー画面もしくはSUBMITした場合
                case "knjz100changeDiv":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjz100Model();    //コントロールマスタの呼び出し
                    $this->callView("knjz100Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
      }
        }
            
    }
}
$knjz100Ctl = new knjz100Controller;
var_dump($_REQUEST);
?>
