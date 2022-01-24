<?php

require_once('for_php7.php');

require_once('knjd619aModel.inc');
require_once('knjd619aQuery.inc');

class knjd619aController extends Controller {
    var $ModelClassName = "knjd619aModel";
    var $ProgramID      = "KNJD619A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "init":
                case "knjd619a":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd619aModel();
                    $this->callView("knjd619aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd619aForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd619aCtl = new knjd619aController;
//var_dump($_REQUEST);
?>
