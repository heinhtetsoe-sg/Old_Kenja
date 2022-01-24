<?php

require_once('for_php7.php');

require_once('knjd615gModel.inc');
require_once('knjd615gQuery.inc');

class knjd615gController extends Controller {
    var $ModelClassName = "knjd615gModel";
    var $ProgramID      = "KNJD615G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd615gForm1");
                    }
                    break 2;
                case "":
                case "knjd615g":
                case "change_grade":
                case "gakki":
                    $sessionInstance->knjd615gModel();
                    $this->callView("knjd615gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd615gCtl = new knjd615gController;
//var_dump($_REQUEST);
?>
