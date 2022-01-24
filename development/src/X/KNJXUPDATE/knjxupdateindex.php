<?php

require_once('for_php7.php');

require_once('knjxupdateModel.inc');
require_once('knjxupdateQuery.inc');

class knjxupdateController extends Controller {
    var $ModelClassName = "knjxupdateModel";
    var $ProgramID      = "KNJXUPDATE";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if ($sessionInstance->makeCsvModel()){
                        $this->callView("knjxupdateForm1");
                    }
                    break 2;
                case "csvGet":
                    if ($sessionInstance->getDownloadModel()){
                        $this->callView("knjxupdateForm1");
                    }
                    break 2;
                case "execute":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("knjxupdate");
                    break 1;
                case "del":
                    if ($sessionInstance->getDeleteModel()){
                        $this->callView("knjxupdateForm1");
                    }
                    break 2;
                case "":
                case "knjxupdate":
                    $sessionInstance->knjxupdateModel();
                    $this->callView("knjxupdateForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjxupdateCtl = new knjxupdateController;
?>
