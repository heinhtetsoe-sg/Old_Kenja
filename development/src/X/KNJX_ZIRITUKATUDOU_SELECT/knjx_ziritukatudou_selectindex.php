<?php

require_once('for_php7.php');

require_once('knjx_ziritukatudou_selectModel.inc');
require_once('knjx_ziritukatudou_selectQuery.inc');

class knjx_ziritukatudou_selectController extends Controller
{
    public $ModelClassName = "knjx_ziritukatudou_selectModel";
    public $ProgramID      = "KNJX_ZIRITUKATUDOU_SELECTMODEL";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                    $this->callView("knjx_ziritukatudou_selectForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_ziritukatudou_selectCtl = new knjx_ziritukatudou_selectController();
