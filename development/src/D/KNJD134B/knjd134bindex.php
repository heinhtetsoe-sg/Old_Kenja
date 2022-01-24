<?php

require_once('for_php7.php');

require_once('knjd134bModel.inc');
require_once('knjd134bQuery.inc');

class knjd134bController extends Controller
{
    public $ModelClassName = "knjd134bModel";
    public $ProgramID      = "KNJD134B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd134b":
                    $sessionInstance->knjd134bModel();
                    $this->callView("knjd134bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd134bCtl = new knjd134bController();
