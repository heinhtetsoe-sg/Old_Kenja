<?php

require_once('for_php7.php');

require_once('knjd626iModel.inc');
require_once('knjd626iQuery.inc');

class knjd626iController extends Controller
{
    public $ModelClassName = "knjd626iModel";
    public $ProgramID      = "KNJD626I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knjd626i":
                    $sessionInstance->knjd626iModel();
                    $this->callView("knjd626iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd626iCtl = new knjd626iController();
