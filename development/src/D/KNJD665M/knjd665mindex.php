<?php

require_once('for_php7.php');

require_once('knjd665mModel.inc');
require_once('knjd665mQuery.inc');

class knjd665mController extends Controller
{
    public $ModelClassName = "knjd665mModel";
    public $ProgramID      = "KNJD665M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "clear":
                case "knjd665m":
                    $sessionInstance->knjd665mModel();
                    $this->callView("knjd665mForm1");
                    exit;
                case "chgClsCd":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd665mModel();
                    $this->callView("knjd665mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd665mCtl = new knjd665mController();
