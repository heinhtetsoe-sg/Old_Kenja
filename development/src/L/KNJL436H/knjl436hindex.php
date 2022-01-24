<?php

require_once('for_php7.php');

require_once('knjl436hModel.inc');
require_once('knjl436hQuery.inc');

class knjl436hController extends Controller
{
    public $ModelClassName = "knjl436hModel";
    public $ProgramID      = "KNJL436H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl436h":
                    $sessionInstance->knjl436hModel();
                    $this->callView("knjl436hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl436hCtl = new knjl436hController();
