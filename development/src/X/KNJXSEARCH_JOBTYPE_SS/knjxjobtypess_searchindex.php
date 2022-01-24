<?php

require_once('for_php7.php');

require_once('knjxjobtypess_searchModel.inc');
require_once('knjxjobtypess_searchQuery.inc');

class knjxjobtypess_searchController extends Controller
{
    public $ModelClassName = "knjxjobtypess_searchModel";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjxjobtypess_search":
                    $sessionInstance->knjxjobtypess_searchModel();
                    $this->callView("knjxjobtypess_search");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxjobtypess_searchCtl = new knjxjobtypess_searchController();
