<?php
require_once('knjl355yModel.inc');
require_once('knjl355yQuery.inc');

class knjl355yController extends Controller
{
    public $ModelClassName = "knjl355yModel";
    public $ProgramID      = "KNJL355Y";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl355yForm1");
                    }
                    break 2;
                case "":
                case "knjl355y":
                    $sessionInstance->knjl355yModel();
                    $this->callView("knjl355yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl355yCtl = new knjl355yController();
