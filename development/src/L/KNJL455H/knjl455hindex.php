<?php

require_once('for_php7.php');

require_once('knjl455hModel.inc');
require_once('knjl455hQuery.inc');

class knjl455hController extends Controller
{
    public $ModelClassName = "knjl455hModel";
    public $ProgramID      = "KNJL455H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl455h":
                    $sessionInstance->knjl455hModel();
                    $this->callView("knjl455hForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl455hCtl = new knjl455hController();
