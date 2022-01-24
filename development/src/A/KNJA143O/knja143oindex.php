<?php

require_once('for_php7.php');

require_once('knja143oModel.inc');
require_once('knja143oQuery.inc');

class knja143oController extends Controller
{
    public $ModelClassName = "knja143oModel";
    public $ProgramID      = "KNJA143O";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knja143o":
                    $sessionInstance->knja143oModel();
                    $this->callView("knja143oForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja143oCtl = new knja143oController();
