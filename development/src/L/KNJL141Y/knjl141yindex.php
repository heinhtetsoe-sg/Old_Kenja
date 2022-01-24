<?php

require_once('for_php7.php');

require_once('knjl141yModel.inc');
require_once('knjl141yQuery.inc');

class knjl141yController extends Controller
{
    public $ModelClassName = "knjl141yModel";
    public $ProgramID      = "KNJL141Y";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl141yForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl141yForm1");
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
$knjl141yCtl = new knjl141yController();
