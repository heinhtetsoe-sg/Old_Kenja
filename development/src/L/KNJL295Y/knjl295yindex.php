<?php

require_once('for_php7.php');

require_once('knjl295yModel.inc');
require_once('knjl295yQuery.inc');

class knjl295yController extends Controller
{
    public $ModelClassName = "knjl295yModel";
    public $ProgramID      = "KNJL295Y";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl295yForm1");
                    }
                    break 2;
                case "":
                case "knjl295y":
                case "change":
                    $sessionInstance->knjl295yModel();
                    $this->callView("knjl295yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl295yCtl = new knjl295yController();
