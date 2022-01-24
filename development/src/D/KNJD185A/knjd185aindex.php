<?php

require_once('for_php7.php');

require_once('knjd185aModel.inc');
require_once('knjd185aQuery.inc');

class knjd185aController extends Controller
{
    public $ModelClassName = "knjd185aModel";
    public $ProgramID      = "KNJD185A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd185a":
                    $sessionInstance->knjd185aModel();
                    $this->callView("knjd185aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd185aCtl = new knjd185aController;
