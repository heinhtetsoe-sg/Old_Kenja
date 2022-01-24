<?php

require_once('for_php7.php');

require_once('knjl506jModel.inc');
require_once('knjl506jQuery.inc');

class knjl506jController extends Controller
{
    public $ModelClassName = "knjl506jModel";
    public $ProgramID      = "KNJL506J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl506jForm1");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl506jForm1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl506jCtl = new knjl506jController();
