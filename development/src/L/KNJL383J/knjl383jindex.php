<?php

require_once('for_php7.php');

require_once('knjl383jModel.inc');
require_once('knjl383jQuery.inc');

class knjl383jController extends Controller
{
    public $ModelClassName = "knjl383jModel";
    public $ProgramID      = "KNJL383J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl383jForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl383jForm1");
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
$knjl383jCtl = new knjl383jController;
