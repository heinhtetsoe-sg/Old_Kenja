<?php

require_once('for_php7.php');

require_once('knjc110cModel.inc');
require_once('knjc110cQuery.inc');

class knjc110cController extends Controller
{
    public $ModelClassName = "knjc110cModel";
    public $ProgramID      = "KNJC110C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc110c":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjc110cModel();
                    $this->callView("knjc110cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc110cCtl = new knjc110cController();
