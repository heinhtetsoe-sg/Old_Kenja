<?php

require_once('for_php7.php');

require_once('knjd625hModel.inc');
require_once('knjd625hQuery.inc');

class knjd625hController extends Controller {
    var $ModelClassName = "knjd625hModel";
    var $ProgramID      = "KNJD625H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd625hForm1");
                    }
                    break 2;
                case "":
                case "knjd625h":
                case "change_grade":
                case "gakki":
                    $sessionInstance->knjd625hModel();
                    $this->callView("knjd625hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd625hCtl = new knjd625hController;
//var_dump($_REQUEST);
?>
