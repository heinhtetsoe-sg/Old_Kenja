<?php

require_once('for_php7.php');

require_once('knjj144hModel.inc');
require_once('knjj144hQuery.inc');

class knjj144hController extends Controller {
    var $ModelClassName = "knjj144hModel";
    var $ProgramID      = "KNJJ144H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":     //エラー出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjj144hForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjj144hForm1");
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
$knjj144hCtl = new knjj144hController;
?>
