<?php

require_once('for_php7.php');

require_once('knjd187tModel.inc');
require_once('knjd187tQuery.inc');

class knjd187tController extends Controller
{
    public $ModelClassName = "knjd187tModel";
    public $ProgramID      = "KNJD187T";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd187t":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd187tModel();
                    $this->callView("knjd187tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd187tCtl = new knjd187tController();
