<?php

require_once('for_php7.php');

require_once('knjx_transfer_selectModel.inc');
require_once('knjx_transfer_selectQuery.inc');

class knjx_transfer_selectController extends Controller
{
    public $ModelClassName = "knjx_transfer_selectModel";
    public $ProgramID      = "KNJX_TRANSFER_SELECT";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                    $this->callView("knjx_transfer_selectForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_transfer_selectCtl = new knjx_transfer_selectController();
