<?php

require_once('for_php7.php');

require_once('knjx_d137jModel.inc');
require_once('knjx_d137jQuery.inc');

class knjx_d137jController extends Controller {
    var $ModelClassName = "knjx_d137jModel";
    var $ProgramID      = "KNJX_D137J";

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
                        $this->callView("knjx_d137jForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx_d137jForm1");
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
$knjx_d137jCtl = new knjx_d137jController;
?>
