<?php

require_once('for_php7.php');

require_once('knjf175cModel.inc');
require_once('knjf175cQuery.inc');

class knjf175cController extends Controller
{
    public $ModelClassName = "knjf175cModel";
    public $ProgramID      = "KNJF175C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf175c":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf175cModel();
                    $this->callView("knjf175cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf175cCtl = new knjf175cController();
