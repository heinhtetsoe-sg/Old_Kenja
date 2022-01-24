<?php

require_once('for_php7.php');

require_once('knjx651csvModel.inc');
require_once('knjx651csvQuery.inc');

class knjx651csvController extends Controller {
    var $ModelClassName = "knjx651csvModel";
    var $ProgramID      = "KNJX651CSV";

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
                        $this->callView("knjx651csvForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjx651csvForm1");
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
$knjx651csvCtl = new knjx651csvController;
?>
