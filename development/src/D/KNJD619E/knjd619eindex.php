<?php
require_once('knjd619eModel.inc');
require_once('knjd619eQuery.inc');

class knjd619eController extends Controller {
    var $ModelClassName = "knjd619eModel";
    var $ProgramID      = "KNJD619E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd619eForm1");
                    }
                    break 2;
                case "":
                case "knjd619e":
                    $sessionInstance->knjd619eModel();
                    $this->callView("knjd619eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd619eCtl = new knjd619eController;
?>
