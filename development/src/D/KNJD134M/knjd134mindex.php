<?php

require_once('for_php7.php');

require_once('knjd134mModel.inc');
require_once('knjd134mQuery.inc');

class knjd134mController extends Controller
{
    public $ModelClassName = "knjd134mModel";
    public $ProgramID      = "KNJD134M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knjd134m":
                    $sessionInstance->knjd134mModel();
                    $this->callView("knjd134mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd134mCtl = new knjd134mController();
