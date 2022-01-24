<?php

require_once('for_php7.php');

require_once('knjc100aModel.inc');
require_once('knjc100aQuery.inc');

class knjc100aController extends Controller
{
    public $ModelClassName = "knjc100aModel";
    public $ProgramID      = "KNJC100A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knjc100a":
                    $sessionInstance->knjc100aModel();
                    $this->callView("knjc100aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc100aCtl = new knjc100aController();
