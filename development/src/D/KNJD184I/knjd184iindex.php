<?php

require_once('for_php7.php');

require_once('knjd184iModel.inc');
require_once('knjd184iQuery.inc');

class knjd184iController extends Controller
{
    public $ModelClassName = "knjd184iModel";
    public $ProgramID      = "KNJD184I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd184i":
                    $sessionInstance->knjd184iModel();
                    $this->callView("knjd184iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd184iCtl = new knjd184iController();
