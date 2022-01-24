<?php
require_once('knjl111iModel.inc');
require_once('knjl111iQuery.inc');

class knjl111iController extends Controller
{
    public $ModelClassName = "knjl111iModel";
    public $ProgramID      = "KNJL111I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl111iForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl111iForm1");
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
$knjl111iCtl = new knjl111iController;
