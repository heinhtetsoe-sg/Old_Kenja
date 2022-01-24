<?php

require_once('for_php7.php');

require_once('knjd183eModel.inc');
require_once('knjd183eQuery.inc');

class knjd183eController extends Controller
{
    public $ModelClassName = "knjd183eModel";
    public $ProgramID      = "KNJD183E";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd183e":
                    $sessionInstance->knjd183eModel();
                    $this->callView("knjd183eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd183eCtl = new knjd183eController();
