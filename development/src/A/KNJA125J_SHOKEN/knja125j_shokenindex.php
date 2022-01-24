<?php

require_once('for_php7.php');

require_once('knja125j_shokenModel.inc');
require_once('knja125j_shokenQuery.inc');

class knja125j_shokenController extends Controller
{
    public $ModelClassName = "knja125j_shokenModel";
    public $ProgramID      = "KNJA125J_SHOKEN";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "":
                    $sessionInstance->knja125j_shokenModel();
                    $this->callView("knja125j_shokenForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja125j_shokenCtl = new knja125j_shokenController();
