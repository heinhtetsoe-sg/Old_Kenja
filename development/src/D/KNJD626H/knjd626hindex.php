<?php

require_once('for_php7.php');

require_once('knjd626hModel.inc');
require_once('knjd626hQuery.inc');

class knjd626hController extends Controller
{
    public $ModelClassName = "knjd626hModel";
    public $ProgramID      = "KNJD626H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knjd626h":
                    $sessionInstance->knjd626hModel();
                    $this->callView("knjd626hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd626hCtl = new knjd626hController();
