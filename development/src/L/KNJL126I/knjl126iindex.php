<?php
require_once('knjl126iModel.inc');
require_once('knjl126iQuery.inc');

class knjl126iController extends Controller
{
    public $ModelClassName = "knjl126iModel";
    public $ProgramID      = "KNJL126I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl126iForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl126iForm1");
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
$knjl126iCtl = new knjl126iController;
