<?php

require_once('for_php7.php');

require_once('knjx_club_kirokubikou_selectModel.inc');
require_once('knjx_club_kirokubikou_selectQuery.inc');

class knjx_club_kirokubikou_selectController extends Controller {
    var $ModelClassName = "knjx_club_kirokubikou_selectModel";
    var $ProgramID      = "KNJX_CLUB_KIROKUBIKOU_SELECT";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjx_club_kirokubikou_selectForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_club_kirokubikou_selectCtl = new knjx_club_kirokubikou_selectController;
?>
