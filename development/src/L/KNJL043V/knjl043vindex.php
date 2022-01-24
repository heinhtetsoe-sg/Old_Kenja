<?php
require_once('knjl043vModel.inc');
require_once('knjl043vQuery.inc');

class knjl043vController extends Controller
{
    public $ModelClassName = "knjl043vModel";
    public $ProgramID      = "KNJL043V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl043vForm1");
                    }
                    break 2;
                case "":
                case "main":
                case "chgSchKind":
                case "chgAppDiv":
                case "chgCourse":
                case "chgFrequency":
                    $this->callView("knjl043vForm1");
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
$knjl043vCtl = new knjl043vController();
