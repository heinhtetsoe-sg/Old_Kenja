<?php
require_once('knjl354Model.inc');
require_once('knjl354Query.inc');

class knjl354Controller extends Controller
{
    public $ModelClassName = "knjl354Model";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl354":
                    $sessionInstance->knjl354Model();
                    $this->callView("knjl354Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl354Ctl = new knjl354Controller;
var_dump($_REQUEST);
