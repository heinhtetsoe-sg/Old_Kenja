<?php

require_once('for_php7.php');

require_once('knjx_special_activity_selectModel.inc');
require_once('knjx_special_activity_selectQuery.inc');

class knjx_special_activity_selectController extends Controller
{
    public $ModelClassName = "knjx_special_activity_selectModel";
    public $ProgramID      = "KNJX_SPECIAL_ACTIVITY_SELECT";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "yearseme":
                case "main":
                    $this->callView("knjx_special_activity_selectForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_special_activity_selectCtl = new knjx_special_activity_selectController();
