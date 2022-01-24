<?php

require_once('for_php7.php');

require_once('knjl141rModel.inc');
require_once('knjl141rQuery.inc');

class knjl141rController extends Controller
{
    public $ModelClassName = "knjl141rModel";
    public $ProgramID      = "KNJL141R";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl141rForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl141rForm1");
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
$knjl141rCtl = new knjl141rController();
