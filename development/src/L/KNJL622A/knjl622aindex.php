<?php
require_once('knjl622aModel.inc');
require_once('knjl622aQuery.inc');

class knjl622aController extends Controller {
    var $ModelClassName = "knjl622aModel";
    var $ProgramID      = "KNJL622A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "clear":
                case "search":
                    $sessionInstance->knjl622aModel();
                    $this->callView("knjl622aForm1");
                    exit;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("search");
                    break 1;
                case "csvInput":    //CSV取込
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getCsvInputModel();
                    $sessionInstance->setCmd("clear");
                    break 1;
                case "csvOutput":   //CSV出力
                    if (!$sessionInstance->getCsvModel()){
                        $this->callView("knjl622aForm1");
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
$knjl622aCtl = new knjl622aController;
//var_dump($_REQUEST);
?>
