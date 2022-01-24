<?php

require_once('for_php7.php');

require_once('knjl_event_searchModel.inc');
require_once('knjl_event_searchQuery.inc');

class knjl_event_searchController extends Controller
{
    public $ModelClassName = "knjlEventSearchModel";
    public $ProgramID      = "KNJL_EVENT_SEARCH";

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
                case "priChange":
                case "searchUpd":
                    $this->callView("knjl_event_searchForm1");
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
$knjl_event_searchCtl = new knjl_event_searchController;
//var_dump($_REQUEST);
?>
