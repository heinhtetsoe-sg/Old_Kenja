<?php

require_once('for_php7.php');

require_once('knjd617eModel.inc');
require_once('knjd617eQuery.inc');

class knjd617eController extends Controller
{
    public $ModelClassName = "knjd617eModel";
    public $ProgramID      = "KNJD617E";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "clear":
                case "chgTerm":
                case "knjd617e":
                    $sessionInstance->knjd617eModel();
                    $this->callView("knjd617eForm1");
                    exit;
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd617eModel();
                    $this->callView("knjd617eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd617eCtl = new knjd617eController();
