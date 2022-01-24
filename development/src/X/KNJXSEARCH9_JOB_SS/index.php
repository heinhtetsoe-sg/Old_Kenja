<?php

require_once('for_php7.php');
require_once('knjxsearch9_job_ssModel.inc');
require_once('knjxsearch9_job_ssQuery.inc');

class knjxsearch9_job_ssController extends Controller
{
    public $ModelClassName = "knjxsearch9_job_ssModel";
    public $ProgramID      = "KNJXSEARCH9_JOB_SS";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "search":
                    $this->callView("knjxsearch9_job_ssForm1");
                    break 2;
                case "search_view":  //検索画面
                    $this->callView("knjxsearch9_job_ss");
                    break 2;
                case "":
                    $this->callView("knjxsearch9_job_ssForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxsearch9_job_ssCtl = new knjxsearch9_job_ssController();
