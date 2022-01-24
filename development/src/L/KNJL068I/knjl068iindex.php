<?php
require_once('knjl068iModel.inc');
require_once('knjl068iQuery.inc');

class knjl068iController extends Controller
{
    public $ModelClassName = "knjl068iModel";
    public $ProgramID      = "KNJL068I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl068iForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl068iForm1");
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
$knjl068iCtl = new knjl068iController;
