<?php

require_once('for_php7.php');

require_once('knjx_sienkeikakuModel.inc');
require_once('knjx_sienkeikakuQuery.inc');

class knjx_sienkeikakuController extends Controller
{
    public $ModelClassName = "knjx_sienkeikakuModel";
    public $ProgramID      = "KNJX_SIENKEIKAKU";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjx_sienkeikakuForm");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_sienkeikakuCtl = new knjx_sienkeikakuController();
