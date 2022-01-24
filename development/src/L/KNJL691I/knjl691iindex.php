<?php

require_once('for_php7.php');

require_once('knjl691iModel.inc');
require_once('knjl691iQuery.inc');

class knjl691iController extends Controller
{
    public $ModelClassName = "knjl691iModel";
    public $ProgramID      = "KNJL691I";     //プログラムID

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "match":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl691iForm1");
                    break 2;
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("match");
                    break 1;
                case "csv":
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjl691iForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl691iCtl = new knjl691iController();
