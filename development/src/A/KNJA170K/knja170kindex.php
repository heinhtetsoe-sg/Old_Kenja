<?php

require_once('for_php7.php');

require_once('knja170kModel.inc');
require_once('knja170kQuery.inc');

class knja170kController extends Controller
{
    public $ModelClassName = "knja170kModel";
    public $ProgramID      = "KNJA170K";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja170k":
                case "read":
                    $sessionInstance->knja170kModel();
                    $this->callView("knja170kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja170kCtl = new knja170kController();
var_dump($_REQUEST);
