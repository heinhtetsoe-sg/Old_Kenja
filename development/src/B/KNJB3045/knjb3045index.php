<?php

require_once('for_php7.php');

require_once('knjb3045Model.inc');
require_once('knjb3045Query.inc');

class knjb3045Controller extends Controller {
    var $ModelClassName = "knjb3045Model";
    var $ProgramID      = "KNJB3045";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "prevRead":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjb3045Form1");
                    break 2;
                case "update": // 自動生成
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "selectBasic": // 保存値読込
                    $this->callView("knjb3045PtrnPreChaToBasic");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjb3045Ctl = new knjb3045Controller;
?>
