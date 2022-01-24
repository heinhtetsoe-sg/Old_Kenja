<?php

require_once('for_php7.php');

require_once('knjx191oModel.inc');
require_once('knjx191oQuery.inc');

class knjx191oController extends Controller {
    var $ModelClassName = "knjx191oModel";
    var $ProgramID      = "KNJX191O";

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
                        $this->callView("knjx191oForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjx191oForm1");
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
$knjx191oCtl = new knjx191oController;
?>
