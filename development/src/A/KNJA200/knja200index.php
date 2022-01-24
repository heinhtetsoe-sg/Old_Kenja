<?php

require_once('for_php7.php');

require_once('knja200Model.inc');
require_once('knja200Query.inc');

class knja200Controller extends Controller
{
    public $ModelClassName = "knja200Model";
    public $ProgramID      = "KNJA200";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "defaultUpd":
                    $sessionInstance->updPrgDefaultVal("KNJA200", SCHOOLCD, SCHOOLKIND);
                    $sessionInstance->setCmd("knja200");
                    break 1;
                case "":
                case "knja200":
                    $sessionInstance->knja200Model();
                    $this->callView("knja200Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja200Ctl = new knja200Controller();
var_dump($_REQUEST);
