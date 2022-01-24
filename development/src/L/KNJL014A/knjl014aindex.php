<?php

require_once('for_php7.php');

require_once('knjl014aModel.inc');
require_once('knjl014aQuery.inc');

class knjl014aController extends Controller
{
    public $ModelClassName = "knjl014aModel";
    public $ProgramID      = "KNJL014A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV取込
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                //CSV出力
                case "head":
                case "error":
                case "data":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl014aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl014aForm1");
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
$knjl014aCtl = new knjl014aController;
