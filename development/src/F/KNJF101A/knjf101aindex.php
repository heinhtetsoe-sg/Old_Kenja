<?php

require_once('for_php7.php');

require_once('knjf101aModel.inc');
require_once('knjf101aQuery.inc');

class knjf101aController extends Controller
{
    public $ModelClassName = "knjf101aModel";
    public $ProgramID      = "KNJF101A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf101a":
                case "semechg":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf101aModel();
                    $this->callView("knjf101aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf101aCtl = new knjf101aController();
//var_dump($_REQUEST);
