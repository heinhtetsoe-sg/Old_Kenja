<?php

require_once('for_php7.php');

require_once('knja031Model.inc');
require_once('knja031Query.inc');

class knja031Controller extends Controller
{
    public $ModelClassName = "knja031Model";
    public $ProgramID      = "KNJA031";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "sort":
                case "main":
                case "reset":
                    $sessionInstance->getMainModel();
                    $this->callView("knja031Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    // no break
                case "read":
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja031Ctl = new knja031Controller();
//var_dump($_REQUEST);
