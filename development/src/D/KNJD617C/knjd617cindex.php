<?php

require_once('for_php7.php');

require_once('knjd617cModel.inc');
require_once('knjd617cQuery.inc');

class knjd617cController extends Controller {
    var $ModelClassName = "knjd617cModel";
    var $ProgramID      = "KNJD617C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd617cForm1");
                    }
                    break 2;
                case "":
                case "knjd617c":
                case "gakki":
                    $sessionInstance->knjd617cModel();
                    $this->callView("knjd617cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd617cCtl = new knjd617cController;
//var_dump($_REQUEST);
?>
