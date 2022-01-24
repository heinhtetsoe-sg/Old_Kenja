<?php

require_once('for_php7.php');

require_once('knjl611a_2Model.inc');
require_once('knjl611a_2Query.inc');

class knjl611a_2Controller extends Controller {
    var $ModelClassName = "knjl611a_2Model";
    var $ProgramID      = "KNJL611A_2";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":       //CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl611a_2Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl611a_2Form1");
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
$knjl611a_2Ctl = new knjl611a_2Controller;
?>
