<?php

require_once('for_php7.php');

require_once('knjh344Model.inc');
require_once('knjh344Query.inc');

class knjh344Controller extends Controller {
    var $ModelClassName = "knjh344Model";
    var $ProgramID      = "KNJH344";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh344":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjh344Model();
                    $this->callView("knjh344Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjh344Form1");
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
$knjh344Ctl = new knjh344Controller;
?>
