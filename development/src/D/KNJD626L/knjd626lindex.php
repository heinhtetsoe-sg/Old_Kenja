<?php

require_once('for_php7.php');

require_once('knjd626lModel.inc');
require_once('knjd626lQuery.inc');

class knjd626lController extends Controller
{
    public $ModelClassName = "knjd626lModel";
    public $ProgramID      = "KNJD626L";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd626l":
                case "read":
                    $sessionInstance->knjd626lModel();
                    $this->callView("knjd626lForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd626lCtl = new knjd626lController();
