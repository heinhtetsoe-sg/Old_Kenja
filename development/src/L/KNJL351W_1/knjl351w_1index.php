<?php

require_once('for_php7.php');

require_once('knjl351w_1Model.inc');
require_once('knjl351w_1Query.inc');

class knjl351w_1Controller extends Controller {
    var $ModelClassName = "knjl351w_1Model";
    var $ProgramID      = "KNJL351W_1";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":     //CSV出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl351w_1Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl351w_1Form1");
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
$knjl351w_1Ctl = new knjl351w_1Controller;
?>
