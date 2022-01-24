<?php

require_once('for_php7.php');

require_once('knjx_club_selectModel.inc');
require_once('knjx_club_selectQuery.inc');

class knjx_club_selectController extends Controller
{
    public $ModelClassName = "knjx_club_selectModel";
    public $ProgramID      = "KNJX_CLUB_SELECT";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                    $this->callView("knjx_club_selectForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_club_selectCtl = new knjx_club_selectController();
