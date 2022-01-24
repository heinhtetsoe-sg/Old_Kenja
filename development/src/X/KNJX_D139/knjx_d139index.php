<?php

require_once('for_php7.php');

require_once('knjx_d139Model.inc');
require_once('knjx_d139Query.inc');

class knjx_d139Controller extends Controller {
    var $ModelClassName = "knjx_d139Model";
    var $ProgramID      = "KNJX_D139";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjx_d139Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjx_d139Form1");
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
$knjx_d139Ctl = new knjx_d139Controller;
?>
