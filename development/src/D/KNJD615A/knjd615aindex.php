<?php

require_once('for_php7.php');

require_once('knjd615aModel.inc');
require_once('knjd615aQuery.inc');

class knjd615aController extends Controller {
    var $ModelClassName = "knjd615aModel";
    var $ProgramID      = "KNJD615A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd615aForm1");
                    }
                    break 2;
                case "":
                case "knjd615a":
                case "change_test":
                case "change_grade":
                case "gakki":
                    $sessionInstance->knjd615aModel();
                    $this->callView("knjd615aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd615aCtl = new knjd615aController;
//var_dump($_REQUEST);
?>
