<?php

require_once('for_php7.php');

require_once('knjc120aModel.inc');
require_once('knjc120aQuery.inc');

class knjc120aController extends Controller
{
    public $ModelClassName = "knjc120aModel";
    public $ProgramID      = "KNJC120A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knjc120a":
                    $sessionInstance->knjc120aModel();
                    $this->callView("knjc120aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc120aCtl = new knjc120aController();
