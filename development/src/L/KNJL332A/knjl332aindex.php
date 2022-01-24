<?php

require_once('for_php7.php');

require_once('knjl332aModel.inc');
require_once('knjl332aQuery.inc');

class knjl332aController extends Controller
{
    public $ModelClassName = "knjl332aModel";
    public $ProgramID      = "KNJL332A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl332a":
                    $sessionInstance->knjl332aModel();
                    $this->callView("knjl332aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl332aCtl = new knjl332aController();
