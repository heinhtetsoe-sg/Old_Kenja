<?php

require_once('for_php7.php');

require_once('knjc161bModel.inc');
require_once('knjc161bQuery.inc');

class knjc161bController extends Controller
{
    public $ModelClassName = "knjc161bModel";
    public $ProgramID      = "knjc161b";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjc161bForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc161bCtl = new knjc161bController();
//var_dump($_REQUEST);
