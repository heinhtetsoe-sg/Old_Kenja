<?php

require_once('knjl416hModel.inc');
require_once('knjl416hQuery.inc');

class knjl416hController extends Controller
{
    public $ModelClassName = "knjl416hModel";
    public $ProgramID      = "KNJL416H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl416hForm1");
                    }
                    break 2;
                case "":
                case "main":
                case "chgAppDiv":
                case "chgTestDiv":
                            $this->callView("knjl416hForm1");
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
$knjl416hCtl = new knjl416hController;
