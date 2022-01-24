<?php

require_once('for_php7.php');

require_once('knjb070kModel.inc');
require_once('knjb070kQuery.inc');

class knjb070kController extends Controller
{
    public $ModelClassName = "knjb070kModel";
    public $ProgramID      = "KNJB070K";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjb070kModel();
                    $this->callView("knjb070kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb070kCtl = new knjb070kController();
var_dump($_REQUEST);
