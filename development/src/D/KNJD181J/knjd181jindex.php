<?php

require_once('for_php7.php');

require_once('knjd181jModel.inc');
require_once('knjd181jQuery.inc');

class knjd181jController extends Controller
{
    public $ModelClassName = "knjd181jModel";
    public $ProgramID      = "KNJD181J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd181j":
                    $sessionInstance->knjd181jModel();
                    $this->callView("knjd181jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd181jCtl = new knjd181jController();
