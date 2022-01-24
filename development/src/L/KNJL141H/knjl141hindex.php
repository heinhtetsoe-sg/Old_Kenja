<?php

require_once('for_php7.php');

require_once('knjl141hModel.inc');
require_once('knjl141hQuery.inc');

class knjl141hController extends Controller
{
    public $ModelClassName = "knjl141hModel";
    public $ProgramID      = "KNJL141H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl141hForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl141hForm1");
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
$knjl141hCtl = new knjl141hController();
