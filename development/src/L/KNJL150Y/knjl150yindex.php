<?php

require_once('for_php7.php');

require_once('knjl150yModel.inc');
require_once('knjl150yQuery.inc');

class knjl150yController extends Controller
{
    public $ModelClassName = "knjl150yModel";
    public $ProgramID      = "KNJL150Y";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl150yForm1");
                    }
                    break 2;
                case "":
                case "knjl150y":
                case "change":
                    $sessionInstance->knjl150yModel();
                    $this->callView("knjl150yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl150yCtl = new knjl150yController();
