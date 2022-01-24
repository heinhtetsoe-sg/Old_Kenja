<?php

require_once('for_php7.php');

require_once('knjwexam_fin_searchModel.inc');
require_once('knjwexam_fin_searchQuery.inc');

class knjwexam_fin_searchController extends Controller {
    var $ModelClassName = "knjwexam_fin_searchModel";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "searchMain2":
                case "searchMain":
                case "search":
                case "knjwexam_fin_search":
                    $sessionInstance->knjwexam_fin_searchModel();
                    $this->callView("knjwexam_fin_search");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjwexam_fin_searchCtl = new knjwexam_fin_searchController;
var_dump($_REQUEST);
?>
