<?php

require_once('for_php7.php');

require_once('knjb1258Model.inc');
require_once('knjb1258Query.inc');

class knjb1258Controller extends Controller {
    var $ModelClassName = "knjb1258Model";
    var $ProgramID      = "KNJB1258";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv2": // 自動生成
                    $sessionInstance->setAccessLogDetail("IEO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel2()){
                        $this->callView("knjb1258Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjb1258Form1");
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
$knjb1258Ctl = new knjb1258Controller;
?>
