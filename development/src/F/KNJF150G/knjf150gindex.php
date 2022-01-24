<?php

require_once('for_php7.php');

require_once('knjf150gModel.inc');
require_once('knjf150gQuery.inc');

class knjf150gController extends Controller
{
    public $ModelClassName = "knjf150gModel";
    public $ProgramID        = "KNJF150G";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "search":
                    $this->callView("knjf150gForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("edit");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf150gCtl = new knjf150gController();
//var_dump($_REQUEST);
