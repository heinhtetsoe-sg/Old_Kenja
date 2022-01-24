<?php
require_once('knjl099iModel.inc');
require_once('knjl099iQuery.inc');

class knjl099iController extends Controller
{
    public $ModelClassName = "knjl099iModel";
    public $ProgramID      = "KNJL099I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl099iForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl099iForm1");
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
$knjl099iCtl = new knjl099iController;
