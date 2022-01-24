<?php

require_once('for_php7.php');

require_once('knjd320aModel.inc');
require_once('knjd320aQuery.inc');

class knjd320aController extends Controller {
    var $ModelClassName = "knjd320aModel";
    var $ProgramID      = "KNJD320A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd320a":
                    $sessionInstance->knjd320aModel();
                    $this->callView("knjd320aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd320aForm1");
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
$knjd320aCtl = new knjd320aController;
?>
