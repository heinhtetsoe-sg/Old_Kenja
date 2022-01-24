<?php

require_once('for_php7.php');

require_once('knjh132Model.inc');
require_once('knjh132Query.inc');

class knjh132Controller extends Controller {
    var $ModelClassName = "knjh132Model";
    var $ProgramID      = "KNJH132";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh132":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjh132Model();
                    $this->callView("knjh132Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjh132Form1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh132Ctl = new knjh132Controller;
?>
