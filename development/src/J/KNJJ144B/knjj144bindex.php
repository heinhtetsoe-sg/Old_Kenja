<?php

require_once('for_php7.php');

require_once('knjj144bModel.inc');
require_once('knjj144bQuery.inc');

class knjj144bController extends Controller {
    var $ModelClassName = "knjj144bModel";
    var $ProgramID      = "KNJJ144B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":       //CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjj144bForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjj144bForm1");
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
$knjj144bCtl = new knjj144bController;
?>
