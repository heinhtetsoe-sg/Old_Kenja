<?php

require_once('for_php7.php');
require_once('knjb103eModel.inc');
require_once('knjb103eQuery.inc');

class knjb103eController extends Controller
{
    public $ModelClassName = "knjb103eModel";
    public $ProgramID      = "KNJB103E";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjb103eForm1");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjb103eForm1");
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
$knjb103eCtl = new knjb103eController();
//var_dump($_REQUEST);
