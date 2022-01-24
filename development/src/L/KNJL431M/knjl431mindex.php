<?php

require_once('for_php7.php');

require_once('knjl431mModel.inc');
require_once('knjl431mQuery.inc');

class knjl431mController extends Controller
{
    public $ModelClassName = "knjl431mModel";
    public $ProgramID      = "KNJL431M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl431m":
                    $sessionInstance->knjl431mModel();
                    $this->callView("knjl431mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl431mCtl = new knjl431mController();
