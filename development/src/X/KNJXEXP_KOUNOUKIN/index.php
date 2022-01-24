<?php

require_once('for_php7.php');

require_once('knjxexp_kounoukinModel.inc');
require_once('knjxexp_kounoukinQuery.inc');

class knjxexp_kounoukinController extends Controller
{
    public $ModelClassName = "knjxexp_kounoukinModel";
    public $ProgramID      = "KNJXEXP_KOUNOUKIN";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "edit":
                case "select":
                case "search":
                case "search2":
                case "sendSearch":
                    $this->callView("knjxexp_kounoukinForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("list");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxexp_kounoukinCtl = new knjxexp_kounoukinController();
//var_dump($_REQUEST);
