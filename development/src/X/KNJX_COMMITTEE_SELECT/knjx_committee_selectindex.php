<?php

require_once('for_php7.php');

require_once('knjx_committee_selectModel.inc');
require_once('knjx_committee_selectQuery.inc');

class knjx_committee_selectController extends Controller
{
    public $ModelClassName = "knjx_committee_selectModel";
    public $ProgramID      = "KNJX_COMMITTEE_SELECT";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjx_committee_selectForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_committee_selectCtl = new knjx_committee_selectController();
