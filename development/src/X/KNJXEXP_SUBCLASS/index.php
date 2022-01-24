<?php
require_once('knjxexp_subclassModel.inc');
require_once('knjxexp_subclassQuery.inc');

class knjxexp_subclassController extends Controller
{
    public $ModelClassName = "knjxexp_subclassModel";
    public $ProgramID      = "KNJXEXP_SUBCLASS";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "edit":
                case "select":
                case "search":
                case "search2":
                    $this->callView("knjxexp_subclassForm1");
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
$knjxexp_subclassCtl = new knjxexp_subclassController();
//var_dump($_REQUEST);
