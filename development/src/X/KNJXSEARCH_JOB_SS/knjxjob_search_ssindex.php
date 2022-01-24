<?php

require_once('for_php7.php');

require_once('knjxjob_search_ssModel.inc');
require_once('knjxjob_search_ssQuery.inc');

class knjxjob_search_ssController extends Controller
{
    public $ModelClassName = "knjxjob_search_ssModel";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "search":
                case "knjxjob_search_ss":
                    $sessionInstance->knjxjob_search_ssModel();
                    $this->callView("knjxjob_search_ss");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxjob_search_ssCtl = new knjxjob_search_ssController();
